CREATE TABLE tags
(
    user_id  BIGINT NOT NULL,
    tag_name TEXT   NOT NULL,
    link_id  BIGINT NOT NULL,
    PRIMARY KEY (user_id, tag_name, link_id)
);
