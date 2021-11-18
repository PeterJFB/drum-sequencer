## Spring Boot as a web service

We previously mentioned accessing storage through a REST-api. This has been achieved with [Spring](https://spring.io/), a popular [IoC](https://www.baeldung.com/inversion-control-and-dependency-injection-in-spring)-based framework to create such services.

The framework itself uses similar programming concepts as [Jakarta](https://en.wikipedia.org/wiki/Jakarta_RESTful_Web_Services), i.e. creating independent services which can be injected and managed by a main application. Spring itself is packaged in an additional layer of logic to run the project as a web service, called [Spring Boot](https://spring.io/projects/spring-boot). This has allowed us to focus on writing the REST-logic of our application, while [Tomcat](http://tomcat.apache.org/) and other dependecies are configured to the expected standards. Spring boot also supports strong REST-enforcement with their preconfigured controllers, adding and additional layer of code quality checks when writing the application.

### Preserve server with rate limiting

Our application is very accessible when it comes to sharing tracks. Tracks are available to anyone familiar with our API, and any track in a valid format will be stored. This also means anyone can post to our server, and can run into the risk of getting a high server load, or other similar server attacks.

There are several solutions to mitigate this, such as requiring authentication, end-to-end encryption and other security implementations. Many of these will dampen our problem, but also challenge our users with security-checks which will likely appear meaningless, as authentication (intentionally) does not change how the application behaves.

Track information contains no user-sensitive data (see examples of post-requests), which means our greatest concern is server load. An satifying solution to this is to use ip-based rate limiting, which is implemented with [Bucket4j](https://github.com/MarcGiffing/bucket4j-spring-boot-starter#bucket4j_complete_properties). Our implementation can also easily be changed to be user-based, if this is something we wish to use later.

Ip-based limiting requires somewhere to store the ip-adresses and their respective [buckets](https://en.wikipedia.org/wiki/Token_bucket). There's no reason to store this long-term, though a high access time is crucial to maintain a high server performance. This is why we store it with in-memory cache, which is achieved with [caffeine](https://github.com/ben-manes/caffeine).
