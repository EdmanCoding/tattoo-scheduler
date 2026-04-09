-- Insert fixed test data that will be available in EVERY test
INSERT INTO artists(id, name, email, password, created_at)
VALUES (1, 'Nikita', 'nikita@test.com',
        '$2a$10$kg5iPXsEwbFBq/fBtlRm5uWqBoQx4h4YYvjGCDRr1pTAsOGiErseS', CURRENT_TIMESTAMP);

INSERT INTO artists(id, name, email, password, created_at)
VALUES (2, 'Vasya', 'vasay@test.com',
        '$2a$10$kg5iPXsEwbFBq/fBtlRm5uWqBoQx4h4YYvjGCDRr1pTAsOGiErseS', CURRENT_TIMESTAMP);

