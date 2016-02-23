# resurfaceio-logger-java
&copy; 2016 Resurface Labs LLC, All Rights Reserved

## Installing with Maven

Add these sections to your pom.xml:

    <dependencies>
        <dependency>
            <groupId>io.resurface</groupId>
            <artifactId>resurfaceio-logger</artifactId>
            <version>1.0.0-SNAPSHOT</version>
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
    
    HttpLogger logger = HttpLoggerFactory.get();  // returns default cached logger
    logger.logRequest(request);                   // log http request details
    logger.logResponse(response);                 // log http response details
    if (logger.isEnabled()) ...                   // intending to send messages?
    logger.enable();                              // enable sending for dev/staging/production
    logger.disable();                             // disable sending for automated tests

## Using with Spark Framework

### Logging HTTP requests and responses

    HttpLogger logger = HttpLoggerFactory.get();
    before((req, res) -> logger.logRequest(req.raw()));
    after((req, res) -> logger.logResponse(res.raw()));    

### Logging just HTTP requests

    before((req, res) -> HttpLoggerFactory.get().logRequest(req.raw()));

### Logging just HTTP responses

    after((req, res) -> HttpLoggerFactory.get().logResponse(res.raw()));    
