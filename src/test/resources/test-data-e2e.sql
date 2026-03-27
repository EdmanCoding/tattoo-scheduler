-- Insert artist (if not exists)
INSERT INTO artists(id, name, email, password, created_at)
SELECT 1, 'Nikita', 'nikita@test.com', 'secret', CURRENT_TIMESTAMP
    WHERE NOT EXISTS (SELECT 1 FROM artists WHERE id = 1);

-- Insert user with id 1 (the ID used in the test)
INSERT INTO users(id, name, email, phone_number, birth_date, password, created_at)
SELECT 1, 'Test User', 'testuser@example.com', '+123456789', '2000-01-01', 'secret', CURRENT_TIMESTAMP
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 1);