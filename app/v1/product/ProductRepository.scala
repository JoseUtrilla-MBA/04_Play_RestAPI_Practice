package v1.product

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import scala.concurrent.Future

final case class ProductData(id: ProductId, typeProduct: String, name: String, gender: String, size: String, price: Double)

class ProductId private(val underlying: Int) extends AnyVal {
  override def toString: String = underlying.toString
}

object ProductId {
  def apply(raw: String): ProductId = {
    require(raw != null)
    new ProductId(Integer.parseInt(raw))
  }
}

class ProductExecutionContext @Inject()(actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the ProductRepository.
  */
trait ProductRepository {
  def create(data: ProductData)(implicit mc: MarkerContext): Future[ProductId]

  def list()(implicit mc: MarkerContext): Future[Iterable[ProductData]]

  def get(id: ProductId)(implicit mc: MarkerContext): Future[Option[ProductData]]
}

/**
  * A trivial implementation for the ProductRepository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class ProductRepositoryImpl @Inject()()(implicit ec: ProductExecutionContext)
  extends ProductRepository {

  private val logger = Logger(this.getClass)

  private val listTypeProduct = Map(1 -> "clothing", 2 -> "shoes", 3 -> "accessories")

  private val ProductList = List(
    ProductData(ProductId("1"), listTypeProduct(1), "Shirt", "W", "M", 30.5),
    ProductData(ProductId("2"), listTypeProduct(1), "Skirt", "W", "L", 32),
    ProductData(ProductId("3"), listTypeProduct(2), "Boots", "M", "42", 45.99),
    ProductData(ProductId("4"), listTypeProduct(1), "Shirt", "M", "XL", 35.99),
    ProductData(ProductId("5"), listTypeProduct(3), "Hat", "W", "s", 59.99),
    ProductData(ProductId("6"), listTypeProduct(2), "Sneakers", "W", "41", 47.5),
  )

  override def list()(implicit mc: MarkerContext): Future[Iterable[ProductData]] = {
    Future {
      logger.trace(s"list: ")
      ProductList
    }
  }

  override def get(id: ProductId)(implicit mc: MarkerContext): Future[Option[ProductData]] = {
    Future {
      logger.trace(s"get: id = $id")
      ProductList.find(product => product.id == id)
    }
  }

  def create(data: ProductData)(implicit mc: MarkerContext): Future[ProductId] = {
    Future {
      logger.trace(s"create: data = $data")
      data.id
    }
  }

}
