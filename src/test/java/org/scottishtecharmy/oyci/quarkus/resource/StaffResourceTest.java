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
import static org.hamcrest.Matchers.hasSize;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StaffResourceTest {

    private static Long createdStaffId;

    @Test
    @Order(1)
    void testListStaffUnauthenticated() {
        given()
            .contentType("application/json")
            .when().get("/api/staff")
            .then()
            .statusCode(401);
    }

    @Test
    @Order(2)
    void testCreateStaffAsAdmin() {
        createdStaffId = given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body("""
                {
                    "name": "Jane Doe",
                    "email": "jane.doe@example.com",
                    "passwordHash": "password123",
                    "maxHoursPerWeek": 30
                }
                """)
            .when().post("/api/staff")
            .then()
            .statusCode(201)
            .body("name", is("Jane Doe"))
            .body("email", is("jane.doe@example.com"))
            .body("maxHoursPerWeek", is(30))
            .extract().jsonPath().getLong("id");
    }

    @Test
    @Order(3)
    void testCreateStaffDuplicateEmail() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body("""
                {
                    "name": "Jane Duplicate",
                    "email": "jane.doe@example.com",
                    "passwordHash": "password123"
                }
                """)
            .when().post("/api/staff")
            .then()
            .statusCode(400);
    }

    @Test
    @Order(4)
    void testGetStaffById() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .when().get("/api/staff/" + createdStaffId)
            .then()
            .statusCode(200)
            .body("name", is("Jane Doe"));
    }

    @Test
    @Order(5)
    void testListAllStaff() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .when().get("/api/staff")
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(6)
    void testUpdateStaff() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body("""
                {
                    "name": "Jane Updated",
                    "email": "jane.updated@example.com",
                    "maxHoursPerWeek": 25
                }
                """)
            .when().put("/api/staff/" + createdStaffId)
            .then()
            .statusCode(200)
            .body("name", is("Jane Updated"))
            .body("maxHoursPerWeek", is(25));
    }

    @Test
    @Order(7)
    void testUpdateAvailability() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.staffToken(createdStaffId))
            .body("""
                [
                    {"dayOfWeek": 1, "startTime": "09:00", "endTime": "17:00"},
                    {"dayOfWeek": 3, "startTime": "10:00", "endTime": "16:00"}
                ]
                """)
            .when().put("/api/staff/" + createdStaffId + "/availability")
            .then()
            .statusCode(204);
    }

    @Test
    @Order(8)
    void testUpdateMaxHours() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.staffToken(createdStaffId))
            .body("""
                {"maxHoursPerWeek": 20}
                """)
            .when().put("/api/staff/" + createdStaffId + "/max-hours")
            .then()
            .statusCode(204);
    }

    @Test
    @Order(9)
    void testUpdateStaffTags() {
        // Create a tag first
        Long tagId = given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body("""
                {"name": "DBS Checked"}
                """)
            .when().post("/api/tags")
            .then()
            .statusCode(201)
            .extract().jsonPath().getLong("id");

        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.staffToken(createdStaffId))
            .body("[" + tagId + "]")
            .when().put("/api/staff/" + createdStaffId + "/tags")
            .then()
            .statusCode(204);
    }

    @Test
    @Order(10)
    void testAddHoliday() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.staffToken(createdStaffId))
            .body("""
                {
                    "startDate": "2026-07-01",
                    "endDate": "2026-07-14"
                }
                """)
            .when().post("/api/staff/" + createdStaffId + "/holidays")
            .then()
            .statusCode(201)
            .body("startDate", is("2026-07-01"))
            .body("endDate", is("2026-07-14"));
    }

    @Test
    @Order(11)
    void testGetHolidays() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.staffToken(createdStaffId))
            .when().get("/api/staff/" + createdStaffId + "/holidays")
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(12)
    void testGetSchedule() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.staffToken(createdStaffId))
            .when().get("/api/staff/" + createdStaffId + "/schedule")
            .then()
            .statusCode(200);
    }

    @Test
    @Order(13)
    void testGetNonExistentStaff() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .when().get("/api/staff/99999")
            .then()
            .statusCode(404);
    }

    @Test
    @Order(14)
    void testDeleteStaff() {
        given()
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .when().delete("/api/staff/" + createdStaffId)
            .then()
            .statusCode(204);
    }

    @Test
    @Order(15)
    void testCreateStaffAsStaffForbidden() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.staffToken(1L))
            .body("""
                {
                    "name": "Unauthorized Staff",
                    "email": "unauth@example.com",
                    "passwordHash": "password123"
                }
                """)
            .when().post("/api/staff")
            .then()
            .statusCode(403);
    }
}
