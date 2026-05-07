package org.scottishtecharmy.oyci.quarkus;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class EventResourceTest {

    @Test
    void testGetEventPicklist() {
        given()
            .when()
                .get("/events/get-event-picklist")
            .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("eventTypes", notNullValue())
                .body("locations", notNullValue())
                .body("eventTypes.size()", greaterThan(0))
                .body("locations.size()", greaterThan(0))
                .body("eventTypes[0].id", notNullValue())
                .body("eventTypes[0].name", notNullValue())
                .body("locations[0].id", notNullValue())
                .body("locations[0].name", notNullValue());
    }

    @Test
    void testCreateEvent_Success() {
        String requestBody = """
            {
                "eventName": "Test Community Event",
                "eventType": "Community",
                "description": "A test event for the community",
                "eventDate": "15/05/2026",
                "startTime": "09:00",
                "endTime": "17:00",
                "location": "Glasgow City Hall",
                "maxAttendees": 30
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("X-User-Id", "1")
            .body(requestBody)
            .when()
                .post("/events/create-event")
            .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("eventName", equalTo("Test Community Event"))
                .body("eventTypeName", equalTo("Community"))
                .body("eventLocation", equalTo("Glasgow City Hall"))
                .body("status", equalTo("draft"));
    }

    @Test
    void testCreateEvent_InvalidEventType() {
        String requestBody = """
            {
                "eventName": "Test Event",
                "eventType": "NonExistentType",
                "description": "A test event",
                "eventDate": "15/05/2026",
                "startTime": "09:00",
                "endTime": "17:00",
                "location": "Glasgow City Hall",
                "maxAttendees": 30
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("X-User-Id", "1")
            .body(requestBody)
            .when()
                .post("/events/create-event")
            .then()
                .statusCode(404)
                .body("error", equalTo("Resource Not Found"))
                .body("message", containsString("Event type 'NonExistentType' not found"));
    }

    @Test
    void testCreateEvent_InvalidLocation() {
        String requestBody = """
            {
                "eventName": "Test Event",
                "eventType": "Community",
                "description": "A test event",
                "eventDate": "15/05/2026",
                "startTime": "09:00",
                "endTime": "17:00",
                "location": "NonExistent Location",
                "maxAttendees": 30
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("X-User-Id", "1")
            .body(requestBody)
            .when()
                .post("/events/create-event")
            .then()
                .statusCode(404)
                .body("error", equalTo("Resource Not Found"))
                .body("message", containsString("Location 'NonExistent Location' not found"));
    }

    @Test
    void testCreateEvent_InvalidDateFormat() {
        String requestBody = """
            {
                "eventName": "Test Event",
                "eventType": "Community",
                "description": "A test event",
                "eventDate": "2026-05-15",
                "startTime": "09:00",
                "endTime": "17:00",
                "location": "Glasgow City Hall",
                "maxAttendees": 30
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("X-User-Id", "1")
            .body(requestBody)
            .when()
                .post("/events/create-event")
            .then()
                .statusCode(400);
    }

    @Test
    void testCreateEvent_InvalidTimeFormat() {
        String requestBody = """
            {
                "eventName": "Test Event",
                "eventType": "Community",
                "description": "A test event",
                "eventDate": "15/05/2026",
                "startTime": "9:00 AM",
                "endTime": "17:00",
                "location": "Glasgow City Hall",
                "maxAttendees": 30
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("X-User-Id", "1")
            .body(requestBody)
            .when()
                .post("/events/create-event")
            .then()
                .statusCode(400);
    }

    @Test
    void testCreateEvent_EndTimeBeforeStartTime() {
        String requestBody = """
            {
                "eventName": "Test Event",
                "eventType": "Community",
                "description": "A test event",
                "eventDate": "15/05/2026",
                "startTime": "17:00",
                "endTime": "09:00",
                "location": "Glasgow City Hall",
                "maxAttendees": 30
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("X-User-Id", "1")
            .body(requestBody)
            .when()
                .post("/events/create-event")
            .then()
                .statusCode(400)
                .body("error", equalTo("Business Validation Error"))
                .body("message", allOf(containsString("End time"), containsString("must be after start time")));
    }

    @Test
    void testCreateEvent_MaxAttendeesExceedsLimit() {
        String requestBody = """
            {
                "eventName": "Test Event",
                "eventType": "Community",
                "description": "A test event",
                "eventDate": "15/05/2026",
                "startTime": "09:00",
                "endTime": "17:00",
                "location": "Glasgow City Hall",
                "maxAttendees": 100
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("X-User-Id", "1")
            .body(requestBody)
            .when()
                .post("/events/create-event")
            .then()
                .statusCode(400);
    }

    @Test
    void testCreateEvent_MaxAttendeesZero() {
        String requestBody = """
            {
                "eventName": "Test Event",
                "eventType": "Community",
                "description": "A test event",
                "eventDate": "15/05/2026",
                "startTime": "09:00",
                "endTime": "17:00",
                "location": "Glasgow City Hall",
                "maxAttendees": 0
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("X-User-Id", "1")
            .body(requestBody)
            .when()
                .post("/events/create-event")
            .then()
                .statusCode(400);
    }

    @Test
    void testCreateEvent_WithoutUserId() {
        String requestBody = """
            {
                "eventName": "System Event",
                "eventType": "Training",
                "description": "A system-created event",
                "eventDate": "20/06/2026",
                "startTime": "10:00",
                "endTime": "16:00",
                "location": "Edinburgh Conference Centre",
                "maxAttendees": 25
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
            .when()
                .post("/events/create-event")
            .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("eventName", equalTo("System Event"))
                .body("eventTypeName", equalTo("Training"));
    }

    @Test
    void testCreateEvent_MissingRequiredFields() {
        String requestBody = """
            {
                "eventName": "Test Event"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .header("X-User-Id", "1")
            .body(requestBody)
            .when()
                .post("/events/create-event")
            .then()
                .statusCode(400);
    }
}

