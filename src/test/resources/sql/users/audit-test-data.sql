INSERT INTO users (
    id, name, email, password_hash, status, active,
    created_at, created_by, updated_at, updated_by
) VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'Audit Test User',
    'audit.test@example.com',
    'password-hash-audit',
    'ACTIVE',
    TRUE,
    TIMESTAMP '2026-01-01 10:00:00',
    '11111111-1111-1111-1111-111111111111',
    TIMESTAMP '2026-01-01 10:00:00',
    '11111111-1111-1111-1111-111111111111'
);

INSERT INTO users_roles (user_id, role) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'USER');
