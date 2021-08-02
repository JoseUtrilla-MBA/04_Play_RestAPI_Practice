package products.data.testRepositories

import com.dimafeng.testcontainers.PostgreSQLContainer
import play.api.db.evolutions.Evolutions
import play.api.db.{Database, Databases}

trait createRepositories {

  def database(container: PostgreSQLContainer): Database = Databases(
    driver = container.driverClassName,
    url = container.jdbcUrl,
    name = "default",
    config = Map(
      "username" -> container.username,
      "password" -> container.password
    )
  )

  def createTables(container: PostgreSQLContainer): Unit =
    Evolutions.applyEvolutions(database(container))


}
