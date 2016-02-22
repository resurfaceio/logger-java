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

## Using with Spark Framework
    
    HttpLogger logger = new HttpLogger();
    before((req, res) -> logger.logRequest(req.raw()));
    after((req, res) -> logger.logResponse(res.raw()));    
