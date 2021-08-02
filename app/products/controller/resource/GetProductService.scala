package products.controller.resource

import cats.effect.{IO, Resource}
import doobie.hikari.HikariTransactor
import play.api.libs.typedmap.TypedKey
import products.data.PoolConnection
import products.data.repositories.{ProductRepository, TypeProductRepository}
import products.services.ProductService

import scala.concurrent.ExecutionContext.Implicits.global

object GetProductService {

  val connectionKey: TypedKey[Resource[IO, HikariTransactor[IO]]] =
    TypedKey[Resource[IO, HikariTransactor[IO]]]("connection")

  def productService( connection: Either[Throwable,Resource[IO, HikariTransactor[IO]]]): ProductService = {
    val transactor = connection match {
      case Right(transactor)=>transactor
      case Left(_)=>PoolConnection.transactor
    }
    val productRepository: ProductRepository = ProductRepository(transactor)
    val typeProductRepository: TypeProductRepository = TypeProductRepository(transactor)

    ProductService(productRepository, typeProductRepository)
  }
}
