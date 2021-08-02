package scala

import org.scalatest.flatspec.AnyFlatSpec
import play.api.libs.json._
import products.data.resource.Report._
import products.models.{ProductResource, ProductsToProcess}

class ReportSpec extends AnyFlatSpec {

  //setReportFromValidationError method test

  "setReportFromValidationError method" should
    "translate errors from 'play.api.libs.json,JsValue.validate' to a Report instance" in {
    val productsResourceValidated = jsonProductsToProcessWithErrors.validate[ProductsToProcess]
    productsResourceValidated.fold(
      errors => {
        println(errors.head._1.toString())
        val report = setReportFromValidationError(errors)
        println(report.idsFailure.head._1)
        val reportMessage = report.idsFailure.head._1.contains("TypeFieldError.path.missing")
        val errorPath = errors.head._1.toString().contains("/products(0)")
        val errorMessages = errors.head._2.head.messages.head.contains("error.path.missing")

        assert(reportMessage && errorPath && errorMessages)
      },
      _ => false)
  }




  //RESOURCES______________________________________________________________________________________________________
  //_______________________________________________________________________________________________________________

  //case class not available by setReportFromValidationError method__________________
  case class ErrorProductsToProcess(typeProcess: String, products: List[(Int, Int, String, String, String, Double)])

  object ErrorProductsToProcess {
    implicit val format: Format[ErrorProductsToProcess] = Json.format
  }


  //Products to process instances to evaluate________________________________________
  val errorProductResource: (Int, Int, String, String, String, Double) = (3, 3, "BatmanShirt", "M", "L", 35.50)
  implicit val errorProductResourceFormat: Format[(Int, Int, String, String, String, Double)] = Json.format
  val productsToProcessWithErrors: ErrorProductsToProcess = ErrorProductsToProcess("insert", List(errorProductResource))

  val correctProductResource: ProductResource = ProductResource(3, "Clothes", "BatmanShirt", "M", "L", 35.50)
  val productsToProcess: ProductsToProcess = ProductsToProcess("insert", List(correctProductResource))

  val jsonProductsToProcessWithErrors: JsValue = Json.toJson(productsToProcessWithErrors)
  val jsonProductsResource: JsValue = Json.toJson(correctProductResource)


}



