package com.github.finatrascaffold.util

import com.github.finatrascaffold.util.FutureEither.{Sfe, Tfe}
import com.twitter.util.{Future => TwitterFuture}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

object TypeUtils {
  val TwitterPromise = com.twitter.util.Promise
  val TwitterReturn = com.twitter.util.Return
  val TwitterThrow = com.twitter.util.Throw

  def scalaToTwitterFuture[A](f: Future[A]): TwitterFuture[A] = {
    val promise = TwitterPromise[A]()
    f.onComplete(t => promise update (t match {
      case Success(r) => TwitterReturn(r)
      case Failure(e) => TwitterThrow(e)
    }))
    promise
  }

  def twitterToScalaFuture[A](twitterFuture: TwitterFuture[A]): Future[A] = {
    val promise = Promise[A]()
    twitterFuture respond {
      case TwitterReturn(r) => promise success r
      case TwitterThrow(e) => promise failure e
    }
    promise.future
  }

  def sfe2tfe[L, R](future: Sfe[L, R]): Tfe[L, R] = {
    Tfe(scalaToTwitterFuture(future.future))
  }

  def tfe2sfe[L, R](twitterFuture: Tfe[L, R]): Sfe[L, R] = {
    Sfe(twitterToScalaFuture(twitterFuture.future))
  }
}

class \/[+L, +R](e: Either[L, R]) {
  def flatMap[LL >: L, R2](func: R => (LL \/ R2)): (LL \/ R2) = e match {
    case Right(right) => func(right)
    case Left(left) => -\/(left)
  }

  def map[R2](func: R => R2): (L \/ R2) = e match {
    case Right(right) => \/-(func(right))
    case Left(left) => -\/(left)
  }
}

case class -\/[+L](right: L) extends (L \/ Nothing)(Left(right))
case class \/-[+R](left: R) extends (Nothing \/ R)(Right(left))

object FutureEither {

  /** Twitter future either */
  case class Tfe[L, R](future: TwitterFuture[L \/ R]) extends AnyVal {
    def flatMap[R2](f: R => Tfe[L, R2]): Tfe[L, R2] = {
      Tfe(future.flatMap {
        case \/-(right) => f(right).future
        case -\/(left) => TwitterFuture.value(-\/(left))
      })
    }

    def map[R2](f: R => R2): Tfe[L, R2] = Tfe(future.map(_.map(f)))
  }

  def right[L, R](future: TwitterFuture[R]): TwitterFuture[\/[L, R]] = future.map(\/-(_))
  def left[L, R](future: TwitterFuture[L]): TwitterFuture[\/[L, R]] = future.map(-\/(_))

  /** Scala future either */
  case class Sfe[L, R](future: Future[L \/ R]) extends AnyVal {
    def flatMap[R2](f: R => Sfe[L, R2]): Sfe[L, R2] = {
      Sfe(future.flatMap {
        case \/-(right) => f(right).future
        case -\/(left) => Future.successful(-\/(left))
      })
    }

    def map[R2](f: R => R2): Sfe[L, R2] = Sfe(future.map(_.map(f)))
  }

  implicit final class ImplicitTfe[L, R](val f: TwitterFuture[L \/ R]) extends AnyVal {
    def t = Tfe(f)
  }

  implicit final class ImplicitSfe[L, R](val f: Future[L \/ R]) extends AnyVal {
    def t = Sfe(f)
  }
}


object FutureOption {

  /** Twitter future option */
  case class Tfo[+A](future: TwitterFuture[Option[A]]) extends AnyVal {
    def flatMap[B](f: A => Tfo[B]): Tfo[B] = {
      val newFuture = future.flatMap {
        case Some(a) => f(a).future
        case None => TwitterFuture.value(None)
      }
      Tfo(newFuture)
    }

    def map[B](f: A => B): Tfo[B] = Tfo(future.map(_.map(f)))
  }

  /** Scala future option */
  case class Sfo[+A](future: Future[Option[A]]) extends AnyVal {
    def flatMap[B](f: A => Sfo[B]): Sfo[B] = {
      val newFuture = future.flatMap {
        case Some(a) => f(a).future
        case None => Future.successful(None)
      }
      Sfo(newFuture)
    }

    def map[B](f: A => B): Sfo[B] = Sfo(future.map(_.map(f)))
  }

  implicit final class ImplicitTfo[A](val f: TwitterFuture[Option[A]]) extends AnyVal {
    def t = Tfo(f)
  }
}
