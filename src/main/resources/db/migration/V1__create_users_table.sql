CREATE TABLE users (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    reset_password_code_hash VARCHAR(255),
    reset_password_expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    created_by UUID,
    updated_at TIMESTAMP,
    updated_by UUID
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
    role VARCHAR(50),
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

CREATE SEQUENCE revinfo_seq START WITH 1 INCREMENT BY 50;
