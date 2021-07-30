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


Likewise, you can also send a POST directly as JSON to:

```bash
http POST http://localhost:9000/products
```

First for all, you must to create a json object which contain two fields: 'typeProcess':"String" and 'products':List[ProductResource].  
'typeProcess' just can contains two values:  

    'insert' --> to insert a list of products  
    'update' --> to update a list of products  

The program will return a report with failure result if the value of typeProcess is different from them.  

Here you have a Json's instance, to insert two products:


```bash
{
    "typeProcess": "insert",
    "products": [
        {
            "id": 1,
            "typeProductName": "Shoes",
            "name": "Boots",
            "gender": "M",
            "size": "42",
            "price": 45.99
        },
        {
            "id": 2,
            "typeProductName": "Clothes",
            "name": "Shirt",
            "gender": "M",
            "size": "XL",
            "price": 35.99
        }
    ]
}

```

If the 'insert' or 'update' process progresses successfully, it will return a report like the example shown below:

```bash
{
    "typeRequest": "insert",
    "items": 2,
    "ok": 1,
    "ko": 1,
    "idsFailure": {
        "ERROR: llave duplicada viola restricción de unicidad «product_pkey»\n  Detail: Ya existe la llave (id_product)=(2).": [2]
    }
}

```
