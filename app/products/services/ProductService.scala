package products.services

import products.data.repositories._
import products.data.resource._
import products.models.ProductResource
import cats.implicits._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

case class ProductService(productRepository: ProductRepository,
                          typeProductRepository: TypeProductRepositoryImpl)
                         (implicit ec: ExecutionContext) {

  def getProducts: Future[List[ProductResource]] = {
    productRepository.getList.map(productsData =>
      productsData.map(productData => ProductResource.fromData(productData)))
  }

  def getProduct(id: String): Future[Either[Report,ProductResource]] = {
    val nId = Either.catchNonFatal(id.toInt)
    nId match {
      case Right(n) => productRepository.get(n).map {
        case Some(product) => Right(ProductResource.fromData(product))
        case None => Left(Report("getProduct",1,0,1,Map("id not found in database" -> List())))
      }
      case Left(_) => getProduct("0")
    }
  }



  def toProductData(productResource: ProductResource): Future[ProductData] = {
    typeProductRepository.getByName(productResource.typeProductName).map(typeProductData =>
      ProductData(productResource.id,
        typeProductData.getOrElse(TypeProductData()),
        productResource.name,
        productResource.gender,
        productResource.size,
        productResource.price))
  }

  def insertProducts(products: List[ProductResource]): Future[Report] = {
    products.map(toProductData).sequence.flatMap(products => productRepository.insert(products))
  }

  def updateProducts(products: List[ProductResource]): Future[Report] = {
    products.map(toProductData).sequence.flatMap(products => productRepository.update(products))
  }

  def removeProduct(id: String): Future[Report] = {
    val nId = Either.catchNonFatal(id.toInt)
    nId match {
      case Right(n) => productRepository.delete(n)
      case Left(_) => Future(Report("delete", 1, 0, 1, Map("numeric value not introduced" -> List())))
    }
  }
}