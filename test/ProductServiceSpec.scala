
import com.dimafeng.testcontainers._
import org.scalatest.flatspec.AsyncFlatSpec
import persistence.GetProductService
import products.models.ProductResource


class ProductServiceSpec extends AsyncFlatSpec
  with ForAllTestContainer
  with GetProductService {

  override val container: PostgreSQLContainer = PostgreSQLContainer()

  "inserting 11 products " should "have 11 records in database" in {
    val productService = getProductService(container)
    productService.insertProducts(productList, false)
    productService.listProductResource.map {
      listProducts =>
        assert(listProducts.length == 11)
    }
  }

  it should "insert 1 product in database" in {
    val productService = getProductService(container)
    val product = ProductResource(13, "Clothes", "BatmanShirt", "W", "M", 35.50)

    val product13BeforeInsert = productService.lookupProduct("13").map(product => product)
    //val promiseProduct13BeforeInsert = Promise[Option[ProductResource]]() completeWith product13BeforeInsert
    productService.insert(product, false)
    product13BeforeInsert.map(product=>assume(product.isEmpty))
    productService.lookupProduct("13").map(product=>assert(product.get.id==13))


  }

}
