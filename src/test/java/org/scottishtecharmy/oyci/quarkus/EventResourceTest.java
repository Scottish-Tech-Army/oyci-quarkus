package org.scottishtecharmy.oyci.quarkus;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class EventResourceTest {

    @Test
    void testCreateAndGetEventById() {
        String eventDate = LocalDate.now().plusDays(30).toString();

        String createdEventId = given()
                .contentType("application/json")
                .body("""
                        {
                          "eventType": "MEETUP",
                          "eventDate": "%s",
                          "eventLocation": "Glasgow"
                        }
                        """.formatted(eventDate))
                .when()
                .post("/events")
                .then()
                .statusCode(201)
                .body("eventId", notNullValue())
                .body("eventType", org.hamcrest.Matchers.is("MEETUP"))
                .body("eventDate", org.hamcrest.Matchers.is(eventDate))
                .body("eventLocation", org.hamcrest.Matchers.is("Glasgow"))
                .extract()
                .path("eventId");

        given()
                .when()
                .get("/events/{eventId}", createdEventId)
                .then()
                .statusCode(200)
                .body("eventId", org.hamcrest.Matchers.is(createdEventId))
                .body("eventType", org.hamcrest.Matchers.is("MEETUP"))
                .body("eventDate", org.hamcrest.Matchers.is(eventDate))
                .body("eventLocation", org.hamcrest.Matchers.is("Glasgow"));
    }

    @Test
    void testGetEventsReturnsOnlyUpcoming() {
        String futureDate = LocalDate.now().plusDays(7).toString();
        String pastDate = LocalDate.now().minusDays(7).toString();

        String futureEventId = createEvent("WORKSHOP", futureDate, "Edinburgh");
        String pastEventId = createEvent("CONFERENCE", pastDate, "Aberdeen");

        given()
                .when()
                .get("/events")
                .then()
                .statusCode(200)
                .body("eventId", hasItem(futureEventId))
                .body("eventId", not(hasItem(pastEventId)));
    }

    @Test
    void testGetEventByIdNotFound() {
        given()
                .when()
                .get("/events/{eventId}", UUID.randomUUID().toString())
                .then()
                .statusCode(404);
    }

    private String createEvent(String eventType, String eventDate, String eventLocation) {
        return given()
                .contentType("application/json")
                .body("""
                        {
                          "eventType": "%s",
                          "eventDate": "%s",
                          "eventLocation": "%s"
                        }
                        """.formatted(eventType, eventDate, eventLocation))
                .when()
                .post("/events")
                .then()
                .statusCode(201)
                .extract()
                .path("eventId");
    }
}

