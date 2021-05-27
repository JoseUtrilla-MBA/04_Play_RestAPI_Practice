package v1.product

import javax.inject.Inject

import play.api.routing.Router.Routes
import play.api.routing.SimpleRouter
import play.api.routing.sird._

/**
  * Routes and URLs to the ProductResource controller.
  */
class ProductRouter @Inject()(controller: ProductController) extends SimpleRouter {

  override def routes: Routes = {
    case GET(p"/") =>
      controller.productList

    case GET(p"/basic") =>
      controller.basicProductList

    case GET(p"/$id") =>
      controller.showProduct(id)

    case GET(p"/$id/basic") =>
      controller.showBasicProduct(id)

    case POST(p"/") =>
      controller.process
  }

}
