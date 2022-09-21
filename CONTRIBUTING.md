# Contributing

## Coding Conventions

Our code style is whatever IntelliJ IDEA does by default, with the exception of allowing lines up to 130 characters.
If you don't use IDEA, that's ok, but your code may get reformatted.

## Git Workflow 

Initial setup: 

```
git clone git@github.com:resurfaceio/logger-java.git resurfaceio-logger-java
cd resurfaceio-logger-java
```

Running unit tests:

```
mvn test
```

Committing changes:

```
git add -A
git commit -m "#123 Updated readme"       (123 is the GitHub issue number)
git pull --rebase                         (avoid merge bubbles)
git push origin master
```

Check if any newer dependencies are available:

```
mvn versions:display-dependency-updates
```

## Release Process

Configure environment variables (in `.bash_profile`):

```
export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk1.8.0_131.jdk/Contents/Home"
export GPG_TTY=$(tty)
```

Configure Maven settings (in `.m2/settings.xml`):

```
<settings>
  <profiles>
    <profile>
      <id>ossrh</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <properties>
        <gpg.keyname>[public-key-id]</gpg.keyname>
      </properties>
    </profile>
  </profiles>
  <servers>
    <server>
      <id>ossrh</id>
      <username>resurfaceio</username>
      <password>[ask-rob]</password>
    </server>
  </servers>
</settings>
```

Push artifacts to [Maven Central](https://search.maven.org/):

```
git add -A
git commit -m "Update version to 2.2.#"
mvn deploy
```

Log into `oss.sonatype.org` and close/release the latest staging repository.

Tag release version:

```
git tag v2.2.#
git push origin master --tags
```

Start the next version by incrementing the version number in `pom.xml` and `Baselogger.java`.
