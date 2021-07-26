
import com.dimafeng.testcontainers._
import org.scalatest.flatspec.AnyFlatSpec

import java.sql.DriverManager

class PostgresqlSpec extends AnyFlatSpec with ForAllTestContainer {

  override val container: PostgreSQLContainer = PostgreSQLContainer()
  println(container.driverClassName)


  "PostgreSQL container" should "be started" in {
    Class.forName(container.driverClassName)
    val connection = DriverManager.getConnection(container.jdbcUrl, container.username, container.password)
  }

  "Start program test" should "insert" in {

  }
}

/*
class PostRouterSpec extends PlaySpec with GuiceOneAppPerTest {

  "ProductRouter" should {

    "render the list of products" in {
      val request = FakeRequest(GET, "/products").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val home:Future[Result] = route(app, request).get

      val products: Seq[ProductResource] = Json.fromJson[Seq[ProductResource]](contentAsJson(home)).get
      products.filter(_.id == 1).head mustBe (ProductResource(1,"Clothes", "Shirt", "W", "M", 30.5 ))
    }

    "render the list of products when url ends with a trailing slash" in {
      val request = FakeRequest(GET, "/products/").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val home:Future[Result] = route(app, request).get

      val products: Seq[ProductResource] = Json.fromJson[Seq[ProductResource]](contentAsJson(home)).get
      products.filter(_.id == 1).head mustBe (ProductResource(1,"Clothes", "Shirt", "W", "M", 30.5 ))
    }
  }

}*/