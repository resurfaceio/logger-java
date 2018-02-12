# resurfaceio-logger-java
&copy; 2016-2018 Resurface Labs LLC

## Logger API

<ul>
<li><a href="#creating_loggers">Creating Loggers</a></li>
<li><a href="#logging_http">Logging HTTP Calls</a></li>
<li><a href="#setting_default_rules">Setting Default Rules</a></li>
<li><a href="#setting_default_url">Setting Default URL</a></li>
<li><a href="#enabling_and_disabling_loggers">Enabling and Disabling Loggers</a></li>
</ul>

<a name="creating_loggers"/>

## Creating Loggers

To get started, first you'll need to create a `HttpLogger` instance. Here there are options to specify a URL (for where JSON 
messages will be sent) and/or a specific set of <a href="https://resurface.io/rules.html">logging rules</a> (for what privacy 
protections to apply). Default values will be used for either of these if specific values are not provided.

```java
import io.resurface.*;

// with default url and rules
HttpLogger logger = new HttpLogger();

// with specific url and default rules
logger = new HttpLogger("https://...");

// with specific url and rules
logger = new HttpLogger("https://...", "include standard");
```

<a name="logging_http"/>

## Logging HTTP Calls

Now that you have a logger instance, let's do some logging. Here you can pass standard request/response objects, as well
as response body and request body content when these are available. 

```java
// with standard objects
logger.log(request, response);

// with response body
logger.log(request, response, "my-response-body");

// with response and request body
logger.log(request, response, "my-response-body", "my-request-body");
```

If standard request and response objects aren't available in your case, create mock implementations to pass instead.

```java
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
logger.log(request, response);
```

<a name="setting_default_rules"/>

## Setting Default Rules

If no rules are provided when creating a logger, the default value of `include standard` will be applied. A different default
value can be specified as shown below.

```java
HttpLogger.setDefaultRules("include weblog");
```

When specifying multiple default rules, put each on a separate line.

```java
HttpLogger.setDefaultRules(
    "include weblog\n" +
    "sample 10\n"
);
```

<a name="setting_default_url"/>

## Setting Default URL

If your application creates more than one logger, or requires different URLs for different environments (development vs
testing vs production), then set the `USAGE_LOGGERS_URL` environment variable. This value will be applied if no other URL
is specified when creating a logger.

```java
// from command line
export USAGE_LOGGERS_URL="https://..."

// when launching Java app
java -DUSAGE_LOGGERS_URL="https://..." ...

// for Heroku app
heroku config:set USAGE_LOGGERS_URL=https://...
```

<a name="enabling_and_disabling_loggers"/>

## Enabling and Disabling Loggers

Individual loggers can be controlled through their `enable` and `disable` methods. When disabled, loggers will
not send any logging data, and the result returned by the `log` method will always be true (success).

All loggers for an application can be enabled or disabled at once with the `UsageLoggers` class. This even controls
loggers that have not yet been created by the application.

```java
UsageLoggers.disable();    // disable all loggers
UsageLoggers.enable();     // enable all loggers
```

All loggers can be permanently disabled with the `USAGE_LOGGERS_DISABLE` environment variable. When set to true,
loggers will never become enabled, even if `UsageLoggers.enable()` is called by the application. This is primarily 
done by automated tests to disable all logging even if other control logic exists. 

```java
// from command line
export USAGE_LOGGERS_DISABLE="true"

// when launching Java app
java -DUSAGE_LOGGERS_DISABLE="true" ...

// for Heroku app
heroku config:set USAGE_LOGGERS_DISABLE=true
```