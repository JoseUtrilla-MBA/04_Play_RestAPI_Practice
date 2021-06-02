package v1.product.data

import cats.effect.{Blocker, IO}
import doobie.Transactor
import doobie.util.ExecutionContexts

 class Connection() {
  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)
  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", // driver classname
    "jdbc:postgresql://localhost:5432/pg_products_rest", // connect URL (driver-specific)
    "postgres", // user
    "admin", // password
    Blocker.liftExecutionContext(ExecutionContexts.synchronous) // just for testing
  )
}
