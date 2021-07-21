package products.controller

import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import products.models._
import products.services.ProductService
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class ProductController @Inject()(cc: ControllerComponents,
                                  productService: ProductService)
                                 (implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  private val logger = Logger(this.getClass)

  def productList: Action[AnyContent] = Action.async { implicit request =>
    logger.trace("productList:")
    productService.listProductResource.map { products =>
      Ok(Json.toJson(products))
    }
  }

  def basicProductList: Action[AnyContent] = Action.async { implicit request =>
    logger.trace("basicProductList:")
    productService.listProductResource.map { products =>
      val basicProducts = products.map(product => BasicProductResource(product.name, product.price))
      Ok(Json.toJson(basicProducts))
    }
  }

  def showProduct(id: String): Action[AnyContent] = Action.async {
    implicit request =>
      logger.trace(s"showProduct: id = $id")
      productService.lookupProduct(id).map { product =>
        Ok(Json.toJson(product))
      }
  }

  def showBasicProduct(id: String): Action[AnyContent] = Action.async {
    implicit request =>
      logger.trace(s"showBasicProduct: id = $id")
      productService.lookupProduct(id).map { product =>
        val basicProduct = product match {
          case Some(value) => BasicProductResource(value.name, value.price)
          case None => null
        }
        Ok(Json.toJson(Option(basicProduct)))
      }
  }

  def add: Action[JsValue] = Action.async(parse.json) { implicit request =>
    logger.trace("add:")
    Future {
      val productResource = request.body.validate[ProductResource]
      productResource.fold(
        errors => {
          BadRequest(Json.obj("message" -> JsError.toJson(errors)))
        },
        p => {
          val str = productService.insertService(p)
          Ok(Json.obj("message" -> s"new product '${p.name}' $str"))
        }
      )
    }
  }

  def delete(id: String): Action[AnyContent] = Action.async { implicit request =>
    logger.trace(s"delete: id = $id")
    productService.remove(id).map { message =>
      Ok(Json.obj("message" -> s" $message"))
    }
  }

}
