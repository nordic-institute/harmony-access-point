-- ********************************************************************************************************
-- Domibus 5.0to 5.0 ongoing messages data migration package
--
-- Main entry point is the procedure 'migrate'. To be executed into a begin/end; block
--
-- Parameters to be adjusted:
-- BULK_COLLECT_LIMIT - limit to avoid reading a high number of records into memory; default value is 100
-- ********************************************************************************************************

-- prepare migration PKs tables
DECLARE
    table_does_not_exist exception;
    PRAGMA EXCEPTION_INIT(table_does_not_exist, -942);
BEGIN
    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_MESSAGE_STATUS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_MESSAGE_STATUS: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PARTY';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PARTY: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PART_PROPERTY';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PART_PROPERTY: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_MPC';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_MPC: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_ROLE';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_ROLE: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_SERVICE';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_SERVICE: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_AGREEMENT';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_AGREEMENT: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_ACTION';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_ACTION: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_MSH_ROLE';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_MSH_ROLE: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_TIMEZONE_OFFSET';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_TIMEZONE_OFFSET: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_NOTIFIC_STATUS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_NOTIFIC_STATUS: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_MESSAGE_PROPERTY';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_MESSAGE_PROPERTY: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_MESSAGE_STATUS CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_MESSAGE_STATUS: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PARTY CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PARTY: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PART_PROPERTY CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PART_PROPERTY: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_MPC CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_MPC: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_ROLE CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_ROLE: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_SERVICE CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_SERVICE: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_AGREEMENT CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_AGREEMENT: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_ACTION CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_ACTION: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_MSH_ROLE CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_MSH_ROLE: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_TIMEZONE_OFFSET CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_TIMEZONE_OFFSET: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_NOTIFIC_STATUS CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_NOTIFIC_STATUS: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_MESSAGE_PROPERTY CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_MESSAGE_PROPERTY: table does not exist');
    END;
