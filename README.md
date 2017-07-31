# resurfaceio-logger-java
&copy; 2016-2017 Resurface Labs LLC

This library makes it easy to log actual usage of Java web/json apps.

## Contents

<ul>
<li><a href="#dependencies">Dependencies</a></li>
<li><a href="#installing_with_maven">Installing With Maven</a></li>
<li><a href="#logging_from_servlet_filter">Logging From Servlet Filter</a></li>
<li><a href="#logging_from_spring_boot">Logging From Spring Boot</a></li>
<li><a href="#logging_from_spark_framework">Logging From Spark Framework</a></li>
<li><a href="#advanced_topics">Advanced Topics</a><ul>
<li><a href="#setting_default_url">Setting Default URL</a></li>
<li><a href="#disabling_all_logging">Disabling All Logging</a></li>
<li><a href="#using_api_directly">Using API Directly</a></li>
</ul></li>
</ul>

<a name="dependencies"/>

## Dependencies

Requires Java 8. No other dependencies to conflict with your app.

<a name="installing_with_maven"/>

## Installing with Maven

Add this section to `pom.xml`:

    <dependencies>
        <dependency>
            <groupId>io.resurface</groupId>
            <artifactId>resurfaceio-logger</artifactId>
            <version>RELEASE</version>
        </dependency>
    </dependencies>

<a name="logging_from_servlet_filter"/>

## Logging From Servlet Filter

After <a href="#installing_with_maven">installing the library</a>, add a logging filter to `web.xml`.

    <filter>
        <filter-name>HttpLoggerForServlets</filter-name>
        <filter-class>io.resurface.HttpLoggerForServlets</filter-class>
        <init-param>
            <param-name>url</param-name>
            <param-value>https://...</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>HttpLoggerForServlets</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>

<a name="logging_from_spring_boot"/>

## Logging From Spring Boot

After <a href="#installing_with_maven">installing the library</a>, use a `FilterRegistrationBean`
to add a logging servlet filter.

    @Bean
    public FilterRegistrationBean httpLoggerFilter() {
        FilterRegistrationBean frb = new FilterRegistrationBean();
        frb.setFilter(new io.resurface.HttpLoggerForServlets());
        frb.addInitParameter("url", "https://...");
        frb.addUrlPatterns("/*");
        frb.setName("HttpLoggerForServlets");
        frb.setOrder(1024);  // usually best with high number
        return frb;
    }

<a name="logging_from_spark_framework"/>

## Logging From Spark Framework

After <a href="#installing_with_maven">installing the library</a>, create a logger and use it from the routes of interest.

    import io.resurface.HttpLogger;

    HttpLogger logger = new HttpLogger("https://...");

    get("/hello", (request, response) -> {
        String response_body = "Hello World";
        logger.log(request.raw(), null, response.raw(), response_body);
        return response_body;
    });

    post("/hello_post", (request, response) -> {
        response.status(401);
        logger.log(request.raw(), request.body(), response.raw(), null);
        return "";
    });

Alternatively configure an `after` filter to log across multiple routes at once.

    after((request, response) -> {
        if (response.body() != null) {  // log successful responses only, not 404/500s
            logger.log(request.raw(), request.body(), response.raw(), response.body());
        }
    });

<a name="advanced_topics"/>

## Advanced Topics

<a name="setting_default_url"/>

### Setting Default URL

Set the `USAGE_LOGGERS_URL` variable to provide a default value whenever the URL is not specified.

    // using Heroku cli
    heroku config:set USAGE_LOGGERS_URL=https://my-https-url

    // when starting Java container
    java -DUSAGE_LOGGERS_URL="https://my-https-url" your_application

Loggers look for this environment variable when no other options are set, as in these examples.

    // for basic logger
    HttpLogger logger = new HttpLogger();

    // for servlet filter
    <filter>
        <filter-name>HttpLoggerForServlets</filter-name>
        <filter-class>io.resurface.HttpLoggerForServlets</filter-class>
        <!-- intentionally leaving url param unspecified -->
    </filter>

<a name="disabling_all_logging"/>

### Disabling All Logging

It's important to have a "kill switch" to universally disable all logging. For example, loggers might be disabled when
running automated tests. All loggers can also be disabled at runtime, either by setting an environment variable or
programmatically.

    // for Heroku app
    heroku config:set USAGE_LOGGERS_DISABLE=true

    // when starting Java container
    java -DUSAGE_LOGGERS_DISABLE="true"

    // at runtime
    UsageLoggers.disable();

<a name="using_api_directly"/>

### Using API Directly

Loggers can be directly integrated into your application if other options don't fit. This requires the most effort, but
yields complete control over how usage logging is implemented.

    import io.resurface.*;

    // manage all loggers (even those not created yet)
    UsageLoggers.disable();                                          // disable all loggers
    UsageLoggers.enable();                                           // enable all loggers

    // create and configure logger
    HttpLogger logger;
    logger = new HttpLogger(my_https_url);                           // log to remote url
    logger = new HttpLogger(my_https_url, false);                    // (initially disabled)
    logger = new HttpLogger(queue);                                  // log to appendable queue
    logger = new HttpLogger(queue, false);                           // (initially disabled)
    logger.disable();                                                // disable logging for tests
    logger.enable();                                                 // enable logging again
    if (logger.isEnabled()) ...                                      // test if logging is enabled

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
