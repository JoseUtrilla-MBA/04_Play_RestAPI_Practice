package scala

import org.scalatest.flatspec.AnyFlatSpec
import play.api.libs.json._
import products.data.resource.Report._
import products.models.ProductsToProcess

class ReportSpec extends AnyFlatSpec {

  //case class to force errors when is passed to play.api.libs.json,JsValue.validate__
  case class ErrorProductsToProcess(typeProcess: String, products: List[ErrorProductResource])

  object ErrorProductsToProcess {
    implicit val format: Format[ErrorProductsToProcess] = Json.format
  }

  //ProductResource with an error: the parameter typeProductName must be a String like ("Clothes"), instead an Integer
  case class ErrorProductResource(id: Int, typeProductName: Int, name: String,
                                  gender: String, size: String, price: Double)

  object ErrorProductResource {
    implicit val format: Format[ErrorProductResource] = Json.format
  }

  val errorProductResource: ErrorProductResource = ErrorProductResource(3, 1, "BatmanShirt", "M", "L", 35.50)
  val productsToProcessWithErrors: ErrorProductsToProcess = ErrorProductsToProcess("insert", List(errorProductResource))

  val jsonProductsToProcessWithErrors: JsValue = Json.toJson(productsToProcessWithErrors)


  "setReportFromValidationError method" should
    "translate errors from 'play.api.libs.json,JsValue.validate' to a products.data.resource.Report instance" in {
    val productsResourceValidated = jsonProductsToProcessWithErrors.validate[ProductsToProcess]
    productsResourceValidated.fold(
      errors => {
        val report = setReportFromValidationError(errors)

        val errorsContainsPathOfFieldAffected = errors.head._1.toString().contains("products(0)/typeProductName")
        val errorContainsMessagesTypeError = errors.head._2.head.messages.head.contains("error.expected.jsstring")

        // The report instance message is built from errors messages and errors path.
        // And then, a list of element's indexes affected are assigned to these messages.
        val reportMessageContainsErrors =
          report.idsFailure.head._1
            .contains("ERROR: TypeFieldError.expected.jsstring in Path: /products/typeProductName. DETAIL: indexes")
        val reportContainsListOfIndexesAffected = report.idsFailure.head._2.head == 0

        assert(reportMessageContainsErrors &&
          errorsContainsPathOfFieldAffected &&
          errorContainsMessagesTypeError &&
          reportContainsListOfIndexesAffected)
      },
      _ => false)
  }

}



