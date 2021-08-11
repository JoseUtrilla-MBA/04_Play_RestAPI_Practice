package products.data.repositories

import cats.effect.{IO, Resource}
import cats.implicits._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import play.api.Logger
import products.data.resource.Report

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class TypeProductData(id_typeProduct: Int = 4, name: String = "Undefined")

trait TypeProductRepository extends Repository[TypeProductData] {
  def Get(id: Int): Option[TypeProductData]
}

case class TypeProductRepositoryImpl(transactor: Resource[IO, HikariTransactor[IO]]) extends TypeProductRepository {
  private val logger = Logger(this.getClass)

  override def getList: Future[List[TypeProductData]] = {
    logger.trace("getList:")

    val typeProductList = sql"select * from typeproduct".query[TypeProductData].to[List]
    transactor.use(typeProductList.transact[IO]).unsafeToFuture()
  }

  override def get(id: Int): Future[Option[TypeProductData]] = {

    val typeProductById = sql"select * from typeproduct where id_typeproduct = $id".query[TypeProductData].option
    transactor.use(typeProductById.transact[IO]).unsafeToFuture()
  }

  def Get(id: Int): Option[TypeProductData] = {

    val typeProductById = sql"select * from typeproduct where id_typeproduct = $id".query[TypeProductData].option
    transactor.use(typeProductById.transact[IO]).unsafeRunSync()
  }

  override def getByName(name: String): Future[Option[TypeProductData]] = {

    val typeProductById = sql"select * from typeproduct where name = $name".query[TypeProductData].option
    transactor.use(typeProductById.transact[IO]).unsafeToFuture()
  }

  override def insert(typeProducts: List[TypeProductData]): Future[Report] = {
    logger.trace(s"insert: typeProducts with ${typeProducts.length} elements")

    def getDoobieConnectionIO(typeProduct: TypeProductData): doobie.ConnectionIO[Int] =
      sql"""insert into typeproduct (id_typeproduct, name)
           values (${typeProduct.id_typeProduct}, ${typeProduct.name})""".update.run

    val idsResultFuture = (for {
      typeProduct <- typeProducts
    } yield {
      transactor.use(getDoobieConnectionIO(typeProduct).transact[IO]).unsafeToFuture().map { n =>
        if (n > 0) (1, typeProduct.id_typeProduct, "Success") else (0, typeProduct.id_typeProduct, "undefined error")
      } recover {
        case e: Throwable => (0, typeProduct.id_typeProduct, e.getMessage)
      }
    }).sequence.map(list => list.groupMap(_._1)(e => (e._3, e._2)))

    idsResultFuture.map { idsResult =>

      val idsSuccess = idsResult.getOrElse(1, Nil)

      idsResult.get(0) match {
        case Some(list) =>
          Report("insert", typeProducts.length, idsSuccess.length, list.length, list.groupMap(_._1)(_._2))
        case None => Report("insert", typeProducts.length, idsSuccess.length, 0, Map())
      }
    }
  }

  override def update(typeProducts: List[TypeProductData]): Future[Report] = {
    logger.trace(s"update: typeProducts with ${typeProducts.length} elements")

    def getDoobieConnectionIO(typeProduct: TypeProductData): doobie.ConnectionIO[Int] =
      sql"""update typeproduct set name = ${typeProduct.name}
            where id_typeproduct = ${typeProduct.id_typeProduct}""".update.run

    val idsResultFuture = (for {
      typeProduct <- typeProducts
    } yield {
      transactor.use(getDoobieConnectionIO(typeProduct).transact[IO]).unsafeToFuture().map { n =>
        if (n > 0) (1, typeProduct.id_typeProduct, "Success")
        else (0, typeProduct.id_typeProduct, "undefined error, possibly this typeProduct is not in database")
      } recover {
        case e: Throwable => (0, typeProduct.id_typeProduct, e.getMessage)
      }
    }).sequence.map(list => list.groupMap(_._1)(e => (e._3, e._2)))

    idsResultFuture.map { idsResult =>

      val idsSuccess = idsResult.getOrElse(1, Nil)

      idsResult.get(0) match {
        case Some(list) =>
          Report("update", typeProducts.length, idsSuccess.length, list.length, list.groupMap(_._1)(_._2))
        case None => Report("update", typeProducts.length, idsSuccess.length, 0, Map())
      }
    }
  }

  override def delete(id: Int): Future[Report] = {
    logger.trace(s"delete: id = $id")

    val deleted = sql"delete from typeproduct where id_typeproduct = $id".update.run
    val nRowsDeletedFuture = Either.catchNonFatal(transactor.use(deleted.transact[IO]).unsafeToFuture()).sequence

    nRowsDeletedFuture.map {
      case Right(n) if n == 1 => Report("delete", 1, 1)
      case Right(_) => Report("delete", 1, 0, 1, Map("undefined reason, " +
        "possibly no longer in this database" -> List(id)))
      case Left(e) => Report("delete", 1, 0, 1, Map(e.getMessage -> List(id)))
    }
  }

}