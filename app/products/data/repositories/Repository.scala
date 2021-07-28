package products.data.repositories

import products.data.resource.Report

trait Repository[T] {

  def list(): List[T]

  def get(id: Int): Option[T]

  def getByName(name: String): Option[T]

  def insert(data: T): Report

  def insert(data: List[T]): Report

  def update(data: T): Report

  def update(data: List[T]): Report

  def delete(id: Int): Report

}
