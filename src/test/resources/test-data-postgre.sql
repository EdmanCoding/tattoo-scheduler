-- Insert artist (if not exists)
INSERT INTO artists(id, name, email, password, created_at)
SELECT 1, 'Nikita', 'nikita@test.com',
       '$2a$10$kg5iPXsEwbFBq/fBtlRm5uWqBoQx4h4YYvjGCDRr1pTAsOGiErseS', CURRENT_TIMESTAMP
    WHERE NOT EXISTS (SELECT 1 FROM artists WHERE id = 1);

INSERT INTO artists(id, name, email, password, created_at)
SELECT 2, 'Vasya', 'vasay@test.com',
       '$2a$10$kg5iPXsEwbFBq/fBtlRm5uWqBoQx4h4YYvjGCDRr1pTAsOGiErseS', CURRENT_TIMESTAMP
    WHERE NOT EXISTS (SELECT 1 FROM artists WHERE id = 2);

-- Insert user with id 1 (the ID used in the test)
INSERT INTO users(id, name, email, phone_number, birth_date, password, created_at)
SELECT 1, 'Test User', 'testuser@example.com', '+123456789', '2000-01-01',
       '$2a$10$kg5iPXsEwbFBq/fBtlRm5uWqBoQx4h4YYvjGCDRr1pTAsOGiErseS', CURRENT_TIMESTAMP
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 1);

-- Reset the sequence
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));