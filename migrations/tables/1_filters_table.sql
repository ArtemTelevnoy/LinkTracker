CREATE TABLE filters
(
    user_id     BIGINT NOT NULL,
    link_id     BIGINT NOT NULL,
    filter_name TEXT   NOT NULL,
    PRIMARY KEY (user_id, link_id, filter_name)
);
