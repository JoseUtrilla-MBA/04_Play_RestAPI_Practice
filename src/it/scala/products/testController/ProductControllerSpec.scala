package products.testController

import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test.CSRFTokenHelper.CSRFRequest
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, HOST, POST, contentAsJson, defaultAwaitTimeout, route, writeableOf_AnyContentAsEmpty}
import products.controller.resource.GetProductService
import products.data.testRepositories.CreateRepositories
import products.dataTest.GetConnection
import products.models.{ProductResource, ProductsToProcess}
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps

class ProductControllerSpec extends PlaySpec
  with GuiceOneAppPerSuite
  with ForAllTestContainer
  with CreateRepositories
  with GetConnection {

  override val container: PostgreSQLContainer = PostgreSQLContainer()


  "ProductRouter" should {

    "render the list of products" in {
      val connection = getDoobieTransactor(container)
      createTables(container)

      val request = FakeRequest(GET, "/products").withHeaders(HOST -> "localhost:9000")
        .withCSRFToken.addAttr(GetProductService.connectionKey, connection)
      val home: Future[Result] = route(app, request).get

      val products: Seq[ProductResource] = Json.fromJson[Seq[ProductResource]](contentAsJson(home)).get
      products.filter(_.id == 1).head mustBe ProductResource(1, "Clothes", "Shirt", "W", "M", 30.5)
    }

    "render the list of products when url ends with a trailing slash" in {
      val connection = getDoobieTransactor(container)
      createTables(container)

      val request = FakeRequest(GET, "/products/").withHeaders(HOST -> "localhost:9000")
        .withCSRFToken.addAttr(GetProductService.connectionKey, connection)
      val home: Future[Result] = route(app, request).get

      val products: Seq[ProductResource] = Json.fromJson[Seq[ProductResource]](contentAsJson(home)).get
      products.filter(_.id == 1).head mustBe ProductResource(1, "Clothes", "Shirt", "W", "M", 30.5)
    }

    "insert or delete a product to and from database" in {
      val connection = getDoobieTransactor(container)
      createTables(container)

      val product13 = ProductResource(13, "Clothes", "redPants", "L", "40", 19.99)
      val body = Json.toJson(ProductsToProcess("insert", List(product13)))

      val showRequest = FakeRequest(GET, "/products").withHeaders(HOST -> "localhost:9000")
        .withCSRFToken.addAttr(GetProductService.connectionKey, connection)

      def showProductsResult: Future[Result] = route(app, showRequest).get

      val productsBeforeInsert = Json.fromJson[Seq[ProductResource]](contentAsJson(showProductsResult)).get
      assume(!productsBeforeInsert.contains(product13))

      val insertRequest = FakeRequest(POST, "/products").withHeaders(HOST -> "localhost:9000")
        .withCSRFToken.addAttr(GetProductService.connectionKey, connection).withBody(body)
      val insertResult: Future[Result] = route(app, insertRequest).get
      Await.ready(insertResult, 400 millis)

      val productsAfterInsert = Json.fromJson[Seq[ProductResource]](contentAsJson(showProductsResult)).get
      assume(productsAfterInsert.contains(product13))

      val deleteRequest = FakeRequest(GET, "/products/delete/13").withHeaders(HOST -> "localhost:9000")
        .withCSRFToken.addAttr(GetProductService.connectionKey, connection)
      val deleteResult: Future[Result] = route(app, deleteRequest).get
      Await.ready(deleteResult, 400 millis)

      val productsAfterDelete = Json.fromJson[Seq[ProductResource]](contentAsJson(showProductsResult)).get
      assert(!productsAfterDelete.contains(product13))
    }


    "update a list of products" in {
      val connection = getDoobieTransactor(container)
      createTables(container)

      val product13 = ProductResource(1, "Clothes", "redPants", "L", "40", 19.99)
      val body = Json.toJson(ProductsToProcess("update", List(product13)))

      val showRequest = FakeRequest(GET, "/products/1").withHeaders(HOST -> "localhost:9000")
        .withCSRFToken.addAttr(GetProductService.connectionKey, connection)

      def showProduct: Future[Result] = route(app, showRequest).get

      val productBeforeUpdate: ProductResource = Json.fromJson[ProductResource](contentAsJson(showProduct)).get

      val updateRequest = FakeRequest(POST, "/products/").withHeaders(HOST -> "localhost:9000")
        .withCSRFToken.addAttr(GetProductService.connectionKey, connection).withBody(body)
      val updateResult: Future[Result] = route(app, updateRequest).get
      Await.ready(updateResult, 400 millis)

      val productAfterUpdate: ProductResource = Json.fromJson[ProductResource](contentAsJson(showProduct)).get

      assert(productBeforeUpdate.price == 30.5 && productAfterUpdate.price == 19.99)

    }
  }
}


