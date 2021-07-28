package products.data.repositories

import cats.effect._
import doobie._
import doobie.hikari.HikariTransactor
import doobie.implicits._
import play.api.Logger
//import products.data.PoolConnection
import products.data.resource.Report

case class ProductData(id: Int, typeProduct: TypeProductData, name: String, gender: String, size: String, price: Double) {
  override def toString: String =
    s"id: $id, idTypeProduct ${typeProduct.id_typeProduct}, typeProductName: ${typeProduct.name}, name: $name, gender:" +
      s" $gender, size: $size, price: $price"
}

trait ProductRepository extends Repository[ProductData]

case class ProductRepositoryImpl(transactor: Resource[IO, HikariTransactor[IO]]) extends ProductRepository {
    val typeProductRepository: Repository[TypeProductData] = new TypeProductRepositoryImpl(transactor)

    implicit val GetTypeProduct: Get[TypeProductData] = Get[Int].tmap(n => typeProductRepository.get(n).getOrElse(TypeProductData()))
    implicit val putTypeProduct: Put[TypeProductData] = Put[Int].tcontramap(n => n.id_typeProduct)
    private val logger = Logger(this.getClass)
    /*val startDataTable: Unit = {
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
      val l = transactor.use((truncatedTable, startTable).mapN(_ + _).transact[IO]).unsafeRunSync()
      logger.trace(s"inserting initial rows(${l}):\n\t$inserts")
    }*/

    override def list(): List[ProductData] = {
      logger.trace("list():")
      val productList = sql"select * from product".query[ProductData].to[List]
      transactor.use(productList.transact[IO]).unsafeRunSync()
    }

    override def get(id: Int): Option[ProductData] = {
      logger.trace(s"get: id = $id")
      val productDB = sql"select * from product where id_product = $id".query[ProductData].option
      transactor.use(productDB.transact[IO]).unsafeRunSync()
    }

    override def getByName(name: String): Option[ProductData] = ???

    override def insert(data: ProductData): Report = {
      logger.trace(s"insert: data = $data")
      val insert =
        sql"""insert into product (id_product, id_typeProduct, name, gender, size, price) values
             (${data.id},${data.typeProduct.id_typeProduct}, ${data.name},${data.gender},${data.size},${data.price})"""
          .update.run
      try {
        val n = transactor.use(insert.transact[IO]).unsafeRunSync()
        n match {
          case 1 => Report("insert", 1, 1, 0, Map())
          case 0 => Report("insert", 1, 0, 1, Map("undefined reason" -> List(data.id)))
        }
      } catch {
        case e: java.sql.SQLException => Report("insert", 1, 0, 1, Map(e.getMessage -> List(data.id)))
      }
    }

    override def insert(dataList: List[ProductData]): Report = {
      logger.trace(s"insert: dataList within ${dataList.length} elements")

      def toQuery(data: ProductData): doobie.ConnectionIO[Int] =
        sql"""insert into product (id_product, id_typeProduct, name, gender, size, price)
           values (${data.id},${data.typeProduct.id_typeProduct}, ${data.name},
           ${data.gender},${data.size},${data.price})""".update.run

      val result = (for {
        data <- dataList
      } yield try {
        val success = transactor.use(toQuery(data).transact[IO]).unsafeRunSync()
        (success, data.id, if (success == 1) "Success" else "undefined error")
      } catch {
        case e: java.sql.SQLException => (0, data.id, e.getMessage)
      }).groupMap(_._1)(e => (e._2, e._3))
      val idsSuccess = result.getOrElse(1, Nil)
      val idsFailure = result.get(0)
      idsFailure match {
        case Some(list) =>
          val mapList = list.groupMap(_._2)(_._1)
          Report("insert", dataList.length, idsSuccess.length, list.length, mapList)
        case None => Report("insert", dataList.length, idsSuccess.length, 0, Map())
      }
    }

    override def update(data: ProductData): Report = {
      logger.trace(s"update: data = $data")
      val updateRow =
        sql"""update product set id_typeProduct= ${data.typeProduct.id_typeProduct},
           name= ${data.name}, gender= ${data.gender}, size= ${data.size},
           price= ${data.price} where id_product= ${data.id}""".update.run
      try {
        val n = transactor.use(updateRow.transact[IO]).unsafeRunSync()
        n match {
          case 1 => Report("update", 1, 1, 0, Map())
          case 0 => Report("update", 1, 0, 1, Map("undefined reason" -> List(data.id)))
        }
      } catch {
        case e: java.sql.SQLException => Report("update", 1, 0, 1, Map(e.getMessage -> List(data.id)))
      }
    }

    override def update(dataList: List[ProductData]): Report = {
      logger.trace(s"update: dataList within ${dataList.length} elements")

      def toQuery(data: ProductData): doobie.ConnectionIO[Int] =
        sql"""update product set id_typeProduct= ${data.typeProduct.id_typeProduct},
           name= ${data.name}, gender= ${data.gender}, size= ${data.size},
           price= ${data.price} where id_product= ${data.id}""".update.run

      val result = (for {
        data <- dataList
      } yield try {
        val success = transactor.use(toQuery(data).transact[IO]).unsafeRunSync()
        (success, data.id, if (success == 1) "Success" else "id out of database")
      } catch {
        case e: java.sql.SQLException => (0, data.id, e.getMessage)
      }).groupMap(_._1)(e => (e._2, e._3))
      val idsSuccess = result.getOrElse(1, Nil)
      val idsFailure = result.get(0)
      idsFailure match {
        case Some(list) =>
          val mapList = list.groupMap(_._2)(_._1)
          Report("update", dataList.length, idsSuccess.length, list.length, mapList)
        case None => Report("update", dataList.length, idsSuccess.length, 0, Map())
      }
    }

    override def delete(id: Int): Report = {
      logger.trace(s"delete: id = $id")
      val deleted = sql"delete from product where id_product = $id".update.run
      try {
        val n = transactor.use(deleted.transact[IO]).unsafeRunSync()
        n match {
          case x if x > 0 => Report("delete", 1, 1)
          case _ => Report("delete", 1, 0, 1, Map("undefined reason, " +
            "possibly no longer in this database" -> List(id)))
        }
      } catch {
        case e: java.sql.SQLException => Report("delete", 1, 0, 1, Map(e.getMessage -> List(id)))
      }
    }

}
