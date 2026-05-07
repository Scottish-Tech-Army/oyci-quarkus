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
class LocationResourceTest {

    private static Long createdLocationId;

    @Test
    @Order(1)
    void testCreateLocationUnauthenticated() {
        given()
            .contentType("application/json")
            .body("""
                {
                    "name": "Community Hall",
                    "addressLine1": "123 Main St",
                    "city": "Edinburgh",
                    "zipCode": "EH1 1AA",
                    "defaultCapacity": 50
                }
                """)
            .when().post("/api/locations")
            .then()
            .statusCode(401);
    }

    @Test
    @Order(2)
    void testCreateLocationAsAdmin() {
        createdLocationId = given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body("""
                {
                    "name": "Community Hall",
                    "addressLine1": "123 Main St",
                    "city": "Edinburgh",
                    "zipCode": "EH1 1AA",
                    "contactName": "John Smith",
                    "contactPhone": "01onal234567",
                    "contactEmail": "john@hall.org",
                    "defaultCapacity": 50
                }
                """)
            .when().post("/api/locations")
            .then()
            .statusCode(201)
            .body("name", is("Community Hall"))
            .body("city", is("Edinburgh"))
            .body("defaultCapacity", is(50))
            .extract().jsonPath().getLong("id");
    }

    @Test
    @Order(3)
    void testGetLocation() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.staffToken(1L))
            .when().get("/api/locations/" + createdLocationId)
            .then()
            .statusCode(200)
            .body("name", is("Community Hall"));
    }

    @Test
    @Order(4)
    void testListLocations() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.staffToken(1L))
            .when().get("/api/locations")
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(5)
    void testUpdateLocation() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body("""
                {
                    "name": "Updated Hall",
                    "addressLine1": "456 New St",
                    "city": "Glasgow",
                    "zipCode": "G1 1AA",
                    "defaultCapacity": 100
                }
                """)
            .when().put("/api/locations/" + createdLocationId)
            .then()
            .statusCode(200)
            .body("name", is("Updated Hall"))
            .body("city", is("Glasgow"));
    }

    @Test
    @Order(6)
    void testDeleteLocation() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .when().delete("/api/locations/" + createdLocationId)
            .then()
            .statusCode(204);
    }

    @Test
    @Order(7)
    void testGetNonExistentLocation() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .when().get("/api/locations/99999")
            .then()
            .statusCode(404);
    }
}
