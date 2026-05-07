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
class ParticipantResourceTest {

    private static Long locationId;
    private static Long eventTypeId;
    private static Long staffId;
    private static Long publishedInstanceId;
    private static Long participantId;

    private Long createLocation() {
        return given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body("""
                {
                    "name": "Participant Test Venue",
                    "city": "Edinburgh",
                    "defaultCapacity": 20
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
                    "name": "Community Event",
                    "durationMinutes": 60
                }
                """)
            .when().post("/api/event-types")
            .then().statusCode(201)
            .extract().jsonPath().getLong("id");
    }

    private Long createStaff() {
        return given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body("""
                {
                    "name": "Participant Test Staff",
                    "email": "participant.test.staff@example.com",
                    "passwordHash": "password123",
                    "maxHoursPerWeek": 40
                }
                """)
            .when().post("/api/staff")
            .then().statusCode(201)
            .extract().jsonPath().getLong("id");
    }

    private Long registerParticipant() {
        // Register a new participant
        given()
            .contentType("application/json")
            .body("""
                {
                    "name": "Alice Participant",
                    "email": "alice.participant@example.com",
                    "password": "password123",
                    "dateOfBirth": "1995-05-20"
                }
                """)
            .when().post("/api/auth/register")
            .then().statusCode(201);

        // Login to get their ID from the token subject
        String token = given()
            .contentType("application/json")
            .body("""
                {
                    "email": "alice.participant@example.com",
                    "password": "password123"
                }
                """)
            .when().post("/api/auth/login")
            .then().statusCode(200)
            .extract().jsonPath().getString("token");

        // Decode subject from JWT (sub claim)
        String[] parts = token.split("\\.");
        String payloadJson = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
        String sub = payloadJson.replaceAll(".*\"sub\":\"(\\d+)\".*", "$1");
        return Long.parseLong(sub);
    }

    @Test
    @Order(1)
    void testListPublishedEventsUnauthenticated() {
        given()
            .when().get("/api/participant/events")
            .then()
            .statusCode(401);
    }

    @Test
    @Order(2)
    void testListPublishedEventsAsStaffForbidden() {
        given()
            .header("Authorization", "Bearer " + TestTokens.staffToken(1L))
            .when().get("/api/participant/events")
            .then()
            .statusCode(403);
    }

    @Test
    @Order(3)
    void testSetupPublishedEvent() {
        locationId = createLocation();
        eventTypeId = createEventType();
        staffId = createStaff();
        participantId = registerParticipant();

        // Create event instance dated in the future
        publishedInstanceId = given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body(String.format("""
                {
                    "eventType": {"id": %d},
                    "location":  {"id": %d},
                    "eventDate": "2026-09-15",
                    "startTime": "10:00:00"
                }
                """, eventTypeId, locationId))
            .when().post("/api/event-instances")
            .then().statusCode(201)
            .extract().jsonPath().getLong("id");

        // Assign staff so we can publish
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body(String.format("""
                {"staffId": %d}
                """, staffId))
            .when().post("/api/event-instances/" + publishedInstanceId + "/assign")
            .then().statusCode(201);

        // Publish the event
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .when().post("/api/event-instances/" + publishedInstanceId + "/publish")
            .then().statusCode(200)
            .body("status", is("PUBLISHED"));
    }

    @Test
    @Order(4)
    void testListPublishedEventsAsParticipant() {
        given()
            .header("Authorization", "Bearer " + TestTokens.participantToken(participantId))
            .when().get("/api/participant/events")
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    @Order(5)
    void testRegisterForEvent() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.participantToken(participantId))
            .when().post("/api/participant/events/" + publishedInstanceId + "/register")
            .then()
            .statusCode(201);
    }

    @Test
    @Order(6)
    void testRegisterForEventTwiceFails() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.participantToken(participantId))
            .when().post("/api/participant/events/" + publishedInstanceId + "/register")
            .then()
            .statusCode(400);
    }

    @Test
    @Order(7)
    void testRegisterForNonExistentEvent() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.participantToken(participantId))
            .when().post("/api/participant/events/99999/register")
            .then()
            .statusCode(404);
    }

    @Test
    @Order(8)
    void testCancelRegistration() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.participantToken(participantId))
            .when().delete("/api/participant/events/" + publishedInstanceId + "/register")
            .then()
            .statusCode(204);
    }

    @Test
    @Order(9)
    void testCancelNonExistentRegistration() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.participantToken(participantId))
            .when().delete("/api/participant/events/" + publishedInstanceId + "/register")
            .then()
            .statusCode(404);
    }

    @Test
    @Order(10)
    void testRegisterForDraftEventFails() {
        // Create a draft event (not published)
        Long draftInstanceId = given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body(String.format("""
                {
                    "eventType": {"id": %d},
                    "location":  {"id": %d},
                    "eventDate": "2026-10-01",
                    "startTime": "09:00:00"
                }
                """, eventTypeId, locationId))
            .when().post("/api/event-instances")
            .then().statusCode(201)
            .extract().jsonPath().getLong("id");

        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.participantToken(participantId))
            .when().post("/api/participant/events/" + draftInstanceId + "/register")
            .then()
            .statusCode(400);
    }
}
