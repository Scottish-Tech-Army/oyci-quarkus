-- ============================================================
-- Schema DDL and Seed Data for OYCI Quarkus API
-- Executed automatically by Hibernate on startup
-- ============================================================

-- Insert sample data for testing (with audit fields)

-- Roles
INSERT INTO role (name, role_type, record_created_by, record_updated_by, record_created_datetime, record_updated_datetime)
VALUES
('Admin', 'ADMIN', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Manager', 'MANAGER', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Staff', 'STAFF', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Contact Details (with address fields included)
INSERT INTO contact_detail (primary_email, secondary_email, primary_phone, secondary_phone,
                             address_line_1, address_line_2, city, country, postcode,
                             record_created_by, record_updated_by, record_created_datetime, record_updated_datetime)
VALUES
('admin', NULL, '07700900000', NULL,
 '1 Admin Street', NULL, 'Glasgow', 'Scotland', 'G1 1AA',
 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('john.doe@oyci.scot', 'john.personal@gmail.com', '07700900001', '07700900002',
 '10 Downing Street', NULL, 'London', 'UK', 'SW1A 2AA',
 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('jane.smith@oyci.scot', NULL, '07700900003', NULL,
 '20 Castle Road', 'Apartment 5B', 'Edinburgh', 'Scotland', 'EH1 2NG',
 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Locations
INSERT INTO location (name, address_line_1, city, country, postcode, record_created_by, record_updated_by, record_created_datetime, record_updated_datetime)
VALUES
('Glasgow City Hall', '80 George Square', 'Glasgow', 'Scotland', 'G2 1DU', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Edinburgh Conference Centre', '150 Morrison Street', 'Edinburgh', 'Scotland', 'EH3 8EE', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Aberdeen Community Center', '27 Belmont Street', 'Aberdeen', 'Scotland', 'AB10 1JR', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Event Types
INSERT INTO event_type (name, description, record_created_by, record_updated_by, record_created_datetime, record_updated_datetime)
VALUES
('Community', 'Community events and gatherings', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Training', 'Training and educational sessions', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Fundraiser', 'Fundraising events', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Outreach', 'Community outreach programs', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Other', 'Other event types', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Staff Types
INSERT INTO staff_type (name, description, record_created_by, record_updated_by, record_created_datetime, record_updated_datetime)
VALUES
('COORDINATOR', 'Event coordinator', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('VOLUNTEER', 'Volunteer staff member', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('DRIVER', 'Driver for transportation', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MANAGER', 'Manager staff member', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Users (now with contact_detail_id and BCrypt hashed passwords)
-- Admin user password: "Welcome@123" (BCrypt hash: $2a$10$We3KIr7ZcNnf4fW4mYQHCu3xlCp8UFn0fBGgWYlF8Pu68UDb0/EZa)
-- Other users password: "password" (BCrypt hash: $2a$10$kMcM0udyu74SWGOsI6tU2.aLUZtnfWUvJ8RusLb5aRa1GmwqO/iji)
-- User 1: Admin (role_id=1), User 2: Manager (role_id=2), User 3: Staff (role_id=3)
INSERT INTO users (role_id, first_name, middle_name, last_name, dob, contact_detail_id, password, is_active, record_created_by, record_updated_by, record_created_datetime, record_updated_datetime)
VALUES
(1, 'Admin', NULL, 'User', '1980-01-01', 1, '$2a$10$We3KIr7ZcNnf4fW4mYQHCu3xlCp8UFn0fBGgWYlF8Pu68UDb0/EZa', true, 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'John', 'A', 'Doe', '1985-03-15', 2, '$2a$10$kMcM0udyu74SWGOsI6tU2.aLUZtnfWUvJ8RusLb5aRa1GmwqO/iji', true, 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Jane', NULL, 'Smith', '1990-07-22', 3, '$2a$10$kMcM0udyu74SWGOsI6tU2.aLUZtnfWUvJ8RusLb5aRa1GmwqO/iji', true, 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Qualifications
INSERT INTO qualification (name, description, record_created_by, record_updated_by, record_created_datetime, record_updated_datetime)
VALUES
('First Aid', 'Basic first aid certification', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Health & Safety', 'Health and safety level 2', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('DBS Checked', 'Enhanced DBS clearance', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Food Hygiene', 'Food hygiene certificate level 2', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Driving Licence', 'Full UK driving licence', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Staff (linking users to staff types and qualifications)
-- User ID 1: Admin (MANAGER), User ID 2: John Doe (COORDINATOR), User ID 3: Jane Smith (VOLUNTEER)
INSERT INTO staff (staff_type_id, user_id, qualification_id, weekly_hours_cap, preferred_shift_times, is_active, record_created_by, record_updated_by, record_created_datetime, record_updated_datetime)
VALUES
(4, 1, 2, 40, 'Full Day 8:30am - 9:30pm||Morning 8:30am - 3:30pm||Afternoon 12:30pm - 6:30pm', true, 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 2, 1, 40, 'Full Day 8:30am - 9:30pm||Morning 8:30am - 3:30pm', true, 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 3, 3, 20, 'Afternoon 12:30pm - 6:30pm||Evening 4:30pm - 9:00pm', true, 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Events
-- Owner IDs: 1=Admin, 2=John Doe, 3=Jane Smith
INSERT INTO event (event_name, event_type_id, description, start_datetime, end_datetime, location_id, owner_id, status, max_attendees, record_created_by, record_updated_by, record_created_datetime, record_updated_datetime)
VALUES
('Spring Community Fair', 1, 'Annual spring fair with food, games, and entertainment', '2026-05-15 10:00:00+00:00', '2026-05-15 18:00:00+00:00', 1, 2, 'scheduled', 50, 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('First Aid Training Workshop', 2, 'Basic first aid certification course', '2026-06-10 09:00:00+00:00', '2026-06-10 17:00:00+00:00', 2, 2, 'completed', 30, 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Charity Gala Night', 3, 'Annual charity fundraising dinner and auction', '2026-07-15 18:00:00+00:00', '2026-07-15 23:00:00+00:00', 3, 3, 'draft', 40, 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Community Outreach Day', 4, 'Community outreach and support services', '2026-08-20 10:00:00+00:00', '2026-08-20 16:00:00+00:00', 1, 3, 'completed', 35, 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Event Type Requirements
INSERT INTO event_type_requirement (event_type_id, qualification_id, requirement_level, record_created_by, record_updated_by, record_created_datetime, record_updated_datetime)
VALUES
(1, 1, 'preferred', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 2, 'required', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, 'required', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 3, 'required', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 3, 'preferred', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Staff Availability
INSERT INTO staff_availability (staff_id, start_datetime, end_datetime, record_created_by, record_updated_by, record_created_datetime, record_updated_datetime)
VALUES
(1, '2026-05-01 08:00:00+00:00', '2026-05-31 20:00:00+00:00', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '2026-05-10 08:00:00+00:00', '2026-05-20 20:00:00+00:00', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '2026-06-01 08:00:00+00:00', '2026-06-30 20:00:00+00:00', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '2026-07-01 08:00:00+00:00', '2026-07-31 20:00:00+00:00', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Staff Rota
INSERT INTO staff_rota (staff_id, event_id, start_datetime, end_datetime, is_published, record_created_by, record_updated_by, record_created_datetime, record_updated_datetime)
VALUES
(1, 1, '2026-05-15 10:00:00+00:00', '2026-05-15 18:00:00+00:00', true, 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 1, '2026-05-15 10:00:00+00:00', '2026-05-15 14:00:00+00:00', true, 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, 2, '2026-06-10 09:00:00+00:00', '2026-06-10 17:00:00+00:00', true, 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 4, '2026-08-20 10:00:00+00:00', '2026-08-20 16:00:00+00:00', false, 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Staff Leave
INSERT INTO staff_leave (staff_id, start_datetime, end_datetime, leave_type, status, record_created_by, record_updated_by, record_created_datetime, record_updated_datetime)
VALUES
(1, '2026-04-01 00:00:00+00:00', '2026-04-05 23:59:59+00:00', 'ANNUAL', true, 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, '2026-04-10 00:00:00+00:00', '2026-04-12 23:59:59+00:00', 'SICK', true, 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(1, '2026-09-15 00:00:00+00:00', '2026-09-20 23:59:59+00:00', 'ANNUAL', false, 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Registrations
INSERT INTO registration (event_id, registrant_user_id, registered_by_user_id, registration_date, status, record_created_by, record_updated_by, record_created_datetime, record_updated_datetime)
VALUES
(1, 3, 2, CURRENT_TIMESTAMP, 'APPROVED', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 2, 2, CURRENT_TIMESTAMP, 'APPROVED', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 3, 2, CURRENT_TIMESTAMP, 'PENDING', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 3, 3, CURRENT_TIMESTAMP, 'APPROVED', 'SYSTEM', 'SYSTEM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

