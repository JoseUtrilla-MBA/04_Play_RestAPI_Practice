package v1.product.data

import cats.effect.{Blocker, IO, Resource}
import doobie.util.ExecutionContexts
import doobie.hikari._


class Connection() {
  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

  // Resource yielding a transactor configured with a bounded connect EC and an unbounded
  // transaction EC. Everything will be closed and shut down cleanly after use.
  val transactor: Resource[IO, HikariTransactor[IO]] =
  for {
    ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
    be <- Blocker[IO] // our blocking EC
    xa <- HikariTransactor.newHikariTransactor[IO](
      "org.postgresql.Driver", // driver classname
      "jdbc:postgresql://localhost:5432/pg_products_rest", // connect URL (driver-specific)
      "postgres", // user
      "admin", // password
      ce, // await connection here
      be // execute JDBC operations here
    )
  } yield xa
}