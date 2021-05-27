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
http --verbose http://localhost:9000/v1/products                --> to see a json which contains all prducts with complete information
http --verbose http://localhost:9000/v1/products/basic          --> to see a json which contains all prducts with basic information (product's name and price)
http --verbose http://localhost:9000/v1/products/"id"           --> to see a json which contains the complete information of the product selected by its id
http --verbose http://localhost:9000/v1/products/"id"/basic     --> to see a json which contains the basic information of the product selected by its id
```

and get back:

```routes
GET /v1/posts HTTP/1.1
```

Likewise, you can also send a POST directly as JSON:

```bash
http --verbose POST http://localhost:9000/v1/products typeProduct="clothes" name="pants" gender="M" size="40" price="0.0"
```

and get:

```routes
POST /v1/posts HTTP/1.1
```


### Load Testing

The best way to see what Play can do is to run a load test.  We've included Gatling in this test project for integrated load testing.

Start Play in production mode, by [staging the application](https://www.playframework.com/documentation/2.5.x/Deploying) and running the play scripts:

```bash
sbt stage
cd target/universal/stage
./bin/play-scala-rest-api-example -Dplay.http.secret.key=some-long-key-that-will-be-used-by-your-application
```

Then you'll start the Gatling load test up (it's already integrated into the project):

```bash
sbt ";project gatling;gatling:test"
```

For best results, start the gatling load test up on another machine so you do not have contending resources.  You can edit the [Gatling simulation](http://gatling.io/docs/2.2.2/general/simulation_structure.html#simulation-structure), and change the numbers as appropriate.

Once the test completes, you'll see an HTML file containing the load test chart:

```bash
./play-scala-rest-api-example/target/gatling/gatlingspec-1472579540405/index.html
```

That will contain your load test results.
