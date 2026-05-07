package org.scottishtecharmy.oyci.quarkus.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.scottishtecharmy.oyci.quarkus.entity.EventType;
import org.scottishtecharmy.oyci.quarkus.entity.Tag;

import java.util.List;

@ApplicationScoped
public class EventTypeService {

    public List<EventType> listAll() {
        return EventType.listAll();
    }

    public EventType findById(Long id) {
        EventType et = EventType.findById(id);
        if (et == null) throw new NotFoundException("Event type not found");
        return et;
    }

    @Transactional
    public EventType create(EventType eventType) {
        eventType.persist();
        return eventType;
    }

    @Transactional
    public EventType update(Long id, EventType updated) {
        EventType et = EventType.findById(id);
        if (et == null) throw new NotFoundException("Event type not found");
        et.name = updated.name;
        et.description = updated.description;
        et.durationMinutes = updated.durationMinutes;
        return et;
    }

    @Transactional
    public EventType updateTags(Long id, List<Long> tagIds) {
        EventType et = EventType.findById(id);
        if (et == null) throw new NotFoundException("Event type not found");
        et.requiredTags.clear();
        for (Long tagId : tagIds) {
            Tag tag = Tag.findById(tagId);
            if (tag != null) et.requiredTags.add(tag);
        }
        return et;
    }

    @Transactional
    public void delete(Long id) {
        EventType et = EventType.findById(id);
        if (et == null) throw new NotFoundException("Event type not found");
        et.delete();
    }
}

