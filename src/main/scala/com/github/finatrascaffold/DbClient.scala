package com.github.finatrascaffold

import javax.inject.Singleton

import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{FailoverStrategy, MongoConnectionOptions, MongoDriver, ScramSha1Authentication}
import reactivemongo.core.nodeset.Authenticate

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class DbClient(nodes: List[String], dbName: String, user: String, password: String) extends Logging {

  private val connection = createConnection()
  private val db = connectDb()

  private[this] def createConnection() = {
    debugEnterMethod()
    val driver = new MongoDriver
    if (user.isEmpty && password.isEmpty) {
      driver.connection(nodes)
    } else {
      driver.connection(nodes,
        options = MongoConnectionOptions(authMode = ScramSha1Authentication),
        authentications = Seq(Authenticate(dbName, user, password)))
    }
  }

  private[this] def connectDb() = {
    debugEnterMethod(s"DB name=$dbName")
    connection.database(dbName, FailoverStrategy.remote)
  }

  def collection(name: String): Future[BSONCollection] = {
    debugEnterMethod(s"Collection name=$name")
    db.map(_.collection(name))
  }
}
