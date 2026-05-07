package org.scottishtecharmy.oyci.quarkus.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.scottishtecharmy.oyci.quarkus.TestTokens;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EventInstanceResourceTest {

    // IDs shared across tests in this class
    private static Long locationId;
    private static Long eventTypeId;
    private static Long staffId;
    private static Long instanceId;
    private static Long assignmentStaffId;

    // ---- Setup helpers ----

    private Long createLocation() {
        return given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body("""
                {
                    "name": "Test Venue",
                    "city": "Edinburgh",
                    "defaultCapacity": 10
                }
                """)
            .when().post("/api/locations")
            .then().statusCode(201)
            .extract().jsonPath().getLong("id");
    }

    private Long createEventType() {
        return given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body("""
                {
                    "name": "Volunteering Day",
                    "durationMinutes": 240
                }
                """)
            .when().post("/api/event-types")
            .then().statusCode(201)
            .extract().jsonPath().getLong("id");
    }

    private Long createStaff(String email) {
        return given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body(String.format("""
                {
                    "name": "Staff Member",
                    "email": "%s",
                    "passwordHash": "password123",
                    "maxHoursPerWeek": 40
                }
                """, email))
            .when().post("/api/staff")
            .then().statusCode(201)
            .extract().jsonPath().getLong("id");
    }

    // ---- Tests ----

    @Test
    @Order(1)
    void testListEventInstancesUnauthenticated() {
        given()
            .when().get("/api/event-instances")
            .then()
            .statusCode(401);
    }

    @Test
    @Order(2)
    void testCreateEventInstanceAsStaffForbidden() {
        locationId = createLocation();
        eventTypeId = createEventType();

        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.staffToken(1L))
            .body(String.format("""
                {
                    "eventType": {"id": %d},
                    "location":  {"id": %d},
                    "eventDate": "2026-06-15",
                    "startTime": "10:00:00"
                }
                """, eventTypeId, locationId))
            .when().post("/api/event-instances")
            .then()
            .statusCode(403);
    }

    @Test
    @Order(3)
    void testCreateEventInstance() {
        instanceId = given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body(String.format("""
                {
                    "eventType": {"id": %d},
                    "location":  {"id": %d},
                    "eventDate": "2026-06-15",
                    "startTime": "10:00:00"
                }
                """, eventTypeId, locationId))
            .when().post("/api/event-instances")
            .then()
            .statusCode(201)
            .body("status", is("DRAFT"))
            .body("eventDate", is("2026-06-15"))
            .extract().jsonPath().getLong("id");
    }

    @Test
    @Order(4)
    void testGetEventInstance() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.staffToken(1L))
            .when().get("/api/event-instances/" + instanceId)
            .then()
            .statusCode(200)
            .body("id", is(instanceId.intValue()))
            .body("status", is("DRAFT"));
    }

    @Test
    @Order(5)
    void testListEventInstances() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.staffToken(1L))
            .when().get("/api/event-instances")
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(6)
    void testListEventInstancesWithFilters() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.staffToken(1L))
            .queryParam("from", "2026-06-01")
            .queryParam("to", "2026-06-30")
            .queryParam("status", "DRAFT")
            .when().get("/api/event-instances")
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(7)
    void testUpdateEventInstance() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body(String.format("""
                {
                    "eventType": {"id": %d},
                    "location":  {"id": %d},
                    "eventDate": "2026-06-20",
                    "startTime": "14:00:00",
                    "capacityOverride": 5
                }
                """, eventTypeId, locationId))
            .when().put("/api/event-instances/" + instanceId)
            .then()
            .statusCode(200)
            .body("eventDate", is("2026-06-20"))
            .body("capacityOverride", is(5));
    }

    @Test
    @Order(8)
    void testGetAvailableStaff() {
        assignmentStaffId = createStaff("assignable.staff@example.com");

        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .when().get("/api/event-instances/" + instanceId + "/available-staff")
            .then()
            .statusCode(200);
    }

    @Test
    @Order(9)
    void testAssignStaff() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body(String.format("""
                {"staffId": %d}
                """, assignmentStaffId))
            .when().post("/api/event-instances/" + instanceId + "/assign")
            .then()
            .statusCode(201);
    }

    @Test
    @Order(10)
    void testAssignSameStaffTwice() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body(String.format("""
                {"staffId": %d}
                """, assignmentStaffId))
            .when().post("/api/event-instances/" + instanceId + "/assign")
            .then()
            .statusCode(400);
    }

    @Test
    @Order(11)
    void testPublishEventWithNoStaffFails() {
        // Create a second event with no staff to test the guard
        Long emptyInstanceId = given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body(String.format("""
                {
                    "eventType": {"id": %d},
                    "location":  {"id": %d},
                    "eventDate": "2026-07-01",
                    "startTime": "09:00:00"
                }
                """, eventTypeId, locationId))
            .when().post("/api/event-instances")
            .then().statusCode(201)
            .extract().jsonPath().getLong("id");

        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .when().post("/api/event-instances/" + emptyInstanceId + "/publish")
            .then()
            .statusCode(400);
    }

    @Test
    @Order(12)
    void testPublishEvent() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .when().post("/api/event-instances/" + instanceId + "/publish")
            .then()
            .statusCode(200)
            .body("status", is("PUBLISHED"));
    }

    @Test
    @Order(13)
    void testPublishAlreadyPublishedEventFails() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .when().post("/api/event-instances/" + instanceId + "/publish")
            .then()
            .statusCode(400);
    }

    @Test
    @Order(14)
    void testUnassignStaff() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .when().delete("/api/event-instances/" + instanceId + "/assign/" + assignmentStaffId)
            .then()
            .statusCode(204);
    }

    @Test
    @Order(15)
    void testDeleteEventInstance() {
        given()
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .when().delete("/api/event-instances/" + instanceId)
            .then()
            .statusCode(204);
    }

    @Test
    @Order(16)
    void testGetNonExistentEventInstance() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .when().get("/api/event-instances/99999")
            .then()
            .statusCode(404);
    }
}
