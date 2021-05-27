package v1.product

import play.api.MarkerContext
import play.api.libs.json.{Format, Json}

import javax.inject.{Inject, Provider}
import scala.concurrent.{ExecutionContext, Future}

/**
  * DTO for displaying product information.
  */
case class ProductResource(id: String, typeProduct: String, name: String, gender: String, size: String, price: Double)
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
class ProductResourceHandler @Inject()(
                                     routerProvider: Provider[ProductRouter],
                                     productRepository: ProductRepository)(implicit ec: ExecutionContext) {

  def create(productInput: ProductFormInput)(implicit mc: MarkerContext): Future[ProductResource] = {
    val data = ProductData(ProductId("999"), productInput.typeProduct, productInput.name, productInput.gender, productInput.size, productInput.price.toDouble)
    // We don't actually create the product, so return what we have
    productRepository.create(data).map { id =>
      createProductResource(data)
    }
  }

  def lookupProduct(id: String)(implicit mc: MarkerContext): Future[Option[ProductResource]] = {
    val productFuture = productRepository.get(ProductId(id))
    productFuture.map { maybeProductData =>
      maybeProductData.map { productData =>
        createProductResource(productData)
      }
    }
  }

  def lookupBasicProduct(id: String)(implicit mc: MarkerContext): Future[Option[BasicProductResource]] = {
    val productFuture = productRepository.get(ProductId(id))
    productFuture.map { maybeProductData =>
      maybeProductData.map { productData =>
        createBasicProductResource(productData)
      }
    }
  }

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

  private def createProductResource(p: ProductData): ProductResource = {
    ProductResource(p.id.toString, p.typeProduct, p.name, p.gender, p.size, p.price)
  }

  private def createBasicProductResource(p: ProductData): BasicProductResource = {
    BasicProductResource(p.name, p.price)
  }

}
