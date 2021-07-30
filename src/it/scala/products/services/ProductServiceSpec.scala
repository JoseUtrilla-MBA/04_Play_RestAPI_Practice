package products.services

import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import org.scalatest.flatspec.AsyncFlatSpec
import products.models.ProductResource
import products.services.resource.GetProductService

class ProductServiceSpec extends AsyncFlatSpec
  with ForAllTestContainer
  with GetProductService {

  override val container: PostgreSQLContainer = PostgreSQLContainer()

  "inserting 11 products " should "have 11 records in database" in {
    val productService = getProductService(container)

    for {
      products <- productService.getProducts
    } yield assert(products.length == 11)
  }

  "inserting an empty list of products " should "not vary the number of rows in the table" in {
    val productService = getProductService(container)

    for {
      products <- productService.getProducts
      _ <- productService.insertProducts(List[ProductResource]())
      productsII <- productService.getProducts
    } yield assert(products.length == productsII.length)
  }


  it should "insert 1 product in database" in {
    val productService = getProductService(container)

    val product = ProductResource(13, "Clothes", "BatmanShirt", "W", "M", 35.50)

    for {
      _ <- productService.getProduct("13").map(optionProduct => assume(optionProduct.isEmpty))
      _ <- productService.insertProducts(List(product))
      products <- productService.getProducts
    } yield assert(products.contains(product))
  }

  "the number of products in a database," should
    "be length minus 1, if one of their records is deleted" in {
    val productService = getProductService(container)

    for {
      initialProducts <- productService.getProducts
      _ <- productService.removeProduct("2")
      listProductsII <- productService.getProducts
    } yield {
      assert(listProductsII.length == initialProducts.length - 1)
    }
  }

  "the number of products in a database," should
    "not vary if one of them is deleted by an non-existent id" in {
    val productService = getProductService(container)

    for {
      initialProducts <- productService.getProducts
      _ <- productService.removeProduct("22")
      listProductsII <- productService.getProducts
    } yield {
      assert(listProductsII.length == initialProducts.length)
    }
  }

  "if we change the value of a product's price, from 35.50 to 29.99. its price" should
    "also updated in database to 29,99" in {
    val productService = getProductService(container)

    val product = ProductResource(13, "Clothes", "BatmanShirt", "W", "M", 35.50)
    val productToUpdate = ProductResource(13, "Clothes", "BatmanShirt", "W", "M", 29.99)

    for {
      _ <- productService.insertProducts(List(product))
      _ <- productService.getProduct("13").map(product => assume(product.get.price == 35.50))
      _ <- productService.updateProducts(List(productToUpdate))
      productUpdated <- productService.getProduct("13")
    } yield {
      assert(productUpdated.get.price == 29.99)
    }
  }


}
