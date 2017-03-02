package com.github.finatrascaffold

import java.util.{List => JavaList, Map => JavaMap}

import io.swagger.models.properties.{ObjectProperty, Property}
import io.swagger.models.{Model, ModelImpl, Swagger}

import scala.reflect.runtime.{universe => ru}

object SwaggerInstance extends Swagger {

  def filter(propertyClassPaths: Seq[String]): Swagger = {
    if (propertyClassPaths.isEmpty) return SwaggerInstance

    val m = ru.runtimeMirror(getClass.getClassLoader)
    val optionSymbol = ru.typeOf[Option.type].typeSymbol
    val headerType = ru.typeOf[com.twitter.finatra.request.Header]
    val symbolCache = new collection.mutable.HashMap[String, Option[List[(ru.Symbol, Int)]]]

    val swaggerSpecFilter = new io.swagger.core.filter.AbstractSpecFilter {
      override def isPropertyAllowed(model: Model,
                                     property: Property,
                                     propertyName: String,
                                     params: JavaMap[String, JavaList[String]],
                                     cookies: JavaMap[String, String],
                                     headers: JavaMap[String, JavaList[String]]) = {
        var allow = true
        model match {
          case modelImpl: ModelImpl =>
            propertyClassPaths.exists { pkg =>
              getParamSymbols(s"$pkg.${modelImpl.getName}").map { symbols =>
                allow = updateProperty(symbols, property, propertyName)
                AnyRef
              }.isDefined
            }

          case _ => println("Unprocessed model " + model)
        }
        allow
      }

      def getParamSymbols(className: String) = {
        symbolCache.getOrElseUpdate(
          className, {
            scala.util.Try(m.reflectClass(m.staticClass(className))).toOption.map { c =>
              (for {
                paramList <- c.symbol.primaryConstructor.typeSignature.paramLists
                param <- paramList
              } yield {
                param
              }).zipWithIndex
            }
          }
        )
      }

      def updateProperty(symbols: List[(ru.Symbol, Int)],
                         property: io.swagger.models.properties.Property,
                         propertyName: String): Boolean = {
        var allow = true
        symbols.collectFirst { case (symbol, index)
          if symbol.name.decodedName.toString.trim == propertyName => (symbol, index)
        }.foreach { case (s, i) =>
          val isHeader = symbols(i)._1.annotations.headOption.map(_.tree.tpe == headerType)
          allow = !isHeader.getOrElse(false) // If the field is for header, hide it from body
          if (allow) {
            val isOption = s.info.baseClasses.exists(_.fullName == optionSymbol.fullName)
            property.setRequired(!isOption)
            if (isOption && s.info.typeArgs.nonEmpty) {
              property match {
                case objProp: ObjectProperty => objProp.setType(s.info.typeArgs.head.toString)
                case _ => // Keep original
              }
            }
          }
        }
        allow
      }
    }
    new io.swagger.core.filter.SpecFilter().filter(this, swaggerSpecFilter, null, null, null)
  }
}
