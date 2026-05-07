-- ===== USERS =====
CREATE TABLE users (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(150)  NOT NULL,
    email            VARCHAR(200)  NOT NULL UNIQUE,
    password_hash    VARCHAR(255)  NOT NULL,
    role             VARCHAR(20)   NOT NULL DEFAULT 'STAFF',
    max_hours_per_week INTEGER     NOT NULL DEFAULT 40,
    date_of_birth    DATE,
    CONSTRAINT users_role_check CHECK (role IN ('ADMIN', 'STAFF', 'PARTICIPANT'))
);

-- ===== TAGS =====
CREATE TABLE tags (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

-- ===== USER TAGS (many-to-many) =====
CREATE TABLE user_tags (
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    tag_id  BIGINT NOT NULL REFERENCES tags(id)  ON DELETE CASCADE,
    PRIMARY KEY (user_id, tag_id)
);

-- ===== USER AVAILABILITY =====
CREATE TABLE user_availability (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT  NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    day_of_week  INTEGER NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    start_time   TIME    NOT NULL,
    end_time     TIME    NOT NULL
);

-- ===== USER HOLIDAYS =====
CREATE TABLE user_holidays (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    start_date DATE   NOT NULL,
    end_date   DATE   NOT NULL,
    CONSTRAINT user_holidays_date_check CHECK (end_date >= start_date)
);

-- ===== LOCATIONS =====
CREATE TABLE locations (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(200) NOT NULL,
    address_line1    VARCHAR(255),
    address_line2    VARCHAR(255),
    city             VARCHAR(100),
    zip_code         VARCHAR(20),
    contact_name     VARCHAR(150),
    contact_phone    VARCHAR(50),
    contact_email    VARCHAR(150),
    default_capacity INTEGER
);

-- ===== EVENT TYPES =====
CREATE TABLE event_types (
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(200) NOT NULL,
    description      TEXT,
    duration_minutes INTEGER      NOT NULL
);

-- ===== EVENT TYPE TAGS (many-to-many) =====
CREATE TABLE event_type_tags (
    event_type_id BIGINT NOT NULL REFERENCES event_types(id) ON DELETE CASCADE,
    tag_id        BIGINT NOT NULL REFERENCES tags(id)         ON DELETE CASCADE,
    PRIMARY KEY (event_type_id, tag_id)
);

-- ===== EVENT INSTANCES =====
CREATE TABLE event_instances (
    id                BIGSERIAL PRIMARY KEY,
    event_type_id     BIGINT      NOT NULL REFERENCES event_types(id) ON DELETE RESTRICT,
    location_id       BIGINT      NOT NULL REFERENCES locations(id)   ON DELETE RESTRICT,
    event_date        DATE        NOT NULL,
    start_time        TIME        NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    capacity_override INTEGER,
    CONSTRAINT event_instances_status_check CHECK (status IN ('DRAFT', 'PUBLISHED', 'STAFFED', 'COMPLETED', 'CANCELLED'))
);

-- ===== EVENT ASSIGNMENTS (many-to-many: instances <-> users) =====
CREATE TABLE event_assignments (
    id                BIGSERIAL PRIMARY KEY,
    event_instance_id BIGINT NOT NULL REFERENCES event_instances(id) ON DELETE CASCADE,
    user_id           BIGINT NOT NULL REFERENCES users(id)            ON DELETE CASCADE,
    UNIQUE (event_instance_id, user_id)
);

-- ===== INDEXES =====
CREATE INDEX idx_user_availability_user_id  ON user_availability(user_id);
CREATE INDEX idx_user_holidays_user_id      ON user_holidays(user_id);
CREATE INDEX idx_event_instances_date       ON event_instances(event_date);
CREATE INDEX idx_event_instances_status     ON event_instances(status);
CREATE INDEX idx_event_assignments_instance ON event_assignments(event_instance_id);
CREATE INDEX idx_event_assignments_user     ON event_assignments(user_id);

