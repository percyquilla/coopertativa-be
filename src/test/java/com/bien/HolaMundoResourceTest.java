package com.bien;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class HolaMundoResourceTest {
    @Test
    void testHolaMundoEndpoint() {
        given()
          .when().get("/hola-mundo")
          .then()
             .statusCode(200)
             .contentType("application/json")
             .body("mensaje", is("Hola Mundo"));
    }

}
