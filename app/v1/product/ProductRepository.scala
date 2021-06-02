package v1.product

import akka.actor.ActorSystem
import doobie.implicits._
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}
import v1.product.data.Connection

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

case class ProductData(id: Int, typeProduct: Int, name: String, gender: String, size: String, price: Double)

//case class TypeProduct(id_typeProduct: Int, name: String)

class ProductExecutionContext @Inject()(actorSystem: ActorSystem)
  extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the ProductRepository.
  */

/*object ObjectTypeProduct {

  val connection = new Connection

  def create(data: TypeProduct): doobie.Update0 = {
    sql"insert into typeProduct (id_typeProduct, name) values (${data.id_typeProduct},${data.name})".update
  }

  def list(): Iterable[TypeProduct] = {
    sql"select * from typeproduct".query[TypeProduct].to[List].transact(connection.xa).unsafeRunSync()
  }

  def get(id: Int): TypeProduct = {
    sql"select * from typeproduct where id_typeProduct = $id".query[TypeProduct].to[List].transact(connection.xa).unsafeRunSync().head
  }
}*/


trait ProductRepository {
  def create(data: ProductData)(implicit mc: MarkerContext): Future[doobie.Update0]

  def createList(dataList: List[ProductData])(implicit mc: MarkerContext): Future[Unit]

  def list()(implicit mc: MarkerContext): Future[Iterable[ProductData]]

  def get(id: Int)(implicit mc: MarkerContext): Future[Option[ProductData]]
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

  val connection = new Connection
  private val logger = Logger(this.getClass)

 /* private val ProductList = List(
    ProductData(1, ObjectTypeProduct.get(1), "Shirt", "W", "M", 30.5),
    ProductData(2, ObjectTypeProduct.get(1), "Skirt", "W", "L", 32),
    ProductData(3, ObjectTypeProduct.get(2), "Boots", "M", "42", 45.99),
    ProductData(4, ObjectTypeProduct.get(1), "Shirt", "M", "XL", 35.99),
    ProductData(5, ObjectTypeProduct.get(3), "Hat", "W", "s", 59.99),
    ProductData(6, ObjectTypeProduct.get(2), "Sneakers", "W", "41", 47.5),
  )*/

  override def list()(implicit mc: MarkerContext): Future[Iterable[ProductData]] = {
    Future {
      logger.trace("list: ")
      sql"select * from product".query[ProductData].to[List].transact(connection.xa).unsafeRunSync()
    }
  }

  override def get(id: Int)(implicit mc: MarkerContext): Future[Option[ProductData]] = {
    Future {
      logger.trace(s"get: id = $id")
      sql"select * from product where id_product = $id".query[Some[ProductData]].to[List].transact(connection.xa).unsafeRunSync().head
    }
  }

  def create(data: ProductData)(implicit mc: MarkerContext): Future[doobie.Update0] = {
    Future {
      logger.trace(s"create: data = $data")
      sql"insert into product (typeProduct, name, gender, size, price) values (${data.typeProduct}, ${data.name},${data.gender},${data.size},${data.price})"
        .update
    }
  }

  def createList(dataList: List[ProductData])(implicit mc: MarkerContext) = {
    Future {
      dataList.foreach { data =>
        logger.trace(s"create: data = $data")
        sql"insert into product (typeProduct, name, gender, size, price) values (${data.typeProduct}, ${data.name},${data.gender},${data.size},${data.price})"
          .update
      }
    }
  }

}
