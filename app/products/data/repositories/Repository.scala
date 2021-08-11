package products.data.repositories

import products.data.resource.Report

import scala.concurrent.Future

trait Repository[T] {

  def getList: Future[List[T]]

  def get(id: Int): Future[Option[T]]

  def getByName(name: String): Future[Option[T]]

  def insert(data: List[T]): Future[Report]

  def update(data: List[T]): Future[Report]

  def delete(id: Int): Future[Report]

}
