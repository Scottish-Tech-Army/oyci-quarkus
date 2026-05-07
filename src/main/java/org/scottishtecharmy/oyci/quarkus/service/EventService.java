package org.scottishtecharmy.oyci.quarkus.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.scottishtecharmy.oyci.quarkus.dto.CreateEventRequest;
import org.scottishtecharmy.oyci.quarkus.dto.UpdateEventRequest;
import org.scottishtecharmy.oyci.quarkus.enums.EventStatus;
import org.scottishtecharmy.oyci.quarkus.exception.BusinessValidationException;
import org.scottishtecharmy.oyci.quarkus.exception.ResourceNotFoundException;
import org.scottishtecharmy.oyci.quarkus.model.Event;
import org.scottishtecharmy.oyci.quarkus.model.EventType;
import org.scottishtecharmy.oyci.quarkus.model.Location;
import org.scottishtecharmy.oyci.quarkus.model.Staff;
import org.scottishtecharmy.oyci.quarkus.model.StaffRota;
import org.scottishtecharmy.oyci.quarkus.model.User;
import org.scottishtecharmy.oyci.quarkus.repository.EventRepository;
import org.scottishtecharmy.oyci.quarkus.repository.EventTypeRepository;
import org.scottishtecharmy.oyci.quarkus.repository.LocationRepository;
import org.scottishtecharmy.oyci.quarkus.repository.RegistrationRepository;
import org.scottishtecharmy.oyci.quarkus.repository.StaffRepository;
import org.scottishtecharmy.oyci.quarkus.repository.StaffRotaRepository;
import org.scottishtecharmy.oyci.quarkus.repository.UserRepository;
import org.scottishtecharmy.oyci.quarkus.response.EventListResponse;
import org.scottishtecharmy.oyci.quarkus.response.EventPicklistResponse;
import org.scottishtecharmy.oyci.quarkus.response.EventResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class EventService {

    private static final Logger LOG = Logger.getLogger(EventService.class);

    @Inject
    EventRepository eventRepository;

    @Inject
    EventTypeRepository eventTypeRepository;

    @Inject
    LocationRepository locationRepository;

    @Inject
    UserRepository userRepository;

    @Inject
    RegistrationRepository registrationRepository;

    @Inject
    StaffRepository staffRepository;

    @Inject
    StaffRotaRepository staffRotaRepository;

    @Inject
    NotificationManager notificationManager;

    /**
     * Get all events with attendee count, upcoming events count, and total attendees for completed events
     * @return EventListResponse containing all events info, upcoming count, and total attendees
     */
    public EventListResponse getAllEvents() {
        LOG.info("Fetching all events with statistics");

        // Get all events with their attendee counts
        List<EventResponse> eventsInfo = eventRepository.listAll()
                .stream()
                .map(event -> EventResponse.from(
                        event,
                        registrationRepository.countApprovedByEventId(event.getEventId()),
                        staffRotaRepository.findByEventId(event.getEventId())
                ))
                .collect(Collectors.toList());

        // Count upcoming events (status = "scheduled")
        long upcomingEvent = eventRepository.countByStatus(EventStatus.SCHEDULED);

        // Count total approved attendees for completed events
        long totalAttendes = registrationRepository.countApprovedByEventStatus("completed");

        LOG.infof("Returning %d events, %d upcoming, %d total attendees for completed events",
                  eventsInfo.size(), upcomingEvent, totalAttendes);

        return new EventListResponse(eventsInfo, upcomingEvent, totalAttendes);
    }

    /**
     * Get event by ID
     */
    public EventResponse getEventById(Long eventId) {
        LOG.infof("Fetching event with ID: %d", eventId);
        Event event = eventRepository.findByIdOptional(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "eventId", eventId));

        long attendeeCount = registrationRepository.countApprovedByEventId(eventId);
        return EventResponse.from(event, attendeeCount, staffRotaRepository.findByEventId(eventId));
    }

    /**
     * Get picklist data for event creation (event types and locations)
     * @return EventPicklistResponse containing all active event types and locations
     */
    public EventPicklistResponse getEventPicklist() {
        LOG.info("Fetching event picklist data");

        try {
            // Fetch all event types
            List<EventPicklistResponse.PicklistItem> eventTypes = eventTypeRepository.listAll()
                    .stream()
                    .map(eventType -> new EventPicklistResponse.PicklistItem(
                            eventType.getEventTypeId(),
                            eventType.getName()
                    ))
                    .collect(Collectors.toList());

            // Fetch all locations
            List<EventPicklistResponse.PicklistItem> locations = locationRepository.listAll()
                    .stream()
                    .map(location -> new EventPicklistResponse.PicklistItem(
                            location.getLocationId(),
                            location.getName()
                    ))
                    .collect(Collectors.toList());

            LOG.infof("Fetched %d event types and %d locations", eventTypes.size(), locations.size());

            if (eventTypes.isEmpty()) {
                LOG.warn("No event types found in the database");
            }

            if (locations.isEmpty()) {
                LOG.warn("No locations found in the database");
            }

            return new EventPicklistResponse(eventTypes, locations);

        } catch (Exception e) {
            LOG.error("Error fetching event picklist data", e);
            throw new BusinessValidationException("Failed to fetch event picklist data: " + e.getMessage());
        }
    }


    /**
     * Create a new event with comprehensive validation
     * Accepts event type and location as names, and date/time as separate fields
     */
    @Transactional
    public EventResponse createEvent(CreateEventRequest request, String createdBy) {
        LOG.infof("Creating new event: %s", request.getEventName());

        // Parse and validate date and time fields
        OffsetDateTime startDateTime = parseDateTimeFields(
                request.getEventDate(),
                request.getStartTime(),
                "start"
        );
        OffsetDateTime endDateTime = parseDateTimeFields(
                request.getEventDate(),
                request.getEndTime(),
                "end"
        );

        // Validate that the event is in the future
        validateEventIsInFuture(startDateTime);

        // Validate start and end times
        validateEventDateTimes(startDateTime, endDateTime);

        // Lookup and validate EventType by name
        EventType eventType = lookupEventTypeByName(request.getEventType());

        // Lookup and validate Location by name
        Location location = lookupLocationByName(request.getLocation());

        // Determine owner - use X-User-Id if available, otherwise fallback to SYSTEM user
        User owner = determineEventOwner(createdBy);

        // Validate max attendees
        validateMaxAttendees(request.getMaxAttendees());

        // Check for duplicate event name on the same date at the same location
        checkDuplicateEvent(request.getEventName(), startDateTime, location.getLocationId());

        // Create Event entity
        Event event = new Event();
        event.setEventName(request.getEventName());
        event.setEventType(eventType);
        event.setDescription(request.getDescription());
        event.setStartDatetime(startDateTime);
        event.setEndDatetime(endDateTime);
        event.setLocation(location);
        event.setOwner(owner);
        event.setMaxAttendees(request.getMaxAttendees());
        event.setStatus(EventStatus.fromValue(request.getStatus()));

        // Set audit fields
        String auditUser = (createdBy != null && !createdBy.isEmpty()) ? createdBy : "SYSTEM";
        event.setRecordCreatedBy(auditUser);
        event.setRecordUpdatedBy(auditUser);

        // Persist event
        eventRepository.persist(event);
        LOG.infof("Event created successfully with ID: %d, Max Attendees: %d",
                  event.getEventId(), event.getMaxAttendees());


        // Handle staff rota assignments
        if (request.getStaffAssignments() != null && !request.getStaffAssignments().isEmpty()) {
            LOG.infof("Processing %d staff assignments for new event ID: %d",
                      request.getStaffAssignments().size(), event.getEventId());

            for (CreateEventRequest.StaffAssignment assignment : request.getStaffAssignments()) {
                // Look up staff member
                Staff staff = staffRepository.findByIdOptional(assignment.getStaffId())
                        .orElseThrow(() -> new ResourceNotFoundException("Staff", "staffId", assignment.getStaffId()));

                // Validate staff is active
                if (staff.getIsActive() != null && !staff.getIsActive()) {
                    throw new BusinessValidationException(
                            String.format("Cannot assign inactive staff member with ID: %d to the event.", assignment.getStaffId())
                    );
                }

                // Parse shift times using the event date
                OffsetDateTime shiftStart = parseDateTimeFields(
                        request.getEventDate(),
                        assignment.getShiftStart(),
                        "shift start"
                );
                OffsetDateTime shiftEnd = parseDateTimeFields(
                        request.getEventDate(),
                        assignment.getShiftEnd(),
                        "shift end"
                );

                // Create and persist rota entry
                StaffRota rota = new StaffRota();
                rota.setStaff(staff);
                rota.setEvent(event);
                rota.setRole(assignment.getRole());
                rota.setStartDatetime(shiftStart);
                rota.setEndDatetime(shiftEnd);
                rota.setIsPublished(false);
                rota.setRecordCreatedBy(auditUser);
                rota.setRecordUpdatedBy(auditUser);

                staffRotaRepository.persist(rota);
                LOG.infof("Created rota entry for staff ID: %d, role: %s, shift: %s - %s",
                          assignment.getStaffId(), assignment.getRole(),
                          assignment.getShiftStart(), assignment.getShiftEnd());
            }
        }

        // Send event creation notification to admins and assigned staff
        EventResponse createdEventResponse = EventResponse.from(event, 0L, staffRotaRepository.findByEventId(event.getEventId()));
        notificationManager.notifyEventCreated(createdEventResponse);

        return createdEventResponse;
    }

    /**
     * Soft delete an event by setting its status to DELETED
     * @param eventId ID of the event to delete
     * @throws ResourceNotFoundException if the event does not exist
     * @throws BusinessValidationException if the event is already deleted
     */
    @Transactional
    public void deleteEvent(Long eventId) {
        LOG.infof("Soft-deleting event with ID: %d", eventId);

        Event event = eventRepository.findByIdOptional(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "eventId", eventId));

        if (EventStatus.DELETED.equals(event.getStatus())) {
            throw new BusinessValidationException(
                    String.format("Event with ID %d is already deleted.", eventId)
            );
        }

        event.setStatus(EventStatus.DELETED);
        eventRepository.persist(event);

        LOG.infof("Event with ID %d successfully marked as deleted.", eventId);
    }

    /**
     * Update an existing event and manage staff rota assignments
     * @param eventId ID of the event to update
     * @param request UpdateEventRequest containing updated event details and staff assignments
     * @param updatedBy User performing the update
     * @return EventResponse with the updated event details
     */
    @Transactional
    public EventResponse updateEvent(Long eventId, UpdateEventRequest request, String updatedBy) {
        LOG.infof("Updating event with ID: %d", eventId);

        // Find existing event
        Event event = eventRepository.findByIdOptional(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "eventId", eventId));

        // Cannot update a deleted event
        if (EventStatus.DELETED.equals(event.getStatus())) {
            throw new BusinessValidationException(
                    String.format("Cannot update event with ID %d because it has been deleted.", eventId)
            );
        }

        // Parse and validate date and time fields
        OffsetDateTime startDateTime = parseDateTimeFields(
                request.getEventDate(),
                request.getStartTime(),
                "start"
        );
        OffsetDateTime endDateTime = parseDateTimeFields(
                request.getEventDate(),
                request.getEndTime(),
                "end"
        );

        // Validate start and end times
        validateEventDateTimes(startDateTime, endDateTime);

        // Lookup and validate EventType by name
        EventType eventType = lookupEventTypeByName(request.getEventType());

        // Lookup and validate Location by name
        Location location = lookupLocationByName(request.getLocation());

        // Validate max attendees
        validateMaxAttendees(request.getMaxAttendees());

        // Update event fields
        event.setEventName(request.getEventName());
        event.setEventType(eventType);
        event.setDescription(request.getDescription());
        event.setStartDatetime(startDateTime);
        event.setEndDatetime(endDateTime);
        event.setLocation(location);
        event.setMaxAttendees(request.getMaxAttendees());
        event.setStatus(EventStatus.fromValue(request.getStatus()));

        // Set audit fields
        String auditUser = (updatedBy != null && !updatedBy.isEmpty()) ? updatedBy : "SYSTEM";
        event.setRecordUpdatedBy(auditUser);

        // Persist updated event
        eventRepository.persist(event);
        LOG.infof("Event updated successfully with ID: %d", event.getEventId());

        // Handle staff rota assignments
        if (request.getStaffAssignments() != null && !request.getStaffAssignments().isEmpty()) {
            LOG.infof("Processing %d staff assignments for event ID: %d",
                      request.getStaffAssignments().size(), eventId);

            // Remove existing rota entries for this event
            long deletedCount = staffRotaRepository.deleteByEventId(eventId);
            LOG.infof("Deleted %d existing rota entries for event ID: %d", deletedCount, eventId);

            // Create new rota entries
            for (UpdateEventRequest.StaffAssignment assignment : request.getStaffAssignments()) {
                // Look up staff member
                Staff staff = staffRepository.findByIdOptional(assignment.getStaffId())
                        .orElseThrow(() -> new ResourceNotFoundException("Staff", "staffId", assignment.getStaffId()));

                // Validate staff is active
                if (staff.getIsActive() != null && !staff.getIsActive()) {
                    throw new BusinessValidationException(
                            String.format("Cannot assign inactive staff member with ID: %d to the event.", assignment.getStaffId())
                    );
                }

                // Parse shift times using the event date
                OffsetDateTime shiftStart = parseDateTimeFields(
                        request.getEventDate(),
                        assignment.getShiftStart(),
                        "shift start"
                );
                OffsetDateTime shiftEnd = parseDateTimeFields(
                        request.getEventDate(),
                        assignment.getShiftEnd(),
                        "shift end"
                );

                // Create and persist rota entry
                StaffRota rota = new StaffRota();
                rota.setStaff(staff);
                rota.setEvent(event);
                rota.setRole(assignment.getRole());
                rota.setStartDatetime(shiftStart);
                rota.setEndDatetime(shiftEnd);
                rota.setIsPublished(false);
                rota.setRecordCreatedBy(auditUser);
                rota.setRecordUpdatedBy(auditUser);

                staffRotaRepository.persist(rota);
                LOG.infof("Created rota entry for staff ID: %d, role: %s, shift: %s - %s",
                          assignment.getStaffId(), assignment.getRole(),
                          assignment.getShiftStart(), assignment.getShiftEnd());
            }
        }

        long attendeeCount = registrationRepository.countApprovedByEventId(eventId);
        EventResponse updatedEventResponse = EventResponse.from(event, attendeeCount, staffRotaRepository.findByEventId(eventId));

        // Send event update notification to admins and assigned staff
        notificationManager.notifyEventUpdated(updatedEventResponse);

        return updatedEventResponse;
    }

    /**
     * Parse date and time fields into OffsetDateTime
     * @param dateStr Date in dd/MM/yyyy format
     * @param timeStr Time in HH:mm format
     * @param fieldType "start" or "end" for error messages
     * @return OffsetDateTime in UTC timezone
     */
    private OffsetDateTime parseDateTimeFields(String dateStr, String timeStr, String fieldType) {
        try {
            if (dateStr == null || dateStr.trim().isEmpty()) {
                throw new BusinessValidationException("Event date cannot be null or empty");
            }
            if (timeStr == null || timeStr.trim().isEmpty()) {
                throw new BusinessValidationException(
                        String.format("%s time cannot be null or empty",
                                fieldType.substring(0, 1).toUpperCase() + fieldType.substring(1))
                );
            }

            // Parse date using dd/MM/yyyy format
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate localDate = LocalDate.parse(dateStr.trim(), dateFormatter);

            // Parse time using HH:mm format
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime localTime = LocalTime.parse(timeStr.trim(), timeFormatter);

            // Combine date and time with UTC timezone
            OffsetDateTime offsetDateTime = OffsetDateTime.of(
                    localDate,
                    localTime,
                    ZoneId.of("UTC").getRules().getOffset(localDate.atTime(localTime))
            );

            LOG.infof("Parsed %s datetime: %s from date=%s time=%s",
                      fieldType, offsetDateTime, dateStr, timeStr);

            return offsetDateTime;

        } catch (DateTimeParseException e) {
            LOG.errorf("Failed to parse %s date/time - date: %s, time: %s", fieldType, dateStr, timeStr);
            throw new BusinessValidationException(
                    String.format("Invalid %s date or time format. Expected date: dd/MM/yyyy, time: HH:mm. Error: %s",
                            fieldType, e.getMessage())
            );
        }
    }

    /**
     * Validate that the event start time is in the future
     */
    private void validateEventIsInFuture(OffsetDateTime startDateTime) {
        OffsetDateTime now = OffsetDateTime.now();
        if (startDateTime.isBefore(now) || startDateTime.isEqual(now)) {
            throw new BusinessValidationException(
                    String.format("Event start date and time must be in the future. Provided: %s, Current: %s",
                            startDateTime, now)
            );
        }
    }

    /**
     * Lookup EventType by name
     * @param eventTypeName Event type name from the dropdown
     * @return EventType entity
     * @throws ResourceNotFoundException if event type not found
     * @throws BusinessValidationException if event type name is invalid
     */
    private EventType lookupEventTypeByName(String eventTypeName) {
        if (eventTypeName == null || eventTypeName.trim().isEmpty()) {
            throw new BusinessValidationException("Event type name cannot be null or empty");
        }

        String trimmedName = eventTypeName.trim();
        LOG.infof("Looking up event type by name: '%s'", trimmedName);

        EventType eventType = eventTypeRepository.findByName(trimmedName)
                .orElseThrow(() -> {
                    LOG.errorf("Event type not found: '%s'", trimmedName);
                    return new ResourceNotFoundException(
                            String.format("Event type '%s' not found. Please select a valid event type from the dropdown.", trimmedName)
                    );
                });

        LOG.infof("Found event type: ID=%d, Name='%s'", eventType.getEventTypeId(), eventType.getName());
        return eventType;
    }

    /**
     * Lookup Location by name
     * @param locationName Location name from the dropdown
     * @return Location entity
     * @throws ResourceNotFoundException if location not found
     * @throws BusinessValidationException if location name is invalid
     */
    private Location lookupLocationByName(String locationName) {
        if (locationName == null || locationName.trim().isEmpty()) {
            throw new BusinessValidationException("Location name cannot be null or empty");
        }

        String trimmedName = locationName.trim();
        LOG.infof("Looking up location by name: '%s'", trimmedName);

        // First try exact match
        List<Location> locations = locationRepository.find("name", trimmedName).list();

        if (locations.isEmpty()) {
            // Try case-insensitive match
            locations = locationRepository.list("LOWER(name) = LOWER(?1)", trimmedName);
        }

        if (locations.isEmpty()) {
            LOG.errorf("Location not found: '%s'", trimmedName);
            throw new ResourceNotFoundException(
                    String.format("Location '%s' not found. Please select a valid location from the dropdown.", trimmedName)
            );
        }

        if (locations.size() > 1) {
            LOG.warnf("Multiple locations found with name '%s', using first match", trimmedName);
        }

        Location location = locations.get(0);
        LOG.infof("Found location: ID=%d, Name='%s'", location.getLocationId(), location.getName());
        return location;
    }

    /**
     * Determine the event owner
     * Uses the X-User-Id header if provided, otherwise uses SYSTEM user
     */
    private User determineEventOwner(String userId) {
        if (userId == null || userId.trim().isEmpty() || "SYSTEM".equalsIgnoreCase(userId.trim())) {
            LOG.info("No user ID provided or SYSTEM specified, looking up SYSTEM user");

            // Try to find a SYSTEM user or use the first admin user
            List<User> systemUsers = userRepository.list("LOWER(firstName) = 'system' OR LOWER(lastName) = 'system'");

            if (!systemUsers.isEmpty()) {
                User systemUser = systemUsers.get(0);
                LOG.infof("Using SYSTEM user: ID=%d", systemUser.getUserId());
                return systemUser;
            }

            // Fallback to first active admin user
            List<User> adminUsers = userRepository.list("roleId.roleType = 'ADMIN' and isActive = true");
            if (!adminUsers.isEmpty()) {
                User adminUser = adminUsers.get(0);
                LOG.infof("Using admin user as fallback: ID=%d", adminUser.getUserId());
                return adminUser;
            }

            throw new BusinessValidationException(
                    "No SYSTEM or admin user found. Please ensure at least one admin user exists in the system."
            );
        }

        // Parse userId and lookup user
        Long ownerUserId;
        try {
            ownerUserId = Long.parseLong(userId.trim());
        } catch (NumberFormatException e) {
            throw new BusinessValidationException(
                    String.format("Invalid user ID format: '%s'. User ID must be a valid number.", userId)
            );
        }

        User owner = userRepository.findByIdOptional(ownerUserId)
                .orElseThrow(() -> {
                    LOG.errorf("User not found with ID: %d", ownerUserId);
                    return new ResourceNotFoundException("User", "userId", ownerUserId);
                });

        // Validate user is active
        if (owner.getIsActive() != null && !owner.getIsActive()) {
            throw new BusinessValidationException(
                    String.format("Cannot create event with inactive user as owner. User ID: %d", ownerUserId)
            );
        }

        LOG.infof("Using user as owner: ID=%d, Name='%s %s'",
                  owner.getUserId(), owner.getFirstName(), owner.getLastName());
        return owner;
    }

    /**
     * Validate maximum attendees
     */
    private void validateMaxAttendees(Integer maxAttendees) {
        if (maxAttendees == null) {
            throw new BusinessValidationException("Maximum attendees cannot be null");
        }
        if (maxAttendees < 1) {
            throw new BusinessValidationException(
                    String.format("Maximum attendees must be at least 1. Provided: %d", maxAttendees)
            );
        }
        if (maxAttendees > 50) {
            throw new BusinessValidationException(
                    String.format("Maximum attendees cannot exceed 50. Provided: %d", maxAttendees)
            );
        }
        LOG.infof("Max attendees validated: %d", maxAttendees);
    }

    /**
     * Validate event date and times
     */
    private void validateEventDateTimes(OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
        if (startDateTime == null) {
            throw new BusinessValidationException("Start date and time cannot be null");
        }
        if (endDateTime == null) {
            throw new BusinessValidationException("End date and time cannot be null");
        }

        if (endDateTime.isBefore(startDateTime)) {
            throw new BusinessValidationException(
                    String.format("End time (%s) must be after start time (%s)",
                            endDateTime.toLocalTime(), startDateTime.toLocalTime())
            );
        }

        if (endDateTime.isEqual(startDateTime)) {
            throw new BusinessValidationException(
                    String.format("End time (%s) cannot be the same as start time (%s). Event must have a duration.",
                            endDateTime.toLocalTime(), startDateTime.toLocalTime())
            );
        }

        // Check if event duration is reasonable (not more than 24 hours for single day event)
        if (startDateTime.plusHours(24).isBefore(endDateTime)) {
            throw new BusinessValidationException(
                    String.format("Event duration cannot exceed 24 hours. Start: %s, End: %s",
                            startDateTime, endDateTime)
            );
        }

        // Validate minimum duration (at least 30 minutes)
        if (startDateTime.plusMinutes(30).isAfter(endDateTime)) {
            throw new BusinessValidationException(
                    String.format("Event duration must be at least 30 minutes. Start: %s, End: %s",
                            startDateTime.toLocalTime(), endDateTime.toLocalTime())
            );
        }

        LOG.infof("Event datetime validated - Start: %s, End: %s, Duration: %d minutes",
                  startDateTime, endDateTime,
                  java.time.Duration.between(startDateTime, endDateTime).toMinutes());
    }

    /**
     * Check for duplicate events
     */
    private void checkDuplicateEvent(String eventName, OffsetDateTime startDateTime, Long locationId) {
        if (eventName == null || eventName.trim().isEmpty()) {
            throw new BusinessValidationException("Event name cannot be null or empty for duplicate check");
        }
        if (startDateTime == null) {
            throw new BusinessValidationException("Start date time cannot be null for duplicate check");
        }
        if (locationId == null) {
            throw new BusinessValidationException("Location ID cannot be null for duplicate check");
        }

        LOG.infof("Checking for duplicate events - Name: '%s', Date: %s, Location ID: %d",
                  eventName, startDateTime.toLocalDate(), locationId);

        // Use CAST to DATE for H2 database compatibility
        List<Event> existingEvents = eventRepository.list(
                "eventName = ?1 and location.locationId = ?2 and CAST(startDatetime AS DATE) = CAST(?3 AS DATE)",
                eventName, locationId, startDateTime
        );

        if (!existingEvents.isEmpty()) {
            Event duplicate = existingEvents.get(0);
            LOG.warnf("Duplicate event found: ID=%d, Name='%s', Date=%s, Location ID=%d",
                      duplicate.getEventId(), duplicate.getEventName(),
                      duplicate.getStartDatetime().toLocalDate(), locationId);

            throw new BusinessValidationException(
                    String.format("An event with name '%s' already exists at this location on %s. Please choose a different name or date.",
                            eventName, startDateTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
            );
        }

        LOG.info("No duplicate events found");
    }
}

