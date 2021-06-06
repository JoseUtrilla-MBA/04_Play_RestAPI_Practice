
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json.{ JsResult, Json }
import play.api.mvc.{ RequestHeader, Result }
import play.api.test._
import play.api.test.Helpers._
import play.api.test.CSRFTokenHelper._
import v1.product.ProductResource

import scala.concurrent.Future

class PostRouterSpec extends PlaySpec with GuiceOneAppPerTest {

  "ProductRouter" should {

    "render the list of products" in {
      val request = FakeRequest(GET, "/v1/products").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val home:Future[Result] = route(app, request).get

      val products: Seq[ProductResource] = Json.fromJson[Seq[ProductResource]](contentAsJson(home)).get
      products.filter(_.id == 1).head mustBe (ProductResource(1,"Clothes", "Shirt", "W", "M", 30.5 ))
    }

    "render the list of products when url ends with a trailing slash" in {
      val request = FakeRequest(GET, "/v1/products/").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val home:Future[Result] = route(app, request).get

      val products: Seq[ProductResource] = Json.fromJson[Seq[ProductResource]](contentAsJson(home)).get
      products.filter(_.id == 1).head mustBe (ProductResource(1,"Clothes", "Shirt", "W", "M", 30.5 ))
    }
  }

}