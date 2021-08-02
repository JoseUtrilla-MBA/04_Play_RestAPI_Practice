package products.controller

import cats.implicits._
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import products.controller.resource.GetProductService._
import products.data.resource.Report
import products.models._
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

case class ProductController @Inject()(cc: ControllerComponents)
                                      (implicit ec: ExecutionContext)
  extends AbstractController(cc) {

  private val logger = Logger(this.getClass)

  def showProducts(typeProduct: String = ""): Action[AnyContent] =
    Action.async { implicit request =>
      logger.trace("productList:")
      val testConnection = Either.catchNonFatal(request.attrs(connectionKey))

      productService(testConnection).getProducts.map { products =>
        typeProduct match {
          case "" => Ok(Json.toJson(products))
          case "basic" => {
            val basicProducts = products.map(product => BasicProductResource(product.name, product.price))
            Ok(Json.toJson(basicProducts))
          }
        }
      }
    }

  def showProduct(idAndType: String): Action[AnyContent] = Action.async { implicit request =>
    val id = idAndType.substring(0,
      if (idAndType.contains('/')) idAndType.indexOf('/')
      else idAndType.length)

    logger.trace(s"showProduct: id = $id")
    val testConnection = Either.catchNonFatal(request.attrs(connectionKey))

    productService(testConnection).getProduct(id).map { product =>
      idAndType match {

        case str if !str.contains("/") => Ok(Json.toJson(product))
        case str if str == s"$id/basic" => {
          val basicProduct = product.map(product => BasicProductResource(product.name, product.price))
          Ok(Json.toJson(basicProduct))
        }
      }
    }
  }

  def process: Action[JsValue] = Action.async(parse.json) { implicit request =>
    logger.trace("add:")
    val testConnection = Either.catchNonFatal(request.attrs(connectionKey))
    val prService = productService(testConnection)
    val productsToProcess = request.body.validate[ProductsToProcess]
    productsToProcess.fold(
      errors => Future(BadRequest(Json.toJson(Report.setReportFromValidationError(errors))))
      ,
      productsToProcess => {
        val typeProcess = productsToProcess.typeProcess

        typeProcess match {
          case "insert" => prService.insertProducts(productsToProcess.products).map(report =>
            Ok(Json.toJson(report)))

          case "update" => prService.updateProducts(productsToProcess.products).map(report =>
            Ok(Json.toJson(report)))

          case _ => Future {
            val report = Report(typeProcess, productsToProcess.products.length, 0,
              productsToProcess.products.length, Map(
                s"""ERROR: tipo de proceso no especificado o es incorrecto <<typeProcess>>.
                   | Detail: tipo introducido: '$typeProcess', tipos habilitados: 'insert', 'update'."""
                  .stripMargin -> productsToProcess.products.map(product => product.id)))

            Ok(Json.toJson(report))
          }
        }
      })
  }

  def delete(id: String): Action[AnyContent] = Action.async { implicit request =>
    logger.trace(s"delete: id = $id")
    val testConnection = Either.catchNonFatal(request.attrs(connectionKey))

    productService(testConnection).removeProduct(id).map(message => Ok(Json.toJson(message)))
  }

}
