package org.scottishtecharmy.oyci.quarkus.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.scottishtecharmy.oyci.quarkus.entity.EventAssignment;
import org.scottishtecharmy.oyci.quarkus.entity.EventInstance;
import org.scottishtecharmy.oyci.quarkus.entity.User;
import org.scottishtecharmy.oyci.quarkus.enums.EventStatus;
import org.scottishtecharmy.oyci.quarkus.enums.Role;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class ParticipantService {

    public List<EventInstance> listPublishedEvents() {
        return EventInstance.findByFilters(LocalDate.now(), null, EventStatus.PUBLISHED);
    }

    @Transactional
    public void register(Long eventInstanceId, Long participantId) {
        EventInstance ei = EventInstance.findById(eventInstanceId);
        if (ei == null) throw new NotFoundException("Event not found");
        if (ei.status != EventStatus.PUBLISHED) {
            throw new BadRequestException("Event is not open for registration");
        }
        User participant = User.findById(participantId);
        if (participant == null || participant.role != Role.PARTICIPANT) {
            throw new BadRequestException("User is not a participant");
        }
        if (EventAssignment.findByEventAndUser(eventInstanceId, participantId) != null) {
            throw new BadRequestException("Already registered for this event");
        }

        // Check capacity
        int capacity = ei.capacityOverride != null
                ? ei.capacityOverride
                : (ei.location.defaultCapacity != null ? ei.location.defaultCapacity : Integer.MAX_VALUE);
        long currentRegistrations = EventAssignment.count("eventInstance.id", eventInstanceId);
        if (currentRegistrations >= capacity) {
            throw new BadRequestException("Event is at full capacity");
        }

        EventAssignment registration = new EventAssignment();
        registration.eventInstance = ei;
        registration.user = participant;
        registration.persist();
    }

    @Transactional
    public void cancelRegistration(Long eventInstanceId, Long participantId) {
        EventAssignment registration = EventAssignment.findByEventAndUser(eventInstanceId, participantId);
        if (registration == null) throw new NotFoundException("Registration not found");
        registration.delete();
    }
}

