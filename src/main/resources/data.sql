-- Insert default ADMIN user
INSERT INTO users (id, full_name, email, phone, password, role)
VALUES (
    1,
    'Deep Saha',
    'dip23447@gmail.com',
    1234567890,
    '$2a$10$feWjZINlKmmaTOc4rR3FYOyYjO/QUTxK1jfVW6fcyCY1b3VPBwxEm',
    'ADMIN'
);