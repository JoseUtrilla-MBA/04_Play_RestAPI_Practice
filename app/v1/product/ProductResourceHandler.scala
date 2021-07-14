package v1.product

import play.api.MarkerContext
import play.api.libs.json._
import v1.product.data.{ManageTypeProduct, ProductData, ProductRepository}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class ProductResource(id: Int, typeProduct: String, name: String, gender: String, size: String, price: Double)

case class BasicProductResource(name: String, price: Double)

/**
  * Mapping to read/write a ProductResource out as a JSON value.
  */
object ProductResource {
  implicit val format: Format[ProductResource] = Json.format
}

object BasicProductResource {
  implicit val format: Format[BasicProductResource] = Json.format
}

/**
  * Controls access to the backend data, returning [[ProductResource]]
  */
class ProductResourceHandler @Inject()(manageTypeProduct: ManageTypeProduct, productRepository: ProductRepository)(implicit ec: ExecutionContext) {


  def listProductResource(implicit mc: MarkerContext): Future[Iterable[ProductResource]] = {
    productRepository.list().map { productDataList =>
      productDataList.map(productData => createProductResource(productData))
    }
  }

  def listBasicProductResource(implicit mc: MarkerContext): Future[Iterable[BasicProductResource]] = {
    productRepository.list().map { productDataList =>
      productDataList.map(productData => createBasicProductResource(productData))
    }
  }

  def lookupProduct(id: String)(implicit mc: MarkerContext): Future[Option[ProductResource]] = {
    try {
      val productFuture = productRepository.get(id.toInt)
      productFuture.map { maybeProductData =>
        maybeProductData.map { productData =>
          createProductResource(productData)
        }
      }
    } catch {
      case e: Exception => lookupProduct("0")
    }
  }

  def lookupBasicProduct(id: String)(implicit mc: MarkerContext): Future[Option[BasicProductResource]] = {
    try {
      val productFuture = productRepository.get(id.toInt)
      productFuture.map { maybeProductData =>
        maybeProductData.map { productData =>
          createBasicProductResource(productData)
        }
      }
    } catch {
      case e: Exception => lookupBasicProduct("0")
    }
  }

  def create(productInput: ProductFormInput)(implicit mc: MarkerContext): Future[ProductResource] = {
    val data = ProductData(productInput.id_product, productInput.id_typeProduct,
      productInput.name, productInput.gender, productInput.size, productInput.price)
    productRepository.create(data).map { x =>
      createProductResource(data)
    }
  }

  private def createProductResource(p: ProductData): ProductResource = {
    ProductResource(p.id, manageTypeProduct.get(p.id_typeProduct).name, p.name, p.gender, p.size, p.price)
  }

  private def createBasicProductResource(p: ProductData): BasicProductResource = {
    BasicProductResource(p.name, p.price)
  }

  def remove(id: String)(implicit mc: MarkerContext): Future[Iterable[ProductResource]] = try {
    productRepository.delete(id.toInt).map { productDataList =>
      productDataList.map(productData => createProductResource(productData))
    }
  } catch {
    case e: Exception => listProductResource
  }
}