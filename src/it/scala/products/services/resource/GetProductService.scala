package products.services.resource

import cats.effect.{Blocker, ContextShift, IO, Resource}
import com.dimafeng.testcontainers.PostgreSQLContainer
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import products.data.repositories.{ProductRepository, TypeProductRepository}
import products.services.ProductService
import scala.concurrent.ExecutionContext.Implicits.global

trait GetProductService extends createRepositories {
  def getProductService(container: PostgreSQLContainer): ProductService = {
    val transactor: Resource[IO, HikariTransactor[IO]] = getTestTransactor(
      container.driverClassName,
      container.jdbcUrl,
      container.username,
      container.password)

    createTables(transactor)

    val typeProductRepository: TypeProductRepository = TypeProductRepository(transactor)
    val productRepository: ProductRepository = ProductRepository(transactor)

    ProductService(productRepository, typeProductRepository)
  }

  private def getTestTransactor(driver: String, url: String, user: String, password: String): Resource[IO, HikariTransactor[IO]] = {
    implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

    val transactor: Resource[IO, HikariTransactor[IO]] = {
      for {
        ce <- ExecutionContexts.fixedThreadPool[IO](32)
        be <- Blocker[IO]
        xa <- HikariTransactor.newHikariTransactor[IO](
          driver,
          url,
          user,
          password,
          ce,
          be
        )
      } yield xa
    }
    transactor
  }

}
