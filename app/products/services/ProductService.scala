package products.services

import products.data.repositories._
import products.data.resource._
import products.models.ProductResource
import scala.concurrent.{ExecutionContext, Future}

case class ProductService(productRepository: ProductRepository,
                          typeProductRepository: TypeProductRepository)
                         (implicit ec: ExecutionContext) {

  def getProducts: Future[List[ProductResource]] = {
    Future(productRepository.getList.map(productData => ProductResource.fromData(productData)))
  }

  def getProduct(id: String): Future[Option[ProductResource]] =
    Future {
      productRepository.get(id.toInt).map(productData => ProductResource.fromData(productData))
    } recover {
      case _:Exception => productRepository.get(0)
        .map(productData => ProductResource.fromData(productData))
    }

  def insertProducts(products: List[ProductResource]): Future[Report] = {
    Future {
      def toProductData(productResource: ProductResource): ProductData = ProductData(productResource.id,
        typeProductRepository.getByName(productResource.typeProductName).getOrElse(TypeProductData()),
        productResource.name, productResource.gender, productResource.size, productResource.price)

      val productDataList = products.map(pr => toProductData(pr))
      productRepository.insert(productDataList)
    }
  }

  def updateProducts(products: List[ProductResource]): Future[Report] = {
    Future {
      def toProductData(productResource: ProductResource): ProductData = ProductData(productResource.id,
        typeProductRepository.getByName(productResource.typeProductName).getOrElse(TypeProductData()),
        productResource.name, productResource.gender, productResource.size, productResource.price)

      val productDataList = products.map(productResource => toProductData(productResource))
      productRepository.update(productDataList)
    }
  }


  def removeProduct(id: String): Future[Report] = Future {
    productRepository.delete(id.toInt)
  } recover {
    case  _:NumberFormatException => Report("delete", 1, 0, 1, Map("numeric value not introduced" -> List()))
    case  _:Exception => productRepository.delete(0)
  }
}