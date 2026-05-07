-- Seed initial Admin user
-- Password: 'admin123' hashed with BCrypt (cost 10)
INSERT INTO users (name, email, password_hash, role, max_hours_per_week)
VALUES (
    'System Admin',
    'admin@oyci.org',
    '$2a$10$jHFhKQz7FnfmpV1u5YOWOur/3XCpw3V4cB0DO8gyDsF.MYMYjjL9y',
    'ADMIN',
    40
);

