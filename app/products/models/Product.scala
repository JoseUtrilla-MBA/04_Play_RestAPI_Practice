package products.models

import play.api.libs.json._

case class ProductResource(id: Int, typeProduct: String, name: String,
                           gender: String, size: String, price: Double)

object ProductResource {
  //def fromData...
  implicit val format: Format[ProductResource] = Json.format
  implicit val readJson: Reads[ProductResource] = Json.reads
}


case class BasicProductResource(name: String, price: Double)

object BasicProductResource {
  implicit val format: Format[BasicProductResource] = Json.format
}
