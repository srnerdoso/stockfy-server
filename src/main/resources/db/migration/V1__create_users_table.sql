CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(60) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    reset_password_code_hash VARCHAR(64) UNIQUE,
    reset_password_expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_at TIMESTAMP,
    updated_by UUID
);

CREATE TABLE users_roles (
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT pk_users_roles PRIMARY KEY (user_id, role),
    CONSTRAINT fk_users_roles_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE revinfo (
    rev INTEGER PRIMARY KEY,
    revtstmp BIGINT
);

CREATE TABLE users_aud (
    id UUID NOT NULL,
    rev INTEGER NOT NULL,
    revtype SMALLINT,
    name VARCHAR(255),
    email VARCHAR(255),
    password_hash VARCHAR(255),
    status VARCHAR(20),
    active BOOLEAN,
    reset_password_code_hash VARCHAR(255),
    reset_password_expires_at TIMESTAMP,
    created_at TIMESTAMP,
    created_by UUID,
    updated_at TIMESTAMP,
    updated_by UUID,
    PRIMARY KEY (id, rev),
    FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

CREATE TABLE users_roles_aud (
    rev INTEGER NOT NULL,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    revtype SMALLINT,
    CONSTRAINT pk_users_roles_aud PRIMARY KEY (rev, user_id, role),
    CONSTRAINT fk_users_roles_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(rev)
);

CREATE SEQUENCE revinfo_seq START WITH 1 INCREMENT BY 50;
