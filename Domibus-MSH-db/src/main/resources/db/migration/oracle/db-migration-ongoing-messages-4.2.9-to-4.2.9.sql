-- ********************************************************************************************************
-- Domibus 4.2.9 to 4.2.9 ongoing messages data migration package
--
-- Main entry point is the procedure 'migrate'. To be executed into a begin/end; anonymous block. Uncomment trailing
-- lines, edit values in the migration anonymous block and execute this full script: package and package body
-- definitions, migration anonymous block.
--
-- Parameters to be adjusted:
-- BULK_COLLECT_LIMIT - limit to avoid reading a high number of records into memory; default value is 100
-- ********************************************************************************************************
CREATE OR REPLACE PACKAGE MIGRATE_ONGOING_MESSAGES_429 IS
    -- limit loading a high number of records into memory
    BULK_COLLECT_LIMIT CONSTANT NUMBER := 100;

    DEFAULT_MIGRATION_START_DATE            CONSTANT TIMESTAMP               := TIMESTAMP '1970-01-01 00:00:00.00';
    DEFAULT_MIGRATION_END_DATE              CONSTANT TIMESTAMP               := SYSTIMESTAMP;

    TYPE T_MIGRATION_DETAILS IS RECORD (
        -- optional, before end date (default: 1st of January 1970 00:00:00.00)
        startDate           TIMESTAMP            := DEFAULT_MIGRATION_START_DATE,

        -- optional (default: SYSTIMESTAMP)
        endDate             TIMESTAMP            := DEFAULT_MIGRATION_END_DATE
    );

    PROCEDURE migrate(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS DEFAULT T_MIGRATION_DETAILS());

END MIGRATE_ONGOING_MESSAGES_429;
/

