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
            <version>1.3.2-SNAPSHOT</version>
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
        HttpLogger logger = HttpLoggerFactory.get();
        logger.logRequest(request.raw());
        logger.logResponse(response.raw(), response_body);
        return response_body;
    });

    post("/hello_post", (request, response) -> {
        response.status(401);
        HttpLogger logger = HttpLoggerFactory.get();
        logger.logRequest(request.raw(), request.body());
        logger.logResponse(response.raw());
        return "";
    });

NOTE: Logging from before/after filters is discouraged because of quirks in handling body content.

## Logging From Servlet Filter

This works for Tomcat, Jetty and other application servers that support standard servlet filters.

Copy resurfaceio-logger-1.3.2.jar into the appropriate /lib directory. (no other dependencies required)

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
    HttpLogger logger = HttpLoggerFactory.get();               // returns cached HTTP logger
    logger.disable();                                          // disable logging for automated tests
    logger.enable();                                           // re-enable logging after being disabled
    if (logger.isEnabled()) ...                                // branch on logging being enabled

    // log a HTTP exchange
    HttpServletRequest req = new HttpServletRequestImpl();     // define request to log
    req.setRequestURL("http://google.com");
    HttpServletResponse res = new HttpServletResponseImpl();   // define response to log
    res.setCharacterEncoding("UTF-8");
    res.setContentType("text/html");
    res.setStatus(200);
    logger.logRequest(req);                                    // log the request  (without body)
    logger.logRequest(req, body);                              // log the request  (with specified body)
    logger.logResponse(res);                                   // log the response (without body)
    logger.logResponse(res, body);                             // log the response (with specified body)
