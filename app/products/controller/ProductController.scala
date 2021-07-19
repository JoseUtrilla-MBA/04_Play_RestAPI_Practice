package products.controller

import play.api.Logger
import play.api.data.Form
import play.api.libs.json._
import play.api.mvc._
import products.models._
import products.services.ProductService

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


case class ProductFormInput(id_product: Int, id_typeProduct: Int, name: String, gender: String, size: String, price: Double)

class ProductController @Inject()(cc: ControllerComponents,
                                  productService: ProductService)
                                 (implicit ec: ExecutionContext)
  extends AbstractController(cc)  with RequestMarkerContext {


  private val logger = Logger(getClass)

  private val form: Form[ProductFormInput] = {
    import play.api.data.Forms._
    import play.api.data.format.Formats._
    Form(
      mapping(
        "id_product" -> of[Int],
        "id_typeProduct" -> of[Int],
        "name" -> of[String],
        "gender" -> of[String],
        "size" -> of[String],
        "price" -> of[Double],
      )(ProductFormInput.apply)(ProductFormInput.unapply)
    )
  }

  def productList: Action[AnyContent] = Action.async { implicit request =>
    logger.trace("productList: ")
    productService.listProductResource.map { products =>
      Ok(Json.toJson(products))
    }
  }

  def basicProductList: Action[AnyContent] = Action.async { implicit request =>
    logger.trace("productList: ")
    productService.listProductResource.map { products =>
      val basicProducts = products.map(product => BasicProductResource(product.name, product.price))
      Ok(Json.toJson(basicProducts))
    }
  }

  def showProduct(id: String): Action[AnyContent] = Action.async {
    implicit request =>
      logger.trace(s"show: id = $id")
      productService.lookupProduct(id).map { product =>
        Ok(Json.toJson(product))
      }
  }

  def showBasicProduct(id: String): Action[AnyContent] = Action.async {
    implicit request =>
      logger.trace(s"show: id = $id")
      productService.lookupProduct(id).map { product =>
        val basicProduct = product match {
          case Some(value) => BasicProductResource(value.name, value.price)
          case None => null
        }
        Ok(Json.toJson(Option(basicProduct)))
      }
  }

  def process: Action[AnyContent] = Action.async { implicit request =>
    logger.trace("process: ")
    Future(Ok("do nothing until new update"))
    //processJsonProduct()
  }

  private def processJsonProduct[A]()(implicit request: ProductRequest[A]): Future[Result] = {
    def failure(badForm: Form[ProductFormInput]): Future[Result] = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: ProductFormInput): Future[Result] = {
      productService.create(input).map { product =>
        Created(Json.toJson(product))
      }
    }

    form.bindFromRequest().fold(failure, success)
  }

  def delete(id: String): Action[AnyContent] = Action.async { implicit request =>
    logger.trace(s"delete by id: id = $id")
    productService.remove(id).map { products =>
      Ok(Json.toJson(products))
    }
  }

}
