package products.data.repositories

import cats.effect._
import cats.implicits.catsSyntaxTuple2Semigroupal
import doobie.implicits._
import doobie.{Fragment, Get}
import play.api.Logger
import products.data.Connection

import javax.inject.Inject

case class ProductData(id: Int, typeProduct: TypeProduct, name: String, gender: String, size: String, price: Double) {
  override def toString: String =
    s"id: $id, idTypeProduct ${typeProduct.id_typeProduct}, typeProductName: ${typeProduct.name}, name: $name, gender:" +
      s" $gender, size: $size, price: $price"
}

trait ProductRepository extends Repository[ProductData] {}

class ProductRepositoryImpl @Inject()(connection: Connection,
                                      typeProductRepository: TypeProductRepository)
  extends ProductRepository {

  implicit val GetTypeProduct: Get[TypeProduct] = Get[Int].tmap(n => typeProductRepository.get(n).getOrElse(TypeProduct()))
  private val logger = Logger(this.getClass)
  val startDataTable: Unit = {
    val productList = List(
      ProductData(1, typeProductRepository.get(1) getOrElse TypeProduct(), "Shirt", "W", "M", 30.5),
      ProductData(2, typeProductRepository.get(1) getOrElse TypeProduct(), "Skirt", "W", "L", 32),
      ProductData(3, typeProductRepository.get(1) getOrElse TypeProduct(), "Boots", "M", "42", 45.99),
      ProductData(4, typeProductRepository.get(1) getOrElse TypeProduct(), "Shirt", "M", "XL", 35.99),
      ProductData(5, typeProductRepository.get(1) getOrElse TypeProduct(), "Hat", "W", "s", 59.99),
      ProductData(6, typeProductRepository.get(1) getOrElse TypeProduct(), "Sneakers", "W", "41", 47.5)
    )

    val truncatedTable = sql"truncate table product".update.run
    val inserts = productList.flatMap(p => s"(${p.id}, ${p.typeProduct.id_typeProduct}, \'${p.name}\', \'${p.gender}\', " +
      s"\'${p.size}\', ${p.price})").mkString.replace(")(", "),\n\t(")
    val startTable = (fr"insert into product (id_product, id_typeProduct, name, gender, size, price) values"
      ++ Fragment.const(inserts)).update.run
    val l = connection.transactor.use((truncatedTable, startTable).mapN(_ + _).transact[IO]).unsafeRunSync()
    logger.trace(s"inserting initial rows(${l}):\n\t$inserts")
  }

  override def list(): List[ProductData] = {
    logger.trace("list():")
    val productList = sql"select * from product".query[ProductData].to[List]
    connection.transactor.use(productList.transact[IO]).unsafeRunSync()
  }

  override def get(id: Int): Option[ProductData] = {
    logger.trace(s"get: id = $id")
    val productDB = sql"select * from product where id_product = $id".query[ProductData].option
    connection.transactor.use(productDB.transact[IO]).unsafeRunSync()
  }

  def insert(data: ProductData): String = {
    logger.trace(s"insert: data = $data")
    val insert =
      sql"""insert into product (id_product, id_typeProduct, name, gender, size, price) values
             (${data.id},${data.typeProduct.id_typeProduct}, ${data.name},${data.gender},${data.size},${data.price})"""
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
    val deleted = sql"delete from product where id_product = $id".update.run
    try {
      val productToDelete = get(id) match {
        case Some(value)=>s"{${value.toString}}"
        case None => "product not removed, possibly no longer in this database"
      }
      connection.transactor.use(deleted.transact[IO]).unsafeRunSync()
      s"deleted product: $productToDelete"
    } catch {
      case e: java.sql.SQLException => s"product not deleted, reason: ${e.getMessage}"
    }
  }

}