CREATE OR REPLACE PACKAGE BODY MIGRATE_ONGOING_MESSAGES_429 IS

    TYPE T_LOCAL_TO_REMOTE_PRIMARY_KEYS IS TABLE OF NUMBER INDEX BY VARCHAR2(38);

    FUNCTION lookup_value_safely(lookup_table T_LOCAL_TO_REMOTE_PRIMARY_KEYS, lookup_key NUMBER) RETURN NUMBER IS
        value NUMBER;
    BEGIN
        value := lookup_key;

        IF value IS NOT NULL THEN
            value := lookup_table(TO_CHAR(value));
        END IF;

        RETURN value;
    END lookup_value_safely;

    FUNCTION new_remote_pk(db_link IN VARCHAR2) RETURN NUMBER IS
        remote_pk NUMBER;
    BEGIN
        EXECUTE IMMEDIATE 'SELECT HIBERNATE_SEQUENCE.nextval@' || db_link || ' FROM DUAL' INTO remote_pk;
        RETURN remote_pk;
    END new_remote_pk;

    PROCEDURE migrate_message_log(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_message_log IS
            SELECT DELETED, MESSAGE_ID, MESSAGE_STATUS, MESSAGE_TYPE, MPC, MSH_ROLE, NEXT_ATTEMPT, NOTIFICATION_STATUS, RECEIVED, RESTORED, DOWNLOADED, FAILED, SEND_ATTEMPTS, SEND_ATTEMPTS_MAX, BACKEND, ENDPOINT, MESSAGE_SUBTYPE, SOURCE_MESSAGE, MESSAGE_FRAGMENT, SCHEDULED, VERSION, CREATED_BY, CREATION_TIME
            FROM TB_MESSAGE_LOG
            WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
              AND RECEIVED BETWEEN migration.startDate AND migration.endDate;

        TYPE T_MESSAGE_LOG IS TABLE OF c_message_log%ROWTYPE;
        message_log T_MESSAGE_LOG;
    BEGIN
        dbms_output.put_line('Migrating TB_MESSAGE_LOG entries...');
        OPEN c_message_log;
        LOOP
            FETCH c_message_log BULK COLLECT INTO message_log LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN message_log.COUNT = 0;

            FOR i IN message_log.FIRST .. message_log.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO TB_MESSAGE_LOG@' || db_link || ' (ID_PK, DELETED, MESSAGE_ID, MESSAGE_STATUS, MESSAGE_TYPE, MPC, MSH_ROLE, NEXT_ATTEMPT, NOTIFICATION_STATUS, RECEIVED, RESTORED, DOWNLOADED, FAILED, SEND_ATTEMPTS, SEND_ATTEMPTS_MAX, BACKEND, ENDPOINT, MESSAGE_SUBTYPE, SOURCE_MESSAGE, MESSAGE_FRAGMENT, SCHEDULED, VERSION, CREATED_BY, CREATION_TIME) VALUES (HIBERNATE_SEQUENCE.NEXTVAL@' || db_link || ', :p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12, :p_13, :p_14, :p_15, :p_16, :p_17, :p_18, :p_19, :p_20, :p_21, :p_22, :p_23)'
                        USING message_log(i).DELETED,
                        message_log(i).MESSAGE_ID,
                        message_log(i).MESSAGE_STATUS,
                        message_log(i).MESSAGE_TYPE,
                        message_log(i).MPC,
                        message_log(i).MSH_ROLE,
                        message_log(i).NEXT_ATTEMPT,
                        message_log(i).NOTIFICATION_STATUS,
                        message_log(i).RECEIVED,
                        message_log(i).RESTORED,
                        message_log(i).DOWNLOADED,
                        message_log(i).FAILED,
                        message_log(i).SEND_ATTEMPTS,
                        message_log(i).SEND_ATTEMPTS_MAX,
                        message_log(i).BACKEND,
                        message_log(i).ENDPOINT,
                        message_log(i).MESSAGE_SUBTYPE,
                        message_log(i).SOURCE_MESSAGE,
                        message_log(i).MESSAGE_FRAGMENT,
                        message_log(i).SCHEDULED,
                        message_log(i).VERSION,
                        message_log(i).CREATED_BY,
                        message_log(i).CREATION_TIME;
                END LOOP;
            dbms_output.put_line('Wrote ' || message_log.COUNT || ' records');
        END LOOP;
        CLOSE c_message_log;
    END migrate_message_log;

    PROCEDURE migrate_message_ui(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_message_ui IS
            SELECT MESSAGE_ID, MESSAGE_STATUS, NOTIFICATION_STATUS, MSH_ROLE, MESSAGE_TYPE, DELETED, RECEIVED, SEND_ATTEMPTS, SEND_ATTEMPTS_MAX, NEXT_ATTEMPT, CONVERSATION_ID, FROM_ID, TO_ID, FROM_SCHEME, TO_SCHEME, REF_TO_MESSAGE_ID, FAILED, RESTORED, MESSAGE_SUBTYPE, LAST_MODIFIED, CREATED_BY, CREATION_TIME
            FROM TB_MESSAGE_UI
            WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
              AND RECEIVED BETWEEN migration.startDate AND migration.endDate;

        TYPE T_MESSAGE_UI IS TABLE OF c_message_ui%ROWTYPE;
        message_ui T_MESSAGE_UI;
    BEGIN
        dbms_output.put_line('Migrating TB_MESSAGE_UI entries...');
        OPEN c_message_ui;
        LOOP
            FETCH c_message_ui BULK COLLECT INTO message_ui LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN message_ui.COUNT = 0;

            FOR i IN message_ui.FIRST .. message_ui.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO TB_MESSAGE_UI@' || db_link || ' (ID_PK, MESSAGE_ID, MESSAGE_STATUS, NOTIFICATION_STATUS, MSH_ROLE, MESSAGE_TYPE, DELETED, RECEIVED, SEND_ATTEMPTS, SEND_ATTEMPTS_MAX, NEXT_ATTEMPT, CONVERSATION_ID, FROM_ID, TO_ID, FROM_SCHEME, TO_SCHEME, REF_TO_MESSAGE_ID, FAILED, RESTORED, MESSAGE_SUBTYPE, LAST_MODIFIED, CREATED_BY, CREATION_TIME) VALUES (HIBERNATE_SEQUENCE.NEXTVAL@' || db_link || ', :p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12, :p_13, :p_14, :p_15, :p_16, :p_17, :p_18, :p_19, :p_20, :p_21, :p_22)'
                        USING message_ui(i).MESSAGE_ID,
                        message_ui(i).MESSAGE_STATUS,
                        message_ui(i).NOTIFICATION_STATUS,
                        message_ui(i).MSH_ROLE,
                        message_ui(i).MESSAGE_TYPE,
                        message_ui(i).DELETED,
                        message_ui(i).RECEIVED,
                        message_ui(i).SEND_ATTEMPTS,
                        message_ui(i).SEND_ATTEMPTS_MAX,
                        message_ui(i).NEXT_ATTEMPT,
                        message_ui(i).CONVERSATION_ID,
                        message_ui(i).FROM_ID,
                        message_ui(i).TO_ID,
                        message_ui(i).FROM_SCHEME,
                        message_ui(i).TO_SCHEME,
                        message_ui(i).REF_TO_MESSAGE_ID,
                        message_ui(i).FAILED,
                        message_ui(i).RESTORED,
                        message_ui(i).MESSAGE_SUBTYPE,
                        message_ui(i).LAST_MODIFIED,
                        message_ui(i).CREATED_BY,
                        message_ui(i).CREATION_TIME;
                END LOOP;
            dbms_output.put_line('Wrote ' || message_ui.COUNT || ' records');
        END LOOP;
        CLOSE c_message_ui;
    END migrate_message_ui;

    FUNCTION migrate_message_info(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) RETURN T_LOCAL_TO_REMOTE_PRIMARY_KEYS IS
        CURSOR c_message_info IS
            SELECT ID_PK, MESSAGE_ID, REF_TO_MESSAGE_ID, TIME_STAMP, CREATED_BY, CREATION_TIME
            FROM TB_MESSAGE_INFO
            WHERE MESSAGE_ID IN (
                SELECT MESSAGE_ID
                FROM TB_MESSAGE_LOG
                WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                  AND RECEIVED BETWEEN migration.startDate AND migration.endDate
            );

        TYPE T_MESSAGE_INFO IS TABLE OF c_message_info%ROWTYPE;
        message_info T_MESSAGE_INFO;

        remote_id TB_MESSAGE_INFO.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
        localToRemotePks T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
    BEGIN
        dbms_output.put_line('Migrating TB_MESSAGE_INFO entries...');

        OPEN c_message_info;
        LOOP
            FETCH c_message_info BULK COLLECT INTO message_info LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN message_info.COUNT = 0;

            FOR i IN message_info.FIRST .. message_info.LAST LOOP
                    remote_id := new_remote_pk(db_link);

                    EXECUTE IMMEDIATE 'INSERT INTO TB_MESSAGE_INFO@' || db_link || ' (ID_PK, MESSAGE_ID, REF_TO_MESSAGE_ID, TIME_STAMP, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6)'
                        USING remote_id,
                        message_info(i).MESSAGE_ID,
                        message_info(i).REF_TO_MESSAGE_ID,
                        message_info(i).TIME_STAMP,
                        message_info(i).CREATED_BY,
                        message_info(i).CREATION_TIME;

                    localToRemotePks(TO_CHAR(message_info(i).ID_PK)) := remote_id;

                    dbms_output.put_line('Local to remote mapping: TB_MESSAGE_INFO[' || message_info(i).ID_PK || '] = ' || remote_id);
                END LOOP;
            dbms_output.put_line('Wrote ' || message_info.COUNT || ' records');
        END LOOP;
        CLOSE c_message_info;

        RETURN localToRemotePks;
    END migrate_message_info;

    FUNCTION migrate_user_message(messageInfoLookupTable IN T_LOCAL_TO_REMOTE_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) RETURN T_LOCAL_TO_REMOTE_PRIMARY_KEYS IS
        CURSOR c_user_message IS
            SELECT ID_PK, COLLABORATION_INFO_ACTION, AGREEMENT_REF_PMODE, AGREEMENT_REF_TYPE, AGREEMENT_REF_VALUE, COLL_INFO_CONVERS_ID, SERVICE_TYPE, SERVICE_VALUE, MPC, FROM_ROLE, TO_ROLE, MESSAGEINFO_ID_PK, FK_MESSAGE_FRAGMENT_ID, SPLIT_AND_JOIN, CREATED_BY, CREATION_TIME
            FROM TB_USER_MESSAGE
            WHERE MESSAGEINFO_ID_PK IN (
                SELECT ID_PK
                FROM TB_MESSAGE_INFO
                WHERE MESSAGE_ID IN (
                    SELECT MESSAGE_ID
                    FROM TB_MESSAGE_LOG
                    WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                      AND RECEIVED BETWEEN migration.startDate AND migration.endDate
                )
            );

        TYPE T_USER_MESSAGE IS TABLE OF c_user_message%ROWTYPE;
        user_message T_USER_MESSAGE;

        remote_id TB_USER_MESSAGE.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
        localToRemotePks T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
    BEGIN
        dbms_output.put_line('Migrating TB_USER_MESSAGE entries...');

        OPEN c_user_message;
        LOOP
            FETCH c_user_message BULK COLLECT INTO user_message LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN user_message.COUNT = 0;

            FOR i IN user_message.FIRST .. user_message.LAST LOOP
                    remote_id := new_remote_pk(db_link);

                    EXECUTE IMMEDIATE 'INSERT INTO TB_USER_MESSAGE@' || db_link || ' (ID_PK, COLLABORATION_INFO_ACTION, AGREEMENT_REF_PMODE, AGREEMENT_REF_TYPE, AGREEMENT_REF_VALUE, COLL_INFO_CONVERS_ID, SERVICE_TYPE, SERVICE_VALUE, MPC, FROM_ROLE, TO_ROLE, MESSAGEINFO_ID_PK, FK_MESSAGE_FRAGMENT_ID, SPLIT_AND_JOIN, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12, :p_13, :p_14, :p_15, :p_16)'
                        USING remote_id,
                        user_message(i).COLLABORATION_INFO_ACTION,
                        user_message(i).AGREEMENT_REF_PMODE,
                        user_message(i).AGREEMENT_REF_TYPE,
                        user_message(i).AGREEMENT_REF_VALUE,
                        user_message(i).COLL_INFO_CONVERS_ID,
                        user_message(i).SERVICE_TYPE,
                        user_message(i).SERVICE_VALUE,
                        user_message(i).MPC,
                        user_message(i).FROM_ROLE,
                        user_message(i).TO_ROLE,
                        messageInfoLookupTable(TO_CHAR(user_message(i).MESSAGEINFO_ID_PK)),
                        user_message(i).FK_MESSAGE_FRAGMENT_ID,
                        user_message(i).SPLIT_AND_JOIN,
                        user_message(i).CREATED_BY,
                        user_message(i).CREATION_TIME;

                    localToRemotePks(TO_CHAR(user_message(i).ID_PK)) := remote_id;

                    dbms_output.put_line('Local to remote mapping: TB_USER_MESSAGE[' || user_message(i).ID_PK || '] = ' || remote_id);
                END LOOP;
            dbms_output.put_line('Wrote ' || user_message.COUNT || ' records');
        END LOOP;
        CLOSE c_user_message;

        RETURN localToRemotePks;
    END migrate_user_message;

    PROCEDURE migrate_error(signalMessageLookupTable IN T_LOCAL_TO_REMOTE_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_error IS
            SELECT ID_PK, CATEGORY, DESCRIPTION_LANG, DESCRIPTION_VALUE, ERROR_CODE, ERROR_DETAIL, ORIGIN, REF_TO_MESSAGE_ID, SEVERITY, SHORT_DESCRIPTION, SIGNALMESSAGE_ID, CREATED_BY, CREATION_TIME
            FROM TB_ERROR
            WHERE SIGNALMESSAGE_ID IN (
                SELECT ID_PK
                FROM TB_SIGNAL_MESSAGE
                WHERE MESSAGEINFO_ID_PK IN (
                    SELECT ID_PK
                    FROM TB_MESSAGE_INFO
                    WHERE MESSAGE_ID IN (
                        SELECT MESSAGE_ID
                        FROM TB_MESSAGE_LOG
                        WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                          AND RECEIVED BETWEEN migration.startDate AND migration.endDate
                    )
                )
            );

        TYPE T_ERROR IS TABLE OF c_error%ROWTYPE;
        error T_ERROR;
    BEGIN
        dbms_output.put_line('Migrating TB_ERROR entries...');

        OPEN c_error;
        LOOP
            FETCH c_error BULK COLLECT INTO error LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN error.COUNT = 0;

            FOR i IN error.FIRST .. error.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO TB_ERROR@' || db_link || ' (ID_PK, CATEGORY, DESCRIPTION_LANG, DESCRIPTION_VALUE, ERROR_CODE, ERROR_DETAIL, ORIGIN, REF_TO_MESSAGE_ID, SEVERITY, SHORT_DESCRIPTION, SIGNALMESSAGE_ID, CREATED_BY, CREATION_TIME) VALUES (HIBERNATE_SEQUENCE.nextval@' || db_link || ', :p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12)'
                        USING error(i).CATEGORY,
                        error(i).DESCRIPTION_LANG,
                        error(i).DESCRIPTION_VALUE,
                        error(i).ERROR_CODE,
                        error(i).ERROR_DETAIL,
                        error(i).ORIGIN,
                        error(i).REF_TO_MESSAGE_ID,
                        error(i).SEVERITY,
                        error(i).SHORT_DESCRIPTION,
                        signalMessageLookupTable(TO_CHAR(error(i).SIGNALMESSAGE_ID)),
                        error(i).CREATED_BY,
                        error(i).CREATION_TIME;
                END LOOP;
            dbms_output.put_line('Wrote ' || error.COUNT || ' records');
        END LOOP;
        CLOSE c_error;
    END migrate_error;

    PROCEDURE migrate_rawenvelope_log(userMessageLookupTable IN T_LOCAL_TO_REMOTE_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_raw_envelope_log IS
            SELECT ID_PK, USERMESSAGE_ID_FK, SIGNALMESSAGE_ID_FK, MESSAGE_ID, CREATED_BY, CREATION_TIME
            FROM TB_RAWENVELOPE_LOG
            WHERE USERMESSAGE_ID_FK IN (
                SELECT ID_PK
                FROM TB_USER_MESSAGE
                WHERE MESSAGEINFO_ID_PK IN (
                    SELECT ID_PK
                    FROM TB_MESSAGE_INFO
                    WHERE MESSAGE_ID IN (
                        SELECT MESSAGE_ID
                        FROM TB_MESSAGE_LOG
                        WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                          AND RECEIVED BETWEEN migration.startDate AND migration.endDate
                    )
                )
            );

        TYPE T_RAW_ENVELOPE_LOG IS TABLE OF c_raw_envelope_log%ROWTYPE;
        raw_envelope_log T_RAW_ENVELOPE_LOG;

        remote_id TB_RAWENVELOPE_LOG.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
        a_null CHAR(1);
    BEGIN
        dbms_output.put_line('Migrating TB_RAWENVELOPE_LOG entries...');

        OPEN c_raw_envelope_log;

        LOOP
            FETCH c_raw_envelope_log BULK COLLECT INTO raw_envelope_log LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN raw_envelope_log.COUNT = 0;

            FOR i IN raw_envelope_log.FIRST .. raw_envelope_log.LAST LOOP
                    remote_id := new_remote_pk(db_link);

                    EXECUTE IMMEDIATE 'INSERT INTO TB_RAWENVELOPE_LOG@' || db_link || ' (ID_PK, USERMESSAGE_ID_FK, SIGNALMESSAGE_ID_FK, MESSAGE_ID, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6)'
                        USING remote_id,
                        lookup_value_safely(userMessageLookupTable, raw_envelope_log(i).USERMESSAGE_ID_FK),
                        a_null,
                        raw_envelope_log(i).MESSAGE_ID,
                        raw_envelope_log(i).CREATED_BY,
                        raw_envelope_log(i).CREATION_TIME;

                    -- CLOBs are special when copying
                    EXECUTE IMMEDIATE 'UPDATE TB_RAWENVELOPE_LOG@' || db_link || ' SET RAW_XML = (SELECT RAW_XML FROM TB_RAWENVELOPE_LOG WHERE ID_PK = :p_1) WHERE ID_PK = :p_2'
                        USING raw_envelope_log(i).ID_PK,
                        remote_id;
                END LOOP;
            dbms_output.put_line('Wrote ' || raw_envelope_log.COUNT || ' records');
        END LOOP;
        CLOSE c_raw_envelope_log;
    END migrate_rawenvelope_log;

    FUNCTION migrate_part_info(userMessageLookupTable IN T_LOCAL_TO_REMOTE_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) RETURN T_LOCAL_TO_REMOTE_PRIMARY_KEYS IS
        CURSOR c_part_info IS
            SELECT ID_PK, DESCRIPTION_LANG, DESCRIPTION_VALUE, HREF, IN_BODY, SCHEMA_LOCATION, SCHEMA_NAMESPACE, SCHEMA_VERSION, PAYLOADINFO_ID, FILENAME, MIME, PART_ORDER, ENCRYPTED, CREATED_BY, CREATION_TIME
            FROM TB_PART_INFO
            WHERE PAYLOADINFO_ID IN (
                SELECT ID_PK
                FROM TB_USER_MESSAGE
                WHERE MESSAGEINFO_ID_PK IN (
                    SELECT ID_PK
                    FROM TB_MESSAGE_INFO
                    WHERE MESSAGE_ID IN (
                        SELECT MESSAGE_ID
                        FROM TB_MESSAGE_LOG
                        WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                          AND RECEIVED BETWEEN migration.startDate AND migration.endDate
                    )
                )
            );

        TYPE T_PART_INFO IS TABLE OF c_part_info%ROWTYPE;
        part_info T_PART_INFO;

        remote_id TB_PART_INFO.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
        localToRemotePks T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
    BEGIN
        dbms_output.put_line('Migrating TB_PART_INFO entries...');

        OPEN c_part_info;
        LOOP
            FETCH c_part_info BULK COLLECT INTO part_info LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN part_info.COUNT = 0;

            FOR i IN part_info.FIRST .. part_info.LAST LOOP
                    remote_id := new_remote_pk(db_link);

                    EXECUTE IMMEDIATE 'INSERT INTO TB_PART_INFO@' || db_link || ' (ID_PK, DESCRIPTION_LANG, DESCRIPTION_VALUE, HREF, IN_BODY, SCHEMA_LOCATION, SCHEMA_NAMESPACE, SCHEMA_VERSION, PAYLOADINFO_ID, FILENAME, MIME, PART_ORDER, ENCRYPTED, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12, :p_13, :p_14, :p_15)'
                        USING remote_id,
                        part_info(i).DESCRIPTION_LANG,
                        part_info(i).DESCRIPTION_VALUE,
                        part_info(i).HREF,
                        part_info(i).IN_BODY,
                        part_info(i).SCHEMA_LOCATION,
                        part_info(i).SCHEMA_NAMESPACE,
                        part_info(i).SCHEMA_VERSION,
                        userMessageLookupTable(TO_CHAR(part_info(i).PAYLOADINFO_ID)),
                        part_info(i).FILENAME,
                        part_info(i).MIME,
                        part_info(i).PART_ORDER,
                        part_info(i).ENCRYPTED,
                        part_info(i).CREATED_BY,
                        part_info(i).CREATION_TIME;

                    -- BLOBs are special when copying
                    EXECUTE IMMEDIATE 'UPDATE TB_PART_INFO@' || db_link || ' SET BINARY_DATA = (SELECT BINARY_DATA FROM TB_PART_INFO WHERE ID_PK = :p_1) WHERE ID_PK = :p_2'
                        USING part_info(i).ID_PK,
                        remote_id;

                    localToRemotePks(TO_CHAR(part_info(i).ID_PK)) := remote_id;

                    dbms_output.put_line('Local to remote mapping: TB_PART_INFO[' || part_info(i).ID_PK || '] = ' || remote_id);
                END LOOP;
            dbms_output.put_line('Wrote ' || part_info.COUNT || ' records');
        END LOOP;
        CLOSE c_part_info;

        RETURN localToRemotePks;
    END migrate_part_info;

    PROCEDURE migrate_property(userMessageLookupTable IN T_LOCAL_TO_REMOTE_PRIMARY_KEYS, partInfoLookupTable IN T_LOCAL_TO_REMOTE_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_property IS
            SELECT ID_PK, NAME, VALUE, MESSAGEPROPERTIES_ID, PARTPROPERTIES_ID, "TYPE", CREATED_BY, CREATION_TIME
            FROM TB_PROPERTY
            WHERE MESSAGEPROPERTIES_ID IN (
                SELECT ID_PK
                FROM TB_USER_MESSAGE
                WHERE MESSAGEINFO_ID_PK IN (
                    SELECT ID_PK
                    FROM TB_MESSAGE_INFO
                    WHERE MESSAGE_ID IN (
                        SELECT MESSAGE_ID
                        FROM TB_MESSAGE_LOG
                        WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                          AND RECEIVED BETWEEN migration.startDate AND migration.endDate
                    )
                )
            )
            UNION ALL
            SELECT ID_PK, NAME, VALUE, MESSAGEPROPERTIES_ID, PARTPROPERTIES_ID, "TYPE", CREATED_BY, CREATION_TIME
            FROM TB_PROPERTY
            WHERE PARTPROPERTIES_ID IN (
                SELECT ID_PK
                FROM TB_PART_INFO
                WHERE PAYLOADINFO_ID IN (
                    SELECT ID_PK
                    FROM TB_USER_MESSAGE
                    WHERE MESSAGEINFO_ID_PK IN (
                        SELECT ID_PK
                        FROM TB_MESSAGE_INFO
                        WHERE MESSAGE_ID IN (
                            SELECT MESSAGE_ID
                            FROM TB_MESSAGE_LOG
                            WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                              AND RECEIVED BETWEEN migration.startDate AND migration.endDate
                        )
                    )
                )
            );

        TYPE T_PROPERTY IS TABLE OF c_property%ROWTYPE;
        property T_PROPERTY;
    BEGIN
        dbms_output.put_line('Migrating TB_PROPERTY entries...');

        OPEN c_property;
        LOOP
            FETCH c_property BULK COLLECT INTO property LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN property.COUNT = 0;

            FOR i IN property.FIRST .. property.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO TB_PROPERTY@' || db_link || ' (ID_PK, NAME, VALUE, MESSAGEPROPERTIES_ID, PARTPROPERTIES_ID, TYPE, CREATED_BY, CREATION_TIME) VALUES (HIBERNATE_SEQUENCE.nextval@' || db_link || ', :p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7)'
                        USING property(i).NAME,
                        property(i).VALUE,
                        lookup_value_safely(userMessageLookupTable, property(i).MESSAGEPROPERTIES_ID),
                        lookup_value_safely(partInfoLookupTable, property(i).PARTPROPERTIES_ID),
                        property(i).TYPE,
                        property(i).CREATED_BY,
                        property(i).CREATION_TIME;
                END LOOP;
            dbms_output.put_line('Wrote ' || property.COUNT || ' records');
        END LOOP;
        CLOSE c_property;
    END migrate_property;

    PROCEDURE migrate_party_id(userMessageLookupTable IN T_LOCAL_TO_REMOTE_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_party_id IS
            SELECT ID_PK, "TYPE", VALUE, TO_ID, FROM_ID, CREATED_BY, CREATION_TIME
            FROM TB_PARTY_ID
            WHERE TO_ID IN (
                SELECT ID_PK
                FROM TB_USER_MESSAGE
                WHERE MESSAGEINFO_ID_PK IN (
                    SELECT ID_PK
                    FROM TB_MESSAGE_INFO
                    WHERE MESSAGE_ID IN (
                        SELECT MESSAGE_ID
                        FROM TB_MESSAGE_LOG
                        WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                          AND RECEIVED BETWEEN migration.startDate AND migration.endDate
                    )
                )
            )
            UNION ALL
            SELECT ID_PK, "TYPE", VALUE, TO_ID, FROM_ID, CREATED_BY, CREATION_TIME
            FROM TB_PARTY_ID
            WHERE FROM_ID IN (
                SELECT ID_PK
                FROM TB_USER_MESSAGE
                WHERE MESSAGEINFO_ID_PK IN (
                    SELECT ID_PK
                    FROM TB_MESSAGE_INFO
                    WHERE MESSAGE_ID IN (
                        SELECT MESSAGE_ID
                        FROM TB_MESSAGE_LOG
                        WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                          AND RECEIVED BETWEEN migration.startDate AND migration.endDate
                    )
                )
            );

        TYPE T_PARTY_ID IS TABLE OF c_party_id%ROWTYPE;
        party_id T_PARTY_ID;
    BEGIN
        dbms_output.put_line('Migrating TB_PARTY_ID entries...');

        OPEN c_party_id;
        LOOP
            FETCH c_party_id BULK COLLECT INTO party_id LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN party_id.COUNT = 0;

            FOR i IN party_id.FIRST .. party_id.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO TB_PARTY_ID@' || db_link || ' (ID_PK, TYPE, VALUE, TO_ID, FROM_ID, CREATED_BY, CREATION_TIME) VALUES (HIBERNATE_SEQUENCE.nextval@' || db_link || ', :p_1, :p_2, :p_3, :p_4, :p_5, :p_6)'
                        USING party_id(i).TYPE,
                        party_id(i).VALUE,
                        lookup_value_safely(userMessageLookupTable, party_id(i).TO_ID),
                        lookup_value_safely(userMessageLookupTable, party_id(i).FROM_ID),
                        party_id(i).CREATED_BY,
                        party_id(i).CREATION_TIME;
                END LOOP;
            dbms_output.put_line('Wrote ' || party_id.COUNT || ' records');
        END LOOP;
        CLOSE c_party_id;
    END migrate_party_id;

    PROCEDURE migrate_error_log(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_error_log IS
            SELECT ID_PK, ERROR_CODE, ERROR_DETAIL, ERROR_SIGNAL_MESSAGE_ID, MESSAGE_IN_ERROR_ID, MSH_ROLE, NOTIFIED, TIME_STAMP, CREATED_BY, CREATION_TIME
            FROM TB_ERROR_LOG
            WHERE MESSAGE_IN_ERROR_ID IN (
                SELECT MESSAGE_ID
                FROM TB_MESSAGE_LOG
                WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                  AND RECEIVED BETWEEN migration.startDate AND migration.endDate
            );

        TYPE T_ERROR_LOG IS TABLE OF c_error_log%ROWTYPE;
        error_log T_ERROR_LOG;
    BEGIN
        dbms_output.put_line('Migrating TB_ERROR_LOG entries...');

        OPEN c_error_log;
        LOOP
            FETCH c_error_log BULK COLLECT INTO error_log LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN error_log.COUNT = 0;

            FOR i IN error_log.FIRST .. error_log.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO TB_ERROR_LOG@' || db_link || ' (ID_PK, ERROR_CODE, ERROR_DETAIL, ERROR_SIGNAL_MESSAGE_ID, MESSAGE_IN_ERROR_ID, MSH_ROLE, NOTIFIED, TIME_STAMP, CREATED_BY, CREATION_TIME) VALUES (HIBERNATE_SEQUENCE.nextval@' || db_link || ', :p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9)'
                        USING error_log(i).ERROR_CODE,
                        error_log(i).ERROR_DETAIL,
                        error_log(i).ERROR_SIGNAL_MESSAGE_ID,
                        error_log(i).MESSAGE_IN_ERROR_ID,
                        error_log(i).MSH_ROLE,
                        error_log(i).NOTIFIED,
                        error_log(i).TIME_STAMP,
                        error_log(i).CREATED_BY,
                        error_log(i).CREATION_TIME;
                END LOOP;
            dbms_output.put_line('Wrote ' || error_log.COUNT || ' records');
        END LOOP;
        CLOSE c_error_log;
    END migrate_error_log;

    PROCEDURE migrate_messaging(userMessageLookupTable IN T_LOCAL_TO_REMOTE_PRIMARY_KEYS, db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_messaging IS
            SELECT ID_PK, ID, SIGNAL_MESSAGE_ID, USER_MESSAGE_ID, CREATED_BY, CREATION_TIME
            FROM TB_MESSAGING
            WHERE USER_MESSAGE_ID IN (
                SELECT ID_PK
                FROM TB_USER_MESSAGE
                WHERE MESSAGEINFO_ID_PK IN (
                    SELECT ID_PK
                    FROM TB_MESSAGE_INFO
                    WHERE MESSAGE_ID IN (
                        SELECT MESSAGE_ID
                        FROM TB_MESSAGE_LOG
                        WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                          AND RECEIVED BETWEEN migration.startDate AND migration.endDate
                    )
                )
            );

        TYPE T_MESSAGING IS TABLE OF c_messaging%ROWTYPE;
        messaging T_MESSAGING;
        a_null CHAR(1);
    BEGIN
        dbms_output.put_line('Migrating TB_MESSAGING entries...');

        OPEN c_messaging;
        LOOP
            FETCH c_messaging BULK COLLECT INTO messaging LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN messaging.COUNT = 0;

            FOR i IN messaging.FIRST .. messaging.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO TB_MESSAGING@' || db_link || ' (ID_PK, ID, SIGNAL_MESSAGE_ID, USER_MESSAGE_ID, CREATED_BY, CREATION_TIME) VALUES (HIBERNATE_SEQUENCE.nextval@' || db_link || ', :p_1, :p_2, :p_3, :p_4, :p_5)'
                        USING messaging(i).ID,
                        a_null,
                        lookup_value_safely(userMessageLookupTable, messaging(i).USER_MESSAGE_ID),
                        messaging(i).CREATED_BY,
                        messaging(i).CREATION_TIME;

                END LOOP;
            dbms_output.put_line('Wrote ' || messaging.COUNT || ' records');
        END LOOP;
        CLOSE c_messaging;
    END migrate_messaging;

    PROCEDURE migrate_action_audit(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_action_audit IS
            SELECT ID_PK, AUDIT_TYPE, ENTITY_ID, MODIFICATION_TYPE, REVISION_DATE, USER_NAME, FROM_QUEUE, TO_QUEUE, CREATED_BY, CREATION_TIME
            FROM TB_ACTION_AUDIT
            WHERE ENTITY_ID IN (
                SELECT MESSAGE_ID
                FROM TB_MESSAGE_LOG
                WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                  AND RECEIVED BETWEEN migration.startDate AND migration.endDate
            );

        TYPE T_ACTION_AUDIT IS TABLE OF c_action_audit%ROWTYPE;
        action_audit T_ACTION_AUDIT;
    BEGIN
        dbms_output.put_line('Migrating TB_ACTION_AUDIT entries...');

        OPEN c_action_audit;
        LOOP
            FETCH c_action_audit BULK COLLECT INTO action_audit LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN action_audit.COUNT = 0;

            FOR i IN action_audit.FIRST .. action_audit.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO TB_ACTION_AUDIT@' || db_link || ' (ID_PK, AUDIT_TYPE, ENTITY_ID, MODIFICATION_TYPE, REVISION_DATE, USER_NAME, FROM_QUEUE, TO_QUEUE, CREATED_BY, CREATION_TIME) VALUES (HIBERNATE_SEQUENCE.nextval@' || db_link || ', :p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9)'
                        USING action_audit(i).AUDIT_TYPE,
                        action_audit(i).ENTITY_ID,
                        action_audit(i).MODIFICATION_TYPE,
                        action_audit(i).REVISION_DATE,
                        action_audit(i).USER_NAME,
                        action_audit(i).FROM_QUEUE,
                        action_audit(i).TO_QUEUE,
                        action_audit(i).CREATED_BY,
                        action_audit(i).CREATION_TIME;
                END LOOP;
            dbms_output.put_line('Wrote ' || action_audit.COUNT || ' records');
        END LOOP;
        CLOSE c_action_audit;
    END migrate_action_audit;

    PROCEDURE migrate_send_attempt(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_send_attempt IS
            SELECT ID_PK, MESSAGE_ID, START_DATE, END_DATE, STATUS, ERROR, CREATED_BY, CREATION_TIME
            FROM TB_SEND_ATTEMPT
            WHERE MESSAGE_ID IN (
                SELECT MESSAGE_ID
                FROM TB_MESSAGE_LOG
                WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                  AND RECEIVED BETWEEN migration.startDate AND migration.endDate
            );

        TYPE T_SEND_ATTEMPT IS TABLE OF c_send_attempt%ROWTYPE;
        send_attempt T_SEND_ATTEMPT;
    BEGIN
        dbms_output.put_line('Migrating TB_SEND_ATTEMPT entries...');

        OPEN c_send_attempt;
        LOOP
            FETCH c_send_attempt BULK COLLECT INTO send_attempt LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN send_attempt.COUNT = 0;

            FOR i IN send_attempt.FIRST .. send_attempt.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO TB_SEND_ATTEMPT@' || db_link || ' (ID_PK, MESSAGE_ID, START_DATE, END_DATE, STATUS, ERROR, CREATED_BY, CREATION_TIME) VALUES (HIBERNATE_SEQUENCE.nextval@' || db_link || ', :p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7)'
                        USING send_attempt(i).MESSAGE_ID,
                        send_attempt(i).START_DATE,
                        send_attempt(i).END_DATE,
                        send_attempt(i).STATUS,
                        send_attempt(i).ERROR,
                        send_attempt(i).CREATED_BY,
                        send_attempt(i).CREATION_TIME;
                END LOOP;
            dbms_output.put_line('Wrote ' || send_attempt.COUNT || ' records');
        END LOOP;
        CLOSE c_send_attempt;
    END migrate_send_attempt;

    PROCEDURE migrate_messaging_lock(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_messaging_lock IS
            SELECT ID_PK, MESSAGE_TYPE, MESSAGE_RECEIVED, MESSAGE_STATE, MESSAGE_ID, INITIATOR, MPC, SEND_ATTEMPTS, SEND_ATTEMPTS_MAX, NEXT_ATTEMPT, MESSAGE_STALED, CREATED_BY, CREATION_TIME
            FROM TB_MESSAGING_LOCK
            WHERE MESSAGE_ID IN (
                SELECT MESSAGE_ID
                FROM TB_MESSAGE_LOG
                WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                  AND RECEIVED BETWEEN migration.startDate AND migration.endDate
            );

        TYPE T_MESSAGING_LOCK IS TABLE OF c_messaging_lock%ROWTYPE;
        messaging_lock T_MESSAGING_LOCK;
    BEGIN
        dbms_output.put_line('Migrating TB_MESSAGING_LOCK entries...');

        OPEN c_messaging_lock;
        LOOP
            FETCH c_messaging_lock BULK COLLECT INTO messaging_lock LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN messaging_lock.COUNT = 0;

            FOR i IN messaging_lock.FIRST .. messaging_lock.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO TB_MESSAGING_LOCK@' || db_link || ' (ID_PK, MESSAGE_TYPE, MESSAGE_RECEIVED, MESSAGE_STATE, MESSAGE_ID, INITIATOR, MPC, SEND_ATTEMPTS, SEND_ATTEMPTS_MAX, NEXT_ATTEMPT, MESSAGE_STALED, CREATED_BY, CREATION_TIME) VALUES (HIBERNATE_SEQUENCE.nextval@' || db_link || ', :p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12)'
                        USING messaging_lock(i).MESSAGE_TYPE,
                        messaging_lock(i).MESSAGE_RECEIVED,
                        messaging_lock(i).MESSAGE_STATE,
                        messaging_lock(i).MESSAGE_ID,
                        messaging_lock(i).INITIATOR,
                        messaging_lock(i).MPC,
                        messaging_lock(i).SEND_ATTEMPTS,
                        messaging_lock(i).SEND_ATTEMPTS_MAX,
                        messaging_lock(i).NEXT_ATTEMPT,
                        messaging_lock(i).MESSAGE_STALED,
                        messaging_lock(i).CREATED_BY,
                        messaging_lock(i).CREATION_TIME;
                END LOOP;
            dbms_output.put_line('Wrote ' || messaging_lock.COUNT || ' records');
        END LOOP;
        CLOSE c_messaging_lock;
    END migrate_messaging_lock;

    PROCEDURE migrate(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS DEFAULT T_MIGRATION_DETAILS()) IS
        messageInfoLookupTable T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
        userMessageLookupTable T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
        partInfoLookupTable T_LOCAL_TO_REMOTE_PRIMARY_KEYS;
    BEGIN
        migrate_message_log(db_link, migration);
        migrate_message_ui(db_link, migration);
        messageInfoLookupTable := migrate_message_info(db_link, migration);
        userMessageLookupTable := migrate_user_message(messageInfoLookupTable, db_link, migration);
        migrate_rawenvelope_log(userMessageLookupTable, db_link, migration);
        partInfoLookupTable := migrate_part_info(userMessageLookupTable, db_link, migration);
        migrate_property(userMessageLookupTable, partInfoLookupTable,db_link, migration);
        migrate_party_id(userMessageLookupTable, db_link, migration);
        migrate_error_log(db_link, migration);
        migrate_messaging(userMessageLookupTable, db_link, migration);
        migrate_action_audit(db_link, migration);
        migrate_send_attempt(db_link, migration);
        migrate_messaging_lock(db_link, migration);

        dbms_output.put_line('Done');
        dbms_output.put_line('Please review the changes and either COMMIT them or ROLLBACK!');
    END migrate;

END MIGRATE_ONGOING_MESSAGES_429;
/

--
-- Uncomment trailing line to execute the MIGRATE_ONGOING_MESSAGES_429.MIGRATE(..) procedure
-- Note: COMMIT or ROLLBACK at the end or immediately after calling it (if you uncomment the automatic COMMIT)
--
--declare
--     DB_LINK VARCHAR2(4000);
--     MIGRATION MIGRATE_ONGOING_MESSAGES_429.T_MIGRATION_DETAILS;
-- begin
--     -- Use the correct database link
--     DB_LINK := 'DATABASE_LINK_NAME_v429';
--
--     -- Uncomment to use custom start and end date values
--     --MIGRATION.startDate := TIMESTAMP '2021-03-11 00:00:00.01';
--     --MIGRATION.endDate := TIMESTAMP '2021-03-11 23:59:59.99';
--
--     MIGRATE_ONGOING_MESSAGES_429.MIGRATE(
--             DB_LINK => DB_LINK,
--             MIGRATION => MIGRATION
--     );
--
--     -- Uncomment to automatically COMMIT
-- --     COMMIT;
-- end;
-- /