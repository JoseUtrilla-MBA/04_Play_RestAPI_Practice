package products.data

import cats.effect.{Blocker, IO, Resource}
import doobie.hikari._
import doobie.util.ExecutionContexts
import play.api.Configuration
import javax.inject.Inject

class Connection @Inject()(configuration: Configuration) {
  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)
  val conf = configuration.underlying.getConfig("pgDataBase")

  val transactor: Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
      be <- Blocker[IO] // our blocking EC
      xa <- HikariTransactor.newHikariTransactor[IO](
        conf.getString("driverClassName"),
        conf.getString("url"),
        conf.getString("user"),
        conf.getString("pass"),
        ce, // await connection here
        be // execute JDBC operations here
      )
    } yield xa
}