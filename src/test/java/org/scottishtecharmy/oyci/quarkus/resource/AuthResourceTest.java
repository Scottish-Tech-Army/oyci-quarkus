package org.scottishtecharmy.oyci.quarkus.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthResourceTest {

    @Test
    @Order(1)
    void testRegisterNewUser() {
        given()
            .contentType("application/json")
            .body("""
                {
                    "name": "Test User",
                    "email": "testuser@example.com",
                    "password": "password123",
                    "dateOfBirth": "1990-01-15"
                }
                """)
            .when().post("/api/auth/register")
            .then()
            .statusCode(201);
    }

    @Test
    @Order(2)
    void testRegisterDuplicateEmail() {
        given()
            .contentType("application/json")
            .body("""
                {
                    "name": "Test User 2",
                    "email": "testuser@example.com",
                    "password": "password456"
                }
                """)
            .when().post("/api/auth/register")
            .then()
            .statusCode(400);
    }

    @Test
    @Order(3)
    void testLoginSuccess() {
        given()
            .contentType("application/json")
            .body("""
                {
                    "email": "admin@oyci.org",
                    "password": "admin123"
                }
                """)
            .when().post("/api/auth/login")
            .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("name", is("System Admin"))
            .body("email", is("admin@oyci.org"))
            .body("role", is("ADMIN"));
    }

    @Test
    @Order(4)
    void testLoginInvalidCredentials() {
        given()
            .contentType("application/json")
            .body("""
                {
                    "email": "admin@oyci.org",
                    "password": "wrongpassword"
                }
                """)
            .when().post("/api/auth/login")
            .then()
            .statusCode(401);
    }

    @Test
    @Order(5)
    void testLoginNonExistentUser() {
        given()
            .contentType("application/json")
            .body("""
                {
                    "email": "nobody@example.com",
                    "password": "password123"
                }
                """)
            .when().post("/api/auth/login")
            .then()
            .statusCode(401);
    }

    @Test
    @Order(6)
    void testLoginRegisteredParticipant() {
        // Login as the user we registered in test 1
        given()
            .contentType("application/json")
            .body("""
                {
                    "email": "testuser@example.com",
                    "password": "password123"
                }
                """)
            .when().post("/api/auth/login")
            .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("role", is("PARTICIPANT"));
    }
}
