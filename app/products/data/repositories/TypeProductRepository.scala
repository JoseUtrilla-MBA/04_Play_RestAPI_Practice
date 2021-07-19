package products.data.repositories

import doobie.implicits._
import cats.effect.IO
import play.api.MarkerContext
import products.data.Connection

import javax.inject.Inject

case class TypeProduct(id_typeProduct: Int, name: String)

class TypeProductRepository @Inject()()(connection: Connection) extends Repository[TypeProduct] {

  override def list()(implicit mc: MarkerContext): List[TypeProduct] = {
    val typeProductListDB = sql"select * from typeproduct".query[TypeProduct].to[List]
    connection.transactor.use(typeProductListDB.transact[IO]).unsafeRunSync()
  }

  override def get(id: Int)(implicit mc: MarkerContext): Option[TypeProduct] = {
    val typeProductDB = sql"""select * from typeproduct where id_typeproduct = $id""".query[TypeProduct].option
    connection.transactor.use(typeProductDB.transact[IO]).unsafeRunSync()
  }

  def getByName(name: String): TypeProduct = {
    val typeProductDB = sql"""select * from typeproduct where name = $name""".query[TypeProduct].unique
    connection.transactor.use(typeProductDB.transact[IO]).unsafeRunSync()
  }

  override def create(data: TypeProduct)(implicit mc: MarkerContext): Int = {
    val insert = sql"insert into typeProduct (id_typeProduct, name) values (${data.id_typeProduct},${data.name})"
      .update.run
    connection.transactor.use(insert.transact[IO]).unsafeRunSync()
  }

  override def delete(id: Int)(implicit mc: MarkerContext): List[TypeProduct] = {
    val typeProductList = sql"select * from typeProduct".query[TypeProduct].to[List]
    val deleted = connection.transactor.use(sql"delete from typeProduct where id_typeProduct = $id"
      .update.run.transact[IO]).unsafeRunSync()
    connection.transactor.use(typeProductList.transact[IO]).unsafeRunSync()
  }
}
