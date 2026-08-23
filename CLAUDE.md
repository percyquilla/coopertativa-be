# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Quarkus (3.38.3) Java 21 REST backend, built with Maven. This is an early-stage scaffold (`com.bien:bienbcb-be`) — currently only the default generated `ExampleResource` endpoint exists, so there is no established multi-package architecture yet. Extensions currently in use: `quarkus-arc` (CDI), `quarkus-rest` (JAX-RS/RESTEasy Reactive).

On Windows, use `mvnw.cmd` (not `./mvnw`) unless running inside the Bash/Git Bash tool.

## Common commands

- Dev mode (live reload, Dev UI at http://localhost:8080/q/dev/): `./mvnw quarkus:dev` (`mvnw.cmd quarkus:dev` on Windows shells)
- Run all tests: `./mvnw test`
- Run a single test class: `./mvnw test -Dtest=ExampleResourceTest`
- Run a single test method: `./mvnw test -Dtest=ExampleResourceTest#testHelloEndpoint`
- Package: `./mvnw package` → produces `target/quarkus-app/quarkus-run.jar` (run with `java -jar target/quarkus-app/quarkus-run.jar`)
- Über-jar package: `./mvnw package -Dquarkus.package.jar.type=uber-jar`
- Native build: `./mvnw package -Dnative` (or `-Dquarkus.native.container-build=true` without GraalVM installed)
- Integration tests (`*IT.java`, run via failsafe against packaged/native artifact): bound to the `verify` phase; only run when `-Dnative` profile is active (`skipITs` defaults to `true` otherwise)

## Architecture notes

- Source root: `src/main/java/com/bien` — REST resources go here (JAX-RS style: `@Path`, `@GET`/`@POST`, `@Produces`).
- Config: `src/main/resources/application.properties` (currently empty).
- Tests: `src/test/java/com/bien`. Unit-style tests (`*Test.java`) use `@QuarkusTest` + REST Assured (`given().when().get(...).then()...`) and run in the `test` phase. Integration tests (`*IT.java`) run in the `integration-test`/`verify` phase against the packaged artifact and are only enabled under the `native` Maven profile.
