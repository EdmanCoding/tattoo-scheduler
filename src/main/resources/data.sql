INSERT INTO users(id, name, email, phone_number, birth_date, password, created_at)
SELECT 1, 'User1', 'testUser1@email.com', '+1111111', '2000-02-02', 'secret', CURRENT_TIMESTAMP
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 1);

INSERT INTO users(id, name, email, phone_number, birth_date, password, created_at)
SELECT 2, 'User2', 'testUser2@email.com', '+2111111', '2002-02-02', 'secret', CURRENT_TIMESTAMP
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE id = 2);

INSERT INTO artists(id, name, email, password, created_at)
SELECT 1, 'Nikita', 'tattooArtist@email.com', 'secret', CURRENT_TIMESTAMP
    WHERE NOT EXISTS (SELECT 1 FROM artists WHERE id = 1);