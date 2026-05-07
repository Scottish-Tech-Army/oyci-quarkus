package org.scottishtecharmy.oyci.quarkus.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.scottishtecharmy.oyci.quarkus.TestTokens;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TagResourceTest {

    private static Long createdTagId;

    @Test
    @Order(1)
    void testCreateTagUnauthenticated() {
        given()
            .contentType("application/json")
            .body("""
                {"name": "First Aid"}
                """)
            .when().post("/api/tags")
            .then()
            .statusCode(401);
    }

    @Test
    @Order(2)
    void testCreateTagAsStaff() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.staffToken(1L))
            .body("""
                {"name": "First Aid"}
                """)
            .when().post("/api/tags")
            .then()
            .statusCode(403);
    }

    @Test
    @Order(3)
    void testCreateTagAsAdmin() {
        createdTagId = given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body("""
                {"name": "First Aid"}
                """)
            .when().post("/api/tags")
            .then()
            .statusCode(201)
            .body("name", is("First Aid"))
            .body("id", notNullValue())
            .extract().jsonPath().getLong("id");
    }

    @Test
    @Order(4)
    void testListTagsAsStaff() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.staffToken(1L))
            .when().get("/api/tags")
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(5)
    void testUpdateTag() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body("""
                {"name": "Advanced First Aid"}
                """)
            .when().put("/api/tags/" + createdTagId)
            .then()
            .statusCode(200)
            .body("name", is("Advanced First Aid"));
    }

    @Test
    @Order(6)
    void testDeleteTag() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .when().delete("/api/tags/" + createdTagId)
            .then()
            .statusCode(204);
    }
}
