package org.scottishtecharmy.oyci.quarkus;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class DomainScaffoldResourceTest {

    @Test
    void testStaffScaffoldEndpointReturnsRecords() {
        given()
                .when()
                .get("/staff")
                .then()
                .statusCode(200)
                .body("size()", greaterThan(0))
                .body("[0].id", notNullValue())
                .body("[0].firstName", notNullValue())
                .body("[0].email", notNullValue());
    }
}
