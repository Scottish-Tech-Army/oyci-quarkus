package org.scottishtecharmy.oyci.quarkus.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.scottishtecharmy.oyci.quarkus.TestTokens;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommunicationsResourceTest {

    private static Long locationId;
    private static Long eventTypeId;
    private static Long staffId;
    private static Long instanceId;

    private Long createLocation() {
        return given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body("""
                {
                    "name": "Comms Test Venue",
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
                    "name": "Comms Test Event",
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
                    "name": "Comms Staff",
                    "email": "comms.staff@example.com",
                    "passwordHash": "password123",
                    "maxHoursPerWeek": 40
                }
                """)
            .when().post("/api/staff")
            .then().statusCode(201)
            .extract().jsonPath().getLong("id");
    }

    @Test
    @Order(1)
    void testNotifyStaffUnauthenticated() {
        given()
            .contentType("application/json")
            .body("""
                {
                    "fromDate": "2026-06-01",
                    "toDate": "2026-06-30"
                }
                """)
            .when().post("/api/communications/notify-staff")
            .then()
            .statusCode(401);
    }

    @Test
    @Order(2)
    void testNotifyStaffAsStaffForbidden() {
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.staffToken(1L))
            .body("""
                {
                    "fromDate": "2026-06-01",
                    "toDate": "2026-06-30"
                }
                """)
            .when().post("/api/communications/notify-staff")
            .then()
            .statusCode(403);
    }

    @Test
    @Order(3)
    void testNotifyStaffNoAssignmentsInRange() {
        // Date range with no assignments returns 0 emails sent
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body("""
                {
                    "fromDate": "2020-01-01",
                    "toDate": "2020-01-31"
                }
                """)
            .when().post("/api/communications/notify-staff")
            .then()
            .statusCode(200)
            .body("emailsSent", is(0));
    }

    @Test
    @Order(4)
    void testSetupAssignedEvent() {
        locationId = createLocation();
        eventTypeId = createEventType();
        staffId = createStaff();

        instanceId = given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body(String.format("""
                {
                    "eventType": {"id": %d},
                    "location":  {"id": %d},
                    "eventDate": "2026-08-15",
                    "startTime": "09:00:00"
                }
                """, eventTypeId, locationId))
            .when().post("/api/event-instances")
            .then().statusCode(201)
            .extract().jsonPath().getLong("id");

        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body(String.format("""
                {"staffId": %d}
                """, staffId))
            .when().post("/api/event-instances/" + instanceId + "/assign")
            .then().statusCode(201);
    }

    @Test
    @Order(5)
    void testNotifyStaffWithAssignments() {
        // Mailer is mocked in test profile, so this should succeed and return 1 email sent
        given()
            .contentType("application/json")
            .header("Authorization", "Bearer " + TestTokens.adminToken())
            .body("""
                {
                    "fromDate": "2026-08-01",
                    "toDate": "2026-08-31"
                }
                """)
            .when().post("/api/communications/notify-staff")
            .then()
            .statusCode(200)
            .body("emailsSent", is(1));
    }
}
