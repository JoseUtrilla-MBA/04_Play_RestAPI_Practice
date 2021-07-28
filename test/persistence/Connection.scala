package persistence

import cats.effect.{Blocker, ContextShift, IO, Resource}
import com.typesafe.config.{Config, ConfigFactory}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts

case class TestConnection(driver:String,url:String,user:String,password:String) {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

  val configFactory: Config = ConfigFactory.load("application.conf")
  val config = configFactory.getConfig("pgDataBase")

  val transactor: Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
      be <- Blocker[IO] // our blocking EC
      xa <- HikariTransactor.newHikariTransactor[IO](
        driver,
        url,
        user,
        password,
        ce, // await connection here
        be // execute JDBC operations here
      )
    } yield xa
}
