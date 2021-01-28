
CREATE GLOBAL TEMPORARY TABLE SENT_TEMP_MESSAGE_ID (
id_pk NUMBER(38)
)
ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY TABLE SENT_TEMP_SIGNAL_ID (
id_pk NUMBER(38),
receipt_id_pk NUMBER(38),
message_id VARCHAR(255),
minfo_pk NUMBER(38)
)
ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY TABLE SENT_TEMP_USER_MESSAGE_ID (
id_pk NUMBER(38),
message_id VARCHAR(255),
minfo_pk NUMBER(38)
)
ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY TABLE SENT_TEMP_PART_ID (
id_pk NUMBER(38)
)
ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY TABLE DOWNLOADED_TEMP_MESSAGE_ID (
id_pk NUMBER(38)
)
ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY TABLE DOWNLOADED_TEMP_SIGNAL_ID (
id_pk NUMBER(38),
receipt_id_pk NUMBER(38),
message_id VARCHAR(255),
minfo_pk NUMBER(38)
)
ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY TABLE DOWNLOADED_TEMP_USER_MESSAGE_ID (
id_pk NUMBER(38),
message_id VARCHAR(255),
minfo_pk NUMBER(38)
)
ON COMMIT DELETE ROWS;

CREATE GLOBAL TEMPORARY TABLE DOWNLOADED_TEMP_PART_ID (
id_pk NUMBER(38)
)
ON COMMIT DELETE ROWS;