package products.services

import products.data.repositories._
import products.data.resource._
import products.models.ProductResource

import scala.concurrent.{ExecutionContext, Future}

/**
  * Controls access to the backend data, returning [[ProductResource]]
  */
case class ProductService(productRepository: ProductRepository,
                     typeProductRepository: TypeProductRepository)
                    (implicit ec: ExecutionContext) {

  def listProductResource: Future[List[ProductResource]] = {
    Future(productRepository.list().map(productData => ProductResource.fromData(productData)))
  }

  def lookupProduct(id: String): Future[Option[ProductResource]] =
    Future {
      productRepository.get(id.toInt).map(productData => ProductResource.fromData(productData))
    } recover {
      case e: Exception => productRepository.get(0)
        .map(productData => ProductResource.fromData(productData))
    }

  def insert(p: ProductResource, toUpdate: Boolean): Report = {
    val data = ProductData(p.id, typeProductRepository.getByName(p.typeProductName).getOrElse(TypeProductData()),
      p.name, p.gender, p.size, p.price)
    toUpdate match {
      case true => {
        productRepository.update(data)
      }
      case _ => productRepository.insert(data)
    }
  }

  def insertProducts(ps: List[ProductResource], toUpdate: Boolean): Report = {
    def toData(p: ProductResource) = ProductData(p.id, typeProductRepository.getByName(p.typeProductName).getOrElse(TypeProductData()),
      p.name, p.gender, p.size, p.price)

    val productDataList = ps.map(pr => toData(pr))
    toUpdate match {
      case true => {
        productRepository.update(productDataList)
      }
      case _ => productRepository.insert(productDataList)
    }
  }

  def remove(id: String): Future[Report] = Future {
    productRepository.delete(id.toInt)
  } recover {
    case ne: NumberFormatException => Report("delete",1,0,1,Map("numeric value not introduced" -> List()))
    case e: Exception => productRepository.delete(0)
  }
}