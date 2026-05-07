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
class EventTypeResourceTest {

    private static Long createdEventTypeId;
    private static Long tagId;

    @Test
    @Order(1)
    void testCreateEventTypeUnauthenticated() {
        given()
            .contentType("application/json")
            .body("""
                {
                    "name": "Coding Workshop",
                    "description": "Intro to Python",
                    "durationMinutes": 120
                }
                """)
            .when().post("/api/event-types")
            .then()
            .statusCode(401);
    }

    @Test
    @Order(2)
    void testCreateEventTypeAsStaff() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.staffToken(1L))
            .body("""
                {
                    "name": "Coding Workshop",
                    "description": "Intro to Python",
                    "durationMinutes": 120
                }
                """)
            .when().post("/api/event-types")
            .then()
            .statusCode(403);
    }

    @Test
    @Order(3)
    void testCreateEventTypeAsAdmin() {
        createdEventTypeId = given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body("""
                {
                    "name": "Coding Workshop",
                    "description": "Intro to Python",
                    "durationMinutes": 120
                }
                """)
            .when().post("/api/event-types")
            .then()
            .statusCode(201)
            .body("name", is("Coding Workshop"))
            .body("durationMinutes", is(120))
            .extract().jsonPath().getLong("id");
    }

    @Test
    @Order(4)
    void testGetEventType() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.staffToken(1L))
            .when().get("/api/event-types/" + createdEventTypeId)
            .then()
            .statusCode(200)
            .body("name", is("Coding Workshop"));
    }

    @Test
    @Order(5)
    void testListEventTypes() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.staffToken(1L))
            .when().get("/api/event-types")
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(6)
    void testUpdateEventType() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body("""
                {
                    "name": "Advanced Coding Workshop",
                    "description": "Python and Java",
                    "durationMinutes": 180
                }
                """)
            .when().put("/api/event-types/" + createdEventTypeId)
            .then()
            .statusCode(200)
            .body("name", is("Advanced Coding Workshop"))
            .body("durationMinutes", is(180));
    }

    @Test
    @Order(7)
    void testUpdateEventTypeTags() {
        // First create a tag
        tagId = given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body("""
                {"name": "Python"}
                """)
            .when().post("/api/tags")
            .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        // Assign tag to event type
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body("[" + tagId + "]")
            .when().put("/api/event-types/" + createdEventTypeId + "/tags")
            .then()
            .statusCode(200)
            .body("requiredTags.size()", is(1));
    }

    @Test
    @Order(8)
    void testDeleteEventType() {
        // Clean up tag first
        given()
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .when().delete("/api/tags/" + tagId)
            .then()
            .statusCode(204);

        given()
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .when().delete("/api/event-types/" + createdEventTypeId)
            .then()
            .statusCode(204);
    }

    @Test
    @Order(9)
    void testGetNonExistentEventType() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .when().get("/api/event-types/99999")
            .then()
            .statusCode(404);
    }
}
