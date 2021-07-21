package products.models

import play.api.libs.json._
import products.data.repositories.ProductData

case class ProductResource(id: Int, typeProductName: String, name: String,
                           gender: String, size: String, price: Double)

object ProductResource {
  def fromData(p: ProductData): ProductResource = ProductResource(p.id, p.typeProduct.name, p.name, p.gender, p.size, p.price)
  implicit val format: Format[ProductResource] = Json.format
  implicit val readJson: Reads[ProductResource] = Json.reads
}


case class BasicProductResource(name: String, price: Double)

object BasicProductResource {
  implicit val format: Format[BasicProductResource] = Json.format
}
