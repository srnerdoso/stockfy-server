INSERT INTO users (
    id,
    name,
    email,
    password_hash,
    status,
    active,
    created_at,
    created_by,
    updated_at,
    updated_by
) VALUES
    (
        '00000000-0000-0000-0000-000000000007',
        'Login Scenario User',
        'login.scenario@example.com',
        'password-hash-7',
        'ACTIVE',
        TRUE,
        TIMESTAMP '2026-04-28 16:00:00',
        '11111111-1111-1111-1111-111111111111',
        TIMESTAMP '2026-04-28 16:30:00',
        '22222222-2222-2222-2222-222222222222'
    ),
    (
        '00000000-0000-0000-0000-000000000008',
        'Locked Unlock User',
        'locked.unlock@example.com',
        'password-hash-8',
        'LOCKED',
        TRUE,
        TIMESTAMP '2026-04-28 17:00:00',
        '11111111-1111-1111-1111-111111111111',
        TIMESTAMP '2026-04-28 17:30:00',
        '22222222-2222-2222-2222-222222222222'
    );

INSERT INTO users_roles (user_id, role) VALUES
    ('00000000-0000-0000-0000-000000000007', 'USER'),
    ('00000000-0000-0000-0000-000000000008', 'USER');
