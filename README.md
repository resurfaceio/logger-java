# resurfaceio-logger-java
Easily log API requests and responses to your own <a href="https://resurface.io">system of record</a>.

[![Maven Central](https://img.shields.io/maven-central/v/io.resurface/resurfaceio-logger?style=for-the-badge)](https://maven-badges.herokuapp.com/maven-central/io.resurface/resurfaceio-logger)
[![CodeFactor](https://www.codefactor.io/repository/github/resurfaceio/logger-java/badge?style=for-the-badge)](https://www.codefactor.io/repository/github/resurfaceio/logger-java)
[![License](https://img.shields.io/github/license/resurfaceio/logger-java?style=for-the-badge)](https://github.com/resurfaceio/logger-java/blob/master/LICENSE)
[![Contributing](https://img.shields.io/badge/contributions-welcome-green.svg?style=for-the-badge)](https://github.com/resurfaceio/logger-java/blob/master/CONTRIBUTING.md)

## Contents

<ul>
<li><a href="#dependencies">Dependencies</a></li>
<li><a href="#installing_with_maven">Installing With Maven</a></li>
<li><a href="#logging_from_servlet_filter">Logging From Servlet Filter</a></li>
<li><a href="#logging_from_spring_boot">Logging From Spring Boot</a></li>
<li><a href="#logging_from_spark_framework">Logging From Spark Framework</a></li>
<li><a href="#logging_from_jersey">Logging From Jersey</a></li>
<li><a href="#logging_with_api">Logging With API</a></li>
<li><a href="#privacy">Protecting User Privacy</a></li>
</ul>

<a name="dependencies"/>

## Dependencies

Requires Java 8+ and Java servlet classes, which are not included. No other dependencies to conflict with your app.

<a name="installing_with_maven"/>

## Installing with Maven

Add this section to `pom.xml`:

```xml
<dependency>
    <groupId>io.resurface</groupId>
    <artifactId>resurfaceio-logger</artifactId>
    <version>RELEASE</version>
</dependency>
```

Add this section too if servlet classes are not already available.

```xml
<dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>RELEASE</version>
</dependency>
```

<a name="logging_from_servlet_filter"/>

## Logging From Servlet Filter

After <a href="#installing_with_maven">installing the library</a>, add a logging filter to `web.xml`.

```xml
<filter>
    <filter-name>HttpLoggerForServlets</filter-name>
    <filter-class>io.resurface.HttpLoggerForServlets</filter-class>
    <init-param>
        <param-name>url</param-name>
        <param-value>http://localhost:4001/message</param-value>
    </init-param>
    <init-param>
        <param-name>rules</param-name>
        <param-value>include debug</param-value>
    </init-param>
</filter>
<filter-mapping>
    <filter-name>HttpLoggerForServlets</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>
```

Add a CDATA section when specifying multiple rules at once like this:

```xml
    <init-param>
        <param-name>rules</param-name>
        <param-value><![CDATA[
            include debug
            sample 10
        ]]></param-value>
    </init-param>
```

<a name="logging_from_spring_boot"/>

## Logging From Spring Boot

After <a href="#installing_with_maven">installing the library</a>, configure a `FilterRegistrationBean`
to add a logging servlet filter.

```java
@Bean
public FilterRegistrationBean httpLoggerFilter() {
    FilterRegistrationBean frb = new FilterRegistrationBean();
    frb.setFilter(new io.resurface.HttpLoggerForServlets());
    frb.setName("HttpLoggerForServlets");
    frb.addUrlPatterns("/*");
    frb.addInitParameter("url", "http://localhost:4001/message");
    frb.addInitParameter("rules", "include debug");
    return frb;
}
```

<a name="logging_from_spark_framework"/>

## Logging From Spark Framework

After <a href="#installing_with_maven">installing the library</a>, create a logger and call it from the routes of interest.

```java
import io.resurface.*;

HttpLogger logger = new HttpLogger("http://localhost:4001/message", "include debug");

get("/hello", (request, response) -> {
    String response_body = "Hello World";
    HttpMessage.send(logger, request.raw(), response.raw(), response_body);
    return response_body;
});

post("/hello_post", (request, response) -> {
    String response_body = "POSTED: " + request.body();
    HttpMessage.send(logger, request.raw(), response.raw(), response_body, request.body());
    return response_body;
});
```

Alternatively configure an `after` filter to log across multiple routes at once.

```java
after((request, response) -> {
    if (response.body() != null) {  // log successful responses only, not 404/500s
        HttpMessage.send(logger, request.raw(), response.raw(), response.body(), request.body());
    }
});
```

<a name="logging_from_jersey"/>

## Logging From Jersey

After <a href="#installing_with_maven">installing the library</a>, register a logger as a Jersey filter/interceptor.
Note this will only log usage when a response body is returned.

```java
ResourceConfig resourceConfig = new ResourceConfig(...);
resourceConfig.register(new io.resurface.HttpLoggerForJersey("http://localhost:4001/message", "include debug"));
HttpServer server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, resourceConfig, false);
```

<a name="logging_with_api"/>

## Logging With API

Loggers can be directly integrated into your application using our [API](API.md). This requires the most effort compared with
the options described above, but also offers the greatest flexibility and control.

[API documentation](API.md)

<a name="privacy"/>

## Protecting User Privacy

Loggers always have an active set of <a href="https://resurface.io/rules.html">rules</a> that control what data is logged
and how sensitive data is masked. All of the examples above apply a predefined set of rules (`include debug`),
but logging rules are easily customized to meet the needs of any application.

<a href="https://resurface.io/rules.html">Logging rules documentation</a>

---
<small>&copy; 2016-2021 <a href="https://resurface.io">Resurface Labs Inc.</a></small>
