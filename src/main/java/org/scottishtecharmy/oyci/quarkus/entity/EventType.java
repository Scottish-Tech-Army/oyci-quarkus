package org.scottishtecharmy.oyci.quarkus.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "event_types")
public class EventType extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 200)
    public String name;

    @Column(columnDefinition = "TEXT")
    public String description;

    @Column(name = "duration_minutes", nullable = false)
    public int durationMinutes;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "event_type_tags",
        joinColumns = @JoinColumn(name = "event_type_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    public List<Tag> requiredTags = new ArrayList<>();
}

