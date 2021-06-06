package v1.product.data

import akka.actor.ActorSystem
import play.api.{Logger, MarkerContext}
import play.api.libs.concurrent.CustomExecutionContext
import cats.effect._
import cats.implicits.catsSyntaxTuple2Semigroupal
import doobie.Fragment
import doobie.implicits._

import javax.inject.Inject
import scala.concurrent.Future

case class ProductData(id: Int, id_typeProduct: Int, name: String, gender: String, size: String, price: Double)

case class TypeProduct(id_typeProduct: Int, name: String)

object ManageTypeProduct {
  val connection = new Connection

  def create(data: TypeProduct): doobie.Update0 = {
    sql"insert into typeProduct (id_typeProduct, name) values (${data.id_typeProduct},${data.name})".update
  }

  def list(): Iterable[TypeProduct] = {
    val typeProductListDB = sql"select * from typeproduct".query[TypeProduct].to[List]
    connection.transactor.use(typeProductListDB.transact[IO]).unsafeRunSync()
  }

  def get(id: Int): TypeProduct = {
    val typeProductDB = sql"""select * from typeproduct where id_typeproduct = $id""".query[TypeProduct].unique
    connection.transactor.use(typeProductDB.transact[IO]).unsafeRunSync()
  }
}

class ProductExecutionContext @Inject()(actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the ProductRepository.
  */
trait ProductRepository {

  def list()(implicit mc: MarkerContext): Future[Iterable[ProductData]]

  def get(id: Int)(implicit mc: MarkerContext): Future[Option[ProductData]]

  def create(data: ProductData)(implicit mc: MarkerContext): Future[Int]

  def delete(id: Int)(implicit mc: MarkerContext): Future[Iterable[ProductData]]

}

class ProductRepositoryImpl @Inject()()(implicit ec: ProductExecutionContext)
  extends ProductRepository {

  val connection = new Connection
  private val logger = Logger(this.getClass)

  lazy val startDataTable: Unit = {

    val productList = List(
      ProductData(1, 1, "Shirt", "W", "M", 30.5),
      ProductData(2, 1, "Skirt", "W", "L", 32),
      ProductData(3, 2, "Boots", "M", "42", 45.99),
      ProductData(4, 1, "Shirt", "M", "XL", 35.99),
      ProductData(5, 3, "Hat", "W", "s", 59.99),
      ProductData(6, 2, "Sneakers", "W", "41", 47.5),
    )

    val truncatedTable = sql"truncate table product".update.run

    val inserts = productList.flatMap(p => s"(${p.id}, ${p.id_typeProduct}, \'${p.name}\', \'${p.gender}\', " +
      s"\'${p.size}\', ${p.price})").mkString.replace(")(", "),\n\t(")
    val startTable = (fr"insert into product (id_product, id_typeProduct, name, gender, size, price) values"
      ++ Fragment.const(inserts)).update.run

    val l = connection.transactor.use((truncatedTable, startTable).mapN(_ + _).transact[IO]).unsafeRunSync()
    logger.trace(s"inserting initial rows(${l}):\n\t$inserts")
  }

  val start = startDataTable


  override def list()(implicit mc: MarkerContext): Future[Iterable[ProductData]] = {
    Future {
      logger.trace("list: ")
      val productList = sql"select * from product".query[ProductData].to[List]
      connection.transactor.use(productList.transact[IO]).unsafeRunSync()
    }
  }

  override def get(id: Int)(implicit mc: MarkerContext): Future[Option[ProductData]] = {
    Future {
      logger.trace(s"get: id = $id")
      val productDB = sql"select * from product where id_product = $id".query[ProductData].option
      connection.transactor.use(productDB.transact[IO]).unsafeRunSync()
    }
  }

  def create(data: ProductData)(implicit mc: MarkerContext): Future[Int] = {
    Future {
      logger.trace(s"create: data = $data")
      val insert =
        sql"""insert into product (id_product, id_typeProduct, name, gender, size, price) values (${data.id},${data.id_typeProduct}, ${data.name},${data.gender},${data.size},${data.price})"""
          .update.run
      connection.transactor.use(insert.transact[IO]).unsafeRunSync()
    }
  }

  override def delete(id: Int)(implicit mc: MarkerContext): Future[Iterable[ProductData]] = {
    Future {
      logger.trace(s"delete record by id: id = $id")
      val productList = sql"select * from product".query[ProductData].to[List]
      val deleted = connection.transactor.use(sql"delete from product where id_product = $id".update.run.transact[IO]).unsafeRunSync()
      connection.transactor.use(productList.transact[IO]).unsafeRunSync()
    }
  }

}
