package products.data.repositories

import cats.effect._
import cats.implicits._
import doobie._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import play.api.Logger
import products.data.resource.Report

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class ProductData(id: Int, typeProduct: TypeProductData, name: String, gender: String, size: String, price: Double) {
  override def toString: String =
    s"id: $id, idTypeProduct ${typeProduct.id_typeProduct}, typeProductName: ${typeProduct.name}, name: $name, gender:" +
      s" $gender, size: $size, price: $price"
}

case class ProductRepository(transactor: Resource[IO, HikariTransactor[IO]]) extends Repository[ProductData] {

  private val logger = Logger(this.getClass)

  val typeProductRepository: TypeProductRepository = TypeProductRepositoryImpl(transactor)

  implicit val GetTypeProduct: Get[TypeProductData] =
    Get[Int].tmap(n => typeProductRepository.Get(n).getOrElse(TypeProductData()))

  implicit val putTypeProduct: Put[TypeProductData] = Put[Int].tcontramap(n => n.id_typeProduct)

  override def getList: Future[List[ProductData]] = {
    logger.trace("getList:")

    val productList = sql"select * from product".query[ProductData].to[List]
    transactor.use(productList.transact[IO]).unsafeToFuture()
  }

  override def get(id: Int): Future[Option[ProductData]] = {
    logger.trace(s"get: id = $id")

    val productById = sql"select * from product where id_product = $id".query[ProductData].option
    transactor.use(productById.transact[IO]).unsafeToFuture()
  }

  override def getByName(name: String): Future[Option[ProductData]] = ???

  override def insert(products: List[ProductData]): Future[Report] = {
    logger.trace(s"insert: products with ${products.length} elements")

    def getDoobieConnectionIO(product: ProductData): doobie.ConnectionIO[Int] =
      sql"""insert into product (id_product, id_typeProduct, name, gender, size, price)
           values (${product.id},${product.typeProduct.id_typeProduct}, ${product.name},
           ${product.gender},${product.size},${product.price})""".update.run

    val idsResultFuture = (for {
      product <- products
    } yield {
      transactor.use(getDoobieConnectionIO(product).transact[IO]).unsafeToFuture().map { n =>
        if (n > 0) (1, product.id, "Success") else (0, product.id, "undefined error")
      } recover {
        case e: Throwable => (0, product.id, e.getMessage)
      }
    }).sequence.map(list => list.groupMap(_._1)(e => (e._3, e._2)))

    idsResultFuture.map { idsResult =>

      val idsSuccess = idsResult.getOrElse(1, Nil)

      idsResult.get(0) match {
        case Some(list) =>
          Report("insert", products.length, idsSuccess.length, list.length, list.groupMap(_._1)(_._2))
        case None => Report("insert", products.length, idsSuccess.length, 0, Map())
      }
    }
  }

  override def update(products: List[ProductData]): Future[Report] = {
    logger.trace(s"update: products with ${
      products.length
    } elements")

    def getDoobieConnectionIO(product: ProductData): doobie.ConnectionIO[Int] =
      sql"""update product set id_typeProduct= ${product.typeProduct.id_typeProduct}, name= ${product.name}, gender= ${product.gender}
           , size= ${product.size}, price= ${product.price} where id_product= ${product.id}""".update.run

    val idsResultFuture = (for {
      product <- products
    } yield {
      transactor.use(getDoobieConnectionIO(product).transact[IO]).unsafeToFuture().map { n =>
        if (n > 0) (1, product.id, "Success")
        else (0, product.id, "undefined error, possibly this typeProduct is not in database")
      } recover {
        case e: Throwable => (0, product.id, e.getMessage)
      }
    }).sequence.map(list => list.groupMap(_._1)(e => (e._3, e._2)))

    idsResultFuture.map { idsResult =>

      val idsSuccess = idsResult.getOrElse(1, Nil)

      idsResult.get(0) match {
        case Some(list) =>
          Report("update", products.length, idsSuccess.length, list.length, list.groupMap(_._1)(_._2))
        case None => Report("update", products.length, idsSuccess.length, 0, Map())
      }
    }
  }

  override def delete(id: Int): Future[Report] = {
    logger.trace(s"delete: id = $id")

    val deleted = sql"delete from product where id_product = $id".update.run
    val nRowsDeletedFuture = Either.catchNonFatal(transactor.use(deleted.transact[IO]).unsafeToFuture()).sequence

    nRowsDeletedFuture.map {
      case Right(n) if n == 1 => Report("delete", 1, 1)
      case Right(_) => Report("delete", 1, 0, 1, Map("undefined reason, " +
        "possibly no longer in this database" -> List(id)))
      case Left(e) => Report("delete", 1, 0, 1, Map(e.getMessage -> List(id)))
    }
  }

}
