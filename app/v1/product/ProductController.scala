package v1.product

import play.api.Logger
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


case class ProductFormInput(typeProduct: String, name: String, gender: String, size: String, price: String)

/**
  * Takes HTTP requests and produces JSON.
  */
class ProductController @Inject()(cc: ProductControllerComponents)(implicit ec: ExecutionContext)
  extends ProductBaseController(cc) {

  private val logger = Logger(getClass)

  private val form: Form[ProductFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "typeProduct" -> nonEmptyText,
        "name" -> nonEmptyText,
        "gender" -> nonEmptyText,
        "size" -> text,
        "price" -> text
      )(ProductFormInput.apply)(ProductFormInput.unapply)
    )
  }

  def productList: Action[AnyContent] = ProductAction.async { implicit request =>
    logger.trace("productList: ")
    ProductResourceHandler.listProductResource.map { products =>
      Ok(Json.toJson(products))
    }
  }

  def basicProductList: Action[AnyContent] = ProductAction.async { implicit request =>
    logger.trace("productList: ")
    ProductResourceHandler.listBasicProductResource.map { products =>
      Ok(Json.toJson(products))
    }
  }

  def process: Action[AnyContent] = ProductAction.async { implicit request =>
    logger.trace("process: ")
    processJsonProduct()
  }

  def showProduct(id: String): Action[AnyContent] = ProductAction.async {
    implicit request =>
      logger.trace(s"show: id = $id")
      ProductResourceHandler.lookupProduct(id).map { product =>
        Ok(Json.toJson(product))
      }
  }

  def showBasicProduct(id: String): Action[AnyContent] = ProductAction.async {
    implicit request =>
      logger.trace(s"show: id = $id")
      ProductResourceHandler.lookupBasicProduct(id).map { product =>
        Ok(Json.toJson(product))
      }
  }

  private def processJsonProduct[A]()(implicit request: ProductRequest[A]): Future[Result] = {
    def failure(badForm: Form[ProductFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: ProductFormInput) = {
      ProductResourceHandler.create(input).map { product =>
        Created(Json.toJson(product))
      }
    }

    form.bindFromRequest().fold(failure, success)
  }


}
