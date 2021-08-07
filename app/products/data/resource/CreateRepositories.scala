package products.data.resource

import com.typesafe.config.{Config, ConfigFactory}
import play.api.db.evolutions.Evolutions
import play.api.db.{Database, Databases}

trait CreateRepositories {
  val configFactory: Config = ConfigFactory.load("application.conf")
  val config: Config = configFactory.getConfig("pgDataBase")

  def database: Database = Databases(
    driver = config.getString("driverClassName"),
    url = config.getString("url"),
    name = "default",
    config = Map(
      "username" -> config.getString("user"),
      "password" -> config.getString("pass")
    )
  )

  def createTables(): Unit = {
    val db = database
    Evolutions.applyEvolutions(db)
    db.shutdown()
  }

  def cleanTables(): Unit = {
    val db = database
    Evolutions.cleanupEvolutions(db)
    db.shutdown()
  }

}
