package products.services

import products.data.repositories._
import products.models.ProductResource

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/**
  * Controls access to the backend data, returning [[ProductResource]]
  */
class ProductService @Inject()(typeProductRepository: TypeProductRepositoryImpl,
                               productRepository: ProductRepository,
                              )(implicit ec: ExecutionContext) {

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

  def insertService(p: ProductResource): String = {
    val data = ProductData(p.id, typeProductRepository.getByName(p.typeProductName).getOrElse(TypeProduct()),
      p.name, p.gender, p.size, p.price)
    productRepository.insert(data)
  }

  def remove(id: String): Future[String] = Future {
     productRepository.delete(id.toInt)
  }recover {
    case ne: NumberFormatException => "you must insert a natural number as Id"
    case e: Exception => productRepository.delete(0)
  }
}