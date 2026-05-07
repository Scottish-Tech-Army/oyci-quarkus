package org.scottishtecharmy.oyci.quarkus.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
public abstract class AuditableEntity {

    @Column(name = "record_created_by", nullable = false, updatable = false)
    private String recordCreatedBy;

    @Column(name = "record_created_datetime", nullable = false, updatable = false)
    private LocalDateTime recordCreatedDatetime;

    @Column(name = "record_updated_by")
    private String recordUpdatedBy;

    @Column(name = "record_updated_datetime")
    private LocalDateTime recordUpdatedDatetime;

    // Explicit getters/setters needed because:
    // 1. recordCreatedBy is nullable=false, so it MUST be set before persist()
    // 2. @PrePersist only sets the datetime fields, not the audit user field
    // 3. Other code needs to explicitly set who created/updated the record
    // 4. Lombok @Getter/@Setter don't propagate through @MappedSuperclass inheritance chains

    public String getRecordCreatedBy() { return recordCreatedBy; }
    public void setRecordCreatedBy(String recordCreatedBy) { this.recordCreatedBy = recordCreatedBy; }

    public String getRecordUpdatedBy() { return recordUpdatedBy; }
    public void setRecordUpdatedBy(String recordUpdatedBy) { this.recordUpdatedBy = recordUpdatedBy; }

    public LocalDateTime getRecordCreatedDatetime() { return recordCreatedDatetime; }
    public LocalDateTime getRecordUpdatedDatetime() { return recordUpdatedDatetime; }

    @PrePersist
    public void prePersist() {
        this.recordCreatedDatetime = LocalDateTime.now();
        this.recordUpdatedDatetime = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.recordUpdatedDatetime = LocalDateTime.now();
    }
}
