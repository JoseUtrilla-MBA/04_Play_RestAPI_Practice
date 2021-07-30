package products.controller.resource

import cats.effect.{IO, Resource}
import doobie.hikari.HikariTransactor
import products.data.PoolConnection
import products.data.repositories.{ProductRepository, TypeProductRepository}
import products.services.ProductService
import scala.concurrent.ExecutionContext.Implicits.global

object defaultProductService {
  private val transactor: Resource[IO, HikariTransactor[IO]] = PoolConnection.transactor
  private val productRepository: ProductRepository = ProductRepository(transactor)
  private val typeProductRepository: TypeProductRepository = TypeProductRepository(transactor)
  val productService: ProductService = ProductService(productRepository, typeProductRepository)
}
