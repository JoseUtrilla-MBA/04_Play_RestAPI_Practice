package products.data.repositories

trait Repository[T] {

  def list(): List[T]

  def get(id: Int): Option[T]

  def insert(data: T): String

  def delete(id: Int): String

}
