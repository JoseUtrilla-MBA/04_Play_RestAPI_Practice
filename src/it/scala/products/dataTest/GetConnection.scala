package products.dataTest

import cats.effect.{Blocker, ContextShift, IO, Resource}
import com.dimafeng.testcontainers.PostgreSQLContainer
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import products.data.testRepositories.createRepositories

trait GetConnection extends createRepositories {
  def getDoobieTransactor(container: PostgreSQLContainer): Resource[IO, HikariTransactor[IO]] = {
    val transactor: Resource[IO, HikariTransactor[IO]] = getTransactorFromDoobie(
      container.driverClassName,
      container.jdbcUrl,
      container.username,
      container.password)

    transactor

  }

  private def getTransactorFromDoobie(driver: String, url: String, user: String, password: String): Resource[IO, HikariTransactor[IO]] = {
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
