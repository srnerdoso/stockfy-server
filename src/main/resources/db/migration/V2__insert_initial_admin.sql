INSERT INTO users (id, name, email, password_hash, role, status, active, created_at)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Administrator',
    'admin@stockfy.com',
    '$2a$10$tjf5Men0533uijkc8hk1cut4PNaoXn26gfE5TylOYlDxwj4sG5cku',
    'ADMIN',
    'ACTIVE',
    TRUE,
    CURRENT_TIMESTAMP
);
