# ProductsRestAPI

ProductRestAPI is created to manage a clothing products store


### Running

You need sbt for this application to run at the command prompt:

```bash
sbt run
```
Play will start up on the HTTP port at <http://localhost:9000/products>.


### Usage

We use play.api.db.evolutions to start a database, creating its tables and eleven inserted rows.  
If at any time you need to reset the database, apply the following URL:  ```http://localhost:9000/products/reset```  


You have the following URLs to test the different options that the Application offers to the client.  

```bash
http://localhost:9000/products                --> to see a json which contains all prducts with complete information.
http://localhost:9000/products/basic          --> to see a json which contains all products with basic information (product's name and price)
http://localhost:9000/products/"id"           --> to see a json which contains the complete information of the product selected by its id
http://localhost:9000/products/"id"/basic     --> to see a json which contains the basic information of the product selected by its id
http://localhost:9000/products/delete/"id"    --> this request will delete a record from database, which is selected previously by its id.
```


Likewise, you can also send a POST directly as JSON to:

```bash
http POST http://localhost:9000/products
```

First of all, you must to create a json object which contain two fields: 'typeProcess':"String" and 'products':List[ProductResource].  
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
        "ERROR: Duplicate key violates unique constraint <<product_pkey>> \ n Detail: Key already exists (id_product) = (1).": [1]
    }
}

```

### Testing

You can test without the need for a local database, via testcontainer from docker. You must first have docker installed,  
and them run the following commands:

```bash
sbt IntegrationTest/test   --> to test ProductControllerSpec
sbt test                   --> to test ReportSpec
```

