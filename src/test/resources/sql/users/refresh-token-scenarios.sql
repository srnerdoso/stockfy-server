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
        '00000000-0000-0000-0000-000000000005',
        'Locked Refresh User',
        'locked.refresh@example.com',
        'password-hash-5',
        'LOCKED',
        TRUE,
        TIMESTAMP '2026-04-28 14:00:00',
        '11111111-1111-1111-1111-111111111111',
        TIMESTAMP '2026-04-28 14:30:00',
        '22222222-2222-2222-2222-222222222222'
    ),
    (
        '00000000-0000-0000-0000-000000000006',
        'Inactive Refresh User',
        'inactive.refresh@example.com',
        'password-hash-6',
        'ACTIVE',
        FALSE,
        TIMESTAMP '2026-04-28 15:00:00',
        '11111111-1111-1111-1111-111111111111',
        TIMESTAMP '2026-04-28 15:30:00',
        '22222222-2222-2222-2222-222222222222'
    );

INSERT INTO users_roles (user_id, role) VALUES
    ('00000000-0000-0000-0000-000000000005', 'USER'),
    ('00000000-0000-0000-0000-000000000006', 'USER');
