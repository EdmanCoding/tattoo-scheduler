-- Insert fixed test data that will be available in EVERY test
INSERT INTO artists(id, name, email, password, created_at)
VALUES (1, 'Nikita', 'nikita@test.com', 'secret', CURRENT_TIMESTAMP);