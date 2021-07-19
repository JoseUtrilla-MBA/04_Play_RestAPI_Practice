# ProductsRestAPI

ProductrestAPI is created to manage a clothing products store


### Running

You need sbt for this application to run at the command prompt:

```bash
sbt run
```
Play will start up on the HTTP port at <http://localhost:9000/>.


### Usage

If you call the same URL from the command line, you’ll see JSON. Using [httpie](https://httpie.org/), we can execute the command:

```bash
http --verbose http://localhost:9000/products                --> to see a json which contains all prducts with complete information.
                                                                    When you start this program for the first time, initial records are set in the database.
http --verbose http://localhost:9000/products/basic          --> to see a json which contains all prducts with basic information (product's name and price)
http --verbose http://localhost:9000/products/"id"           --> to see a json which contains the complete information of the product selected by its id
http --verbose http://localhost:9000/products/"id"/basic     --> to see a json which contains the basic information of the product selected by its id
http --verbose http://localhost:9000/products/delete/"id"    --> this request will delete a record from database, which is selected previously by its id.
                                                                    It will list all the products in our database without the deleted one.
```

and get back:

```routes
GET /v1/posts HTTP/1.1
```

Likewise, you can also send a POST directly as JSON:

```bash
http --verbose POST http://localhost:9000/products id_product="7" id_typeProduct="1" name="pants" gender="M" size="40" price="35.50"
```

and get:

```routes
POST /v1/posts HTTP/1.1
```


