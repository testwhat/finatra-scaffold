package com.github.finatrascaffold

import reactivemongo.api.collections.GenericQueryBuilder
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.commands.WriteResult
import reactivemongo.api.{BSONSerializationPack, Cursor, CursorProducer, ReadPreference}
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class BaseRepository(dbClient: DbClient, name: String) extends Logging {

  protected[this] val collection = dbClient.collection(name)

  def insert[T](data: T)(implicit writer: BSONSerializationPack.Writer[T]): Future[Option[T]] = {
    collect(_.insert(data).map { r => if (r.ok) Some(data) else None })
  }

  def cursorToList[T](cursor: Cursor[T], number: Int): Future[List[T]] = {
    cursor.collect[List](number, Cursor.FailOnError[List[T]]())
  }

  /** Get query result as list
    *
    * @param number the maximum number of documents to be retrieved
    */
  def asList[T](query: GenericQueryBuilder[BSONSerializationPack.type], number: Int = Int.MaxValue,
                readPreference: ReadPreference = ReadPreference.primary)
               (implicit reader: BSONSerializationPack.Reader[T],
                ec: ExecutionContext, cp: CursorProducer[T]): Future[List[T]] = {
    cursorToList(query.cursor[T](readPreference), number)
  }

  def collect[T](collector: BSONCollection => Future[T]): Future[T] = {
    collection.flatMap(collector)
  }

  def removeAll(): Future[WriteResult] = {
    debugEnterMethod()
    warn("removeAll " + name)
    collect(_.remove(BSONDocument.empty))
  }

  def drop(): Future[Boolean] = {
    debugEnterMethod()
    warn("drop " + name)
    collect(_.drop(failIfNotFound = false))
  }
}
