package products.data.repositories

import cats.effect.{IO, Resource}
import cats.implicits._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import play.api.Logger
import products.data.resource.Report

case class TypeProductData(id_typeProduct: Int = 0, name: String = "Undefined")


case class TypeProductRepository(transactor: Resource[IO, HikariTransactor[IO]]) extends Repository[TypeProductData] {
  private val logger = Logger(this.getClass)

  override def getList: List[TypeProductData] = {
    logger.trace("list:")

    val typeProducts = sql"select * from typeproduct".query[TypeProductData].to[List]
    transactor.use(typeProducts.transact[IO]).unsafeRunSync()
  }

  override def get(id: Int): Option[TypeProductData] = {
    //logger.trace(s"get: id = $id") don't use logger for get definition
    val typeProductById = sql"select * from typeproduct where id_typeproduct = $id".query[TypeProductData].option
    transactor.use(typeProductById.transact[IO]).unsafeRunSync()
  }

  override def getByName(name: String): Option[TypeProductData] = {
    //logger.trace(s"getByName: name = $name") don't use logger for get definition
    val typeProductByName = sql"select * from typeproduct where name = $name".query[TypeProductData].option
    transactor.use(typeProductByName.transact[IO]).unsafeRunSync()
  }

  override def insert(typeProducts: List[TypeProductData]): Report = {
    logger.trace(s"insert: typeProduct list with ${typeProducts.length} elements inside")

    def getConnection(typeProduct: TypeProductData): doobie.ConnectionIO[Int] =
      sql"""insert into typeproduct (id_typeproduct, name)
           values (${typeProduct.id_typeProduct}, ${typeProduct.name})""".update.run

    val idsResult = (for {
      typeProduct <- typeProducts
    } yield {
      def idResult: Either[Throwable, Int] = Either.catchNonFatal(transactor.use(getConnection(typeProduct).transact[IO]).unsafeRunSync())

      idResult match {
        case Right(n) if n == 1 => (1, typeProduct.id_typeProduct, "Success")
        case Right(_) => (0, typeProduct.id_typeProduct, "undefined error")
        case Left(e) => (0, typeProduct.id_typeProduct, e.getMessage)
      }
    }).groupMap(_._1)(e => (e._2, e._3))

    val idsSuccess = idsResult.getOrElse(1, Nil)

    idsResult.get(0) match {
      case Some(list) =>
        Report("insert", typeProducts.length, idsSuccess.length, list.length, list.groupMap(_._2)(_._1))
      case None => Report("insert", typeProducts.length, idsSuccess.length, 0, Map())
    }
  }

  override def update(typeProducts: List[TypeProductData]): Report = {
    logger.trace(s"update: typeProduct list with ${typeProducts.length} elements inside")

    def getConnection(typeProduct: TypeProductData): doobie.ConnectionIO[Int] =
      sql"""update typeproduct set name = ${typeProduct.name}
            where id_typeproduct = ${typeProduct.id_typeProduct}""".update.run

    val idsResult = (for {
      typeProduct <- typeProducts
    } yield {
      def idResult: Either[Throwable, Int] = Either.catchNonFatal(transactor.use(getConnection(typeProduct).transact[IO]).unsafeRunSync())

      idResult match {
        case Right(n) if n == 1 => (1, typeProduct.id_typeProduct, "Success")
        case Right(_) => (0, typeProduct.id_typeProduct, "undefined error, possibly this typeProduct is not in database")
        case Left(e) => (0, typeProduct.id_typeProduct, e.getMessage)
      }
    }).groupMap(_._1)(e => (e._2, e._3))

    val idsSuccess = idsResult.getOrElse(1, Nil)

    idsResult.get(0) match {
      case Some(list) =>
        Report("insert", typeProducts.length, idsSuccess.length, list.length, list.groupMap(_._2)(_._1))
      case None => Report("insert", typeProducts.length, idsSuccess.length, 0, Map())
    }
  }

  override def delete(id: Int): Report = {
    logger.trace(s"delete: id = $id")
    val deleted = sql"delete from typeproduct where id_typeproduct = $id".update.run

    val nRowsDeleted = Either.catchNonFatal(transactor.use(deleted.transact[IO]).unsafeRunSync())
    nRowsDeleted match {
      case Right(n) if n == 1 => Report("delete", 1, 1)
      case Right(_) => Report("delete", 1, 0, 1, Map("undefined reason, " +
        "possibly no longer in this database" -> List(id)))
      case Left(e) => Report("delete", 1, 0, 1, Map(e.getMessage -> List(id)))
    }
  }
}
