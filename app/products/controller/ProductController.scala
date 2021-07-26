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
    val toUpdate = request.headers.get("Raw-Request-URI") match {
      case Some(value) if value == "/products/?toUpdate=true" => true
      case _ => false
    } // to set the value, we don't take the param, just compare the Request-URI header
    val whatToDo = if (toUpdate)"add to update" else "add to insert"
    logger.trace(whatToDo)
    Future {
      val productResource = request.body.validate[ProductResource]
      productResource.isError match {
        case true => {
          val listProductResource = request.body.validate[List[ProductResource]]
          listProductResource.fold(
            errors => {
              BadRequest(Json.obj("message" -> JsError.toJson(errors)))
            },
            p => {
              val n = productService.insertService(p, toUpdate)
              Ok(Json.toJson(n))
            }
          )
        }
        case false =>
          productResource.fold(
            errors => {
              BadRequest(Json.obj("message" -> JsError.toJson(errors)))
            },
            p => {
              val str = productService.insertService(p, toUpdate)
              Ok(Json.toJson(str))
            }
          )
      }
    }
  }

  def delete(id: String): Action[AnyContent] = Action.async { implicit request =>
    logger.trace(s"delete: id = $id")
    productService.remove(id).map { message =>
      Ok(Json.toJson(message))
    }
  }

}
