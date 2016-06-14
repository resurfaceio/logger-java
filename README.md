# resurfaceio-logger-java
&copy; 2016 Resurface Labs LLC, All Rights Reserved

This library makes it easy to log server usage including HTTP request/response details.

## Dependencies

Requires Java 8. No other dependencies to conflict with your app.

## Installing with Maven

Add these sections to your pom.xml:

    <dependencies>
        <dependency>
            <groupId>io.resurface</groupId>
            <artifactId>resurfaceio-logger</artifactId>
            <version>1.4.0-SNAPSHOT</version>
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

## Logging From Spark Framework

Spark Framework is a popular micro-framework, nicely introduced by the
[Getting Started on Heroku with Java](https://devcenter.heroku.com/articles/getting-started-with-java) tutorial.

A logger can be used within simple Spark handlers like this:

    import io.resurface.HttpLogger;
    import io.resurface.HttpLoggerFactory;

    get("/hello", (request, response) -> {
        String response_body = "Hello World";
        HttpLoggerFactory.get().log(request.raw(), null, response.raw(), response_body);
        return response_body;
    });

    post("/hello_post", (request, response) -> {
        response.status(401);
        HttpLoggerFactory.get().log(request.raw(), request.body(), response.raw(), null);
        return "";
    });

NOTE: Logging from before/after filters is discouraged because of quirks in handling body content.

## Logging From Servlet Filter

This works for Tomcat, Jetty and other application servers that support standard servlet filters.

Copy resurfaceio-logger-1.4.0.jar into the appropriate /lib directory. (no other dependencies required)

Now configure the filter in web.xml as shown below. You can optionally use a specific url-pattern to limit logging
to specific areas of your application.

    <filter>
        <filter-name>HttpLoggerForServlets</filter-name>
        <filter-class>io.resurface.HttpLoggerForServlets</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>HttpLoggerForServlets</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
    
HttpLoggerForServlets performs some basic filtering: it ignores redirects (304 response codes), and only logs
responses for content types matching a predefined list (including 'text/html' and 'application/json').

## Using API Directly

    import io.resurface.*;

    // manage default logger
    HttpLogger logger = HttpLoggerFactory.get();                 // returns cached HTTP logger
    logger.disable();                                            // disable logging for tests
    logger.enable();                                             // enable logging again
    if (logger.isEnabled()) ...                                  // test if logging is enabled

    // define request to log
    HttpServletRequest request = new HttpServletRequestImpl();
    request.setCharacterEncoding("UTF-8");
    request.setContentType("application/json");
    request.setHeader("A", "123");
    request.setMethod("GET");
    request.setRequestURL("http://google.com");

    // define response to log
    HttpServletResponse response = new HttpServletResponseImpl();
    response.setCharacterEncoding("UTF-8");
    response.setContentType("text/html");
    response.setHeader("B", "234");
    response.setStatus(200);

    // log objects defined above
    logger.log(request, null, response, null);

    // log with specified request/response bodies
    logger.log(request, "my-request, response, "my-response");

    // submit a custom message (destination may accept or not)
    logger.submit("...");
