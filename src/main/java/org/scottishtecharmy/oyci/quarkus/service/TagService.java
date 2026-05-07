package org.scottishtecharmy.oyci.quarkus.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import org.scottishtecharmy.oyci.quarkus.entity.Tag;

import java.util.List;

@ApplicationScoped
public class TagService {

    public List<Tag> listAll() {
        return Tag.listAll();
    }

    @Transactional
    public Tag create(Tag tag) {
        tag.persist();
        return tag;
    }

    @Transactional
    public Tag update(Long id, Tag updated) {
        Tag tag = Tag.findById(id);
        if (tag == null) throw new NotFoundException("Tag not found");
        tag.name = updated.name;
        return tag;
    }

    @Transactional
    public void delete(Long id) {
        Tag tag = Tag.findById(id);
        if (tag == null) throw new NotFoundException("Tag not found");
        tag.delete();
    }
}

