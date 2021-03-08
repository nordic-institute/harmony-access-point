CREATE OR REPLACE PACKAGE MIGRATE_42_TO_50 IS
    TEMP_PREFIX CONSTANT Varchar2(5) := 'TEMP_';

    PROCEDURE drop_table(table_name IN VARCHAR2);

    PROCEDURE migrate_tb_user_message;

    PROCEDURE migrate;

END MIGRATE_42_TO_50;
/

CREATE OR REPLACE PACKAGE BODY MIGRATE_42_TO_50 IS

    PROCEDURE drop_table(table_name IN VARCHAR2) IS
        v_table_exists INT;
    BEGIN
        SELECT COUNT(*) INTO v_table_exists FROM USER_TABLES WHERE table_name = UPPER(table_name);
        IF v_table_exists > 0 THEN
            EXECUTE IMMEDIATE 'DROP TABLE ' || table_name;
            DBMS_OUTPUT.PUT_LINE('Table ' || table_name || ' dropped');
        END IF;
    END drop_table;

    /**-- TB_USER_MESSAGE migration --*/
    PROCEDURE migrate_tb_user_message IS
        v_sql LONG;
        v_temp_table VARCHAR2(30) := TEMP_PREFIX || 'TB_USER_MESSAGE';
    BEGIN

        drop_table(v_temp_table);
        v_sql := 'CREATE TABLE ' || v_temp_table ||
                 '(ID_PK NUMBER(38, 0) NOT NULL, MESSAGE_ID VARCHAR2(255), REF_TO_MESSAGE_ID VARCHAR2(255), CONVERSATION_ID VARCHAR2(255), SPLIT_AND_JOIN NUMBER(1), SOURCE_MESSAGE NUMBER(1), MESSAGE_FRAGMENT NUMBER(1), EBMS3_TIMESTAMP TIMESTAMP, ACTION_ID_FK NUMBER(38, 0), AGREEMENT_ID_FK NUMBER(38, 0), SERVICE_ID_FK NUMBER(38, 0), MPC_ID_FK NUMBER(38, 0), FROM_PARTY_ID_FK NUMBER(38, 0), FROM_ROLE_ID_FK NUMBER(38, 0), TO_PARTY_ID_FK NUMBER(38, 0), TO_ROLE_ID_FK NUMBER(38, 0), MESSAGE_SUBTYPE_ID_FK NUMBER(38, 0), CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_USER_MESSAGE PRIMARY KEY (ID_PK))';
        EXECUTE IMMEDIATE v_sql;
        DBMS_OUTPUT.PUT_LINE('New table: ' || v_temp_table || ' successfully created.');

    END migrate_tb_user_message;

    /**-- main entry point for all migration --*/
    PROCEDURE migrate IS
    BEGIN
        migrate_tb_user_message();
    END migrate;


END MIGRATE_42_TO_50;
/