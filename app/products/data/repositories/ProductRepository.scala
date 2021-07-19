package products.data.repositories

import akka.actor.ActorSystem
import cats.effect._
import cats.implicits.catsSyntaxTuple2Semigroupal
import doobie.Fragment
import doobie.implicits._
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}
import products.data.Connection

import javax.inject.Inject

case class ProductData(id: Int, idTypeProduct: Int, name: String, gender: String, size: String, price: Double)

class ProductExecutionContext @Inject()(actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the ProductRepository.
  */
trait ProductRepository extends Repository [ProductData]{}

class ProductRepositoryImpl @Inject()()(connection: Connection,
                                        typeProductRepository: TypeProductRepository)
                                     (implicit ec: ProductExecutionContext)
  extends ProductRepository {

  private val logger = Logger(this.getClass)

  val startDataTable: Unit = {
    logger.trace(s"Starting application")

    val productList = List(
      ProductData(1, 1, "Shirt", "W", "M", 30.5),
      ProductData(2, 1, "Skirt", "W", "L", 32),
      ProductData(3, 2 ,"Boots", "M", "42", 45.99),
      ProductData(4, 1, "Shirt", "M", "XL", 35.99),
      ProductData(5, 3, "Hat", "W", "s", 59.99),
      ProductData(6, 2, "Sneakers", "W", "41", 47.5),
    )

    val truncatedTable = sql"truncate table product".update.run

    val inserts = productList.flatMap(p => s"(${p.id}, ${p.idTypeProduct}, \'${p.name}\', \'${p.gender}\', " +
      s"\'${p.size}\', ${p.price})").mkString.replace(")(", "),\n\t(")
    val startTable = (fr"insert into product (id_product, id_typeProduct, name, gender, size, price) values"
      ++ Fragment.const(inserts)).update.run

    val l = connection.transactor.use((truncatedTable, startTable).mapN(_ + _).transact[IO]).unsafeRunSync()
    logger.trace(s"inserting initial rows(${l}):\n\t$inserts")
  }

  override def list()(implicit mc: MarkerContext): List[ProductData] = {
      logger.trace("list: ")
      val productList = sql"select * from product".query[ProductData].to[List]
      connection.transactor.use(productList.transact[IO]).unsafeRunSync()
  }

  override def get(id: Int)(implicit mc: MarkerContext): Option[ProductData] = {
      logger.trace(s"get: id = $id")
      val productDB = sql"select * from product where id_product = $id".query[ProductData].option
      connection.transactor.use(productDB.transact[IO]).unsafeRunSync()
  }

  def create(data: ProductData)(implicit mc: MarkerContext): Int = {
      logger.trace(s"create: data = $data")
      val insert =
        sql"""insert into product (id_product, id_typeProduct, name, gender, size, price) values
             (${data.id},${data.idTypeProduct}, ${data.name},${data.gender},${data.size},${data.price})"""
          .update.run
      connection.transactor.use(insert.transact[IO]).unsafeRunSync()
  }

  override def delete(id: Int)(implicit mc: MarkerContext): List[ProductData] = {
      logger.trace(s"delete record by id: id = $id")
      val productList = sql"select * from product".query[ProductData].to[List]
      val deleted = connection.transactor.use(sql"delete from product where id_product = $id".update.run.transact[IO]).unsafeRunSync()
      connection.transactor.use(productList.transact[IO]).unsafeRunSync()
  }

}
