package products.data.repositories

import cats.effect.IO
import doobie.implicits._
import play.api.Logger
import products.data.Connection

import javax.inject.Inject

case class TypeProduct(id_typeProduct: Int = 0, name: String = "Undefined")

trait TypeProductRepository extends Repository[TypeProduct] {}

class TypeProductRepositoryImpl @Inject()()(connection: Connection) extends TypeProductRepository {
  private val logger = Logger(this.getClass)

  override def list(): List[TypeProduct] = {
    logger.trace("list():")
    val typeProductListDB = sql"select * from typeproduct".query[TypeProduct].to[List]
    connection.transactor.use(typeProductListDB.transact[IO]).unsafeRunSync()
  }

  override def get(id: Int): Option[TypeProduct] = {
    //logger.trace(s"get: id = $id") don't use logger for get definition
    val typeProductDB = sql"""select * from typeproduct where id_typeproduct = $id""".query[TypeProduct].option
    connection.transactor.use(typeProductDB.transact[IO]).unsafeRunSync()
  }

  def getByName(name: String): Option[TypeProduct] = {
    logger.trace(s"getByName: name = $name")
    val typeProductDB = sql"""select * from typeproduct where name = $name""".query[TypeProduct].option
    connection.transactor.use(typeProductDB.transact[IO]).unsafeRunSync()
  }

  override def insert(data: TypeProduct): String = {
    logger.trace(s"insert: data = $data")
    val insert = sql"insert into typeProduct (id_typeProduct, name) values (${data.id_typeProduct},${data.name})"
      .update.run
    try {
      val nInserted = connection.transactor.use(insert.transact[IO]).unsafeRunSync()
      "correctly inserted"
    } catch {
      case e: java.sql.SQLException => s"not inserted, reason: ${e.getMessage},  status: ${e.getSQLState}"
    }
  }

  override def delete(id: Int): String = {
    logger.trace(s"delete: id = $id")
    val deleted =
      sql"delete from typeProduct where id_typeProduct = $id".update.run
    try {
      val productToDelete = get(id).toString
      connection.transactor.use(deleted.transact[IO]).unsafeRunSync()
      s"deleted typeProduct:\n${productToDelete}"
    } catch {
      case e: java.sql.SQLException => s"typeProduct not deleted, reason: ${e.getMessage}"
    }
  }
}