END;
/

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_MESSAGE_STATUS (LOCAL_ID NUMBER NOT NULL, REMOTE_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_MESSAGE_STATUS PRIMARY KEY (LOCAL_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PARTY (LOCAL_ID NUMBER NOT NULL, REMOTE_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PARTY PRIMARY KEY (LOCAL_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_PART_PROPERTY (LOCAL_ID NUMBER NOT NULL, REMOTE_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_PART_PROPERTY PRIMARY KEY (LOCAL_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_MPC (LOCAL_ID NUMBER NOT NULL, REMOTE_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_MPC PRIMARY KEY (LOCAL_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_ROLE (LOCAL_ID NUMBER NOT NULL, REMOTE_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_ROLE PRIMARY KEY (LOCAL_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_SERVICE (LOCAL_ID NUMBER NOT NULL, REMOTE_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_SERVICE PRIMARY KEY (LOCAL_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_AGREEMENT (LOCAL_ID NUMBER NOT NULL, REMOTE_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_AGREEMENT PRIMARY KEY (LOCAL_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_ACTION (LOCAL_ID NUMBER NOT NULL, REMOTE_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_ACTION PRIMARY KEY (LOCAL_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_MSH_ROLE (LOCAL_ID NUMBER NOT NULL, REMOTE_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_MSH_ROLE PRIMARY KEY (LOCAL_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_TIMEZONE_OFFSET (LOCAL_ID NUMBER NOT NULL, REMOTE_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_TIMEZONE_OFFSET PRIMARY KEY (LOCAL_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_NOTIFIC_STATUS (LOCAL_ID NUMBER NOT NULL, REMOTE_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_NOTIFIC_STATUS PRIMARY KEY (LOCAL_ID)) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE MIGR_TB_PKS_MESSAGE_PROPERTY (LOCAL_ID NUMBER NOT NULL, REMOTE_ID NUMBER NOT NULL, CONSTRAINT PK_MIGR_PKS_MESSAGE_PROPERTY PRIMARY KEY (LOCAL_ID)) ON COMMIT PRESERVE ROWS;

CREATE OR REPLACE PACKAGE MIGRATE_ONGOING_MESSAGES_50 IS
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

END MIGRATE_ONGOING_MESSAGES_50;
/

CREATE OR REPLACE PACKAGE BODY MIGRATE_ONGOING_MESSAGES_50 IS

    FUNCTION generate_scalable_seq(incr IN NUMBER, creation_time IN TIMESTAMP) RETURN NUMBER IS
        seq_id NUMBER;
        date_format CONSTANT VARCHAR2(255) := 'YYMMDDHH24';
        len CONSTANT VARCHAR2(255) := 'FM0000000000';
    BEGIN
        SELECT to_number(to_char(creation_time, date_format) || to_char(incr, len))
        INTO seq_id
        FROM dual;
        RETURN seq_id;
    END generate_scalable_seq;

    FUNCTION new_remote_pk(db_link IN VARCHAR2, creation_time IN TIMESTAMP) RETURN NUMBER IS
        remote_pk NUMBER;
    BEGIN
        EXECUTE IMMEDIATE 'SELECT DOMIBUS_SCALABLE_SEQUENCE.nextval@' || db_link || ' FROM DUAL' INTO remote_pk;
        RETURN generate_scalable_seq(remote_pk, creation_time);
    END new_remote_pk;

    PROCEDURE migrate_tb_d_message_status(db_link IN VARCHAR2) IS
        CURSOR c_d_message_status IS
            SELECT ID_PK, STATUS, CREATED_BY, CREATION_TIME
            FROM TB_D_MESSAGE_STATUS;

        TYPE T_D_MESSAGE_STATUS IS TABLE OF c_d_message_status%ROWTYPE;
        message_status T_D_MESSAGE_STATUS;

        remote_id TB_D_MESSAGE_STATUS.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_D_MESSAGE_STATUS entries...');
        OPEN c_d_message_status;
        LOOP
            FETCH c_d_message_status BULK COLLECT INTO message_status LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN message_status.COUNT = 0;

        FOR i IN message_status.FIRST .. message_status.LAST LOOP
                BEGIN
                    EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_MESSAGE_STATUS@' || db_link || ' WHERE STATUS = :p_1'
                        INTO remote_id
                        USING message_status(i).STATUS;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        remote_id := new_remote_pk(db_link, message_status(i).CREATION_TIME);

                        EXECUTE IMMEDIATE 'INSERT INTO TB_D_MESSAGE_STATUS@' || db_link || ' (ID_PK, STATUS, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4)'
                            USING remote_id,
                                message_status(i).STATUS,
                                message_status(i).CREATED_BY,
                                message_status(i).CREATION_TIME;
                END;

                INSERT INTO MIGR_TB_PKS_MESSAGE_STATUS (LOCAL_ID, REMOTE_ID)
                VALUES (message_status(i).ID_PK, remote_id);

                DBMS_OUTPUT.PUT_LINE('Local to remote mapping: TB_D_MESSAGE_STATUS[' || message_status(i).ID_PK || '] = ' || remote_id);
            END LOOP;

            DBMS_OUTPUT.PUT_LINE('Processed ' || message_status.COUNT || ' local records');
        END LOOP;
        CLOSE c_d_message_status;
    END migrate_tb_d_message_status;

    PROCEDURE migrate_tb_d_party(db_link IN VARCHAR2) IS
        CURSOR c_d_party IS
            SELECT ID_PK, "VALUE", "TYPE", CREATED_BY, CREATION_TIME
            FROM TB_D_PARTY;

        TYPE T_D_PARTY IS TABLE OF c_d_party%ROWTYPE;
        party T_D_PARTY;

        remote_id TB_D_PARTY.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_D_PARTY entries...');
        OPEN c_d_party;
        LOOP
            FETCH c_d_party BULK COLLECT INTO party LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN party.COUNT = 0;

            FOR i IN party.FIRST .. party.LAST LOOP
                BEGIN
                    IF party(i).TYPE IS NULL THEN
                        EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_PARTY@' || db_link || ' WHERE VALUE = :p_1 AND TYPE IS NULL'
                            INTO remote_id
                            USING party(i).VALUE;
                    ELSE
                        EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_PARTY@' || db_link || ' WHERE VALUE = :p_1 AND TYPE = :p_2'
                            INTO remote_id
                            USING party(i).VALUE,
                                party(i).TYPE;
                    END IF;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        remote_id := new_remote_pk(db_link, party(i).CREATION_TIME);

                        EXECUTE IMMEDIATE 'INSERT INTO TB_D_PARTY@' || db_link || ' (ID_PK, VALUE, TYPE, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5)'
                            USING remote_id,
                                party(i).VALUE,
                                party(i).TYPE,
                                party(i).CREATED_BY,
                                party(i).CREATION_TIME;
                END;

                INSERT INTO MIGR_TB_PKS_PARTY (LOCAL_ID, REMOTE_ID)
                VALUES (party(i).ID_PK, remote_id);

                DBMS_OUTPUT.PUT_LINE('Local to remote mapping: TB_D_PARTY[' || party(i).ID_PK || '] = ' || remote_id);
            END LOOP;

            DBMS_OUTPUT.PUT_LINE('Processed ' || party.COUNT || ' local records');
        END LOOP;
        CLOSE c_d_party;
    END migrate_tb_d_party;

    PROCEDURE migrate_tb_d_part_property(db_link IN VARCHAR2) IS
        CURSOR c_d_part_property IS
            SELECT ID_PK, NAME, "VALUE", "TYPE", CREATED_BY, CREATION_TIME
            FROM TB_D_PART_PROPERTY;

        TYPE T_D_PART_PROPERTY IS TABLE OF c_d_part_property%ROWTYPE;
        part_property T_D_PART_PROPERTY;

        remote_id TB_D_PART_PROPERTY.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_D_PART_PROPERTY entries...');
        OPEN c_d_part_property;
        LOOP
            FETCH c_d_part_property BULK COLLECT INTO part_property LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN part_property.COUNT = 0;

            FOR i IN part_property.FIRST .. part_property.LAST LOOP
                BEGIN
                    IF part_property(i).VALUE IS NULL AND part_property(i).TYPE IS NULL THEN
                        EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_PART_PROPERTY@' || db_link || ' WHERE NAME = :p_1 AND VALUE IS NULL AND TYPE IS NULL'
                            INTO remote_id
                            USING part_property(i).NAME;
                    ELSIF part_property(i).TYPE IS NULL THEN
                        EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_PART_PROPERTY@' || db_link || ' WHERE NAME = :p_1 AND VALUE = :p_2 AND TYPE IS NULL'
                            INTO remote_id
                            USING part_property(i).NAME,
                                part_property(i).VALUE;
                    ELSIF part_property(i).VALUE IS NULL THEN
                        EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_PART_PROPERTY@' || db_link || ' WHERE NAME = :p_1 AND TYPE = :p_2 AND VALUE IS NULL'
                            INTO remote_id
                            USING part_property(i).NAME,
                                part_property(i).TYPE;
                    ELSE
                        EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_PART_PROPERTY@' || db_link || ' WHERE NAME = :p_1 AND VALUE = :p_2 AND TYPE = :p_3'
                            INTO remote_id
                            USING part_property(i).NAME,
                                part_property(i).VALUE,
                                part_property(i).TYPE;
                    END IF;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        remote_id := new_remote_pk(db_link, part_property(i).CREATION_TIME);

                        EXECUTE IMMEDIATE 'INSERT INTO TB_D_PART_PROPERTY@' || db_link || ' (ID_PK, NAME, VALUE, TYPE, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6)'
                            USING remote_id,
                                part_property(i).NAME,
                                part_property(i).VALUE,
                                part_property(i).TYPE,
                                part_property(i).CREATED_BY,
                                part_property(i).CREATION_TIME;
                END;

                INSERT INTO MIGR_TB_PKS_PART_PROPERTY (LOCAL_ID, REMOTE_ID)
                VALUES (part_property(i).ID_PK, remote_id);

                DBMS_OUTPUT.PUT_LINE('Local to remote mapping: TB_D_PART_PROPERTY[' || part_property(i).ID_PK || '] = ' || remote_id);
            END LOOP;

            DBMS_OUTPUT.PUT_LINE('Processed ' || part_property.COUNT || ' local records');
        END LOOP;
        CLOSE c_d_part_property;
    END migrate_tb_d_part_property;

    PROCEDURE migrate_tb_d_mpc(db_link IN VARCHAR2) IS
        CURSOR c_d_mpc IS
            SELECT ID_PK, "VALUE", CREATED_BY, CREATION_TIME
            FROM TB_D_MPC;

        TYPE T_D_PARTY IS TABLE OF c_d_mpc%ROWTYPE;
        mpc T_D_PARTY;

        remote_id TB_D_MPC.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_D_MPC entries...');
        OPEN c_d_mpc;
        LOOP
            FETCH c_d_mpc BULK COLLECT INTO mpc LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN mpc.COUNT = 0;

            FOR i IN mpc.FIRST .. mpc.LAST LOOP
                BEGIN
                    EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_MPC@' || db_link || ' WHERE VALUE = :p_1'
                        INTO remote_id
                        USING mpc(i).VALUE;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        remote_id := new_remote_pk(db_link, mpc(i).CREATION_TIME);

                        EXECUTE IMMEDIATE 'INSERT INTO TB_D_MPC@' || db_link || ' (ID_PK, VALUE, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4)'
                            USING remote_id,
                                mpc(i).VALUE,
                                mpc(i).CREATED_BY,
                                mpc(i).CREATION_TIME;
                END;

                INSERT INTO MIGR_TB_PKS_MPC (LOCAL_ID, REMOTE_ID)
                VALUES (mpc(i).ID_PK, remote_id);

                DBMS_OUTPUT.PUT_LINE('Local to remote mapping: TB_D_MPC[' || mpc(i).ID_PK || '] = ' || remote_id);
            END LOOP;

            DBMS_OUTPUT.PUT_LINE('Processed ' || mpc.COUNT || ' local records');
        END LOOP;
        CLOSE c_d_mpc;
    END migrate_tb_d_mpc;

    PROCEDURE migrate_tb_d_role(db_link IN VARCHAR2) IS
        CURSOR c_d_role IS
            SELECT ID_PK, ROLE, CREATED_BY, CREATION_TIME
            FROM TB_D_ROLE;

        TYPE T_D_ROLE IS TABLE OF c_d_role%ROWTYPE;
        role T_D_ROLE;

        remote_id TB_D_ROLE.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_D_ROLE entries...');
        OPEN c_d_role;
        LOOP
            FETCH c_d_role BULK COLLECT INTO role LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN role.COUNT = 0;

            FOR i IN role.FIRST .. role.LAST LOOP
                BEGIN
                    EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_ROLE@' || db_link || ' WHERE ROLE = :p_1'
                        INTO remote_id
                        USING role(i).ROLE;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        remote_id := new_remote_pk(db_link, role(i).CREATION_TIME);

                        EXECUTE IMMEDIATE 'INSERT INTO TB_D_ROLE@' || db_link || ' (ID_PK, ROLE, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4)'
                            USING remote_id,
                                role(i).ROLE,
                                role(i).CREATED_BY,
                                role(i).CREATION_TIME;
                END;

                INSERT INTO MIGR_TB_PKS_ROLE (LOCAL_ID, REMOTE_ID)
                VALUES (role(i).ID_PK, remote_id);

                DBMS_OUTPUT.PUT_LINE('Local to remote mapping: TB_D_ROLE[' || role(i).ID_PK || '] = ' || remote_id);
            END LOOP;

            DBMS_OUTPUT.PUT_LINE('Processed ' || role.COUNT || ' local records');
        END LOOP;
        CLOSE c_d_role;
    END migrate_tb_d_role;

    PROCEDURE migrate_tb_d_service(db_link IN VARCHAR2) IS
        CURSOR c_d_service IS
            SELECT ID_PK, "VALUE", "TYPE", CREATED_BY, CREATION_TIME
            FROM TB_D_SERVICE;

        TYPE T_D_SERVICE IS TABLE OF c_d_service%ROWTYPE;
        service T_D_SERVICE;

        remote_id TB_D_SERVICE.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_D_SERVICE entries...');
        OPEN c_d_service;
        LOOP
            FETCH c_d_service BULK COLLECT INTO service LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN service.COUNT = 0;

            FOR i IN service.FIRST .. service.LAST LOOP
                BEGIN
                    IF service(i).TYPE IS NULL THEN
                        EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_SERVICE@' || db_link || ' WHERE VALUE = :p_1 AND TYPE IS NULL'
                            INTO remote_id
                            USING service(i).VALUE;
                    ELSE
                        EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_SERVICE@' || db_link || ' WHERE VALUE = :p_1 AND TYPE = :p_2'
                            INTO remote_id
                            USING service(i).VALUE,
                                service(i).TYPE;
                    END IF;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        remote_id := new_remote_pk(db_link, service(i).CREATION_TIME);

                        EXECUTE IMMEDIATE 'INSERT INTO TB_D_SERVICE@' || db_link || ' (ID_PK, VALUE, TYPE, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5)'
                            USING remote_id,
                                service(i).VALUE,
                                service(i).TYPE,
                                service(i).CREATED_BY,
                                service(i).CREATION_TIME;
                END;

                INSERT INTO MIGR_TB_PKS_SERVICE (LOCAL_ID, REMOTE_ID)
                VALUES (service(i).ID_PK, remote_id);

                DBMS_OUTPUT.PUT_LINE('Local to remote mapping: TB_D_SERVICE[' || service(i).ID_PK || '] = ' || remote_id);
            END LOOP;

            DBMS_OUTPUT.PUT_LINE('Processed ' || service.COUNT || ' local records');
        END LOOP;
        CLOSE c_d_service;
    END migrate_tb_d_service;

    PROCEDURE migrate_tb_d_agreement(db_link IN VARCHAR2) IS
        CURSOR c_d_agreement IS
            SELECT ID_PK, "VALUE", "TYPE", CREATED_BY, CREATION_TIME
            FROM TB_D_AGREEMENT;

        TYPE T_D_AGREEMENT IS TABLE OF c_d_agreement%ROWTYPE;
        agreement T_D_AGREEMENT;

        remote_id TB_D_AGREEMENT.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_D_AGREEMENT entries...');
        OPEN c_d_agreement;
        LOOP
            FETCH c_d_agreement BULK COLLECT INTO agreement LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN agreement.COUNT = 0;

            FOR i IN agreement.FIRST .. agreement.LAST LOOP
                BEGIN
                    IF agreement(i).TYPE IS NULL THEN
                        EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_AGREEMENT@' || db_link || ' WHERE VALUE = :p_1 AND TYPE IS NULL'
                            INTO remote_id
                            USING agreement(i).VALUE;
                    ELSE
                        EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_AGREEMENT@' || db_link || ' WHERE VALUE = :p_1 AND TYPE = :p_2'
                            INTO remote_id
                            USING agreement(i).VALUE,
                                agreement(i).TYPE;
                    END IF;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        remote_id := new_remote_pk(db_link, agreement(i).CREATION_TIME);

                        EXECUTE IMMEDIATE 'INSERT INTO TB_D_AGREEMENT@' || db_link || ' (ID_PK, VALUE, TYPE, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5)'
                            USING remote_id,
                                agreement(i).VALUE,
                                agreement(i).TYPE,
                                agreement(i).CREATED_BY,
                                agreement(i).CREATION_TIME;
                END;

                INSERT INTO MIGR_TB_PKS_AGREEMENT (LOCAL_ID, REMOTE_ID)
                VALUES (agreement(i).ID_PK, remote_id);

                DBMS_OUTPUT.PUT_LINE('Local to remote mapping: TB_D_AGREEMENT[' || agreement(i).ID_PK || '] = ' || remote_id);
            END LOOP;

            DBMS_OUTPUT.PUT_LINE('Processed ' || agreement.COUNT || ' local records');
        END LOOP;
        CLOSE c_d_agreement;
    END migrate_tb_d_agreement;

    PROCEDURE migrate_tb_d_action(db_link IN VARCHAR2) IS
        CURSOR c_d_action IS
            SELECT ID_PK, ACTION, CREATED_BY, CREATION_TIME
            FROM TB_D_ACTION;

        TYPE T_D_ACTION IS TABLE OF c_d_action%ROWTYPE;
        action T_D_ACTION;

        remote_id TB_D_ROLE.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_D_ACTION entries...');
        OPEN c_d_action;
        LOOP
            FETCH c_d_action BULK COLLECT INTO action LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN action.COUNT = 0;

            FOR i IN action.FIRST .. action.LAST LOOP
                BEGIN
                    EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_ACTION@' || db_link || ' WHERE ACTION = :p_1'
                        INTO remote_id
                        USING action(i).ACTION;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        remote_id := new_remote_pk(db_link, action(i).CREATION_TIME);

                        EXECUTE IMMEDIATE 'INSERT INTO TB_D_ACTION@' || db_link || ' (ID_PK, ACTION, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4)'
                            USING remote_id,
                                action(i).ACTION,
                                action(i).CREATED_BY,
                                action(i).CREATION_TIME;
                END;

                INSERT INTO MIGR_TB_PKS_ACTION (LOCAL_ID, REMOTE_ID)
                VALUES (action(i).ID_PK, remote_id);

                DBMS_OUTPUT.PUT_LINE('Local to remote mapping: TB_D_ACTION[' || action(i).ID_PK || '] = ' || remote_id);
            END LOOP;

            DBMS_OUTPUT.PUT_LINE('Processed ' || action.COUNT || ' local records');
        END LOOP;
        CLOSE c_d_action;
    END migrate_tb_d_action;

    PROCEDURE migrate_tb_d_msh_role(db_link IN VARCHAR2) IS
        CURSOR c_d_msh_role IS
            SELECT ID_PK, ROLE, CREATED_BY, CREATION_TIME
            FROM TB_D_MSH_ROLE;

        TYPE T_D_MSH_ROLE IS TABLE OF c_d_msh_role%ROWTYPE;
        msh_role T_D_MSH_ROLE;

        remote_id TB_D_MSH_ROLE.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_D_MSH_ROLE entries...');
        OPEN c_d_msh_role;
        LOOP
            FETCH c_d_msh_role BULK COLLECT INTO msh_role LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN msh_role.COUNT = 0;

            FOR i IN msh_role.FIRST .. msh_role.LAST LOOP
                BEGIN
                    EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_MSH_ROLE@' || db_link || ' WHERE ROLE = :p_1'
                        INTO remote_id
                        USING msh_role(i).ROLE;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        remote_id := new_remote_pk(db_link, msh_role(i).CREATION_TIME);

                        EXECUTE IMMEDIATE 'INSERT INTO TB_D_MSH_ROLE@' || db_link || ' (ID_PK, ROLE, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4)'
                            USING remote_id,
                                msh_role(i).ROLE,
                                msh_role(i).CREATED_BY,
                                msh_role(i).CREATION_TIME;
                END;

                INSERT INTO MIGR_TB_PKS_MSH_ROLE (LOCAL_ID, REMOTE_ID)
                VALUES (msh_role(i).ID_PK, remote_id);

                DBMS_OUTPUT.PUT_LINE('Local to remote mapping: TB_D_MSH_ROLE[' || msh_role(i).ID_PK || '] = ' || remote_id);
            END LOOP;

            DBMS_OUTPUT.PUT_LINE('Processed ' || msh_role.COUNT || ' local records');
        END LOOP;
        CLOSE c_d_msh_role;
    END migrate_tb_d_msh_role;

    PROCEDURE migrate_tb_d_timezone_offset(db_link IN VARCHAR2) IS
        CURSOR c_d_timezone_offset IS
            SELECT ID_PK, NEXT_ATTEMPT_TIMEZONE_ID, NEXT_ATTEMPT_OFFSET_SECONDS, CREATED_BY, CREATION_TIME
            FROM TB_D_TIMEZONE_OFFSET;

        TYPE T_D_TIMEZONE_OFFSET IS TABLE OF c_d_timezone_offset%ROWTYPE;
        timezone_offset T_D_TIMEZONE_OFFSET;

        remote_id TB_D_TIMEZONE_OFFSET.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_D_TIMEZONE_OFFSET entries...');
        OPEN c_d_timezone_offset;
        LOOP
            FETCH c_d_timezone_offset BULK COLLECT INTO timezone_offset LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN timezone_offset.COUNT = 0;

            FOR i IN timezone_offset.FIRST .. timezone_offset.LAST LOOP
                BEGIN
                    EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_TIMEZONE_OFFSET@' || db_link || ' WHERE NEXT_ATTEMPT_TIMEZONE_ID = :p_1 AND NEXT_ATTEMPT_OFFSET_SECONDS = :p_2'
                        INTO remote_id
                        USING timezone_offset(i).NEXT_ATTEMPT_TIMEZONE_ID,
                            timezone_offset(i).NEXT_ATTEMPT_OFFSET_SECONDS;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        remote_id := new_remote_pk(db_link, timezone_offset(i).CREATION_TIME);

                        EXECUTE IMMEDIATE 'INSERT INTO TB_D_TIMEZONE_OFFSET@' || db_link || ' (ID_PK, NEXT_ATTEMPT_TIMEZONE_ID, NEXT_ATTEMPT_OFFSET_SECONDS, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5)'
                            USING remote_id,
                                timezone_offset(i).NEXT_ATTEMPT_TIMEZONE_ID,
                                timezone_offset(i).NEXT_ATTEMPT_OFFSET_SECONDS,
                                timezone_offset(i).CREATED_BY,
                                timezone_offset(i).CREATION_TIME;
                END;

                INSERT INTO MIGR_TB_PKS_TIMEZONE_OFFSET (LOCAL_ID, REMOTE_ID)
                VALUES (timezone_offset(i).ID_PK, remote_id);

                DBMS_OUTPUT.PUT_LINE('Local to remote mapping: TB_D_TIMEZONE_OFFSET[' || timezone_offset(i).ID_PK || '] = ' || remote_id);
            END LOOP;

            DBMS_OUTPUT.PUT_LINE('Processed ' || timezone_offset.COUNT || ' local records');
        END LOOP;
        CLOSE c_d_timezone_offset;
    END migrate_tb_d_timezone_offset;

    PROCEDURE migrate_tb_d_notification_status(db_link IN VARCHAR2) IS
        CURSOR c_d_notification_status IS
            SELECT ID_PK, STATUS, CREATED_BY, CREATION_TIME
            FROM TB_D_NOTIFICATION_STATUS;

        TYPE T_D_NOTIFICATION_STATUS IS TABLE OF c_d_notification_status%ROWTYPE;
        notification_status T_D_NOTIFICATION_STATUS;

        remote_id TB_D_NOTIFICATION_STATUS.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_D_NOTIFICATION_STATUS entries...');
        OPEN c_d_notification_status;
        LOOP
            FETCH c_d_notification_status BULK COLLECT INTO notification_status LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN notification_status.COUNT = 0;

            FOR i IN notification_status.FIRST .. notification_status.LAST LOOP
                BEGIN
                    EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_NOTIFICATION_STATUS@' || db_link || ' WHERE STATUS = :p_1'
                        INTO remote_id
                        USING notification_status(i).STATUS;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        remote_id := new_remote_pk(db_link, notification_status(i).CREATION_TIME);
                        EXECUTE IMMEDIATE 'INSERT INTO TB_D_NOTIFICATION_STATUS@' || db_link || ' (ID_PK, STATUS, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4)'
                            USING remote_id,
                                notification_status(i).STATUS,
                                notification_status(i).CREATED_BY,
                                notification_status(i).CREATION_TIME;
                END;

                INSERT INTO MIGR_TB_PKS_NOTIFIC_STATUS (LOCAL_ID, REMOTE_ID)
                VALUES (notification_status(i).ID_PK, remote_id);

                DBMS_OUTPUT.PUT_LINE('Local to remote mapping: TB_D_NOTIFICATION_STATUS[' || notification_status(i).ID_PK || '] = ' || remote_id);
            END LOOP;

            DBMS_OUTPUT.PUT_LINE('Processed ' || notification_status.COUNT || ' local records');
        END LOOP;
        CLOSE c_d_notification_status;
    END migrate_tb_d_notification_status;

    PROCEDURE migrate_tb_d_message_property(db_link IN VARCHAR2) IS
        CURSOR c_d_message_property IS
            SELECT ID_PK, NAME, "VALUE", "TYPE", CREATED_BY, CREATION_TIME
            FROM TB_D_MESSAGE_PROPERTY;

        TYPE T_D_MESSAGE_PROPERTY IS TABLE OF c_d_message_property%ROWTYPE;
        message_property T_D_MESSAGE_PROPERTY;

        remote_id TB_D_MESSAGE_PROPERTY.ID_PK%TYPE; -- using the local table for the type but the remote has the same type too
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_D_MESSAGE_PROPERTY entries...');
        OPEN c_d_message_property;
        LOOP
            FETCH c_d_message_property BULK COLLECT INTO message_property LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN message_property.COUNT = 0;

            FOR i IN message_property.FIRST .. message_property.LAST LOOP
                BEGIN
                    IF message_property(i).VALUE IS NULL AND message_property(i).TYPE IS NULL THEN
                        EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_MESSAGE_PROPERTY@' || db_link || ' WHERE NAME = :p_1 AND VALUE IS NULL AND TYPE IS NULL'
                            INTO remote_id
                            USING message_property(i).NAME;
                    ELSIF message_property(i).VALUE IS NULL THEN
                        EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_MESSAGE_PROPERTY@' || db_link || ' WHERE NAME = :p_1 AND VALUE IS NULL AND TYPE = :p_2'
                            INTO remote_id
                            USING message_property(i).NAME,
                                message_property(i).TYPE;
                    ELSIF message_property(i).TYPE IS NULL THEN
                        EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_MESSAGE_PROPERTY@' || db_link || ' WHERE NAME = :p_1 AND VALUE = :p_2 AND TYPE IS NULL'
                            INTO remote_id
                            USING message_property(i).NAME,
                                message_property(i).VALUE;
                    ELSE
                        EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_MESSAGE_PROPERTY@' || db_link || ' WHERE NAME = :p_1 AND VALUE = :p_2 AND TYPE = :p_3'
                            INTO remote_id
                            USING message_property(i).NAME,
                                message_property(i).VALUE,
                                message_property(i).TYPE;
                    END IF;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        remote_id := new_remote_pk(db_link, message_property(i).CREATION_TIME);

                        EXECUTE IMMEDIATE 'INSERT INTO TB_D_MESSAGE_PROPERTY@' || db_link || ' (ID_PK, NAME, VALUE, TYPE, CREATED_BY, CREATION_TIME) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6)'
                            USING remote_id,
                                message_property(i).NAME,
                                message_property(i).VALUE,
                                message_property(i).TYPE,
                                message_property(i).CREATED_BY,
                                message_property(i).CREATION_TIME;
                END;

                INSERT INTO MIGR_TB_PKS_MESSAGE_PROPERTY (LOCAL_ID, REMOTE_ID)
                VALUES (message_property(i).ID_PK, remote_id);

                DBMS_OUTPUT.PUT_LINE('Local to remote mapping: TB_D_MESSAGE_PROPERTY[' || message_property(i).ID_PK || '] = ' || remote_id);
            END LOOP;

            DBMS_OUTPUT.PUT_LINE('Processed ' || message_property.COUNT || ' local records');
        END LOOP;
        CLOSE c_d_message_property;
    END migrate_tb_d_message_property;

    PROCEDURE migrate_user_message(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_user_message IS
            SELECT ID_PK, MESSAGE_ID, REF_TO_MESSAGE_ID, CONVERSATION_ID, SOURCE_MESSAGE, MESSAGE_FRAGMENT, TEST_MESSAGE, EBMS3_TIMESTAMP, ACTION_ID_FK, AGREEMENT_ID_FK, SERVICE_ID_FK, MPC_ID_FK, FROM_PARTY_ID_FK, FROM_ROLE_ID_FK, TO_PARTY_ID_FK, TO_ROLE_ID_FK, CREATION_TIME, CREATED_BY
            FROM TB_USER_MESSAGE
            WHERE ID_PK IN (
                SELECT ID_PK
                FROM TB_USER_MESSAGE_LOG
                WHERE MESSAGE_STATUS_ID_FK IN (
                    SELECT ID_PK
                    FROM TB_D_MESSAGE_STATUS
                    WHERE STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                ) AND RECEIVED BETWEEN migration.startDate AND migration.endDate);

        TYPE T_USER_MESSAGE IS TABLE OF c_user_message%ROWTYPE;
        user_message T_USER_MESSAGE;

        v_action_id_fk NUMBER;
        v_agreement_id_fk NUMBER;
        v_service_id_fk NUMBER;
        v_mpc_id_fk NUMBER;
        v_from_party_id_fk NUMBER;
        v_from_role_id_fk NUMBER;
        v_to_party_id_fk NUMBER;
        v_to_role_id_fk NUMBER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_USER_MESSAGE entries...');

        OPEN c_user_message;
        LOOP
            FETCH c_user_message BULK COLLECT INTO user_message LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN user_message.COUNT = 0;

            FOR i IN user_message.FIRST .. user_message.LAST LOOP
                BEGIN
                    SELECT REMOTE_ID INTO v_action_id_fk FROM MIGR_TB_PKS_ACTION WHERE LOCAL_ID = user_message(i).ACTION_ID_FK;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        DBMS_OUTPUT.PUT_LINE('No ACTION_ID_FK remote key found for local key = ' || user_message(i).ACTION_ID_FK);
                END;

                BEGIN
                    SELECT REMOTE_ID INTO v_agreement_id_fk FROM MIGR_TB_PKS_AGREEMENT WHERE LOCAL_ID = user_message(i).AGREEMENT_ID_FK;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        DBMS_OUTPUT.PUT_LINE('No AGREEMENT_ID_FK remote key found for local key = ' || user_message(i).AGREEMENT_ID_FK);
                END;

                BEGIN
                    SELECT REMOTE_ID INTO v_service_id_fk FROM MIGR_TB_PKS_SERVICE WHERE LOCAL_ID = user_message(i).SERVICE_ID_FK;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        DBMS_OUTPUT.PUT_LINE('No SERVICE_ID_FK remote key found for local key = ' || user_message(i).SERVICE_ID_FK);
                END;

                BEGIN
                    SELECT REMOTE_ID INTO v_mpc_id_fk FROM MIGR_TB_PKS_MPC WHERE LOCAL_ID = user_message(i).MPC_ID_FK;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        DBMS_OUTPUT.PUT_LINE('No MPC_ID_FK remote key found for local key = ' || user_message(i).MPC_ID_FK);
                END;

                BEGIN
                    SELECT REMOTE_ID INTO v_from_party_id_fk FROM MIGR_TB_PKS_PARTY WHERE LOCAL_ID = user_message(i).FROM_PARTY_ID_FK;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        DBMS_OUTPUT.PUT_LINE('No FROM_PARTY_ID_FK remote key found for local key = ' || user_message(i).FROM_PARTY_ID_FK);
                END;

                BEGIN
                    SELECT REMOTE_ID INTO v_from_role_id_fk FROM MIGR_TB_PKS_ROLE WHERE LOCAL_ID = user_message(i).FROM_ROLE_ID_FK;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        DBMS_OUTPUT.PUT_LINE('No FROM_ROLE_ID_FK remote key found for local key = ' || user_message(i).FROM_ROLE_ID_FK);
                END;

                BEGIN
                    SELECT REMOTE_ID INTO v_to_party_id_fk FROM MIGR_TB_PKS_PARTY WHERE LOCAL_ID = user_message(i).TO_PARTY_ID_FK;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        DBMS_OUTPUT.PUT_LINE('No TO_PARTY_ID_FK remote key found for local key = ' || user_message(i).TO_PARTY_ID_FK);
                END;

                BEGIN
                    SELECT REMOTE_ID INTO v_to_role_id_fk FROM MIGR_TB_PKS_ROLE WHERE LOCAL_ID = user_message(i).TO_ROLE_ID_FK;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        DBMS_OUTPUT.PUT_LINE('No TO_ROLE_ID_FK remote key found for local key = ' || user_message(i).TO_ROLE_ID_FK);
                END;

                EXECUTE IMMEDIATE 'INSERT INTO TB_USER_MESSAGE@' || db_link || ' (ID_PK, MESSAGE_ID, REF_TO_MESSAGE_ID, CONVERSATION_ID, SOURCE_MESSAGE, MESSAGE_FRAGMENT, TEST_MESSAGE, EBMS3_TIMESTAMP, ACTION_ID_FK, AGREEMENT_ID_FK, SERVICE_ID_FK, MPC_ID_FK, FROM_PARTY_ID_FK, FROM_ROLE_ID_FK, TO_PARTY_ID_FK, TO_ROLE_ID_FK, CREATION_TIME, CREATED_BY) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12, :p_13, :p_14, :p_15, :p_16, :p_17, :p_18)'
                    USING user_message(i).ID_PK,
                        user_message(i).MESSAGE_ID,
                        user_message(i).REF_TO_MESSAGE_ID,
                        user_message(i).CONVERSATION_ID,
                        user_message(i).SOURCE_MESSAGE,
                        user_message(i).MESSAGE_FRAGMENT,
                        user_message(i).TEST_MESSAGE,
                        user_message(i).EBMS3_TIMESTAMP,
                        v_action_id_fk,
                        v_agreement_id_fk,
                        v_service_id_fk,
                        v_mpc_id_fk,
                        v_from_party_id_fk,
                        v_from_role_id_fk,
                        v_to_party_id_fk,
                        v_to_role_id_fk,
                        user_message(i).CREATION_TIME,
                        user_message(i).CREATED_BY;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Wrote ' || user_message.COUNT || ' records');
        END LOOP;
        CLOSE c_user_message;
    END migrate_user_message;

    PROCEDURE migrate_signal_message(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_signal_message IS
            SELECT ID_PK, SIGNAL_MESSAGE_ID, REF_TO_MESSAGE_ID, EBMS3_TIMESTAMP, CREATION_TIME, CREATED_BY
            FROM TB_SIGNAL_MESSAGE
            WHERE ID_PK IN (
                SELECT ID_PK
                FROM TB_SIGNAL_MESSAGE_LOG
                WHERE MESSAGE_STATUS_ID_FK IN (
                    SELECT ID_PK
                    FROM TB_D_MESSAGE_STATUS
                    WHERE STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                ) AND RECEIVED BETWEEN migration.startDate AND migration.endDate);

        TYPE T_SIGNAL_MESSAGE IS TABLE OF c_signal_message%ROWTYPE;
        signal_message T_SIGNAL_MESSAGE;
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_SIGNAL_MESSAGE entries...');

        OPEN c_signal_message;
        LOOP
            FETCH c_signal_message BULK COLLECT INTO signal_message LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN signal_message.COUNT = 0;

            FOR i IN signal_message.FIRST .. signal_message.LAST LOOP
                EXECUTE IMMEDIATE 'INSERT INTO TB_SIGNAL_MESSAGE@' || db_link || ' (ID_PK, SIGNAL_MESSAGE_ID, REF_TO_MESSAGE_ID, EBMS3_TIMESTAMP, CREATION_TIME, CREATED_BY) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12, :p_13, :p_14, :p_15, :p_16, :p_17, :p_18)'
                    USING signal_message(i).ID_PK,
                        signal_message(i).SIGNAL_MESSAGE_ID,
                        signal_message(i).REF_TO_MESSAGE_ID,
                        signal_message(i).EBMS3_TIMESTAMP,
                        signal_message(i).CREATION_TIME,
                        signal_message(i).CREATED_BY;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Wrote ' || signal_message.COUNT || ' records');
        END LOOP;
        CLOSE c_signal_message;
    END migrate_signal_message;

    PROCEDURE migrate_user_message_log(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_user_message_log IS
            SELECT ID_PK, BACKEND, RECEIVED, ACKNOWLEDGED, DOWNLOADED, ARCHIVED, EXPORTED, FAILED, RESTORED, DELETED, NEXT_ATTEMPT, FK_TIMEZONE_OFFSET, SEND_ATTEMPTS, SEND_ATTEMPTS_MAX, SCHEDULED, VERSION, MESSAGE_STATUS_ID_FK, MSH_ROLE_ID_FK, NOTIFICATION_STATUS_ID_FK, CREATION_TIME, CREATED_BY, PROCESSING_TYPE
            FROM TB_USER_MESSAGE_LOG
            WHERE MESSAGE_STATUS_ID_FK IN (
                SELECT ID_PK
                FROM TB_D_MESSAGE_STATUS
                WHERE STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
            ) AND RECEIVED BETWEEN migration.startDate AND migration.endDate;

        TYPE T_USER_MESSAGE_LOG IS TABLE OF c_user_message_log%ROWTYPE;
        user_message_log T_USER_MESSAGE_LOG;

        v_fk_timezone_offset NUMBER;
        v_message_status_id_fk NUMBER;
        v_msh_role_id_fk NUMBER;
        v_notification_status_id_fk NUMBER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_USER_MESSAGE_LOG entries...');
        OPEN c_user_message_log;
        LOOP
            FETCH c_user_message_log BULK COLLECT INTO user_message_log LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN user_message_log.COUNT = 0;

            FOR i IN user_message_log.FIRST .. user_message_log.LAST LOOP
                BEGIN
                    SELECT REMOTE_ID INTO v_fk_timezone_offset FROM MIGR_TB_PKS_TIMEZONE_OFFSET WHERE LOCAL_ID = user_message_log(i).FK_TIMEZONE_OFFSET;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        DBMS_OUTPUT.PUT_LINE('No FK_TIMEZONE_OFFSET remote key found for local key = ' || user_message_log(i).FK_TIMEZONE_OFFSET);
                END;

                BEGIN
                    SELECT REMOTE_ID INTO v_message_status_id_fk FROM MIGR_TB_PKS_MESSAGE_STATUS WHERE LOCAL_ID = user_message_log(i).MESSAGE_STATUS_ID_FK;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        DBMS_OUTPUT.PUT_LINE('No MESSAGE_STATUS_ID_FK remote key found for local key = ' || user_message_log(i).MESSAGE_STATUS_ID_FK);
                END;

                -- NOT NULL
                SELECT REMOTE_ID INTO v_msh_role_id_fk FROM MIGR_TB_PKS_MSH_ROLE WHERE LOCAL_ID = user_message_log(i).MSH_ROLE_ID_FK;

                BEGIN
                    SELECT REMOTE_ID INTO v_notification_status_id_fk FROM MIGR_TB_PKS_NOTIFIC_STATUS WHERE LOCAL_ID = user_message_log(i).NOTIFICATION_STATUS_ID_FK;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        DBMS_OUTPUT.PUT_LINE('No NOTIFICATION_STATUS_ID_FK remote key found for local key = ' || user_message_log(i).NOTIFICATION_STATUS_ID_FK);
                END;

                EXECUTE IMMEDIATE 'INSERT INTO TB_USER_MESSAGE_LOG@' || db_link || ' (ID_PK, BACKEND, RECEIVED, ACKNOWLEDGED, DOWNLOADED, ARCHIVED, EXPORTED, FAILED, RESTORED, DELETED, NEXT_ATTEMPT, FK_TIMEZONE_OFFSET, SEND_ATTEMPTS, SEND_ATTEMPTS_MAX, SCHEDULED, VERSION, MESSAGE_STATUS_ID_FK, MSH_ROLE_ID_FK, NOTIFICATION_STATUS_ID_FK, CREATION_TIME, CREATED_BY, PROCESSING_TYPE) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12, :p_13, :p_14, :p_15, :p_16, :p_17, :p_18, :p_19, :p_20, :p_21, :p_22)'
                    USING user_message_log(i).ID_PK,
                        user_message_log(i).BACKEND,
                        user_message_log(i).RECEIVED,
                        user_message_log(i).ACKNOWLEDGED,
                        user_message_log(i).DOWNLOADED,
                        user_message_log(i).ARCHIVED,
                        user_message_log(i).EXPORTED,
                        user_message_log(i).FAILED,
                        user_message_log(i).RESTORED,
                        user_message_log(i).DELETED,
                        user_message_log(i).NEXT_ATTEMPT,
                        v_fk_timezone_offset,
                        user_message_log(i).SEND_ATTEMPTS,
                        user_message_log(i).SEND_ATTEMPTS_MAX,
                        user_message_log(i).SCHEDULED,
                        user_message_log(i).VERSION,
                        v_message_status_id_fk,
                        v_msh_role_id_fk,
                        v_notification_status_id_fk,
                        user_message_log(i).CREATION_TIME,
                        user_message_log(i).CREATED_BY,
                        user_message_log(i).PROCESSING_TYPE;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Wrote ' || user_message_log.COUNT || ' records');
        END LOOP;
        CLOSE c_user_message_log;
    END migrate_user_message_log;

    PROCEDURE migrate_signal_message_log(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_signal_message_log IS
            SELECT ID_PK, RECEIVED, DELETED, MESSAGE_STATUS_ID_FK, MSH_ROLE_ID_FK, CREATION_TIME, CREATED_BY
            FROM TB_SIGNAL_MESSAGE_LOG
            WHERE MESSAGE_STATUS_ID_FK IN (
                SELECT ID_PK
                FROM TB_D_MESSAGE_STATUS
                WHERE STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
            ) AND RECEIVED BETWEEN migration.startDate AND migration.endDate;

        TYPE T_SIGNAL_MESSAGE_LOG IS TABLE OF c_signal_message_log%ROWTYPE;
        signal_message_log T_SIGNAL_MESSAGE_LOG;

        v_message_status_id_fk NUMBER;
        v_msh_role_id_fk NUMBER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_SIGNAL_MESSAGE_LOG entries...');
        OPEN c_signal_message_log;
        LOOP
            FETCH c_signal_message_log BULK COLLECT INTO signal_message_log LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN signal_message_log.COUNT = 0;

            FOR i IN signal_message_log.FIRST .. signal_message_log.LAST LOOP
                BEGIN
                    SELECT REMOTE_ID INTO v_message_status_id_fk FROM MIGR_TB_PKS_MESSAGE_STATUS WHERE LOCAL_ID = signal_message_log(i).MESSAGE_STATUS_ID_FK;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        DBMS_OUTPUT.PUT_LINE('No MESSAGE_STATUS_ID_FK remote key found for local key = ' || signal_message_log(i).MESSAGE_STATUS_ID_FK);
                END;

                -- NOT NULL
                SELECT REMOTE_ID INTO v_msh_role_id_fk FROM MIGR_TB_PKS_MSH_ROLE WHERE LOCAL_ID = signal_message_log(i).MSH_ROLE_ID_FK;

                EXECUTE IMMEDIATE 'INSERT INTO TB_SIGNAL_MESSAGE_LOG@' || db_link || ' (ID_PK, RECEIVED, DELETED, MESSAGE_STATUS_ID_FK, MSH_ROLE_ID_FK, CREATION_TIME, CREATED_BY) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7)'
                    USING signal_message_log(i).ID_PK,
                        signal_message_log(i).RECEIVED,
                        signal_message_log(i).DELETED,
                        v_message_status_id_fk,
                        v_msh_role_id_fk,
                        signal_message_log(i).CREATION_TIME,
                        signal_message_log(i).CREATED_BY;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Wrote ' || signal_message_log.COUNT || ' records');
        END LOOP;
        CLOSE c_signal_message_log;
    END migrate_signal_message_log;

    PROCEDURE migrate_user_message_raw(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_user_message_raw IS
            SELECT ID_PK, RAW_XML, COMPRESSED, CREATION_TIME, CREATED_BY
            FROM TB_USER_MESSAGE_RAW
            WHERE ID_PK IN (
                SELECT ID_PK
                FROM TB_USER_MESSAGE
                WHERE ID_PK IN (
                    SELECT ID_PK
                    FROM TB_USER_MESSAGE_LOG
                    WHERE MESSAGE_STATUS_ID_FK IN (
                        SELECT ID_PK
                        FROM TB_D_MESSAGE_STATUS
                        WHERE STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                    ) AND RECEIVED BETWEEN migration.startDate AND migration.endDate));

        TYPE T_USER_MESSAGE_RAW IS TABLE OF c_user_message_raw%ROWTYPE;
        user_message_raw T_USER_MESSAGE_RAW;
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_USER_MESSAGE_RAW entries...');
        OPEN c_user_message_raw;
        LOOP
            FETCH c_user_message_raw BULK COLLECT INTO user_message_raw LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN user_message_raw.COUNT = 0;

            FOR i IN user_message_raw.FIRST .. user_message_raw.LAST LOOP
                EXECUTE IMMEDIATE 'INSERT INTO TB_USER_MESSAGE_RAW@' || db_link || 'ID_PK, COMPRESSED, CREATION_TIME, CREATED_BY) VALUES (:p_1, :p_2, :p_3, :p_4)'
                    USING user_message_raw(i).ID_PK,
                        user_message_raw(i).COMPRESSED,
                        user_message_raw(i).CREATION_TIME,
                        user_message_raw(i).CREATED_BY;

                -- BLOBs are special when copying
                EXECUTE IMMEDIATE 'UPDATE TB_USER_MESSAGE_RAW@' || db_link || ' SET RAW_XML = (SELECT RAW_XML FROM TB_USER_MESSAGE_RAW WHERE ID_PK = :p_1) WHERE ID_PK = :p_2'
                    USING user_message_raw(i).ID_PK,
                        user_message_raw(i).ID_PK;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Wrote ' || user_message_raw.COUNT || ' records');
        END LOOP;
        CLOSE c_user_message_raw;
    END migrate_user_message_raw;

    PROCEDURE migrate_signal_message_raw(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_signal_message_raw IS
            SELECT ID_PK, RAW_XML, COMPRESSED, CREATION_TIME, CREATED_BY
            FROM TB_SIGNAL_MESSAGE_RAW
            WHERE ID_PK IN (
                SELECT ID_PK
                FROM TB_SIGNAL_MESSAGE
                WHERE ID_PK IN (
                    SELECT ID_PK
                    FROM TB_SIGNAL_MESSAGE_LOG
                    WHERE MESSAGE_STATUS_ID_FK IN (
                        SELECT ID_PK
                        FROM TB_D_MESSAGE_STATUS
                        WHERE STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                    ) AND RECEIVED BETWEEN migration.startDate AND migration.endDate));

        TYPE T_SIGNAL_MESSAGE_RAW IS TABLE OF c_signal_message_raw%ROWTYPE;
        signal_message_raw T_SIGNAL_MESSAGE_RAW;
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_SIGNAL_MESSAGE_RAW entries...');
        OPEN c_signal_message_raw;
        LOOP
            FETCH c_signal_message_raw BULK COLLECT INTO signal_message_raw LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN signal_message_raw.COUNT = 0;

            FOR i IN signal_message_raw.FIRST .. signal_message_raw.LAST LOOP
                EXECUTE IMMEDIATE 'INSERT INTO TB_SIGNAL_MESSAGE_RAW@' || db_link || ' (ID_PK, COMPRESSED, CREATION_TIME, CREATED_BY) VALUES (:p_1, :p_2, :p_3, :p_4)'
                    USING signal_message_raw(i).ID_PK,
                        signal_message_raw(i).COMPRESSED,
                        signal_message_raw(i).CREATION_TIME,
                        signal_message_raw(i).CREATED_BY;

                -- BLOBs are special when copying
                EXECUTE IMMEDIATE 'UPDATE TB_SIGNAL_MESSAGE_RAW@' || db_link || ' SET RAW_XML = (SELECT RAW_XML FROM TB_SIGNAL_MESSAGE_RAW WHERE ID_PK = :p_1) WHERE ID_PK = :p_2'
                    USING signal_message_raw(i).ID_PK,
                        signal_message_raw(i).ID_PK;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Wrote ' || signal_message_raw.COUNT || ' records');
        END LOOP;
        CLOSE c_signal_message_raw;
    END migrate_signal_message_raw;

    PROCEDURE migrate_sj_message_header(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_sj_message_header IS
            SELECT ID_PK, BOUNDARY, START_MULTIPART, CREATION_TIME, CREATED_BY
            FROM TB_SJ_MESSAGE_HEADER
            WHERE ID_PK IN (
                SELECT ID_PK
                FROM TB_SJ_MESSAGE_GROUP
                WHERE SOURCE_MESSAGE_ID_FK IN (
                    SELECT ID_PK
                    FROM TB_USER_MESSAGE
                    WHERE (SOURCE_MESSAGE = 1 OR MESSAGE_FRAGMENT = 1)
                      AND ID_PK IN (
                        SELECT ID_PK
                        FROM TB_USER_MESSAGE_LOG
                        WHERE MESSAGE_STATUS_ID_FK IN (
                            SELECT ID_PK
                            FROM TB_D_MESSAGE_STATUS
                            WHERE STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                        ) AND RECEIVED BETWEEN migration.startDate AND migration.endDate)));

        TYPE T_SJ_MESSAGE_HEADER IS TABLE OF c_sj_message_header%ROWTYPE;
        sj_message_header T_SJ_MESSAGE_HEADER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_SJ_MESSAGE_HEADER entries...');
        OPEN c_sj_message_header;
        LOOP
            FETCH c_sj_message_header BULK COLLECT INTO sj_message_header LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN sj_message_header.COUNT = 0;

            FOR i IN sj_message_header.FIRST .. sj_message_header.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO TB_SJ_MESSAGE_HEADER@' || db_link || ' (ID_PK, BOUNDARY, START_MULTIPART, CREATION_TIME, CREATED_BY) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5)'
                        USING sj_message_header(i).ID_PK,
                        sj_message_header(i).BOUNDARY,
                        sj_message_header(i).START_MULTIPART,
                        sj_message_header(i).CREATION_TIME,
                        sj_message_header(i).CREATED_BY;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Wrote ' || sj_message_header.COUNT || ' records');
        END LOOP;
        CLOSE c_sj_message_header;
    END migrate_sj_message_header;

    PROCEDURE migrate_sj_message_group(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_sj_message_group IS
            SELECT ID_PK, GROUP_ID, MESSAGE_SIZE, FRAGMENT_COUNT, SENT_FRAGMENTS, RECEIVED_FRAGMENTS, COMPRESSION_ALGORITHM, COMPRESSED_MESSAGE_SIZE, SOAP_ACTION, REJECTED, EXPIRED, MSH_ROLE_ID_FK, SOURCE_MESSAGE_ID_FK, CREATION_TIME, CREATED_BY
            FROM TB_SJ_MESSAGE_GROUP
            WHERE SOURCE_MESSAGE_ID_FK IN (
                SELECT ID_PK
                FROM TB_USER_MESSAGE
                WHERE (SOURCE_MESSAGE = 1 OR MESSAGE_FRAGMENT = 1)
                  AND ID_PK IN (
                    SELECT ID_PK
                    FROM TB_USER_MESSAGE_LOG
                    WHERE MESSAGE_STATUS_ID_FK IN (
                        SELECT ID_PK
                        FROM TB_D_MESSAGE_STATUS
                        WHERE STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                    ) AND RECEIVED BETWEEN migration.startDate AND migration.endDate));

        TYPE T_SJ_MESSAGE_GROUP IS TABLE OF c_sj_message_group%ROWTYPE;
        sj_message_group T_SJ_MESSAGE_GROUP;
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_SJ_MESSAGE_GROUP entries...');
        OPEN c_sj_message_group;
        LOOP
            FETCH c_sj_message_group BULK COLLECT INTO sj_message_group LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN sj_message_group.COUNT = 0;

            FOR i IN sj_message_group.FIRST .. sj_message_group.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO TB_SJ_MESSAGE_GROUP@' || db_link || ' (ID_PK, GROUP_ID, MESSAGE_SIZE, FRAGMENT_COUNT, SENT_FRAGMENTS, RECEIVED_FRAGMENTS, COMPRESSION_ALGORITHM, COMPRESSED_MESSAGE_SIZE, SOAP_ACTION, REJECTED, EXPIRED, MSH_ROLE_ID_FK, SOURCE_MESSAGE_ID_FK, CREATION_TIME, CREATED_BY) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12, :p_13, :p_14, :p_15)'
                        USING sj_message_group(i).ID_PK,
                        sj_message_group(i).GROUP_ID,
                        sj_message_group(i).MESSAGE_SIZE,
                        sj_message_group(i).FRAGMENT_COUNT,
                        sj_message_group(i).SENT_FRAGMENTS,
                        sj_message_group(i).RECEIVED_FRAGMENTS,
                        sj_message_group(i).COMPRESSION_ALGORITHM,
                        sj_message_group(i).COMPRESSED_MESSAGE_SIZE,
                        sj_message_group(i).SOAP_ACTION,
                        sj_message_group(i).REJECTED,
                        sj_message_group(i).EXPIRED,
                        sj_message_group(i).MSH_ROLE_ID_FK,
                        sj_message_group(i).SOURCE_MESSAGE_ID_FK,
                        sj_message_group(i).CREATION_TIME,
                        sj_message_group(i).CREATED_BY;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Wrote ' || sj_message_group.COUNT || ' records');
        END LOOP;
        CLOSE c_sj_message_group;
    END migrate_sj_message_group;

    PROCEDURE migrate_sj_message_fragment(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_sj_message_fragment IS
            SELECT ID_PK, FRAGMENT_NUMBER, GROUP_ID_FK, CREATION_TIME, CREATED_BY
            FROM TB_SJ_MESSAGE_FRAGMENT
            WHERE ID_PK IN (
                SELECT ID_PK
                FROM TB_USER_MESSAGE
                WHERE (SOURCE_MESSAGE = 1 OR MESSAGE_FRAGMENT = 1)
                  AND ID_PK IN (
                    SELECT ID_PK
                    FROM TB_USER_MESSAGE_LOG
                    WHERE MESSAGE_STATUS_ID_FK IN (
                        SELECT ID_PK
                        FROM TB_D_MESSAGE_STATUS
                        WHERE STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                    ) AND RECEIVED BETWEEN migration.startDate AND migration.endDate));

        TYPE T_SJ_MESSAGE_FRAGMENT IS TABLE OF c_sj_message_fragment%ROWTYPE;
        sj_message_fragment T_SJ_MESSAGE_FRAGMENT;
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_SJ_MESSAGE_FRAGMENT entries...');
        OPEN c_sj_message_fragment;
        LOOP
            FETCH c_sj_message_fragment BULK COLLECT INTO sj_message_fragment LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN sj_message_fragment.COUNT = 0;

            FOR i IN sj_message_fragment.FIRST .. sj_message_fragment.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO TB_SJ_MESSAGE_FRAGMENT@' || db_link || ' (ID_PK, FRAGMENT_NUMBER, GROUP_ID_FK, CREATION_TIME, CREATED_BY) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5)'
                        USING sj_message_fragment(i).ID_PK,
                        sj_message_fragment(i).FRAGMENT_NUMBER,
                        sj_message_fragment(i).GROUP_ID_FK,
                        sj_message_fragment(i).CREATION_TIME,
                        sj_message_fragment(i).CREATED_BY;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Wrote ' || sj_message_fragment.COUNT || ' records');
        END LOOP;
        CLOSE c_sj_message_fragment;
    END migrate_sj_message_fragment;

    PROCEDURE migrate_ws_plg_msg_log(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_ws_plugin_message_log IS
            SELECT ID_PK, MESSAGE_ID, CONVERSATION_ID, REF_TO_MESSAGE_ID, FROM_PARTY_ID, FINAL_RECIPIENT, ORIGINAL_SENDER, RECEIVED
            FROM WS_PLUGIN_TB_MESSAGE_LOG
            WHERE RECEIVED BETWEEN migration.startDate AND migration.endDate;

        TYPE T_WS_PLUGIN_MESSAGE_LOG IS TABLE OF c_ws_plugin_message_log%ROWTYPE;
        ws_plugin_message_log T_WS_PLUGIN_MESSAGE_LOG;
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating WS_PLUGIN_TB_MESSAGE_LOG entries...');
        OPEN c_ws_plugin_message_log;
        LOOP
            FETCH c_ws_plugin_message_log BULK COLLECT INTO ws_plugin_message_log LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN ws_plugin_message_log.COUNT = 0;

            FOR i IN ws_plugin_message_log.FIRST .. ws_plugin_message_log.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO WS_PLUGIN_TB_MESSAGE_LOG@' || db_link || ' (ID_PK, MESSAGE_ID, CONVERSATION_ID, REF_TO_MESSAGE_ID, FROM_PARTY_ID, FINAL_RECIPIENT, ORIGINAL_SENDER, RECEIVED) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8)'
                        USING ws_plugin_message_log(i).ID_PK,
                        ws_plugin_message_log(i).MESSAGE_ID,
                        ws_plugin_message_log(i).CONVERSATION_ID,
                        ws_plugin_message_log(i).REF_TO_MESSAGE_ID,
                        ws_plugin_message_log(i).FROM_PARTY_ID,
                        ws_plugin_message_log(i).FINAL_RECIPIENT,
                        ws_plugin_message_log(i).ORIGINAL_SENDER,
                        ws_plugin_message_log(i).RECEIVED;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Wrote ' || ws_plugin_message_log.COUNT || ' records');
        END LOOP;
        CLOSE c_ws_plugin_message_log;
    END migrate_ws_plg_msg_log;

    PROCEDURE migrate_ws_plg_backend_msg_log(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_ws_backebd_plugin_msg_log IS
            SELECT ID_PK, CREATION_TIME, CREATED_BY, MESSAGE_ID, FINAL_RECIPIENT, ORIGINAL_SENDER, BACKEND_MESSAGE_STATUS, MESSAGE_STATUS, BACKEND_MESSAGE_TYPE, RULE_NAME, SENT, FAILED, SEND_ATTEMPTS, SEND_ATTEMPTS_MAX, NEXT_ATTEMPT, SCHEDULED
            FROM WS_PLUGIN_TB_BACKEND_MSG_LOG
            WHERE MESSAGE_STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
              AND MESSAGE_ID IN (
                SELECT MESSAGE_ID
                FROM WS_PLUGIN_TB_MESSAGE_LOG
                WHERE RECEIVED BETWEEN migration.startDate AND migration.endDate);

        TYPE T_WS_PLUGIN_BACKEND_MSG_LOG IS TABLE OF c_ws_backebd_plugin_msg_log%ROWTYPE;
        ws_plugin_backend_msg_log T_WS_PLUGIN_BACKEND_MSG_LOG;
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating WS_PLUGIN_TB_BACKEND_MSG_LOG entries...');
        OPEN c_ws_backebd_plugin_msg_log;
        LOOP
            FETCH c_ws_backebd_plugin_msg_log BULK COLLECT INTO ws_plugin_backend_msg_log LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN ws_plugin_backend_msg_log.COUNT = 0;

            FOR i IN ws_plugin_backend_msg_log.FIRST .. ws_plugin_backend_msg_log.LAST LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO WS_PLUGIN_TB_BACKEND_MSG_LOG@' || db_link || ' (ID_PK, CREATION_TIME, CREATED_BY, MESSAGE_ID, FINAL_RECIPIENT, ORIGINAL_SENDER, BACKEND_MESSAGE_STATUS, MESSAGE_STATUS, BACKEND_MESSAGE_TYPE, RULE_NAME, SENT, FAILED, SEND_ATTEMPTS, SEND_ATTEMPTS_MAX, NEXT_ATTEMPT, SCHEDULED) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12, :p_13, :p_14, :p_15, :p_16)'
                        USING ws_plugin_backend_msg_log(i).ID_PK,
                        ws_plugin_backend_msg_log(i).CREATION_TIME,
                        ws_plugin_backend_msg_log(i).CREATED_BY,
                        ws_plugin_backend_msg_log(i).MESSAGE_ID,
                        ws_plugin_backend_msg_log(i).FINAL_RECIPIENT,
                        ws_plugin_backend_msg_log(i).ORIGINAL_SENDER,
                        ws_plugin_backend_msg_log(i).BACKEND_MESSAGE_STATUS,
                        ws_plugin_backend_msg_log(i).MESSAGE_STATUS,
                        ws_plugin_backend_msg_log(i).BACKEND_MESSAGE_TYPE,
                        ws_plugin_backend_msg_log(i).RULE_NAME,
                        ws_plugin_backend_msg_log(i).SENT,
                        ws_plugin_backend_msg_log(i).FAILED,
                        ws_plugin_backend_msg_log(i).SEND_ATTEMPTS,
                        ws_plugin_backend_msg_log(i).SEND_ATTEMPTS_MAX,
                        ws_plugin_backend_msg_log(i).NEXT_ATTEMPT,
                        ws_plugin_backend_msg_log(i).SCHEDULED;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Wrote ' || ws_plugin_backend_msg_log.COUNT || ' records');
        END LOOP;
        CLOSE c_ws_backebd_plugin_msg_log;
    END migrate_ws_plg_backend_msg_log;

    PROCEDURE migrate_message_properties(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_message_properties IS
            SELECT USER_MESSAGE_ID_FK, MESSAGE_PROPERTY_FK, CREATION_TIME, CREATED_BY
            FROM TB_MESSAGE_PROPERTIES
            WHERE USER_MESSAGE_ID_FK IN (
                SELECT ID_PK
                FROM TB_USER_MESSAGE
                WHERE ID_PK IN (
                    SELECT ID_PK
                    FROM TB_USER_MESSAGE_LOG
                    WHERE MESSAGE_STATUS_ID_FK IN (
                        SELECT ID_PK
                        FROM TB_D_MESSAGE_STATUS
                        WHERE STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                    ) AND RECEIVED BETWEEN migration.startDate AND migration.endDate));

        TYPE T_MESSAGE_PROPERTIES IS TABLE OF c_message_properties%ROWTYPE;
        message_properties T_MESSAGE_PROPERTIES;

        v_message_property_fk NUMBER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_MESSAGE_PROPERTIES entries...');

        OPEN c_message_properties;
        LOOP
            FETCH c_message_properties BULK COLLECT INTO message_properties LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN message_properties.COUNT = 0;

            FOR i IN message_properties.FIRST .. message_properties.LAST LOOP
                SELECT REMOTE_ID INTO v_message_property_fk FROM MIGR_TB_PKS_MESSAGE_PROPERTY WHERE LOCAL_ID = message_properties(i).MESSAGE_PROPERTY_FK;

                EXECUTE IMMEDIATE 'INSERT INTO TB_MESSAGE_PROPERTIES@' || db_link || ' (USER_MESSAGE_ID_FK, MESSAGE_PROPERTY_FK, CREATION_TIME, CREATED_BY) VALUES (:p_1, :p_2, :p_3, :p_4)'
                    USING message_properties(i).USER_MESSAGE_ID_FK,
                        v_message_property_fk,
                        message_properties(i).CREATION_TIME,
                        message_properties(i).CREATED_BY;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Wrote ' || message_properties.COUNT || ' records');
        END LOOP;
        CLOSE c_message_properties;
    END migrate_message_properties;

    PROCEDURE migrate_message_acknw(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_message_acknw IS
            SELECT ID_PK, FROM_VALUE, TO_VALUE, ACKNOWLEDGE_DATE, USER_MESSAGE_ID_FK, CREATION_TIME, CREATED_BY
            FROM TB_MESSAGE_ACKNW
            WHERE USER_MESSAGE_ID_FK IN (
                SELECT ID_PK
                FROM TB_USER_MESSAGE
                WHERE ID_PK IN (
                    SELECT ID_PK
                    FROM TB_USER_MESSAGE_LOG
                    WHERE MESSAGE_STATUS_ID_FK IN (
                        SELECT ID_PK
                        FROM TB_D_MESSAGE_STATUS
                        WHERE STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                    ) AND RECEIVED BETWEEN migration.startDate AND migration.endDate));

        TYPE T_MESSAGE_ACKNW IS TABLE OF c_message_acknw%ROWTYPE;
        message_acknw T_MESSAGE_ACKNW;
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_MESSAGE_ACKNW entries...');

        OPEN c_message_acknw;
        LOOP
            FETCH c_message_acknw BULK COLLECT INTO message_acknw LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN message_acknw.COUNT = 0;

            FOR i IN message_acknw.FIRST .. message_acknw.LAST LOOP
                EXECUTE IMMEDIATE 'INSERT INTO TB_MESSAGE_ACKNW@' || db_link || ' (ID_PK, FROM_VALUE, TO_VALUE, ACKNOWLEDGE_DATE, USER_MESSAGE_ID_FK, CREATION_TIME, CREATED_BY) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7)'
                    USING message_acknw(i).ID_PK,
                        message_acknw(i).FROM_VALUE,
                        message_acknw(i).TO_VALUE,
                        message_acknw(i).ACKNOWLEDGE_DATE,
                        message_acknw(i).USER_MESSAGE_ID_FK,
                        message_acknw(i).CREATION_TIME,
                        message_acknw(i).CREATED_BY;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Wrote ' || message_acknw.COUNT || ' records');
        END LOOP;
        CLOSE c_message_acknw;
    END migrate_message_acknw;

    PROCEDURE migrate_message_acknw_prop(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_message_acknw_prop IS
            SELECT ID_PK, PROPERTY_NAME, PROPERTY_VALUE, FK_MSG_ACKNOWLEDGE, CREATION_TIME, CREATED_BY
            FROM TB_MESSAGE_ACKNW_PROP
            WHERE FK_MSG_ACKNOWLEDGE IN (
                SELECT ID_PK
                FROM TB_MESSAGE_ACKNW
                WHERE USER_MESSAGE_ID_FK IN (
                    SELECT ID_PK
                    FROM TB_USER_MESSAGE
                    WHERE ID_PK IN (
                        SELECT ID_PK
                        FROM TB_USER_MESSAGE_LOG
                        WHERE MESSAGE_STATUS_ID_FK IN (
                            SELECT ID_PK
                            FROM TB_D_MESSAGE_STATUS
                            WHERE STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                        ) AND RECEIVED BETWEEN migration.startDate AND migration.endDate)));

        TYPE T_MESSAGE_ACKNW_PROP IS TABLE OF c_message_acknw_prop%ROWTYPE;
        message_acknw_prop T_MESSAGE_ACKNW_PROP;
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_MESSAGE_ACKNW_PROP entries...');

        OPEN c_message_acknw_prop;
        LOOP
            FETCH c_message_acknw_prop BULK COLLECT INTO message_acknw_prop LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN message_acknw_prop.COUNT = 0;

            FOR i IN message_acknw_prop.FIRST .. message_acknw_prop.LAST LOOP
                EXECUTE IMMEDIATE 'INSERT INTO TB_MESSAGE_ACKNW_PROP@' || db_link || ' (ID_PK, PROPERTY_NAME, PROPERTY_VALUE, FK_MSG_ACKNOWLEDGE, CREATION_TIME, CREATED_BY) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6)'
                    USING message_acknw_prop(i).ID_PK,
                        message_acknw_prop(i).PROPERTY_NAME,
                        message_acknw_prop(i).PROPERTY_VALUE,
                        message_acknw_prop(i).FK_MSG_ACKNOWLEDGE,
                        message_acknw_prop(i).CREATION_TIME,
                        message_acknw_prop(i).CREATED_BY;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Wrote ' || message_acknw_prop.COUNT || ' records');
        END LOOP;
        CLOSE c_message_acknw_prop;
    END migrate_message_acknw_prop;

    PROCEDURE migrate_messaging_lock(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_messaging_lock IS
            SELECT ID_PK, MESSAGE_TYPE, MESSAGE_RECEIVED, MESSAGE_STATE, MESSAGE_ID, INITIATOR, MPC, SEND_ATTEMPTS, SEND_ATTEMPTS_MAX, NEXT_ATTEMPT, FK_TIMEZONE_OFFSET, MESSAGE_STALED, CREATION_TIME, CREATED_BY
            FROM TB_MESSAGING_LOCK
            WHERE MESSAGE_ID IN (
                SELECT MESSAGE_ID
                FROM TB_USER_MESSAGE
                WHERE ID_PK IN (
                    SELECT ID_PK
                    FROM TB_USER_MESSAGE_LOG
                    WHERE MESSAGE_STATUS_ID_FK IN (
                        SELECT ID_PK
                        FROM TB_D_MESSAGE_STATUS
                        WHERE STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                    ) AND RECEIVED BETWEEN migration.startDate AND migration.endDate));

        TYPE T_MESSAGING_LOCK IS TABLE OF c_messaging_lock%ROWTYPE;
        messaging_lock T_MESSAGING_LOCK;

        v_fk_timezone_offset NUMBER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_MESSAGING_LOCK entries...');

        OPEN c_messaging_lock;
        LOOP
            FETCH c_messaging_lock BULK COLLECT INTO messaging_lock LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN messaging_lock.COUNT = 0;

            FOR i IN messaging_lock.FIRST .. messaging_lock.LAST LOOP
                SELECT REMOTE_ID INTO v_fk_timezone_offset FROM MIGR_TB_PKS_TIMEZONE_OFFSET WHERE LOCAL_ID = messaging_lock(i).FK_TIMEZONE_OFFSET;

                EXECUTE IMMEDIATE 'INSERT INTO TB_MESSAGING_LOCK@' || db_link || ' (ID_PK, MESSAGE_TYPE, MESSAGE_RECEIVED, MESSAGE_STATE, MESSAGE_ID, INITIATOR, MPC, SEND_ATTEMPTS, SEND_ATTEMPTS_MAX, NEXT_ATTEMPT, FK_TIMEZONE_OFFSET, MESSAGE_STALED, CREATION_TIME, CREATED_BY) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12, :p_13, :p_14)'
                    USING messaging_lock(i).ID_PK,
                        messaging_lock(i).MESSAGE_TYPE,
                        messaging_lock(i).MESSAGE_RECEIVED,
                        messaging_lock(i).MESSAGE_STATE,
                        messaging_lock(i).MESSAGE_ID,
                        messaging_lock(i).INITIATOR,
                        messaging_lock(i).MPC,
                        messaging_lock(i).SEND_ATTEMPTS,
                        messaging_lock(i).SEND_ATTEMPTS_MAX,
                        messaging_lock(i).NEXT_ATTEMPT,
                        v_fk_timezone_offset,
                        messaging_lock(i).MESSAGE_STALED,
                        messaging_lock(i).CREATION_TIME,
                        messaging_lock(i).CREATED_BY;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Wrote ' || messaging_lock.COUNT || ' records');
        END LOOP;
        CLOSE c_messaging_lock;
    END migrate_messaging_lock;

    PROCEDURE migrate_receipt(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_receipt IS
            SELECT ID_PK, RAW_XML, COMPRESSED, CREATION_TIME, CREATED_BY
            FROM TB_RECEIPT
            WHERE ID_PK IN (
                SELECT ID_PK
                FROM TB_SIGNAL_MESSAGE
                WHERE ID_PK IN (
                    SELECT ID_PK
                    FROM TB_SIGNAL_MESSAGE_LOG
                    WHERE MESSAGE_STATUS_ID_FK IN (
                        SELECT ID_PK
                        FROM TB_D_MESSAGE_STATUS
                        WHERE STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                    ) AND RECEIVED BETWEEN migration.startDate AND migration.endDate));

        TYPE T_RECEIPT IS TABLE OF c_receipt%ROWTYPE;
        receipt T_RECEIPT;
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_RECEIPT entries...');

        OPEN c_receipt;
        LOOP
            FETCH c_receipt BULK COLLECT INTO receipt LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN receipt.COUNT = 0;

            FOR i IN receipt.FIRST .. receipt.LAST LOOP
                EXECUTE IMMEDIATE 'INSERT INTO TB_RECEIPT@' || db_link || ' (ID_PK, COMPRESSED, CREATION_TIME, CREATED_BY) VALUES (:p_1, :p_2, :p_3, :p_4)'
                    USING receipt(i).ID_PK,
                        receipt(i).COMPRESSED,
                        receipt(i).CREATION_TIME,
                        receipt(i).CREATED_BY;

                -- BLOBs are special when copying
                EXECUTE IMMEDIATE 'UPDATE TB_RECEIPT@' || db_link || ' SET RAW_XML = (SELECT RAW_XML FROM TB_RECEIPT WHERE ID_PK = :p_1) WHERE ID_PK = :p_2'
                    USING receipt(i).ID_PK,
                        receipt(i).ID_PK;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Wrote ' || receipt.COUNT || ' records');
        END LOOP;
        CLOSE c_receipt;
    END migrate_receipt;

    PROCEDURE migrate_send_attempt(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_send_attempt IS
            SELECT ID_PK, START_DATE, END_DATE, STATUS, ERROR, USER_MESSAGE_ID_FK, CREATION_TIME, CREATED_BY
            FROM TB_SEND_ATTEMPT
            WHERE ID_PK IN (
                SELECT ID_PK
                FROM TB_USER_MESSAGE
                WHERE ID_PK IN (
                    SELECT ID_PK
                    FROM TB_USER_MESSAGE_LOG
                    WHERE MESSAGE_STATUS_ID_FK IN (
                        SELECT ID_PK
                        FROM TB_D_MESSAGE_STATUS
                        WHERE STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                    ) AND RECEIVED BETWEEN migration.startDate AND migration.endDate));

        TYPE T_SEND_ATTEMPT IS TABLE OF c_send_attempt%ROWTYPE;
        send_attempt T_SEND_ATTEMPT;
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_SEND_ATTEMPT entries...');

        OPEN c_send_attempt;
        LOOP
            FETCH c_send_attempt BULK COLLECT INTO send_attempt LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN send_attempt.COUNT = 0;

            FOR i IN send_attempt.FIRST .. send_attempt.LAST LOOP
                EXECUTE IMMEDIATE 'INSERT INTO TB_SEND_ATTEMPT@' || db_link || ' (ID_PK, START_DATE, END_DATE, STATUS, ERROR, USER_MESSAGE_ID_FK, CREATION_TIME, CREATED_BY) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8)'
                    USING send_attempt(i).ID_PK,
                        send_attempt(i).START_DATE,
                        send_attempt(i).END_DATE,
                        send_attempt(i).STATUS,
                        send_attempt(i).ERROR,
                        send_attempt(i).USER_MESSAGE_ID_FK,
                        send_attempt(i).CREATION_TIME,
                        send_attempt(i).CREATED_BY;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Wrote ' || send_attempt.COUNT || ' records');
        END LOOP;
        CLOSE c_send_attempt;
    END migrate_send_attempt;

    PROCEDURE migrate_error_log(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_error_log IS
            SELECT ID_PK, ERROR_CODE, ERROR_DETAIL, ERROR_SIGNAL_MESSAGE_ID, MESSAGE_IN_ERROR_ID, MSH_ROLE_ID_FK, NOTIFIED, TIME_STAMP, USER_MESSAGE_ID_FK, CREATION_TIME, CREATED_BY
            FROM TB_ERROR_LOG
            WHERE USER_MESSAGE_ID_FK IN (
                SELECT ID_PK
                FROM TB_USER_MESSAGE
                WHERE ID_PK IN (
                    SELECT ID_PK
                    FROM TB_USER_MESSAGE_LOG
                    WHERE MESSAGE_STATUS_ID_FK IN (
                        SELECT ID_PK
                        FROM TB_D_MESSAGE_STATUS
                        WHERE STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                    ) AND RECEIVED BETWEEN migration.startDate AND migration.endDate));

        TYPE T_ERROR_LOG IS TABLE OF c_error_log%ROWTYPE;
        error_log T_ERROR_LOG;

        v_msh_role_id_fk NUMBER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_ERROR_LOG entries...');

        OPEN c_error_log;
        LOOP
            FETCH c_error_log BULK COLLECT INTO error_log LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN error_log.COUNT = 0;

            FOR i IN error_log.FIRST .. error_log.LAST LOOP
                BEGIN
                    SELECT REMOTE_ID INTO v_msh_role_id_fk FROM MIGR_TB_PKS_MSH_ROLE WHERE LOCAL_ID = error_log(i).MSH_ROLE_ID_FK;
                EXCEPTION
                    WHEN NO_DATA_FOUND THEN
                        DBMS_OUTPUT.PUT_LINE('No MSH_ROLE_ID_FK remote key found for local key = ' || error_log(i).MSH_ROLE_ID_FK);
                END;

                EXECUTE IMMEDIATE 'INSERT INTO TB_ERROR_LOG@' || db_link || ' (ID_PK, ERROR_CODE, ERROR_DETAIL, ERROR_SIGNAL_MESSAGE_ID, MESSAGE_IN_ERROR_ID, MSH_ROLE_ID_FK, NOTIFIED, TIME_STAMP, USER_MESSAGE_ID_FK, CREATION_TIME, CREATED_BY) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11)'
                    USING error_log(i).ID_PK,
                        error_log(i).ERROR_CODE,
                        error_log(i).ERROR_DETAIL,
                        error_log(i).ERROR_SIGNAL_MESSAGE_ID,
                        error_log(i).MESSAGE_IN_ERROR_ID,
                        v_msh_role_id_fk,
                        error_log(i).NOTIFIED,
                        error_log(i).TIME_STAMP,
                        error_log(i).USER_MESSAGE_ID_FK,
                        error_log(i).CREATION_TIME,
                        error_log(i).CREATED_BY;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Wrote ' || error_log.COUNT || ' records');
        END LOOP;
        CLOSE c_error_log;
    END migrate_error_log;

    PROCEDURE migrate_part_info(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_part_info IS
            SELECT ID_PK, BINARY_DATA, DESCRIPTION_LANG, DESCRIPTION_VALUE, HREF, IN_BODY, FILENAME, MIME, USER_MESSAGE_ID_FK, PART_ORDER, ENCRYPTED, COMPRESSED, CREATION_TIME, CREATED_BY
            FROM TB_PART_INFO
            WHERE USER_MESSAGE_ID_FK IN (
                SELECT ID_PK
                FROM TB_USER_MESSAGE
                WHERE ID_PK IN (
                    SELECT ID_PK
                    FROM TB_USER_MESSAGE_LOG
                    WHERE MESSAGE_STATUS_ID_FK IN (
                        SELECT ID_PK
                        FROM TB_D_MESSAGE_STATUS
                        WHERE STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                    ) AND RECEIVED BETWEEN migration.startDate AND migration.endDate));

        TYPE T_PART_INFO IS TABLE OF c_part_info%ROWTYPE;
        part_info T_PART_INFO;
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_PART_INFO entries...');

        OPEN c_part_info;
        LOOP
            FETCH c_part_info BULK COLLECT INTO part_info LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN part_info.COUNT = 0;

            FOR i IN part_info.FIRST .. part_info.LAST LOOP
                EXECUTE IMMEDIATE 'INSERT INTO TB_PART_INFO@' || db_link || ' (ID_PK, DESCRIPTION_LANG, DESCRIPTION_VALUE, HREF, IN_BODY, FILENAME, MIME, USER_MESSAGE_ID_FK, PART_ORDER, ENCRYPTED, COMPRESSED, CREATION_TIME, CREATED_BY) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10, :p_11, :p_12, :p_13)'
                    USING part_info(i).ID_PK,
                        part_info(i).DESCRIPTION_LANG,
                        part_info(i).DESCRIPTION_VALUE,
                        part_info(i).HREF,
                        part_info(i).IN_BODY,
                        part_info(i).FILENAME,
                        part_info(i).MIME,
                        part_info(i).USER_MESSAGE_ID_FK,
                        part_info(i).PART_ORDER,
                        part_info(i).ENCRYPTED,
                        part_info(i).COMPRESSED,
                        part_info(i).CREATION_TIME,
                        part_info(i).CREATED_BY;

                -- BLOBs are special when copying
                EXECUTE IMMEDIATE 'UPDATE TB_PART_INFO@' || db_link || ' SET BINARY_DATA = (SELECT BINARY_DATA FROM TB_PART_INFO WHERE ID_PK = :p_1) WHERE ID_PK = :p_2'
                    USING part_info(i).ID_PK,
                        part_info(i).ID_PK;
                END LOOP;

            DBMS_OUTPUT.PUT_LINE('Wrote ' || part_info.COUNT || ' records');
        END LOOP;
        CLOSE c_part_info;
    END migrate_part_info;

    PROCEDURE migrate_part_properties(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_part_properties IS
            SELECT PART_INFO_ID_FK, PART_INFO_PROPERTY_FK, CREATION_TIME, CREATED_BY
            FROM TB_PART_PROPERTIES
            WHERE PART_INFO_ID_FK IN (
                SELECT ID_PK
                FROM TB_PART_INFO
                WHERE USER_MESSAGE_ID_FK IN (
                    SELECT ID_PK
                    FROM TB_USER_MESSAGE
                    WHERE ID_PK IN (
                        SELECT ID_PK
                        FROM TB_USER_MESSAGE_LOG
                        WHERE MESSAGE_STATUS_ID_FK IN (
                            SELECT ID_PK
                            FROM TB_D_MESSAGE_STATUS
                            WHERE STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                        ) AND RECEIVED BETWEEN migration.startDate AND migration.endDate)));

        TYPE T_PART_PROPERTIES IS TABLE OF c_part_properties%ROWTYPE;
        part_properties T_PART_PROPERTIES;

        v_part_info_property_fk NUMBER;
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_PART_PROPERTIES entries...');

        OPEN c_part_properties;
        LOOP
            FETCH c_part_properties BULK COLLECT INTO part_properties LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN part_properties.COUNT = 0;

            FOR i IN part_properties.FIRST .. part_properties.LAST LOOP
                SELECT REMOTE_ID INTO v_part_info_property_fk FROM MIGR_TB_PKS_PART_PROPERTY WHERE LOCAL_ID = part_properties(i).PART_INFO_PROPERTY_FK;

                EXECUTE IMMEDIATE 'INSERT INTO TB_PART_PROPERTIES@' || db_link || ' (PART_INFO_ID_FK, PART_INFO_PROPERTY_FK, CREATION_TIME, CREATED_BY) VALUES (:p_1, :p_2, :p_3, :p_4)'
                    USING part_properties(i).PART_INFO_ID_FK,
                        v_part_info_property_fk,
                        part_properties(i).CREATION_TIME,
                        part_properties(i).CREATED_BY;
                END LOOP;

            DBMS_OUTPUT.PUT_LINE('Wrote ' || part_properties.COUNT || ' records');
        END LOOP;
        CLOSE c_part_properties;
    END migrate_part_properties;

    PROCEDURE migrate_action_audit(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS) IS
        CURSOR c_action_audit IS
            SELECT ID_PK, AUDIT_TYPE, ENTITY_ID, MODIFICATION_TYPE, REVISION_DATE, USER_NAME, FROM_QUEUE, TO_QUEUE, CREATION_TIME, CREATED_BY
            FROM TB_ACTION_AUDIT
            WHERE ENTITY_ID IN (
                SELECT MESSAGE_ID
                FROM TB_USER_MESSAGE
                WHERE ID_PK IN (
                    SELECT ID_PK
                    FROM TB_USER_MESSAGE_LOG
                    WHERE MESSAGE_STATUS_ID_FK IN (
                        SELECT ID_PK
                        FROM TB_D_MESSAGE_STATUS
                        WHERE STATUS IN ('SEND_ENQUEUED', 'WAITING_FOR_RETRY', 'READY_TO_PULL', 'WAITING_FOR_RECEIPT')
                    ) AND RECEIVED BETWEEN migration.startDate AND migration.endDate));

        TYPE T_ACTION_AUDIT IS TABLE OF c_action_audit%ROWTYPE;
        action_audit T_ACTION_AUDIT;
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Migrating TB_ACTION_AUDIT entries...');

        OPEN c_action_audit;
        LOOP
            FETCH c_action_audit BULK COLLECT INTO action_audit LIMIT BULK_COLLECT_LIMIT;
            EXIT WHEN action_audit.COUNT = 0;

            FOR i IN action_audit.FIRST .. action_audit.LAST LOOP
                EXECUTE IMMEDIATE 'INSERT INTO TB_ACTION_AUDIT@' || db_link || ' (ID_PK, AUDIT_TYPE, ENTITY_ID, MODIFICATION_TYPE, REVISION_DATE, USER_NAME, FROM_QUEUE, TO_QUEUE, CREATION_TIME, CREATED_BY) VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p_10)'
                    USING action_audit(i).ID_PK,
                        action_audit(i).AUDIT_TYPE,
                        action_audit(i).ENTITY_ID,
                        action_audit(i).MODIFICATION_TYPE,
                        action_audit(i).REVISION_DATE,
                        action_audit(i).USER_NAME,
                        action_audit(i).FROM_QUEUE,
                        action_audit(i).TO_QUEUE,
                        action_audit(i).CREATION_TIME,
                        action_audit(i).CREATED_BY;
                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Wrote ' || action_audit.COUNT || ' records');
        END LOOP;
        CLOSE c_action_audit;
    END migrate_action_audit;

    PROCEDURE migrate(db_link IN VARCHAR2, migration IN T_MIGRATION_DETAILS DEFAULT T_MIGRATION_DETAILS()) IS
    BEGIN
        DBMS_OUTPUT.PUT_LINE('Started migration between startDate=' || migration.startDate || ' and endDate=' || migration.endDate);

        migrate_tb_d_message_status(db_link);
        migrate_tb_d_party(db_link);
        migrate_tb_d_part_property(db_link);
        migrate_tb_d_mpc(db_link);
        migrate_tb_d_role(db_link);
        migrate_tb_d_service(db_link);
        migrate_tb_d_agreement(db_link);
        migrate_tb_d_action(db_link);
        migrate_tb_d_msh_role(db_link);
        migrate_tb_d_timezone_offset(db_link);
        migrate_tb_d_notification_status(db_link);
        migrate_tb_d_message_property(db_link);

        migrate_user_message(db_link, migration);
        migrate_signal_message(db_link, migration);
        migrate_user_message_log(db_link, migration);
        migrate_signal_message_log(db_link, migration);
        migrate_user_message_raw(db_link, migration);
        migrate_signal_message_raw(db_link, migration);
        migrate_sj_message_header(db_link, migration);
        migrate_sj_message_group(db_link, migration);
        migrate_sj_message_fragment(db_link, migration);
        migrate_ws_plg_msg_log(db_link, migration);
        migrate_ws_plg_backend_msg_log(db_link, migration);
        migrate_message_properties(db_link, migration);
        migrate_message_acknw(db_link, migration);
        migrate_message_acknw_prop(db_link, migration);
        migrate_messaging_lock(db_link, migration);
        migrate_receipt(db_link, migration);
        migrate_send_attempt(db_link, migration);
        migrate_error_log(db_link, migration);
        migrate_part_info(db_link, migration);
        migrate_part_properties(db_link, migration);
        migrate_action_audit(db_link, migration);

        DBMS_OUTPUT.PUT_LINE('Done');
        DBMS_OUTPUT.PUT_LINE('Please review the changes and either COMMIT them or ROLLBACK!');
    END migrate;

END MIGRATE_ONGOING_MESSAGES_50;
/

--
-- Uncomment trailing line to execute the MIGRATE_ONGOING_MESSAGES_50.MIGRATE(..) procedure and to clean up the temporary tables
-- Note: COMMIT or ROLLBACK at the end or immediately after invoking it (if you uncomment the automatic COMMIT)
--
--declare
--     DB_LINK VARCHAR2(4000);
--     MIGRATION MIGRATE_ONGOING_MESSAGES_50.T_MIGRATION_DETAILS;
-- begin
--     -- Use the correct database link
--     DB_LINK := 'DATABASE_LINK_NAME_v50';
--
--     -- Uncomment to use custom start and end date values
--     --MIGRATION.startDate := TIMESTAMP '2021-03-11 00:00:00.01';
--     --MIGRATION.endDate := TIMESTAMP '2021-03-11 23:59:59.99';
--
--     MIGRATE_ONGOING_MESSAGES_50.MIGRATE(
--             DB_LINK => DB_LINK,
--             MIGRATION => MIGRATION
--     );
--
--     -- Uncomment to automatically COMMIT
-- --     COMMIT;
-- end;
-- /
--
-- -- clean migration PKs tables
-- DECLARE
--     table_does_not_exist exception;
--     PRAGMA EXCEPTION_INIT(table_does_not_exist, -942);
-- BEGIN
--     BEGIN
--         EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_MESSAGE_STATUS';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_MESSAGE_STATUS: table does not exist');
--     END;
--
--     BEGIN
--         EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PARTY';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PARTY: table does not exist');
--     END;
--
--     BEGIN
--         EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PART_PROPERTY';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PART_PROPERTY: table does not exist');
--     END;
--
--     BEGIN
--         EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_MPC';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_MPC: table does not exist');
--     END;
--
--     BEGIN
--         EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_ROLE';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_ROLE: table does not exist');
--     END;
--
--     BEGIN
--         EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_SERVICE';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_SERVICE: table does not exist');
--     END;
--
--     BEGIN
--         EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_AGREEMENT';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_AGREEMENT: table does not exist');
--     END;
--
--     BEGIN
--         EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_ACTION';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_ACTION: table does not exist');
--     END;
--
--     BEGIN
--         EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_MSH_ROLE';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_MSH_ROLE: table does not exist');
--     END;
--
--     BEGIN
--         EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_TIMEZONE_OFFSET';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_TIMEZONE_OFFSET: table does not exist');
--     END;
--
--     BEGIN
--         EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_NOTIFIC_STATUS';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_NOTIFIC_STATUS: table does not exist');
--     END;
--
--     BEGIN
--         EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_MESSAGE_PROPERTY';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_MESSAGE_PROPERTY: table does not exist');
--     END;
--
--     BEGIN
--         EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_MESSAGE_STATUS CASCADE CONSTRAINTS';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_MESSAGE_STATUS: table does not exist');
--     END;
--
--     BEGIN
--         EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PARTY CASCADE CONSTRAINTS';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PARTY: table does not exist');
--     END;
--
--     BEGIN
--         EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PART_PROPERTY CASCADE CONSTRAINTS';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PART_PROPERTY: table does not exist');
--     END;
--
--     BEGIN
--         EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_MPC CASCADE CONSTRAINTS';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_MPC: table does not exist');
--     END;
--
--     BEGIN
--         EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_ROLE CASCADE CONSTRAINTS';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_ROLE: table does not exist');
--     END;
--
--     BEGIN
--         EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_SERVICE CASCADE CONSTRAINTS';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_SERVICE: table does not exist');
--     END;
--
--     BEGIN
--         EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_AGREEMENT CASCADE CONSTRAINTS';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_AGREEMENT: table does not exist');
--     END;
--
--     BEGIN
--         EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_ACTION CASCADE CONSTRAINTS';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_ACTION: table does not exist');
--     END;
--
--     BEGIN
--         EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_MSH_ROLE CASCADE CONSTRAINTS';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_MSH_ROLE: table does not exist');
--     END;
--
--     BEGIN
--         EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_TIMEZONE_OFFSET CASCADE CONSTRAINTS';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_TIMEZONE_OFFSET: table does not exist');
--     END;
--
--     BEGIN
--         EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_NOTIFIC_STATUS CASCADE CONSTRAINTS';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_NOTIFIC_STATUS: table does not exist');
--     END;
--
--     BEGIN
--         EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_MESSAGE_PROPERTY CASCADE CONSTRAINTS';
--     EXCEPTION
--         WHEN table_does_not_exist THEN
--             DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_MESSAGE_PROPERTY: table does not exist');
--     END;
-- END;
-- /