-- Lab 5 schema. The database itself must be created separately, before running this script:
--     CREATE DATABASE socialnetwork;
-- (CREATE DATABASE cannot run inside a transaction block, so it is not part of this script.)
-- Then connect to socialnetwork and run this file.

CREATE TABLE users (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name VARCHAR NOT NULL,
    last_name  VARCHAR NOT NULL
);

CREATE TABLE friendships (
    id1          BIGINT,
    id2          BIGINT,
    friends_from TIMESTAMP NOT NULL,
    PRIMARY KEY (id1, id2),
    -- the domain normalizes a friendship so that the smaller id comes first
    CHECK (id1 < id2),
    -- safety net: the service deletes the friendships explicitly, the cascade only guards the database
    FOREIGN KEY (id1) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (id2) REFERENCES users (id) ON DELETE CASCADE
);
