CREATE TABLE user_links
(
    user_id BIGINT,
    link_id BIGINT,
    PRIMARY KEY (user_id, link_id),
    FOREIGN KEY (user_id) REFERENCES Users (user_id),
    FOREIGN KEY (link_id) REFERENCES Links (link_id)
);
