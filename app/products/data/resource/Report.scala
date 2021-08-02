package products.data.resource

import play.api.libs.json.{Format, JsPath, Json, JsonValidationError}


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
        val id = pathString.substring(1 + pathString.indexOf('('), pathString.indexOf(')'))
        val errorsString = {
          for {
            jsonValidationError <- error._2
            message <- jsonValidationError.messages
          } yield (Option(id.toInt).getOrElse(999999), "ERROR: TypeFieldE"
            + message.substring(1) + " in Path: "
            + pathString.replace(id, "index") + ". DETAIL: indexes")
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



