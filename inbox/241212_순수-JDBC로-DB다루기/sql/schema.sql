DROP TABLE member IF EXISTS CASCADE;
CREATE TABLE member (
    member_id VARCHAR(10),
    money INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (member_id)
);
INSERT INTO member(member_id, money) VALUES ('hi1', 10000);
INSERT INTO member(member_id, money) VALUES ('hi2', 20000);

SET AUTOCOMMIT TRUE;
SET AUTOCOMMIT FALSE;
