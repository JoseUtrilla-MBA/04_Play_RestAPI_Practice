package products.data.repositories

import cats.effect.IO
import doobie.implicits._
import doobie.util.update.Update
import play.api.Logger
import products.data.PoolConnection
import products.data.resource.Report

case class TypeProduct(id_typeProduct: Int = 0, name: String = "Undefined")

trait TypeProductRepository extends Repository[TypeProduct] {}

class TypeProductRepositoryImpl extends TypeProductRepository {
  private val logger = Logger(this.getClass)

  override def list(): List[TypeProduct] = {
    logger.trace("list():")
    val typeProductListDB = sql"select * from typeproduct".query[TypeProduct].to[List]
    PoolConnection.transactor.use(typeProductListDB.transact[IO]).unsafeRunSync()
  }

  override def get(id: Int): Option[TypeProduct] = {
    //logger.trace(s"get: id = $id") don't use logger for get definition
    val typeProductDB = sql"select * from typeproduct where id_typeproduct = $id".query[TypeProduct].option
    PoolConnection.transactor.use(typeProductDB.transact[IO]).unsafeRunSync()
  }

  def getByName(name: String): Option[TypeProduct] = {
    //logger.trace(s"getByName: name = $name") don't use logger for get definition
    val typeProductDB = sql"select * from typeproduct where name = $name".query[TypeProduct].option
    PoolConnection.transactor.use(typeProductDB.transact[IO]).unsafeRunSync()
  }

  override def insert(data: TypeProduct): Report = {
    logger.trace(s"updateRow: data = $data")
    val insert = sql"insert into typeproduct (id_typeProduct, name) values (${data.id_typeProduct},${data.name})"
      .update.run
    try {
      val nInserted = PoolConnection.transactor.use(insert.transact[IO]).unsafeRunSync()
      Report()
    } catch {
      case e: java.sql.SQLException => Report()
    }
  }

  override def insert(data: List[TypeProduct]): Report = {
    logger.trace(s"insert: data = $data")
    val query =
      "insert into typeproduct (id_typeProduct, name) values (?,?)"
    val insert = Update[TypeProduct](query).updateMany(data)
    try {
      val insertedRows = PoolConnection.transactor.use(insert.transact[IO]).unsafeRunSync()
      Report()
    } catch {
      case e: java.sql.SQLException => Report()
    }
  }

  override def update(data: TypeProduct): Report = {
    logger.trace(s"update: data = $data")
    val updateRow = sql"update typeProduct set name=${data.name} where id_typeProduct= ${data.id_typeProduct}"
      .update.run
    try {
      PoolConnection.transactor.use(updateRow.transact[IO]).unsafeRunSync()
      Report()
    } catch {
      case e: java.sql.SQLException => Report()
    }
  }
  override def update(data: List[TypeProduct]): Report = {
    logger.trace(s"update: data${data.map(p=>p.id_typeProduct).mkString(": ",", ","")}")
    type dataToUpdate = (String,Int)
    val query =
      "update product set name= ? where id_typeProduct= ?"
    val insert = Update[dataToUpdate](query).updateMany(data.map(x=>(x.name,x.id_typeProduct)))
    try {
      val insertedRows = PoolConnection.transactor.use(insert.transact[IO]).unsafeRunSync()
      Report()
    } catch {
      case e: java.sql.SQLException => Report()
    }
  }

  override def delete(id: Int): Report = {
    logger.trace(s"delete: id = $id")
    val deleted =
      sql"delete from typeProduct where id_typeProduct = $id".update.run
    try {
      val productToDelete = get(id).toString
      PoolConnection.transactor.use(deleted.transact[IO]).unsafeRunSync()
      Report()
    } catch {
      case e: java.sql.SQLException => Report()
    }
  }

}
