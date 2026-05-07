package org.scottishtecharmy.oyci.quarkus.services;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.scottishtecharmy.oyci.quarkus.models.CreateEventRequest;
import org.scottishtecharmy.oyci.quarkus.models.Event;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;

@ApplicationScoped
public class EventService {

    @Inject
    DataSource dataSource;

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS events (
                event_id VARCHAR(36) PRIMARY KEY,
                event_type VARCHAR(32) NOT NULL,
                event_date VARCHAR(100) NOT NULL,
                event_location VARCHAR(255) NOT NULL,
                event_epoch_millis BIGINT NOT NULL
            )
            """;

    @PostConstruct
    void seedEvents() {
        createSchemaIfNeeded();
        if (countEvents() > 0) {
            return;
        }
        addSeedEvent("TRAINING", LocalDate.now().plusDays(3).toString(), "Glasgow Hub");
        addSeedEvent("MATCHDAY", LocalDate.now().plusDays(10).toString(), "Edinburgh Centre");
        addSeedEvent("TRAINING", LocalDate.now().plusDays(21).toString(), "Glasgow Hub");
    }

    public Event createEvent(CreateEventRequest request) {
        long eventEpochMillis = parseEventDate(request.eventDate())
                .map(Instant::toEpochMilli)
                .orElseThrow(() -> new IllegalArgumentException("eventDate must be valid"));

        String eventId = UUID.randomUUID().toString();
        Event event = new Event(eventId, request.eventType(), request.eventDate(), request.eventLocation());
        String insertSql = "INSERT INTO events (event_id, event_type, event_date, event_location, event_epoch_millis) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(insertSql)) {
            statement.setString(1, event.eventId());
            statement.setString(2, event.eventType());
            statement.setString(3, event.eventDate());
            statement.setString(4, event.eventLocation());
            statement.setLong(5, eventEpochMillis);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to store event", e);
        }

        return event;
    }

    private void addSeedEvent(String eventType, String eventDate, String eventLocation) {
        createEvent(new CreateEventRequest(eventType, eventDate, eventLocation));
    }

    public Optional<Event> getEventById(String eventId) {
        String selectSql = "SELECT event_id, event_type, event_date, event_location FROM events WHERE event_id = ?";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectSql)) {
            statement.setString(1, eventId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                return Optional.of(readEvent(resultSet));
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to read event", e);
        }
    }

    public List<Event> getUpcomingEvents() {
        String selectSql = "SELECT event_id, event_type, event_date, event_location FROM events WHERE event_epoch_millis >= ? ORDER BY event_epoch_millis ASC";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(selectSql)) {
            statement.setLong(1, Instant.now().toEpochMilli());
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Event> events = new java.util.ArrayList<>();
                while (resultSet.next()) {
                    events.add(readEvent(resultSet));
                }
                return events;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to query upcoming events", e);
        }
    }

    public static boolean isValidDate(String eventDate) {
        return parseEventDate(eventDate).isPresent();
    }

    private static Optional<Instant> parseEventDate(String eventDate) {
        if (eventDate == null || eventDate.isBlank()) {
            return Optional.empty();
        }

        try {
            return Optional.of(Instant.parse(eventDate));
        } catch (DateTimeParseException ignored) {
            // Try next format.
        }

        try {
            return Optional.of(OffsetDateTime.parse(eventDate).toInstant());
        } catch (DateTimeParseException ignored) {
            // Try next format.
        }

        try {
            return Optional.of(LocalDateTime.parse(eventDate).toInstant(ZoneOffset.UTC));
        } catch (DateTimeParseException ignored) {
            // Try next format.
        }

        try {
            return Optional.of(LocalDate.parse(eventDate).atStartOfDay().toInstant(ZoneOffset.UTC));
        } catch (DateTimeParseException ignored) {
            return Optional.empty();
        }
    }

    private void createSchemaIfNeeded() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(CREATE_TABLE_SQL)) {
            statement.execute();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create schema", e);
        }
    }

    private long countEvents() {
        String countSql = "SELECT COUNT(*) FROM events";
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(countSql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getLong(1);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to count events", e);
        }
    }

    private static Event readEvent(ResultSet resultSet) throws SQLException {
        return new Event(
                resultSet.getString("event_id"),
                resultSet.getString("event_type"),
                resultSet.getString("event_date"),
                resultSet.getString("event_location")
        );
    }
}


