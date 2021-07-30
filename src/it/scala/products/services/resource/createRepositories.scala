package products.services.resource

import cats.effect.{IO, Resource}
import cats.implicits.catsSyntaxTuple2Semigroupal
import doobie.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.implicits._

trait createRepositories {

  def createTables(transactor: Resource[IO, HikariTransactor[IO]]): AnyVal = {

    //Creates tables typeproduct and product
    val createTypeProduct: ConnectionIO[Int] =
      sql"""create table if not exists typeproduct
                 (id_typeproduct BIGSERIAL NOT NULL PRIMARY KEY,
           name VARCHAR (50));""".update.run

    val createProduct: ConnectionIO[Int] =
      sql"""create table if not exists product ( id_product BIGSERIAL NOT NULL PRIMARY KEY,
          id_typeproduct BIGINT REFERENCES typeproduct(id_typeproduct),
          name VARCHAR (50),
          gender VARCHAR (1),
          size VARCHAR (50),
          price NUMERIC (5,2));""".update.run

    transactor.use((createTypeProduct, createProduct).mapN(_ + _).transact[IO]).unsafeRunSync()


    //Inserts first rows into tables typeproduct and product
    val insertTypeProduct: ConnectionIO[Int] =
      sql"""INSERT INTO typeproduct (
          id_typeproduct,
          name
          )
        VALUES
          (1,'Clothes'),
          (2,'Shoes'),
          (3,'Complements') ON CONFLICT (id_typeproduct) DO NOTHING;""".update.run
    val insertProduct: ConnectionIO[Int] =
      sql"""INSERT INTO product (
          id_product,
          id_typeProduct,
          name,
          gender,
          size,
          price
          )
        VALUES (1, 1, 'Shirt', 'W', 'M', 30.5),
          (2, 1, 'Skirt', 'W', 'L', 32),
          (3, 2, 'Boots', 'M', '42', 45.99),
          (4, 1, 'Shirt', 'M', 'XL', 35.99),
          (5, 3, 'Hat', 'W', 's', 59.99),
          (6, 2, 'Sneakers', 'W', '41', 37.5),
          (7, 1, 'Skirt', 'W', 'L', 52),
          (8, 2, 'Boots', 'M', '42', 35.99),
          (9, 1, 'Shirt', 'M', 'XL', 45.99),
          (10, 3, 'Hat', 'W', 's', 39.99),
          (11, 2, 'Sneakers', 'W', '41', 67.5) ON CONFLICT (id_product) DO NOTHING;""".update.run

    transactor.use((insertTypeProduct, insertProduct).mapN(_ + _).transact[IO]).unsafeRunSync()

  }

}
