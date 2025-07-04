CREATE TABLE links
(
    link_id     BIGSERIAL PRIMARY KEY,
    url         TEXT NOT NULL UNIQUE,
    update_time TIMESTAMPTZ NOT NULL,
    is_github BOOLEAN NOT NULL
);
