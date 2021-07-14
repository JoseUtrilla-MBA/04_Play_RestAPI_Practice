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

    /**
      * GET(p"/") request is routed to the controller method: productList(), which lists all the products in our database.
      * When this program is started for the first time, initial records are set in the database.
      */
    case GET(p"/") =>
      controller.productList

    /**
      * GET(p"/basic") request is routed to the controller method: productList(), which lists all the products in our database
      * as a basic type format.
      */
    case GET(p"/basic") =>
      controller.basicProductList

    /**
      * GET(p"/delete/$id") request is routed to the controller method: delete(id), which delete a record from database,
      * which is selected previously by its id.
      * It will list all the products in our database without the deleted one.
      */
    case GET(p"/delete/$id") =>
      controller.delete(id)

    /**
      * GET(p"/$id") request is routed to the controller method: showProduct(id),
      * this method gets a record from database, which is selected previously by its id.
      */
    case GET(p"/$id") =>
      controller.showProduct(id)

    /**
      * GET(p"/$id/basic") request is routed to the controller method: showProduct(id),
      * this method gets a record from database in basic type format, which is selected previously by its id.
      */
    case GET(p"/$id/basic") =>
      controller.showBasicProduct(id)

    /**
      * POST(p"/") request is routed to the controller method: process(),
      * the controller's process() method will insert a new record or update an existing one, in our database.
      */
    case POST(p"/") =>
      controller.process
  }

}
