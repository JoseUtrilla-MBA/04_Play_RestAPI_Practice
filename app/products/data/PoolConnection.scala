package products.data

import cats.effect.{Blocker, ContextShift, IO, Resource}
import doobie.hikari._
import doobie.util.ExecutionContexts
import products.data.resource.CreateRepositories

object PoolConnection extends CreateRepositories{
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

  val transactor: Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
      be <- Blocker[IO] // our blocking EC
      xa <- HikariTransactor.newHikariTransactor[IO](
        config.getString("driverClassName"),
        config.getString("url"),
        config.getString("user"),
        config.getString("pass"),
        ce, // await connection here
        be // execute JDBC operations here
      )
    } yield xa

  val init: Unit = createTables()

}