package persistence

import cats.effect.{IO, Resource}
import doobie._
import doobie.implicits._
import doobie.hikari.HikariTransactor
import products.data.repositories.TypeProductData
import products.models.ProductResource

trait DbTestRepository {
  def startTables(transactor: Resource[IO, HikariTransactor[IO]]) = {
    val createTypeProduct: ConnectionIO[Int] =
      sql"""create table if not exists typeproduct
           (id_typeproduct BIGSERIAL NOT NULL PRIMARY KEY,
           name VARCHAR (50));""".update.run
    transactor.use(createTypeProduct.transact[IO]).unsafeRunSync()

    val createProduct: ConnectionIO[Int] =
      sql"""create table if not exists product ( id_product BIGSERIAL NOT NULL PRIMARY KEY,
          id_typeproduct BIGINT REFERENCES typeproduct(id_typeproduct),
          name VARCHAR (50),
          gender VARCHAR (1),
          size VARCHAR (50),
          price NUMERIC (5,2));""".update.run
    transactor.use(createProduct.transact[IO]).unsafeRunSync()

    val getTypeProductData =
      sql"select * from typeproduct where id_typeproduct = 1".query[TypeProductData].option
    val isTypeProductAlreadyInDatabase = transactor.use(getTypeProductData.transact[IO]).unsafeRunSync() match {
      case Some(typeProductData) => typeProductData.id_typeProduct == 1
      case None => false
    }

    val insertTypeProduct: ConnectionIO[Int] =
      sql"""INSERT INTO typeproduct (id_typeproduct, name)
          VALUES (1,'Clothes'),(2,'Shoes'),(3,'Complements');""".update.run
    if (!isTypeProductAlreadyInDatabase) transactor.use(insertTypeProduct.transact[IO]).unsafeRunSync()
  }

  val productList = List(
    ProductResource(1, "Clothes", "Shirt", "W", "M", 30.5),
    ProductResource(2, "Clothes", "Skirt", "W", "L", 32),
    ProductResource(3, "Shoes", "Boots", "M", "42", 45.99),
    ProductResource(4, "Clothes", "Shirt", "M", "XL", 35.99),
    ProductResource(5, "Complements", "Hat", "W", "s", 59.99),
    ProductResource(6, "Shoes", "Sneakers", "W", "41", 37.5),
    ProductResource(7, "Clothes", "Skirt", "W", "L", 52),
    ProductResource(8, "Shoes", "Boots", "M", "42", 35.99),
    ProductResource(9, "Clothes", "Shirt", "M", "XL", 45.99),
    ProductResource(10, "Complements", "Hat", "W", "s", 39.99),
    ProductResource(11, "Shoes", "Sneakers", "W", "41", 67.5)
  )


}
