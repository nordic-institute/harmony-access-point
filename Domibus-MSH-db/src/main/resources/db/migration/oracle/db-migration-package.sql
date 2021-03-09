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
        v_sql        LONG;
        v_temp_table VARCHAR2(30) := TEMP_PREFIX || 'TB_USER_MESSAGE';
        CURSOR c_user_message IS
            SELECT UM.ID_PK,
                   MI.MESSAGE_ID           MESSAGE_ID,
                   MI.REF_TO_MESSAGE_ID    REF_TO_MESSAGE_ID,
                   UM.COLL_INFO_CONVERS_ID CONVERSATION_ID,
                   ML.SOURCE_MESSAGE       SOURCE_MESSAGE,
                   ML.MESSAGE_FRAGMENT     MESSAGE_FRAGMENT,
                   MI.TIME_STAMP           EBMS3_TIMESTAMP
            FROM TB_MESSAGE_LOG ML
                     LEFT OUTER JOIN TB_MESSAGE_INFO MI ON ML.MESSAGE_ID = MI.MESSAGE_ID,
                 TB_USER_MESSAGE UM
            WHERE UM.MESSAGEINFO_ID_PK = MI.ID_PK;
        TYPE T_USER_MESSAGE IS TABLE OF c_user_message%ROWTYPE;
        user_message T_USER_MESSAGE;
    BEGIN
        drop_table(v_temp_table);
        v_sql := 'CREATE TABLE ' || v_temp_table ||
                 '(ID_PK NUMBER(38, 0) NOT NULL, MESSAGE_ID VARCHAR2(255), REF_TO_MESSAGE_ID VARCHAR2(255), CONVERSATION_ID VARCHAR2(255), SPLIT_AND_JOIN NUMBER(1), SOURCE_MESSAGE NUMBER(1), MESSAGE_FRAGMENT NUMBER(1), EBMS3_TIMESTAMP TIMESTAMP, ACTION_ID_FK NUMBER(38, 0), AGREEMENT_ID_FK NUMBER(38, 0), SERVICE_ID_FK NUMBER(38, 0), MPC_ID_FK NUMBER(38, 0), FROM_PARTY_ID_FK NUMBER(38, 0), FROM_ROLE_ID_FK NUMBER(38, 0), TO_PARTY_ID_FK NUMBER(38, 0), TO_ROLE_ID_FK NUMBER(38, 0), MESSAGE_SUBTYPE_ID_FK NUMBER(38, 0), CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_USER_MESSAGE PRIMARY KEY (ID_PK))';
        EXECUTE IMMEDIATE v_sql;
        DBMS_OUTPUT.PUT_LINE('New table: ' || v_temp_table || ' successfully created.');

        /** migrate old columns */
        DBMS_OUTPUT.PUT_LINE('Migrating TB_USER_MESSAGE entries...');
        OPEN c_user_message;
        LOOP
            FETCH c_user_message BULK COLLECT INTO user_message;
            EXIT WHEN user_message.COUNT = 0;

            FOR i IN user_message.FIRST .. user_message.LAST
                LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO ' || v_temp_table ||
                                      ' (ID_PK, MESSAGE_ID, REF_TO_MESSAGE_ID, CONVERSATION_ID, SOURCE_MESSAGE, MESSAGE_FRAGMENT, EBMS3_TIMESTAMP) ' ||
                                      'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7)'
                        USING user_message(i).ID_PK,
                        user_message(i).MESSAGE_ID,
                        user_message(i).REF_TO_MESSAGE_ID,
                        user_message(i).CONVERSATION_ID,
                        user_message(i).SOURCE_MESSAGE,
                        user_message(i).MESSAGE_FRAGMENT,
                        user_message(i).EBMS3_TIMESTAMP;

                END LOOP;
            dbms_output.put_line('Wrote ' || user_message.COUNT || ' records');
        END LOOP;
        CLOSE c_user_message;


    END migrate_tb_user_message;

    /**-- main entry point for all migration --*/
    PROCEDURE migrate IS
    BEGIN
        migrate_tb_user_message();
    END migrate;


END MIGRATE_42_TO_50;
/