package products.controller

import cats.implicits._
import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import products.controller.resource.GetProductService._
import products.data.PoolConnection
import products.data.resource.{Report, CreateRepositories}
import products.models._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

case class ProductController @Inject()(cc: ControllerComponents)
                                      (implicit ec: ExecutionContext)
  extends AbstractController(cc) with CreateRepositories {


  def restartTables: Action[AnyContent] = Action { implicit request =>
    cleanTables()
    createTables()
    Redirect("/products/")
  }

  private val logger = Logger(this.getClass)

  def showProducts(typeProduct: String = ""): Action[AnyContent] =
    Action.async { implicit request =>
      logger.trace("productList:")
      val productService =
        getProductService(Either.catchNonFatal(request.attrs(connectionKey)).getOrElse(PoolConnection.transactor))

      productService.getProducts.map { products =>
        typeProduct match {
          case "" => Ok(Json.toJson(products))
          case "basic" =>
            val basicProducts = products.map(product => BasicProductResource(product.name, product.price))
            Ok(Json.toJson(basicProducts))
        }
      }
    }

  def showProduct(idAndType: String): Action[AnyContent] = Action.async { implicit request =>
    val id = idAndType.substring(0,
      if (idAndType.contains('/')) idAndType.indexOf('/')
      else idAndType.length)

    logger.trace(s"showProduct: id = $id")
    val productService =
      getProductService(Either.catchNonFatal(request.attrs(connectionKey)).getOrElse(PoolConnection.transactor))

    productService.getProduct(id).map { either =>
      idAndType match {

        case str if !str.contains("/") => either match {
          case Right(productResource) => Ok(Json.toJson(productResource))
          case Left(report) => Ok(Json.toJson(report))
        }
        case str if str == s"$id/basic" =>
          either match {
            case Right(productResource) =>
              Ok(Json.toJson(BasicProductResource(productResource.name, productResource.price)))
            case Left(report) => Ok(Json.toJson(report))
          }
      }
    }
  }

  def upsert: Action[JsValue] = Action.async(parse.json) { implicit request =>
    logger.trace("add:")
    val productService =
      getProductService(Either.catchNonFatal(request.attrs(connectionKey)).getOrElse(PoolConnection.transactor))

    val productsToProcess = request.body.validate[ProductsToProcess]
    productsToProcess.fold(
      errors => Future(BadRequest(Json.toJson(Report.setReportFromValidationError(errors))))
      ,
      productsToProcess => {
        val typeProcess = productsToProcess.typeProcess

        typeProcess match {
          case "insert" => productService.insertProducts(productsToProcess.products).map(report =>
            Ok(Json.toJson(report)))

          case "update" => productService.updateProducts(productsToProcess.products).map(report =>
            Ok(Json.toJson(report)))

          case _ => Future {
            val report = Report(typeProcess, productsToProcess.products.length, 0,
              productsToProcess.products.length, Map(
                s"""ERROR: unspecified or incorrect process type <<typeProcess>>.
                   | Detail: process type: '$typeProcess', enabled types: 'insert', 'update'."""
                  .stripMargin -> productsToProcess.products.map(product => product.id)))

            Ok(Json.toJson(report))
          }
        }
      })
  }

  def delete(id: String): Action[AnyContent] = Action.async { implicit request =>
    logger.trace(s"delete: id = $id")
    val productService =
      getProductService(Either.catchNonFatal(request.attrs(connectionKey)).getOrElse(PoolConnection.transactor))

    productService.removeProduct(id).map(message => Ok(Json.toJson(message)))
  }

}
