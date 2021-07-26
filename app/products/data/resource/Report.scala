package products.data.resource

import play.api.libs.json.{Format, Json}


case class Report(typeRequest: String = "", items: Int = 0, ok: Int = 0, ko: Int = 0,
                  idsFailure: Map[String, List[Int]] = Map())

object Report {
  implicit val format: Format[Report] = Json.format
}

