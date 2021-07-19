package products.services

import play.api.MarkerContext
import products.controller.ProductFormInput
import products.data.repositories._
import products.models.ProductResource
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

/**
  * Controls access to the backend data, returning [[ProductResource]]
  */
class ProductService @Inject()(typeProductRepository: TypeProductRepository,
                               productRepository: ProductRepository,
                              )(implicit ec: ExecutionContext) {

  def listProductResource(implicit mc: MarkerContext): Future[List[ProductResource]] = {
    Future(productRepository.list().map(productData => createProductResource(productData)))
  }

  def lookupProduct(id: String)(implicit mc: MarkerContext): Future[Option[ProductResource]] = {
    Future {
      productRepository.get(id.toInt).map(p => createProductResource(p))
    } recover {
      case e: Exception => null
    }
  }

  def create(productInput: ProductFormInput)(implicit mc: MarkerContext): Future[ProductResource] = {
    Future {
      val data = ProductData(productInput.id_product, (productInput.id_typeProduct),
        productInput.name, productInput.gender, productInput.size, productInput.price)
      productRepository.create(data)
      createProductResource(data)
    }
  }

  private def createProductResource(p: ProductData): ProductResource = {
    ProductResource(p.id, typeProductRepository.get(p.idTypeProduct).get.name, p.name, p.gender, p.size, p.price)
  }

  def remove(id: String)(implicit mc: MarkerContext): Future[List[ProductResource]] = Future {
    val toDelete = productRepository.delete(id.toInt)
    productRepository.list().map(productData => createProductResource(productData))
  } recover {
    case e: Exception => productRepository.list().map(productData => createProductResource(productData))
  }
}