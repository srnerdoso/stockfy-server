INSERT INTO users (id, name, email, password_hash, role, status, active, created_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Administrator',
    'admin@stockfy.com',
    '$2a$10$8.UnVuG9HHgffUDAlk8q2OuVGkqEnLPzS4.N9LSpfWJp1I.YF./nu',
    'ADMIN',
    'ACTIVE',
    TRUE,
    CURRENT_TIMESTAMP
);
