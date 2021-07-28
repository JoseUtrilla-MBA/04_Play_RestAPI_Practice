package persistence

import cats.effect.{IO, Resource}
import com.dimafeng.testcontainers.PostgreSQLContainer
import doobie.hikari.HikariTransactor
import products.data.repositories.{ProductRepositoryImpl, TypeProductRepositoryImpl}
import products.services.ProductService
import scala.concurrent.ExecutionContext.Implicits.global

trait GetProductService extends DbTestRepository {
  def getProductService(container: PostgreSQLContainer): ProductService = {
    val transactor: Resource[IO, HikariTransactor[IO]] = TestConnection(container.driverClassName, container.jdbcUrl,
      container.username, container.password).transactor
    startTables(transactor)
    val typeProductRepository: TypeProductRepositoryImpl = TypeProductRepositoryImpl(transactor)
    val productRepository: ProductRepositoryImpl = ProductRepositoryImpl(transactor)
    ProductService(productRepository, typeProductRepository)

  }
}
