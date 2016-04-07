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

## Logging From Servlet Filter

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
    
HttpLoggerForServlets performs some basic filtering: it ignores redirects (304 response codes), and only logs responses for content types matching a predefined list
(including 'text/html' and 'application/json').

## Logging From Spark

A logger can be used selectively for simple cases like this:

    get("/hello", (req, res) -> {
        HttpLogger logger = HttpLoggerFactory.get();
        logger.logRequest(req.raw());
        String body = "...";
        logger.logResponse(res.raw(), body);
        return body;
    });

NOTE: Logging from before/after filters is discouraged because of quirks in handling body content.

## Using API Directly

    import io.resurface.HttpLogger;
    import io.resurface.HttpLoggerFactory;
    
    HttpLogger logger = HttpLoggerFactory.get();     // returns default cached HTTP logger
    logger.logRequest(request);                      // log HTTP request details
    logger.logResponse(response, body);              // log HTTP response details, body is optional
    if (logger.isEnabled()) ...                      // intending to log stuff?
    logger.enable();                                 // enable logging for dev/staging/production
    logger.disable();                                // disable logging for automated tests
