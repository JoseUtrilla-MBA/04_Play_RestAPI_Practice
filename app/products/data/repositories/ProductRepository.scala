package products.data.repositories

import cats.effect._
import cats.implicits._
import doobie._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import play.api.Logger
import products.data.resource.Report

case class ProductData(id: Int, typeProduct: TypeProductData, name: String, gender: String, size: String, price: Double) {
  override def toString: String =
    s"id: $id, idTypeProduct ${typeProduct.id_typeProduct}, typeProductName: ${typeProduct.name}, name: $name, gender:" +
      s" $gender, size: $size, price: $price"
}

case class ProductRepository(transactor: Resource[IO, HikariTransactor[IO]]) extends Repository[ProductData] {

  private val logger = Logger(this.getClass)

  val typeProductRepository: Repository[TypeProductData] = TypeProductRepository(transactor)
  implicit val GetTypeProduct: Get[TypeProductData] = Get[Int].tmap(n => typeProductRepository.get(n).getOrElse(TypeProductData()))
  implicit val putTypeProduct: Put[TypeProductData] = Put[Int].tcontramap(n => n.id_typeProduct)

  override def getList: List[ProductData] = {
    logger.trace("list:")

    val productList = sql"select * from product".query[ProductData].to[List]
    transactor.use(productList.transact[IO]).unsafeRunSync()
  }

  override def get(id: Int): Option[ProductData] = {
    logger.trace(s"get: id = $id")

    val productById = sql"select * from product where id_product = $id".query[ProductData].option
    transactor.use(productById.transact[IO]).unsafeRunSync()
  }

  override def getByName(name: String): Option[ProductData] = ???

  override def insert(products: List[ProductData]): Report = {
    logger.trace(s"insert: dataList within ${products.length} elements")

    def getConnection(product: ProductData): doobie.ConnectionIO[Int] =
      sql"""insert into product (id_product, id_typeProduct, name, gender, size, price)
           values (${product.id},${product.typeProduct.id_typeProduct}, ${product.name},
           ${product.gender},${product.size},${product.price})""".update.run

    val idsResult = (for {
      product <- products
    } yield {
      def idResult: Either[Throwable, Int] = Either.catchNonFatal(transactor.use(getConnection(product).transact[IO]).unsafeRunSync())

      idResult match {
        case Right(n) if n == 1 => (1, product.id, "Success")
        case Right(_) => (0, product.id, "undefined error")
        case Left(e) => (0, product.id, e.getMessage)
      }
    }).groupMap(_._1)(e => (e._2, e._3))

    val idsSuccess = idsResult.getOrElse(1, Nil)

    idsResult.get(0) match {
      case Some(list) =>
        Report("insert", products.length, idsSuccess.length, list.length, list.groupMap(_._2)(_._1))
      case None => Report("insert", products.length, idsSuccess.length, 0, Map())
    }
  }

  override def update(products: List[ProductData]): Report = {
    logger.trace(s"update: dataList within ${products.length} elements")

    def getConnection(product: ProductData): doobie.ConnectionIO[Int] =
      sql"""update product set id_typeProduct= ${product.typeProduct.id_typeProduct},
           name= ${product.name}, gender= ${product.gender}, size= ${product.size},
           price= ${product.price} where id_product= ${product.id}""".update.run

    val idsResult = (for {
      product <- products
    } yield {
      def idResult: Either[Throwable, Int] = Either.catchNonFatal(transactor.use(getConnection(product).transact[IO]).unsafeRunSync())

      idResult match {
        case Right(n) if n == 1 => (1, product.id, "Success")
        case Right(_) => (0, product.id, "undefined error, possibly this product is not in database")
        case Left(e) => (0, product.id, e.getMessage)
      }
    }).groupMap(_._1)(e => (e._2, e._3))

    val idsSuccess = idsResult.getOrElse(1, Nil)

    idsResult.get(0) match {
      case Some(list) =>
        Report("update", products.length, idsSuccess.length, list.length, list.groupMap(_._2)(_._1))
      case None => Report("update", products.length, idsSuccess.length, 0, Map())
    }
  }

  override def delete(id: Int): Report = {
    logger.trace(s"delete: id = $id")
    val deleted = sql"delete from product where id_product = $id".update.run

    val nRowsDeleted = Either.catchNonFatal(transactor.use(deleted.transact[IO]).unsafeRunSync())
    nRowsDeleted match {
      case Right(n) if n == 1 => Report("delete", 1, 1)
      case Right(_) => Report("delete", 1, 0, 1, Map("undefined reason, " +
        "possibly no longer in this database" -> List(id)))
      case Left(e) => Report("delete", 1, 0, 1, Map(e.getMessage -> List(id)))
    }
  }

}
