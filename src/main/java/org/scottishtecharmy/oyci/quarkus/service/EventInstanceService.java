package org.scottishtecharmy.oyci.quarkus.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.scottishtecharmy.oyci.quarkus.entity.*;
import org.scottishtecharmy.oyci.quarkus.enums.EventStatus;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class EventInstanceService {

    public List<EventInstance> listAll(LocalDate from, LocalDate to, EventStatus status) {
        return EventInstance.findByFilters(from, to, status);
    }

    public EventInstance findById(Long id) {
        EventInstance ei = EventInstance.findById(id);
        if (ei == null) throw new NotFoundException("Event instance not found");
        return ei;
    }

    @Transactional
    public EventInstance create(EventInstance instance) {
        instance.status = EventStatus.DRAFT;
        instance.persist();
        return instance;
    }

    @Transactional
    public EventInstance update(Long id, EventInstance updated) {
        EventInstance ei = EventInstance.findById(id);
        if (ei == null) throw new NotFoundException("Event instance not found");
        ei.eventType = updated.eventType;
        ei.location = updated.location;
        ei.eventDate = updated.eventDate;
        ei.startTime = updated.startTime;
        ei.capacityOverride = updated.capacityOverride;
        return ei;
    }

    @Transactional
    public void delete(Long id) {
        EventInstance ei = EventInstance.findById(id);
        if (ei == null) throw new NotFoundException("Event instance not found");
        ei.delete();
    }

    @Transactional
    public EventAssignment assignStaff(Long instanceId, Long staffId) {
        EventInstance ei = EventInstance.findById(instanceId);
        if (ei == null) throw new NotFoundException("Event instance not found");
        User staff = User.findById(staffId);
        if (staff == null) throw new NotFoundException("Staff member not found");
        if (EventAssignment.findByEventAndUser(instanceId, staffId) != null) {
            throw new BadRequestException("Staff already assigned to this event");
        }
        EventAssignment assignment = new EventAssignment();
        assignment.eventInstance = ei;
        assignment.user = staff;
        assignment.persist();
        return assignment;
    }

    @Transactional
    public void unassignStaff(Long instanceId, Long staffId) {
        EventAssignment assignment = EventAssignment.findByEventAndUser(instanceId, staffId);
        if (assignment == null) throw new NotFoundException("Assignment not found");
        assignment.delete();
    }

    @Transactional
    public EventInstance publish(Long id) {
        EventInstance ei = EventInstance.findById(id);
        if (ei == null) throw new NotFoundException("Event instance not found");
        if (ei.status != EventStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT events can be published");
        }
        List<EventAssignment> assignments = EventAssignment.findByEventInstanceId(id);
        if (assignments.isEmpty()) {
            throw new BadRequestException("Cannot publish event with no staff assigned");
        }
        ei.status = EventStatus.PUBLISHED;
        return ei;
    }

    public List<EventAssignment> getAssignments(Long instanceId) {
        if (EventInstance.findById(instanceId) == null) throw new NotFoundException("Event instance not found");
        return EventAssignment.findByEventInstanceId(instanceId);
    }
}

