# Contributing to resurfaceio-logger-java
&copy; 2016-2017 Resurface Labs LLC

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

Publish snapshot build:

```
mvn install
(copy jar from .m2 to resurfaceio-mavenrepo)
(commit changes to resurfaceio-mavenrepo)
```

Publish final build:

Create `$HOME/.m2/settings.xml` file if not present, like this:

```
<settings>
  <servers>
    <server>
      <id>ossrh</id>
      <username>ask-rob</username>
      <password>ask-rob</password>
    </server>
  </servers>
</settings>
```

Remove `-SNAPSHOT` from the end of the version.

```
git add -A
git commit -m "Change version to 1.x.x release"
git tag v1.x.x
git push origin master --tags
```

Finish by incrementing the version number, and adding `-SNAPSHOT` to the end of the version.
