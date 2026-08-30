# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Quarkus (3.38.3) Java 21 REST backend (`com.bien:bienbcb-be`), built with Maven. Backs a cooperative/socios (members) management system: member CRUD plus JWT-based auth, on PostgreSQL via Hibernate ORM with Panache. Extensions in use: `quarkus-arc` (CDI), `quarkus-rest`/`quarkus-rest-jackson`, `quarkus-jdbc-postgresql`, `quarkus-hibernate-orm-panache`, `quarkus-hibernate-validator`, `quarkus-config-yaml`, `quarkus-smallrye-jwt`/`-jwt-build`.

On Windows, use `mvnw.cmd` (not `./mvnw`) unless running inside the Bash/Git Bash tool.

## Local database

Requires PostgreSQL before `quarkus:dev` will connect (config: `src/main/resources/application.yml`, db `bienbcb` / user+pass `bienbcb` on `localhost:5432`):

```sh
docker compose -f bd/docker-compose.yml up -d   # postgres:16 container "bienbcb-postgres"
```

Then run `bd/ddl.sql` (creates schemas `sc_socios`/`sc_seguridad`, tables `persona`/`usuario`, and a seed admin user) and `bd/dcl.sql` (grants) against the `bienbcb` database once. `hibernate-orm.database.generation` is `validate` — schema changes must be made via SQL, not left to Hibernate to auto-generate.

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

- Source root: `src/main/java/com/bien`, organized by feature package (`persona`, `usuario`), each following the same layering: `rest/` (JAX-RS `@Path` resource) → `service/` (business logic, `@Inject`ed into the resource) → `repository/` (Panache repository) → `entity/` (JPA entity) + `dto/` (request/response records). Follow this layering for new features rather than putting logic directly in the resource class.
- `persona` — member CRUD (`PersonaResource` at `/personas`, maps to `sc_socios.persona`). Endpoints are `@RolesAllowed({"ADMIN","USER"})` for reads, `@RolesAllowed("ADMIN")` for create/update.
- `usuario` — auth (`AuthResource` at `POST /auth/login`, `UsuarioResource` at `POST /usuarios` (`@RolesAllowed("ADMIN")`), maps to `sc_seguridad.usuario`). Passwords are hashed with SHA-256 + a random per-user salt (`PasswordHasher`, `usuario.salt`/`usuario.password` columns) — not bcrypt. `AuthService` issues JWTs signed with `smallrye-jwt-build`.
- Config: `src/main/resources/application.yml` (YAML, not `.properties` — `quarkus-config-yaml`). JWT signing/verification keys are `src/main/resources/{privateKey,publicKey}.pem`; token duration is `bienbcb.jwt.duration-minutes` (default 60).
- Tests: `src/test/java/com/bien`. Unit-style tests (`*Test.java`) use `@QuarkusTest` + REST Assured (`given().when().get(...).then()...`) and run in the `test` phase. Integration tests (`*IT.java`) run in the `integration-test`/`verify` phase against the packaged artifact and are only enabled under the `native` Maven profile.
