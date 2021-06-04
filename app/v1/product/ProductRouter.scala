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
      * Route the GET request to list all the products in our database.
      * When you start this program for the first time, initial records are set in the database.
      */
    case GET(p"/") =>
      controller.productList

    /**
      * Route the GET request to list all the products in our database in basic type format.
      */
    case GET(p"/basic") =>
      controller.basicProductList

    /**
      * Route the GET request to delete a record from database, which is selected previously by its id.
      * It will list all the products in our database without the deleted one.
      */
    case GET(p"/delete/$id") =>
      controller.delete(id)

    /**
      * Route the GET request get a record from database, which is selected previously by its id.
      */
    case GET(p"/$id") =>
      controller.showProduct(id)

    /**
      * Route the GET request get a record from database in basic type format,
      * which is selected previously by its id.
      */
    case GET(p"/$id/basic") =>
      controller.showBasicProduct(id)

    /**
      * Route the POST to insert a new record or or update an existing one, in our database.
      */
    case POST(p"/") =>
      controller.process
  }

}
