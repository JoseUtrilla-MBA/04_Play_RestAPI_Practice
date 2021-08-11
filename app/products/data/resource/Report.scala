package products.data.resource

import play.api.libs.json.{Format, JsPath, Json, JsonValidationError}
import cats.implicits._

case class Report(typeRequest: String = "",
                  items: Int = 0,
                  ok: Int = 0,
                  ko: Int = 0,
                  idsFailure: Map[String, List[Int]] = Map())

object Report {
  implicit val format: Format[Report] = Json.format

  def setReportFromValidationError
  (errors: scala.collection.Seq[(JsPath, scala.collection.Seq[JsonValidationError])]): Report = {
    val errorMessages =
      for {
        error <- errors
      } yield {
        val pathString = error._1.toString()
        val id = Either.catchNonFatal(pathString.substring(1 + pathString.indexOf('('), pathString.indexOf(')'))) match {
          case Right(id) => id.toInt
          case Left(_) => 999999
        }

        val errorsString = {
          for {
            jsonValidationError <- error._2
            message <- jsonValidationError.messages
          } yield (id, "ERROR: TypeFieldE"
            + message.substring(1) + " in Path: "
            + pathString.replace("(" + id + ")", "") + ". DETAIL: indexes")
        }.toList
        errorsString
      }

    Report("undefined, jsProductToProcess badly built",
      0,
      0,
      0,
      errorMessages.toList.flatten.groupMap(_._2)(_._1))
  }
}



