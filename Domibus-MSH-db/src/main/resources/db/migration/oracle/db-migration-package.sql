CREATE OR REPLACE PACKAGE MIGRATE_42_TO_50 IS
    TEMP_PREFIX CONSTANT Varchar2(5) := 'TEMP_';
    BATCH_SIZE CONSTANT NUMBER := 3;

    PROCEDURE migrate_tb_user_message;

    PROCEDURE migrate;

END MIGRATE_42_TO_50;
/

CREATE OR REPLACE PACKAGE BODY MIGRATE_42_TO_50 IS

    /** -- Helper procedures start -*/
    PROCEDURE drop_table(tab_name IN VARCHAR2) IS
        v_table_exists INT;
    BEGIN
        SELECT COUNT(*) INTO v_table_exists FROM USER_TABLES WHERE table_name = UPPER(tab_name);
        IF v_table_exists > 0 THEN
            EXECUTE IMMEDIATE 'DROP TABLE ' || tab_name;
            DBMS_OUTPUT.PUT_LINE('Table ' || tab_name || ' dropped');
        END IF;
    END drop_table;

    PROCEDURE truncate_or_create_table(tab_name IN VARCHAR2, create_sql IN VARCHAR2) IS
        v_table_exists INT;
    BEGIN
        SELECT COUNT(*) INTO v_table_exists FROM USER_TABLES WHERE table_name = UPPER(tab_name);
        IF v_table_exists > 0 THEN
            EXECUTE IMMEDIATE 'TRUNCATE TABLE ' || tab_name;
            DBMS_OUTPUT.PUT_LINE('Table ' || tab_name || ' truncated');
        ELSE
            EXECUTE IMMEDIATE create_sql;
            DBMS_OUTPUT.PUT_LINE('Table ' || tab_name || ' created');
        END IF;
    END truncate_or_create_table;

    FUNCTION get_tb_d_mpc_record(mpc_value IN VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER := -1;
    BEGIN
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_MPC WHERE VALUE = :1' INTO v_id_pk USING mpc_value;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_MPC: '||mpc_value);
                v_id_pk := HIBERNATE_SEQUENCE.nextval;
                EXECUTE IMMEDIATE 'INSERT INTO TB_D_MPC(ID_PK, VALUE) VALUES (' || v_id_pk || ', :1)' USING mpc_value;
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_mpc_record;

    FUNCTION get_tb_d_role_record(role IN VARCHAR2) RETURN NUMBER IS
        v_id_pk NUMBER := -1;
    BEGIN
        BEGIN
            EXECUTE IMMEDIATE 'SELECT ID_PK FROM TB_D_ROLE WHERE ROLE = :1' INTO v_id_pk USING role;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN
                -- create new record
                DBMS_OUTPUT.PUT_LINE('Add new record into TB_D_ROLE: '||role);
                v_id_pk := HIBERNATE_SEQUENCE.nextval;
                EXECUTE IMMEDIATE 'INSERT INTO TB_D_ROLE(ID_PK, ROLE) VALUES (' || v_id_pk || ', :1)' USING role;
                COMMIT;
        END;
        RETURN v_id_pk;
    END get_tb_d_role_record;
    /** -- Helper procedures end -*/

    /**-- TB_USER_MESSAGE migration --*/
    PROCEDURE migrate_tb_user_message IS
        v_sql        VARCHAR2(1000);
        v_temp_table VARCHAR2(30) := TEMP_PREFIX || 'TB_USER_MESSAGE';
        v_dict_table VARCHAR2(30);
        CURSOR c_user_message IS
            SELECT UM.ID_PK,
                   MI.MESSAGE_ID           MESSAGE_ID,
                   MI.REF_TO_MESSAGE_ID    REF_TO_MESSAGE_ID,
                   UM.COLL_INFO_CONVERS_ID CONVERSATION_ID,
                   ML.SOURCE_MESSAGE       SOURCE_MESSAGE,
                   ML.MESSAGE_FRAGMENT     MESSAGE_FRAGMENT,
                   MI.TIME_STAMP           EBMS3_TIMESTAMP,
                   UM.MPC                  MPC,
                   UM.FROM_ROLE             FROM_ROLE,
                   UM.TO_ROLE           TO_ROLE
            FROM TB_MESSAGE_LOG ML
                     LEFT OUTER JOIN TB_MESSAGE_INFO MI ON ML.MESSAGE_ID = MI.MESSAGE_ID,
                 TB_USER_MESSAGE UM
            WHERE UM.MESSAGEINFO_ID_PK = MI.ID_PK;
        TYPE T_USER_MESSAGE IS TABLE OF c_user_message%ROWTYPE;
        user_message T_USER_MESSAGE;
        v_batch_no   INT          := 1;
    BEGIN
        v_sql := 'CREATE TABLE ' || v_temp_table ||
                 '(ID_PK NUMBER(38, 0) NOT NULL, MESSAGE_ID VARCHAR2(255), REF_TO_MESSAGE_ID VARCHAR2(255), CONVERSATION_ID VARCHAR2(255), SPLIT_AND_JOIN NUMBER(1), SOURCE_MESSAGE NUMBER(1), MESSAGE_FRAGMENT NUMBER(1), EBMS3_TIMESTAMP TIMESTAMP, ACTION_ID_FK NUMBER(38, 0), AGREEMENT_ID_FK NUMBER(38, 0), SERVICE_ID_FK NUMBER(38, 0), MPC_ID_FK NUMBER(38, 0), FROM_PARTY_ID_FK NUMBER(38, 0), FROM_ROLE_ID_FK NUMBER(38, 0), TO_PARTY_ID_FK NUMBER(38, 0), TO_ROLE_ID_FK NUMBER(38, 0), MESSAGE_SUBTYPE_ID_FK NUMBER(38, 0), CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_USER_MESSAGE PRIMARY KEY (ID_PK))';
        truncate_or_create_table(v_temp_table, v_sql);

        v_sql := 'CREATE TABLE TB_D_MPC (ID_PK NUMBER(38, 0) NOT NULL, VALUE VARCHAR2(255) NOT NULL, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_D_MPC PRIMARY KEY (ID_PK))';
        v_dict_table := 'TB_D_MPC';
        truncate_or_create_table(v_dict_table, v_sql);

        v_sql := 'CREATE TABLE TB_D_ROLE (ID_PK NUMBER(38, 0) NOT NULL, ROLE VARCHAR2(255) NOT NULL, CREATION_TIME TIMESTAMP DEFAULT sysdate NOT NULL, CREATED_BY VARCHAR2(255) DEFAULT user NOT NULL, MODIFICATION_TIME TIMESTAMP, MODIFIED_BY VARCHAR2(255), CONSTRAINT PK_D_ROLE PRIMARY KEY (ID_PK))';
        v_dict_table := 'TB_D_ROLE';
        truncate_or_create_table(v_dict_table, v_sql);

        /** migrate old columns */
        DBMS_OUTPUT.PUT_LINE('Start to migrate TB_USER_MESSAGE entries...');
        OPEN c_user_message;
        LOOP
            FETCH c_user_message BULK COLLECT INTO user_message;
            EXIT WHEN user_message.COUNT = 0;

            FOR i IN user_message.FIRST .. user_message.LAST
                LOOP
                    EXECUTE IMMEDIATE 'INSERT INTO ' || v_temp_table ||
                                      ' (ID_PK, MESSAGE_ID, REF_TO_MESSAGE_ID, CONVERSATION_ID, SOURCE_MESSAGE, MESSAGE_FRAGMENT, EBMS3_TIMESTAMP, MPC_ID_FK, FROM_ROLE_ID_FK, TO_ROLE_ID_FK) ' ||
                                      'VALUES (:p_1, :p_2, :p_3, :p_4, :p_5, :p_6, :p_7, :p_8, :p_9, :p10)'
                        USING user_message(i).ID_PK,
                        user_message(i).MESSAGE_ID,
                        user_message(i).REF_TO_MESSAGE_ID,
                        user_message(i).CONVERSATION_ID,
                        user_message(i).SOURCE_MESSAGE,
                        user_message(i).MESSAGE_FRAGMENT,
                        user_message(i).EBMS3_TIMESTAMP,
                        get_tb_d_mpc_record(user_message(i).MPC),
                        get_tb_d_role_record(user_message(i).FROM_ROLE),
                        get_tb_d_role_record(user_message(i).TO_ROLE);
                    IF i MOD BATCH_SIZE = 0 THEN
                        COMMIT;
                        DBMS_OUTPUT.PUT_LINE('Commit after ' || BATCH_SIZE * v_batch_no || ' records');
                        v_batch_no := v_batch_no + 1;
                    END IF;

                END LOOP;
            DBMS_OUTPUT.PUT_LINE('Wrote ' || user_message.COUNT || ' records in total into ' || v_temp_table);
        END LOOP;

        COMMIT;
        CLOSE c_user_message;


    END migrate_tb_user_message;

    /**-- main entry point for all migration --*/
    PROCEDURE migrate IS
    BEGIN
        migrate_tb_user_message();
    END migrate;


END MIGRATE_42_TO_50;
/