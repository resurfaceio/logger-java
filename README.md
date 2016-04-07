# resurfaceio-logger-java
&copy; 2016 Resurface Labs LLC, All Rights Reserved

This library makes it easy to log server usage including HTTP request/response details.

## Dependencies

No runtime dependencies to conflict with your app. Requires Java 8.

## Installing with Maven

Add these sections to your pom.xml:

    <dependencies>
        <dependency>
            <groupId>io.resurface</groupId>
            <artifactId>resurfaceio-logger</artifactId>
            <version>1.0.X-SNAPSHOT</version>
        </dependency>
    </dependencies>

    <repositories>
        <repository>
            <id>resurfaceio-mavenrepo</id>
            <url>https://github.com/resurfaceio/resurfaceio-mavenrepo/raw/master</url>
            <releases>
                <enabled>false</enabled>
            </releases>
            <snapshots>
                <enabled>true</enabled>
            </snapshots>
        </repository>
    </repositories>

## Java API

    import io.resurface.HttpLogger;
    import io.resurface.HttpLoggerFactory;
    
    HttpLogger logger = HttpLoggerFactory.get();     // returns default cached HTTP logger
    logger.logRequest(request);                      // log HTTP request details
    logger.logResponse(response, body);              // log HTTP response details, body is optional
    if (logger.isEnabled()) ...                      // intending to log stuff?
    logger.enable();                                 // enable logging for dev/staging/production
    logger.disable();                                // disable logging for automated tests

## Using Servlet Filter

This works for Tomcat, Jetty and other application servers that support standard servlet filters.

Copy resurfaceio-logger-1.0.X.jar into the appropriate /lib directory. (no other dependencies required)

Now configure the filter in web.xml as shown below. You can optionally use a specific url-pattern to limit logging to specific areas of your application.

    <filter>
        <filter-name>HttpLoggerForServlets</filter-name>
        <filter-class>io.resurface.HttpLoggerForServlets</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>HttpLoggerForServlets</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    
All requests/responses mapping to the filter will be logged. The filter will record the response body only when the response content type starts with "text/", when a valid 
character encoding is available, and when the response code is not 304 (cache not modified).

## Using Spark Framework

http://sparkjava.com/documentation.html

Spark is really cool, but not all cases are working cleanly yet. (hopefully that's temporary)

### Logging simple routes

A logger can be used selectively (within a single route) for simple cases like this:

    get("/hello", (req, res) -> {
        HttpLogger logger = HttpLoggerFactory.get();
        logger.logRequest(req.raw());
        String body = "...";
        logger.logResponse(res.raw(), body);
        return body;
    });

WARNING: This pattern DOES NOT work if the route uses a template engine or response transformer. The body content as seen by the logger must be final/finished version. 

### Logging using filters

Before/after filters can be configured so existing routes don't have to be modified. (but careful if you intend to log response bodies) 

    HttpLogger logger = HttpLoggerFactory.get();
    before((request, response) -> logger.logRequest(request.raw()));
    after((request, response) -> logger.logResponse(response.raw(), response.body()));    

    get("/hello", (request, response) -> {
        String body = "...";
        response.body(body);       // required to log content with after filter!!!
        response.status(200);
        return body;
    });

WARNING: Response bodies ARE NOT logged for template engine routes and other cases where response.body is never explicitly set like you see above. The logger will see all 
other details about the response (response code, headers, etc) but the body will not be recorded. There are several Spark tickets open relating to this:

* https://github.com/perwendel/spark/issues/221
* https://github.com/perwendel/spark/issues/317
* https://github.com/perwendel/spark/issues/412
