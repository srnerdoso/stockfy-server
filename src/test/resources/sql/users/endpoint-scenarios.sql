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
        '00000000-0000-0000-0000-000000000009',
        'Current Scenario User',
        'current.scenario@example.com',
        'password-hash-9',
        'ACTIVE',
        TRUE,
        TIMESTAMP '2026-04-28 18:00:00',
        '11111111-1111-1111-1111-111111111111',
        TIMESTAMP '2026-04-28 18:30:00',
        '22222222-2222-2222-2222-222222222222'
    ),
    (
        '00000000-0000-0000-0000-000000000010',
        'Delete Scenario User',
        'delete.scenario@example.com',
        'password-hash-10',
        'ACTIVE',
        TRUE,
        TIMESTAMP '2026-04-28 19:00:00',
        '11111111-1111-1111-1111-111111111111',
        TIMESTAMP '2026-04-28 19:30:00',
        '22222222-2222-2222-2222-222222222222'
    ),
    (
        '00000000-0000-0000-0000-000000000011',
        'Other Delete Scenario User',
        'other.delete.scenario@example.com',
        'password-hash-11',
        'ACTIVE',
        TRUE,
        TIMESTAMP '2026-04-28 20:00:00',
        '11111111-1111-1111-1111-111111111111',
        TIMESTAMP '2026-04-28 20:30:00',
        '22222222-2222-2222-2222-222222222222'
    ),
    (
        '00000000-0000-0000-0000-000000000012',
        'Reset Scenario User',
        'reset.scenario@example.com',
        'password-hash-12',
        'ACTIVE',
        TRUE,
        TIMESTAMP '2026-04-28 21:00:00',
        '11111111-1111-1111-1111-111111111111',
        TIMESTAMP '2026-04-28 21:30:00',
        '22222222-2222-2222-2222-222222222222'
    );

INSERT INTO users_roles (user_id, role) VALUES
    ('00000000-0000-0000-0000-000000000009', 'USER'),
    ('00000000-0000-0000-0000-000000000010', 'USER'),
    ('00000000-0000-0000-0000-000000000011', 'USER'),
    ('00000000-0000-0000-0000-000000000012', 'USER');
