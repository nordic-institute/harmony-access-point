-- ********************************************************************************************************
-- Domibus 4.2.3 to 5.0 data migration subprograms
--
-- Main entry point is the procedure 'MIGRATE_42_TO_50_migrate'.
--
-- Parameters to be adjusted:
-- BATCH_SIZE - size of the batch for data migration on each migrated table after which there is a commit;
--              default value is 10000
-- VERBOSE_LOGS - more information into the logs; default to false
--
-- Tables which are migrated: TB_USER_MESSAGE, TB_MESSAGE_FRAGMENT, TB_MESSAGE_GROUP, TB_MESSAGE_HEADER,
-- TB_MESSAGE_LOG, TB_RECEIPT, TB_RECEIPT_DATA, TB_RAWENVELOPE_LOG, TB_PROPERTY, TB_PART_INFO,
-- TB_ERROR_LOG, TB_MESSAGE_ACKNW, TB_SEND_ATTEMPT
-- ********************************************************************************************************
DELIMITER //

-- batch size for commit of the migrated records
SET @BATCH_SIZE := 10000
//

-- enable more verbose logs
SET @VERBOSE_LOGS := FALSE
//

DROP FUNCTION IF EXISTS MIGRATE_42_TO_50_check_table_exists
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_trace
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_log_verbose
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_drop_table_if_exists
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_truncate_or_create_table
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_check_counts
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_d_mpc_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_d_role_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_d_msh_role_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_d_service_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_d_msg_status_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_d_agreement_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_d_action_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_d_party_rec
//

DROP FUNCTION IF EXISTS MIGRATE_42_TO_50_get_msg_subtype
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_d_notif_status_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_user_message_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_signal_message_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_d_msg_property_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_get_tb_d_part_property_rec
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_user_message
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_message_fragment
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_message_group
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_message_header
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_signal_receipt
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_raw_envelope_log
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_message_log
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_property
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_part_info_user
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_part_info_property
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_error_log
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_message_acknw
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate_send_attempt
//

DROP PROCEDURE IF EXISTS MIGRATE_42_TO_50_migrate
//

/** -- Helper procedures and functions start -*/
CREATE FUNCTION MIGRATE_42_TO_50_check_table_exists(in_tab_name VARCHAR(64))
RETURNS BOOLEAN
READS SQL DATA
    BEGIN
        DECLARE v_table_exists INT;
        SELECT COUNT(*) INTO v_table_exists FROM information_schema.TABLES WHERE UPPER(table_name) = UPPER(in_tab_name);
        RETURN v_table_exists > 0;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_trace(in_message MEDIUMTEXT)
    BEGIN
        SELECT in_message AS trace;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_log_verbose(in_message MEDIUMTEXT)
    BEGIN
        IF @VERBOSE_LOGS THEN
            CALL MIGRATE_42_TO_50_trace(in_message);
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_drop_table_if_exists(in_tab_name VARCHAR(64))
    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION
            BEGIN
                 GET DIAGNOSTICS CONDITION 1
                     @p2 = MESSAGE_TEXT;
                CALL MIGRATE_42_TO_50_trace(CONCAT('drop_table_if_exists for ', in_tab_name, ' -> execute immediate error: ', @p2));
            END;

        IF MIGRATE_42_TO_50_check_table_exists(in_tab_name) THEN
            BEGIN
                SET @q := CONCAT('DROP TABLE ', in_tab_name);
                PREPARE stmt FROM @q;
                EXECUTE stmt;
                DEALLOCATE PREPARE stmt;

                CALL MIGRATE_42_TO_50_trace(CONCAT('Table ', in_tab_name, ' dropped'));
            END;
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_truncate_or_create_table(in_tab_name VARCHAR(64), in_create_sql MEDIUMTEXT)
    BEGIN
        DECLARE EXIT HANDLER FOR SQLEXCEPTION
            BEGIN
                GET DIAGNOSTICS CONDITION 1
                    @p2 = MESSAGE_TEXT;
                CALL MIGRATE_42_TO_50_trace(CONCAT('Execute immediate error: ', @p2));
            END;

        IF MIGRATE_42_TO_50_check_table_exists(in_tab_name) THEN
            SET @q := CONCAT('TRUNCATE TABLE ', in_tab_name);
            PREPARE stmt FROM @q;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;

            CALL MIGRATE_42_TO_50_trace(CONCAT('Table ', in_tab_name, ' truncated'));
        ELSE
            SET @q := in_create_sql;
            PREPARE stmt FROM @q;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;

            CALL MIGRATE_42_TO_50_trace(CONCAT('Table ', in_tab_name, ' created'));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_check_counts(in_tab_name1 VARCHAR(64), in_tab_name2 VARCHAR(64), OUT out_count_match BOOLEAN)
    BEGIN
        SET @v_count_tab1 := 0;
        SET @v_count_tab2 := 0;
        BEGIN
            DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                        @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('check_counts -> execute immediate error: ', @p2));
                END;

            SET @q := CONCAT('SELECT COUNT(*) INTO @v_count_tab1 FROM ', in_tab_name1);
            PREPARE stmt FROM @q;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;

            SET @q := CONCAT('SELECT COUNT(*) INTO @v_count_tab2 FROM ', in_tab_name2);
            PREPARE stmt FROM @q;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
        END;

        SELECT @v_count_tab1 = @v_count_tab2 INTO out_count_match;
        IF out_count_match THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT('Table ', in_tab_name1, ' has same number of records as table ',
                                in_tab_name2, ' records=', @v_count_tab1));
        ELSE
            CALL MIGRATE_42_TO_50_trace(CONCAT('Table ', in_tab_name1,
                ' has different number of records as table ', in_tab_name2));
        END IF;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_d_mpc_rec(in_mpc VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_mpc IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record added into TB_D_MPC');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('Add new record into TB_D_MPC: ', in_mpc));

                    -- create new record
                    INSERT INTO TB_D_MPC (VALUE) VALUES (in_mpc);
                    COMMIT;

                    SET out_id_pk := LAST_INSERT_ID();
                END;

            SELECT ID_PK INTO out_id_pk FROM TB_D_MPC WHERE VALUE = in_mpc;
        END;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_d_role_rec(in_role VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_role IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record added into TB_D_ROLE');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('Add new record into TB_D_ROLE: ', in_role));

                    -- create new record
                    INSERT INTO TB_D_ROLE(ROLE) VALUES (in_role);
                    COMMIT;

                    SET out_id_pk := LAST_INSERT_ID();
                END;

            SELECT ID_PK INTO out_id_pk FROM TB_D_ROLE WHERE ROLE = in_role;
        END;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_d_msh_role_rec(in_role VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_role IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record added into TB_D_MSH_ROLE');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('Add new record into TB_D_MSH_ROLE: ', in_role));

                    -- create new record
                    INSERT INTO TB_D_MSH_ROLE(ROLE) VALUES (in_role);
                    COMMIT;

                    SET out_id_pk := LAST_INSERT_ID();
                END;

            SELECT ID_PK INTO out_id_pk FROM TB_D_MSH_ROLE WHERE ROLE = in_role;
        END;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_d_service_rec(in_service_type VARCHAR(255), in_service_value VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_service_type IS NULL AND in_service_value IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record added into TB_D_SERVICE');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('Add new record into TB_D_SERVICE: ',
                            COALESCE(in_service_type, ''), ' , ', COALESCE(in_service_value, '')));

                    -- create new record
                    INSERT INTO TB_D_SERVICE(TYPE, VALUE) VALUES (in_service_type, in_service_value);
                    COMMIT;

                    SET out_id_pk := LAST_INSERT_ID();
                END;

            SELECT ID_PK INTO out_id_pk FROM TB_D_SERVICE WHERE TYPE = in_service_type AND VALUE = in_service_value;
        END;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_d_msg_status_rec(in_message_status VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_message_status IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record added into TB_D_MESSAGE_STATUS');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('Add new record into TB_D_MESSAGE_STATUS: ', in_message_status));

                    -- create new record
                    INSERT INTO TB_D_MESSAGE_STATUS(STATUS) VALUES (in_message_status);
                    COMMIT;

                    SET out_id_pk := LAST_INSERT_ID();
                END;

            SELECT ID_PK INTO out_id_pk FROM TB_D_MESSAGE_STATUS WHERE STATUS = in_message_status;
        END;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_d_agreement_rec(in_agreement_type VARCHAR(255), in_agreement_value VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_agreement_type IS NULL AND in_agreement_value IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record added into TB_D_AGREEMENT');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('Add new record into TB_D_AGREEMENT: ',
                            COALESCE(in_agreement_type, ''), ' , ', COALESCE(in_agreement_value, '')));

                    -- create new record
                    INSERT INTO TB_D_AGREEMENT(TYPE, VALUE) VALUES (in_agreement_type, in_agreement_value);
                    COMMIT;

                    SET out_id_pk := LAST_INSERT_ID();
                END;

            SELECT ID_PK INTO out_id_pk FROM TB_D_AGREEMENT WHERE TYPE = in_agreement_type AND VALUE = in_agreement_value;
        END;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_d_action_rec(in_action VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_action IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record added into TB_D_ACTION');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('Add new record into TB_D_ACTION: ', in_action));

                    -- create new record
                    INSERT INTO TB_D_ACTION(ACTION) VALUES (in_action);
                    COMMIT;

                    SET out_id_pk := LAST_INSERT_ID();
                END;

            SELECT ID_PK INTO out_id_pk FROM TB_D_ACTION WHERE ACTION = in_action;
        END;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_d_party_rec(in_party_type VARCHAR(255), in_party_value VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_party_type IS NULL AND in_party_value IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record added into TB_D_PARTY');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('Add new record into TB_D_PARTY: ',
                            COALESCE(in_party_type, ''), ' , ', COALESCE(in_party_value, '')));

                    -- create new record
                    INSERT INTO TB_D_PARTY(TYPE, VALUE) VALUES (in_party_type, in_party_value);
                    COMMIT;

                    SET out_id_pk := LAST_INSERT_ID();
                END;

            SELECT ID_PK INTO out_id_pk FROM TB_D_PARTY WHERE TYPE = in_party_type AND VALUE = in_party_value;
        END;
    END
//

CREATE FUNCTION MIGRATE_42_TO_50_get_msg_subtype(in_msg_subtype VARCHAR(255))
RETURNS BOOLEAN
DETERMINISTIC
    BEGIN
        DECLARE test_message BOOLEAN DEFAULT FALSE;
        IF in_msg_subtype = 'TEST' THEN
            SET test_message := TRUE;
        END IF;
        RETURN test_message;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_d_notif_status_rec(in_status VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_status IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record added into TB_D_NOTIFICATION_STATUS');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('Add new record into TB_D_NOTIFICATION_STATUS: ', in_status));

                    -- create new record
                    INSERT INTO TB_D_NOTIFICATION_STATUS(STATUS) VALUES (in_status);
                    COMMIT;

                    SET out_id_pk := LAST_INSERT_ID();
                END;

            SELECT ID_PK INTO out_id_pk FROM TB_D_NOTIFICATION_STATUS WHERE STATUS = in_status;
        END;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_user_message_rec(in_message_id VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_message_id IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record to look into MIGR_TB_USER_MESSAGE');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('No record found into MIGR_TB_USER_MESSAGE for MESSAGE_ID = ', in_message_id));
                END;

            -- TODO check index on message_id column?
            SELECT ID_PK INTO out_id_pk FROM MIGR_TB_USER_MESSAGE WHERE MESSAGE_ID = in_message_id;
        END;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_signal_message_rec(in_message_id VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_message_id IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record to look into MIGR_TB_SIGNAL_MESSAGE');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('No record found into MIGR_TB_SIGNAL_MESSAGE for MESSAGE_ID = ', in_message_id));
                END;

            -- TODO check index on signal_message_id column?
            SELECT ID_PK INTO out_id_pk FROM MIGR_TB_SIGNAL_MESSAGE WHERE SIGNAL_MESSAGE_ID = in_message_id;
        END;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_d_msg_property_rec(in_prop_name VARCHAR(255), in_prop_value VARCHAR(1024),
        in_prop_type VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_prop_name IS NULL AND in_prop_value IS NULL AND in_prop_type IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record added into TB_D_MESSAGE_PROPERTY');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('Add new record into TB_D_MESSAGE_PROPERTY: ',
                            COALESCE(in_prop_name, ''), ' , ', COALESCE(in_prop_value, ''), ' , ',
                            COALESCE(in_prop_type, '')));

                    -- create new record
                    INSERT INTO TB_D_MESSAGE_PROPERTY(NAME, VALUE, TYPE) VALUES (in_prop_name, in_prop_value, in_prop_type);
                    COMMIT;

                    SET out_id_pk := LAST_INSERT_ID();
                END;

            SELECT ID_PK INTO out_id_pk FROM TB_D_MESSAGE_PROPERTY WHERE (NAME = in_prop_name AND TYPE = in_prop_type
                    AND VALUE = in_prop_value) OR (NAME = in_prop_name AND TYPE IS NULL AND VALUE = in_prop_value);
        END;
    END
//

CREATE PROCEDURE MIGRATE_42_TO_50_get_tb_d_part_property_rec(in_prop_name VARCHAR(255), in_prop_value VARCHAR(1024),
        in_prop_type VARCHAR(255), OUT out_id_pk BIGINT)
    sp: BEGIN
        IF in_prop_name IS NULL AND in_prop_value IS NULL AND in_prop_type IS NULL THEN
            CALL MIGRATE_42_TO_50_log_verbose('No record added into TB_D_PART_PROPERTY');
            LEAVE sp;
        END IF;

        BEGIN
            DECLARE EXIT HANDLER FOR NOT FOUND
                BEGIN
                    CALL MIGRATE_42_TO_50_trace(CONCAT('Add new record into TB_D_PART_PROPERTY: ',
                            COALESCE(in_prop_name, ''), ' , ', COALESCE(in_prop_value, ''), ' , ',
                            COALESCE(in_prop_type, '')));

                    -- create new record
                    INSERT INTO TB_D_PART_PROPERTY(NAME, VALUE, TYPE) VALUES (in_prop_name, in_prop_value, in_prop_type);
                    COMMIT;

                    SET out_id_pk := LAST_INSERT_ID();
                END;

            SELECT ID_PK INTO out_id_pk FROM TB_D_PART_PROPERTY WHERE (NAME = in_prop_name AND VALUE = in_prop_value
                    AND TYPE = in_prop_type) OR (NAME = in_prop_name AND VALUE = in_prop_value AND TYPE IS NULL);
        END;
    END
//

/**-- TB_USER_MESSAGE migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_user_message()
    BEGIN
        DECLARE action VARCHAR(255);
        DECLARE agreement_ref_type VARCHAR(255);
        DECLARE agreement_ref_value VARCHAR(255);
        DECLARE conversation_id VARCHAR(255);
        DECLARE ebms3_timestamp TIMESTAMP;
        DECLARE from_party_type VARCHAR(255);
        DECLARE from_party_value VARCHAR(255);
        DECLARE from_role VARCHAR(255);
        DECLARE id_pk BIGINT;
        DECLARE message_fragment BIT(1);
        DECLARE message_id VARCHAR(255);
        DECLARE message_subtype VARCHAR(255);
        DECLARE mpc VARCHAR(255);
        DECLARE ref_to_message_id VARCHAR(255);
        DECLARE service_type VARCHAR(255);
        DECLARE service_value VARCHAR(255);
        DECLARE source_message BIT(1);
        DECLARE to_party_type VARCHAR(255);
        DECLARE to_party_value VARCHAR(255);
        DECLARE to_role VARCHAR(255);

        DECLARE calculated_mpc_id_fk BIGINT;
        DECLARE calculated_from_role_id_fk BIGINT;
        DECLARE calculated_to_role_id_fk BIGINT;
        DECLARE calculated_service_id_fk BIGINT;
        DECLARE calculated_agreement_id_fk BIGINT;
        DECLARE calculated_action_id_fk BIGINT;
        DECLARE calculated_from_party_id_fk BIGINT;
        DECLARE calculated_to_party_id_fk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_user_message CURSOR FOR
            SELECT UM.ID_PK,
                   MI.MESSAGE_ID,
                   MI.REF_TO_MESSAGE_ID,
                   UM.COLL_INFO_CONVERS_ID      CONVERSATION_ID,
                   ML.SOURCE_MESSAGE,
                   ML.MESSAGE_FRAGMENT,
                   MI.TIME_STAMP                EBMS3_TIMESTAMP,
                   UM.MPC,
                   UM.FROM_ROLE,
                   UM.TO_ROLE,
                   UM.SERVICE_TYPE,
                   UM.SERVICE_VALUE,
                   UM.AGREEMENT_REF_TYPE,
                   UM.AGREEMENT_REF_VALUE,
                   UM.COLLABORATION_INFO_ACTION ACTION,
                   PA1.TYPE                     FROM_PARTY_TYPE,
                   PA1.VALUE                    FROM_PARTY_VALUE,
                   PA2.TYPE                     TO_PARTY_TYPE,
                   PA2.VALUE                    TO_PARTY_VALUE,
                   ML.MESSAGE_SUBTYPE
            FROM TB_MESSAGE_LOG ML
                     LEFT OUTER JOIN TB_MESSAGE_INFO MI ON ML.MESSAGE_ID = MI.MESSAGE_ID,
                 TB_USER_MESSAGE UM
                     LEFT OUTER JOIN TB_PARTY_ID PA1 ON UM.ID_PK = PA1.FROM_ID
                     LEFT OUTER JOIN TB_PARTY_ID PA2 ON UM.ID_PK = PA2.TO_ID
            WHERE UM.MESSAGEINFO_ID_PK = MI.ID_PK;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_USER_MESSAGE';
        SET @v_tab_new := 'MIGR_TB_USER_MESSAGE';

        /** migrate old columns and add data into dictionary tables */
        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_user_message;
        read_loop: LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                    BEGIN
                        GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                        CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_user_message -> execute immediate error: ', @p2));
                    END;

                FETCH c_user_message INTO id_pk, message_id, ref_to_message_id, conversation_id, source_message,
                        message_fragment, ebms3_timestamp, mpc, from_role, to_role, service_type, service_value,
                        agreement_ref_type, agreement_ref_value, action, from_party_type, from_party_value,
                        to_party_type, to_party_value, message_subtype;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_get_tb_d_mpc_rec(mpc, calculated_mpc_id_fk);
                CALL MIGRATE_42_TO_50_get_tb_d_role_rec(from_role, calculated_from_role_id_fk);
                CALL MIGRATE_42_TO_50_get_tb_d_role_rec(to_role, calculated_to_role_id_fk);
                CALL MIGRATE_42_TO_50_get_tb_d_service_rec(service_type, service_value, calculated_service_id_fk);
                CALL MIGRATE_42_TO_50_get_tb_d_agreement_rec(agreement_ref_type, agreement_ref_value, calculated_agreement_id_fk);
                CALL MIGRATE_42_TO_50_get_tb_d_action_rec(action, calculated_action_id_fk);
                CALL MIGRATE_42_TO_50_get_tb_d_party_rec(from_party_type, from_party_value, calculated_from_party_id_fk);
                CALL MIGRATE_42_TO_50_get_tb_d_party_rec(to_party_type, to_party_value, calculated_to_party_id_fk);

                 INSERT INTO MIGR_TB_USER_MESSAGE (ID_PK, MESSAGE_ID, REF_TO_MESSAGE_ID, CONVERSATION_ID, SOURCE_MESSAGE,
                         MESSAGE_FRAGMENT, EBMS3_TIMESTAMP, MPC_ID_FK , FROM_ROLE_ID_FK, TO_ROLE_ID_FK, SERVICE_ID_FK,
                         AGREEMENT_ID_FK, ACTION_ID_FK, FROM_PARTY_ID_FK, TO_PARTY_ID_FK, TEST_MESSAGE)
                 VALUES (id_pk,
                         message_id,
                         ref_to_message_id,
                         conversation_id,
                         source_message,
                         message_fragment,
                         ebms3_timestamp,
                         calculated_mpc_id_fk,
                         calculated_from_role_id_fk,
                         calculated_to_role_id_fk,
                         calculated_service_id_fk,
                         calculated_agreement_id_fk,
                         calculated_action_id_fk,
                         calculated_from_party_id_fk,
                         calculated_to_party_id_fk,
                         MIGRATE_42_TO_50_get_msg_subtype(message_subtype));

                 SET @i := @i + 1;
                 IF @i MOD @BATCH_SIZE = 0 THEN
                     COMMIT;
                     CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                     SET @v_batch_no := @v_batch_no + 1;
                 END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_user_message;

        -- check counts
        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

/**-- TB_MESSAGE_FRAGMENT migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_message_fragment()
    BEGIN
        DECLARE created_by VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE fragment_number INT;
        DECLARE group_id VARCHAR(255);
        DECLARE group_id_fk BIGINT;
        DECLARE id_pk BIGINT;
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_message_fragment CURSOR FOR
            SELECT UM.ID_PK, -- 1:1 ID_PK implementation
                   MF.GROUP_ID,
                   MF.FRAGMENT_NUMBER,
                   MF.CREATION_TIME,
                   MF.CREATED_BY,
                   MF.MODIFICATION_TIME,
                   MF.MODIFIED_BY,
                   MG.ID_PK GROUP_ID_FK
            FROM TB_MESSAGE_FRAGMENT MF,
                 TB_MESSAGE_GROUP MG,
                 TB_USER_MESSAGE UM
            WHERE UM.FK_MESSAGE_FRAGMENT_ID = MF.ID_PK
              AND MF.GROUP_ID = MG.GROUP_ID;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_MESSAGE_FRAGMENT';
        SET @v_tab_new := 'MIGR_TB_SJ_MESSAGE_FRAGMENT';
        SET @v_tab_user_message := 'TB_USER_MESSAGE';

        IF NOT MIGRATE_42_TO_50_check_table_exists(@v_tab_user_message) THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_user_message, ' should exists before starting ', @v_tab, ' migration'));
        END IF;

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_message_fragment;
        read_loop: LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                    BEGIN
                        GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                        CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_message_fragment -> execute immediate error: ', @p2));
                    END;

                FETCH c_message_fragment INTO id_pk, group_id, fragment_number, creation_time, created_by,
                        modification_time, modified_by, group_id_fk;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                INSERT INTO MIGR_TB_SJ_MESSAGE_FRAGMENT (ID_PK, GROUP_ID_FK, FRAGMENT_NUMBER, CREATION_TIME,
                        CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (id_pk,
                        group_id_fk,
                        fragment_number,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                                @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Migrated ', @i, ' records in total'));
        CLOSE c_message_fragment;

        -- check counts
        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;

    END
//

/**-- TB_MESSAGE_GROUP migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_message_group()
    BEGIN
        DECLARE compressed_message_size BIGINT;
        DECLARE compression_algorithm VARCHAR(255);
        DECLARE created_by VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE expired BIT(1);
        DECLARE fragment_count INT;
        DECLARE group_id VARCHAR(255);
        DECLARE id_pk BIGINT;
        DECLARE message_size BIGINT;
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);
        DECLARE msh_role VARCHAR(255);
        DECLARE received_fragments INT;
        DECLARE rejected BIT(1);
        DECLARE sent_fragments INT;
        DECLARE soap_action VARCHAR(255);
        DECLARE source_message_id VARCHAR(255);

        DECLARE calculated_msh_role_id_fk BIGINT;
        DECLARE calculated_source_message_id_fk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_message_group CURSOR FOR
            SELECT MG.ID_PK,
                   MG.GROUP_ID,
                   MG.MESSAGE_SIZE,
                   MG.FRAGMENT_COUNT,
                   MG.SENT_FRAGMENTS,
                   MG.RECEIVED_FRAGMENTS,
                   MG.COMPRESSION_ALGORITHM,
                   MG.COMPRESSED_MESSAGE_SIZE,
                   MG.SOAP_ACTION,
                   MG.REJECTED,
                   MG.EXPIRED,
                   MG.MSH_ROLE,
                   MG.SOURCE_MESSAGE_ID,
                   MG.CREATION_TIME,
                   MG.CREATED_BY,
                   MG.MODIFICATION_TIME,
                   MG.MODIFIED_BY
            FROM TB_MESSAGE_GROUP MG;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_MESSAGE_GROUP';
        SET @v_tab_new := 'MIGR_TB_SJ_MESSAGE_GROUP';
        SET @v_tab_user_message_new  := 'MIGR_TB_USER_MESSAGE';

        IF NOT MIGRATE_42_TO_50_check_table_exists(@v_tab_user_message_new) THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_user_message_new, ' should exists before starting ', @v_tab, ' migration'));
        END IF;

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_message_group;
        read_loop: LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                    BEGIN
                        GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                        CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_message_group -> execute immediate error: ', @p2));
                    END;

                FETCH c_message_group INTO id_pk, group_id, message_size, fragment_count, sent_fragments,
                        received_fragments, compression_algorithm, compressed_message_size, soap_action, rejected,
                        expired, msh_role, source_message_id, creation_time, created_by, modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_get_tb_d_msh_role_rec(msh_role, calculated_msh_role_id_fk);
                CALL MIGRATE_42_TO_50_get_tb_user_message_rec(source_message_id, calculated_source_message_id_fk);

                INSERT INTO MIGR_TB_SJ_MESSAGE_GROUP (ID_PK, GROUP_ID, MESSAGE_SIZE, FRAGMENT_COUNT, SENT_FRAGMENTS,
                        RECEIVED_FRAGMENTS, COMPRESSION_ALGORITHM, COMPRESSED_MESSAGE_SIZE, SOAP_ACTION, REJECTED,
                        EXPIRED, MSH_ROLE_ID_FK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY,
                        SOURCE_MESSAGE_ID_FK)
                VALUES (id_pk,
                        group_id,
                        message_size,
                        fragment_count,
                        sent_fragments,
                        received_fragments,
                        compression_algorithm,
                        compressed_message_size,
                        soap_action,
                        rejected,
                        expired,
                        calculated_msh_role_id_fk,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by,
                        calculated_source_message_id_fk);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                                @v_tab_new,  ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Migrated ', @i, ' records in total'));
        CLOSE c_message_group;

        -- check counts
        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;

    END
//

/**-- TB_MESSAGE_GROUP migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_message_header()
    BEGIN
        DECLARE boundary VARCHAR(255);
        DECLARE created_by VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE id_pk BIGINT;
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);
        DECLARE start VARCHAR(255);

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_message_header CURSOR FOR
            SELECT MG.ID_PK, -- 1:1 ID_PK implementation
                   MH.BOUNDARY,
                   MH.START,
                   MH.CREATION_TIME,
                   MH.CREATED_BY,
                   MH.MODIFICATION_TIME,
                   MH.MODIFIED_BY
            FROM TB_MESSAGE_HEADER MH,
                 TB_MESSAGE_GROUP MG
            WHERE MG.FK_MESSAGE_HEADER_ID = MH.ID_PK;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab  := 'TB_MESSAGE_HEADER';
        SET @v_tab_new := 'MIGR_TB_SJ_MESSAGE_HEADER';
        SET @v_tab_message_group := 'TB_MESSAGE_GROUP';

        IF NOT MIGRATE_42_TO_50_check_table_exists(@v_tab_message_group) THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_message_group, ' should exists before starting ', @v_tab, ' migration'));
        END IF;

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_message_header;
        read_loop: LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                    BEGIN
                        GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                        CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_message_header -> execute immediate error: ', @p2));
                    END;

                FETCH c_message_header INTO id_pk, boundary, start, creation_time, created_by, modification_time,
                        modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                INSERT INTO MIGR_TB_SJ_MESSAGE_HEADER (ID_PK, BOUNDARY, START_MULTIPART, CREATION_TIME, CREATED_BY,
                        MODIFICATION_TIME, MODIFIED_BY)
                VALUES (id_pk,
                        boundary,
                        start,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                                @v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Migrated ', @i, ' records in total'));
        CLOSE c_message_header;

        -- check counts
        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;

    END
//

/**-- TB_SIGNAL_MESSAGE, TB_RECEIPT and TB_RECEIPT_DATA migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_signal_receipt()
    BEGIN
        DECLARE created_by VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE ebms3_timestamp TIMESTAMP;
        DECLARE id_pk BIGINT;
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);
        DECLARE r_created_by VARCHAR(255);
        DECLARE r_creation_time TIMESTAMP;
        DECLARE r_modification_time TIMESTAMP;
        DECLARE r_modified_by VARCHAR(255);
        DECLARE raw_xml LONGTEXT;
        DECLARE ref_to_message_id VARCHAR(255);
        DECLARE signal_message_id VARCHAR(255);

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_signal_message_receipt CURSOR FOR
            SELECT UM.ID_PK, --  1:1 here
                   MI.MESSAGE_ID        SIGNAL_MESSAGE_ID,
                   MI.REF_TO_MESSAGE_ID REF_TO_MESSAGE_ID,
                   MI.TIME_STAMP        EBMS3_TIMESTAMP,
                   SM.CREATION_TIME,
                   SM.CREATED_BY,
                   SM.MODIFICATION_TIME,
                   SM.MODIFIED_BY,
                   RD.RAW_XML,
                   RD.CREATION_TIME     R_CREATION_TIME,
                   RD.CREATED_BY        R_CREATED_BY,
                   RD.MODIFICATION_TIME R_MODIFICATION_TIME,
                   RD.MODIFIED_BY       R_MODIFIED_BY
            FROM TB_MESSAGE_INFO MI,
                 TB_SIGNAL_MESSAGE SM,
                 TB_MESSAGING ME,
                 TB_USER_MESSAGE UM,
                 TB_RECEIPT RE,
                 TB_RECEIPT_DATA RD
            WHERE SM.MESSAGEINFO_ID_PK = MI.ID_PK
              AND ME.SIGNAL_MESSAGE_ID = SM.ID_PK
              AND ME.USER_MESSAGE_ID = UM.ID_PK
              AND RE.ID_PK = SM.RECEIPT_ID_PK
              AND RD.RECEIPT_ID = RE.ID_PK;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab_signal := 'TB_SIGNAL_MESSAGE';
        SET @v_tab_signal_new := 'MIGR_TB_SIGNAL_MESSAGE';
        SET @v_tab_messaging := 'TB_MESSAGING';
        SET @v_tab_user_message := 'TB_USER_MESSAGE';
        SET @v_tab_receipt := 'TB_RECEIPT';
        SET @v_tab_receipt_data := 'TB_RECEIPT_DATA';
        SET @v_tab_receipt_new := 'MIGR_TB_RECEIPT';

        IF NOT MIGRATE_42_TO_50_check_table_exists(@v_tab_messaging) THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_messaging, ' should exists before starting ', @v_tab_signal, ' migration'));
        END IF;
        IF NOT MIGRATE_42_TO_50_check_table_exists(@v_tab_user_message) THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_user_message, ' should exists before starting ', @v_tab_signal, ' migration'));
        END IF;

        /** migrate old columns and add data into dictionary tables */
        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_signal, ' ,', @v_tab_receipt, ' and', @v_tab_receipt_data, ' migration started...'));

        OPEN c_signal_message_receipt;
        read_loop: LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                    BEGIN
                        GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                        CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_signal_receipt -> execute immediate error: ', @p2));
                    END;

                FETCH c_signal_message_receipt INTO id_pk, signal_message_id, ref_to_message_id, ebms3_timestamp,
                        creation_time, created_by, modification_time, modified_by, raw_xml, r_creation_time,
                        r_created_by, r_modification_time, r_modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                -- new tb_signal_message table
                INSERT INTO MIGR_TB_SIGNAL_MESSAGE (ID_PK, SIGNAL_MESSAGE_ID, REF_TO_MESSAGE_ID, EBMS3_TIMESTAMP,
                        CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (id_pk,
                        signal_message_id,
                        ref_to_message_id,
                        ebms3_timestamp,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                -- new tb_receipt table
                INSERT INTO MIGR_TB_RECEIPT (ID_PK, RAW_XML, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (id_pk,
                        CAST(raw_xml AS BINARY),
                        r_creation_time,
                        r_created_by,
                        r_modification_time,
                        r_modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(
                                @v_tab_signal_new, ' and ', @v_tab_receipt_new, ': Commit after ',
                                @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_signal_new, ' and ', @v_tab_receipt_new));
        CLOSE c_signal_message_receipt;

        -- check counts
        CALL MIGRATE_42_TO_50_check_counts(@v_tab_signal, @v_tab_signal_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_signal, ' migration is done'));
        END IF;

        CALL MIGRATE_42_TO_50_check_counts(@v_tab_receipt, @v_tab_receipt_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_receipt, ' and ', @v_tab_receipt_data, ' migration is done'));
        END IF;

    END
//

/**-- TB_RAWENVELOPE_LOG migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_raw_envelope_log()
    BEGIN
        DECLARE created_by VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE id_pk BIGINT;
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);
        DECLARE raw_xml LONGTEXT;
        DECLARE type VARCHAR(6);

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_raw_envelope CURSOR FOR
            SELECT UM.ID_PK, --  1:1 here
                   'USER' AS TYPE,
                   RA.RAW_XML,
                   RA.CREATION_TIME,
                   RA.CREATED_BY,
                   RA.MODIFICATION_TIME,
                   RA.MODIFIED_BY
            FROM TB_USER_MESSAGE UM,
                 TB_RAWENVELOPE_LOG RA
            WHERE UM.ID_PK = RA.USERMESSAGE_ID_FK
            UNION ALL
            SELECT UM.ID_PK, --  1:1 here
                   'SIGNAL' AS TYPE,
                   RA.RAW_XML,
                   RA.CREATION_TIME,
                   RA.CREATED_BY,
                   RA.MODIFICATION_TIME,
                   RA.MODIFIED_BY
            FROM TB_SIGNAL_MESSAGE SM,
                 TB_MESSAGING ME,
                 TB_USER_MESSAGE UM,
                 TB_RAWENVELOPE_LOG RA
            WHERE ME.SIGNAL_MESSAGE_ID = SM.ID_PK
              AND ME.USER_MESSAGE_ID = UM.ID_PK
              AND SM.ID_PK = RA.SIGNALMESSAGE_ID_FK;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_count_user := 0;
        SET @v_count_signal := 0;
        SET @v_tab_migrated := @v_tab_signal_new;
        SET @v_tab := 'TB_RAWENVELOPE_LOG';
        SET @v_tab_user_new := 'MIGR_TB_USER_MESSAGE_RAW';
        SET @v_tab_signal_new := 'MIGR_TB_SIGNAL_MESSAGE_RAW';
        SET @v_tab_user_message := 'TB_USER_MESSAGE';
        SET @v_tab_messaging := 'TB_MESSAGING';

        IF NOT MIGRATE_42_TO_50_check_table_exists(@v_tab_messaging) THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_messaging, ' should exists before starting ', @v_tab, ' migration'));
        END IF;
        IF NOT MIGRATE_42_TO_50_check_table_exists(@v_tab_user_message) THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_user_message, ' should exists before starting ', @v_tab, ' migration'));
        END IF;

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_raw_envelope;
        read_loop: LOOP
            FETCH c_raw_envelope INTO id_pk, type, raw_xml, creation_time, created_by, modification_time, modified_by;

            IF done THEN
                LEAVE read_loop;
            END IF;

            IF @v_type = 'USER' THEN
                BEGIN
                    DECLARE EXIT HANDLER FOR SQLEXCEPTION
                        BEGIN
                            GET DIAGNOSTICS CONDITION 1
                                @p2 = MESSAGE_TEXT;
                            CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_raw_envelope_log for ', @v_tab_user_new, '-> execute immediate error: ', @p2));
                        END;

                    SET @v_count_user := @v_count_user + 1;

                    INSERT INTO MIGR_TB_USER_MESSAGE_RAW (ID_PK, RAW_XML, CREATION_TIME, CREATED_BY, MODIFICATION_TIME,
                            MODIFIED_BY)
                    VALUES (id_pk,
                        CAST(raw_xml AS BINARY),
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);
                END;
            ELSE
                BEGIN
                    DECLARE EXIT HANDLER FOR SQLEXCEPTION
                        BEGIN
                            GET DIAGNOSTICS CONDITION 1
                                @p2 = MESSAGE_TEXT;
                            CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_raw_envelope_log for ', @v_tab_signal_new, '-> execute immediate error: ', @p2));
                        END;

                    SET @v_count_signal := @v_count_signal + 1;

                    INSERT INTO MIGR_TB_SIGNAL_MESSAGE_RAW (ID_PK, RAW_XML, CREATION_TIME, CREATED_BY,
                            MODIFICATION_TIME, MODIFIED_BY)
                    VALUES (id_pk,
                            CAST(raw_xml AS BINARY),
                            creation_time,
                            created_by,
                            modification_time,
                            modified_by);
                END;
            END IF;

            SET @i = @i + 1;
            IF @i MOD @BATCH_SIZE = 0 THEN
                COMMIT;
                CALL MIGRATE_42_TO_50_trace(CONCAT(
                            @v_tab_migrated, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                SET @v_batch_no := @v_batch_no + 1;
            END IF;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT(
            'Migrated ', @i, ' records in total: ', @v_count_user, ' into ', @v_tab_user_new, ' and ', @v_count_signal,
            ' into ', @v_tab_signal_new));
        CLOSE c_raw_envelope;

        -- check counts
        IF @v_count_user + @v_count_signal = @i THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

/**-- TB_MESSAGE_LOG migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_message_log()
    BEGIN
        DECLARE backend VARCHAR(255);
        DECLARE created_by VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE deleted TIMESTAMP;
        DECLARE downloaded TIMESTAMP;
        DECLARE failed TIMESTAMP;
        DECLARE id_pk BIGINT;
        DECLARE message_id VARCHAR(255);
        DECLARE message_status VARCHAR(255);
        DECLARE message_type VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);
        DECLARE msh_role VARCHAR(255);
        DECLARE next_attempt TIMESTAMP;
        DECLARE notification_status VARCHAR(255);
        DECLARE received TIMESTAMP;
        DECLARE restored TIMESTAMP;
        DECLARE scheduled BIT(1);
        DECLARE send_attempts INT;
        DECLARE send_attempts_max INT;
        DECLARE version INT;

        DECLARE calculated_id_pk BIGINT;
        DECLARE calculated_message_status_id_fk BIGINT;
        DECLARE calculated_msh_role_id_fk BIGINT;
        DECLARE calculated_notification_status_id_fk BIGINT;

        DECLARE done INT DEFAULT FALSE;

        DECLARE c_message_log CURSOR FOR
            SELECT ML.ID_PK,
                   ML.MESSAGE_ID,
                   ML.MESSAGE_TYPE,
                   ML.BACKEND,
                   ML.RECEIVED,
                   ML.DOWNLOADED,
                   ML.FAILED,
                   ML.RESTORED,
                   ML.DELETED,
                   ML.NEXT_ATTEMPT,
                   ML.SEND_ATTEMPTS,
                   ML.SEND_ATTEMPTS_MAX,
                   ML.SCHEDULED,
                   ML.VERSION,
                   ML.MESSAGE_STATUS,
                   ML.MSH_ROLE,
                   ML.NOTIFICATION_STATUS,
                   ML.CREATION_TIME,
                   ML.CREATED_BY,
                   ML.MODIFICATION_TIME,
                   ML.MODIFIED_BY
            FROM TB_MESSAGE_LOG ML;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_count_user := 0;
        SET @v_count_signal := 0;
        SET @v_tab := 'TB_MESSAGE_LOG';
        SET @v_tab_user_new := 'MIGR_TB_USER_MESSAGE_LOG';
        SET @v_tab_signal_new := 'MIGR_TB_SIGNAL_MESSAGE_LOG';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_message_log;
        read_loop: LOOP
            FETCH c_message_log INTO id_pk, message_id, message_type, backend, received, downloaded, failed, restored,
                    deleted, next_attempt, send_attempts, send_attempts_max, scheduled, version, message_status,
                    msh_role, notification_status, creation_time, created_by, modification_time, modified_by;

            IF done THEN
                LEAVE read_loop;
            END IF;

            IF message_type = 'USER_MESSAGE' THEN
                BEGIN
                    DECLARE EXIT HANDLER FOR SQLEXCEPTION
                        BEGIN
                            GET DIAGNOSTICS CONDITION 1
                                @p2 = MESSAGE_TEXT;
                            CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_message_log for ', @v_tab_user_new, '-> execute immediate error: ', @p2));
                        END;

                    SET @v_count_user := @v_count_user + 1;

                    CALL MIGRATE_42_TO_50_get_tb_user_message_rec(message_id, calculated_id_pk);
                    CALL MIGRATE_42_TO_50_get_tb_d_msg_status_rec(message_status, calculated_message_status_id_fk);
                    CALL MIGRATE_42_TO_50_get_tb_d_msh_role_rec(msh_role, calculated_msh_role_id_fk);
                    CALL MIGRATE_42_TO_50_get_tb_d_notif_status_rec(notification_status, calculated_notification_status_id_fk);

                    INSERT INTO MIGR_TB_USER_MESSAGE_LOG (ID_PK, BACKEND, RECEIVED, DOWNLOADED, FAILED, RESTORED,
                            DELETED, NEXT_ATTEMPT, SEND_ATTEMPTS, SEND_ATTEMPTS_MAX, SCHEDULED, VERSION,
                            MESSAGE_STATUS_ID_FK, MSH_ROLE_ID_FK, NOTIFICATION_STATUS_ID_FK, CREATION_TIME, CREATED_BY,
                            MODIFICATION_TIME, MODIFIED_BY)
                    VALUES (calculated_id_pk,
                            backend,
                            received,
                            downloaded,
                            failed,
                            restored,
                            deleted,
                            next_attempt,
                            send_attempts,
                            send_attempts_max,
                            scheduled,
                            version,
                            calculated_message_status_id_fk,
                            calculated_msh_role_id_fk,
                            calculated_notification_status_id_fk,
                            creation_time,
                            created_by,
                            modification_time,
                            modified_by);
                END;
            ELSE
                BEGIN
                    DECLARE EXIT HANDLER FOR SQLEXCEPTION
                        BEGIN
                            GET DIAGNOSTICS CONDITION 1
                                @p2 = MESSAGE_TEXT;
                            CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_message_log for ', @v_tab_signal_new, '-> execute immediate error: ', @p2));
                        END;

                    SET @v_count_signal := @v_count_signal + 1;

                    CALL MIGRATE_42_TO_50_get_tb_signal_message_rec(message_id, calculated_id_pk);
                    CALL MIGRATE_42_TO_50_get_tb_d_msg_status_rec(message_status, calculated_message_status_id_fk);
                    CALL MIGRATE_42_TO_50_get_tb_d_msh_role_rec(msh_role, calculated_msh_role_id_fk);

                    INSERT INTO MIGR_TB_SIGNAL_MESSAGE_LOG (ID_PK, RECEIVED, DELETED, MESSAGE_STATUS_ID_FK,
                            MSH_ROLE_ID_FK, CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                    VALUES (calculated_id_pk,
                            received,
                            downloaded,
                            calculated_message_status_id_fk,
                            calculated_msh_role_id_fk,
                            creation_time,
                            created_by,
                            modification_time,
                            modified_by);
                END;
            END IF;

            SET @i = @i + 1;
            IF @i MOD @BATCH_SIZE = 0 THEN
                COMMIT;
                CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                SET @v_batch_no := @v_batch_no + 1;
            END IF;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT(
            'Migrated ', @i, ' records in total: ', @v_count_user, ' into ', @v_tab_user_new, ' and ', @v_count_signal,
            ' into ', @v_tab_signal_new));
        CLOSE c_message_log;

        -- check counts
        IF @v_count_user + @v_count_signal = @i THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;

    END
//

/**- TB_PROPERTY, TB_USER_MESSAGE data migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_property()
    BEGIN
        DECLARE created_by VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);
        DECLARE name VARCHAR(255);
        DECLARE type VARCHAR(255);
        DECLARE user_message_id_fk BIGINT;
        DECLARE value VARCHAR(1024);

        DECLARE calculated_message_property_fk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_property CURSOR FOR
            SELECT UM.ID_PK USER_MESSAGE_ID_FK,
                   TP.NAME,
                   TP.VALUE,
                   TP.TYPE,
                   TP.CREATION_TIME,
                   TP.CREATED_BY,
                   TP.MODIFICATION_TIME,
                   TP.MODIFIED_BY
            FROM TB_PROPERTY TP,
                 TB_USER_MESSAGE UM
            WHERE TP.MESSAGEPROPERTIES_ID = UM.ID_PK
              AND TP.PARTPROPERTIES_ID IS NULL;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PROPERTY';
        SET @v_tab_user_message := 'TB_USER_MESSAGE';
        SET @v_tab_properties_new := 'MIGR_TB_MESSAGE_PROPERTIES';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_property;
        read_loop: LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                    BEGIN
                        GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                        CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_property -> execute immediate error: ', @p2));
                    END;

                FETCH c_property INTO user_message_id_fk, name, value, type, creation_time, created_by,
                        modification_time, modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_get_tb_d_msg_property_rec(name, value, type, calculated_message_property_fk);

                INSERT INTO MIGR_TB_MESSAGE_PROPERTIES (USER_MESSAGE_ID_FK, MESSAGE_PROPERTY_FK, CREATION_TIME,
                        CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (user_message_id_fk,
                        calculated_message_property_fk,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_properties_new));
        CLOSE c_property;

        -- check counts
        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_properties_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

/**- TB_PART_INFO, TB_USER_MESSAGE data migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_part_info_user()
    BEGIN
        DECLARE binary_data LONGBLOB;
        DECLARE created_by VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE description_lang VARCHAR(255);
        DECLARE description_value VARCHAR(255);
        DECLARE encrypted BIT(1);
        DECLARE filename VARCHAR(255);
        DECLARE href VARCHAR(255);
        DECLARE id_pk BIGINT;
        DECLARE in_body BIT(1);
        DECLARE mime VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);
        DECLARE part_order INT;
        DECLARE user_message_id_fk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_part_info CURSOR FOR
            SELECT PI.ID_PK,
                   PI.BINARY_DATA,
                   PI.DESCRIPTION_LANG,
                   PI.DESCRIPTION_VALUE,
                   PI.HREF,
                   PI.IN_BODY,
                   PI.FILENAME,
                   PI.MIME,
                   PI.PART_ORDER,
                   PI.ENCRYPTED,
                   PI.CREATED_BY,
                   PI.CREATION_TIME,
                   PI.MODIFIED_BY,
                   PI.MODIFICATION_TIME,
                   UM.ID_PK USER_MESSAGE_ID_FK
            FROM TB_USER_MESSAGE UM,
                 TB_PART_INFO PI
            WHERE PI.PAYLOADINFO_ID = UM.ID_PK;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_PART_INFO';
        SET @v_tab_user_message := 'TB_USER_MESSAGE';
        SET @v_tab_new := 'MIGR_TB_PART_INFO';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_part_info;
        read_loop: LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                    BEGIN
                        GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                        CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_user_message -> execute immediate error: ', @p2));
                    END;

                FETCH c_part_info INTO id_pk, binary_data, description_lang, description_value, href, in_body, filename,
                        mime, part_order, encrypted, created_by, creation_time, modified_by, modification_time,
                        user_message_id_fk;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                INSERT INTO MIGR_TB_PART_INFO (ID_PK, BINARY_DATA, DESCRIPTION_LANG, DESCRIPTION_VALUE, HREF, IN_BODY,
                        FILENAME, MIME, PART_ORDER, ENCRYPTED, USER_MESSAGE_ID_FK, CREATION_TIME, CREATED_BY,
                        MODIFICATION_TIME, MODIFIED_BY)
                VALUES (id_pk,
                        binary_data,
                        description_lang,
                        description_value,
                        href,
                        in_body,
                        filename,
                        mime,
                        part_order,
                        encrypted,
                        user_message_id_fk,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_part_info;

        -- check counts
        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' and ', @v_tab_user_message, ' migration is done'));
        END IF;
    END
//

/**- TB_PART_INFO, TB_PROPERTY data migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_part_info_property()
    BEGIN
        DECLARE created_by VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);
        DECLARE name VARCHAR(255);
        DECLARE part_info_id_fk BIGINT;
        DECLARE type VARCHAR(255);
        DECLARE value VARCHAR(1024);

        DECLARE calculated_part_info_property_fk BIGINT;

        DECLARE done INT DEFAULT FALSE;

        DECLARE c_part_prop CURSOR FOR
            SELECT PR.NAME,
                   PR.VALUE,
                   PR.TYPE,
                   PI.ID_PK PART_INFO_ID_FK,
                   PR.CREATION_TIME,
                   PR.CREATED_BY,
                   PR.MODIFICATION_TIME,
                   PR.MODIFIED_BY
            FROM TB_PART_INFO PI,
                 TB_PROPERTY PR
            WHERE PR.PARTPROPERTIES_ID = PI.ID_PK;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab_info := 'TB_PART_INFO';
        SET @v_tab_property := 'TB_PROPERTY';
        SET @v_tab_new := 'MIGR_TB_PART_PROPERTIES';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_info, ' and ', @v_tab_property, ' migration started...'));

        OPEN c_part_prop;
        read_loop: LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                    BEGIN
                        GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                        CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_part_info_property for ', @v_tab_new, ' -> execute immediate error: ', @p2));
                    END;

                FETCH c_part_prop INTO name, value, type, part_info_id_fk, creation_time, created_by, modification_time,
                        modified_by;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_get_tb_d_part_property_rec(name, value, type, calculated_part_info_property_fk);

                -- TODO check if AUTOINCREMENT is fine
                INSERT INTO MIGR_TB_PART_PROPERTIES (PART_INFO_ID_FK, PART_INFO_PROPERTY_FK, CREATION_TIME, CREATED_BY,
                        MODIFICATION_TIME, MODIFIED_BY)
                VALUES (part_info_id_fk,
                    calculated_part_info_property_fk,
                    creation_time,
                    created_by,
                    modification_time,
                    modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_part_prop;
    END
//

/**- TB_ERROR_LOG data migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_error_log()
    BEGIN
        DECLARE created_by VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE error_code VARCHAR(255);
        DECLARE error_detail VARCHAR(255);
        DECLARE error_signal_message_id VARCHAR(255);
        DECLARE id_pk BIGINT;
        DECLARE message_in_error_id VARCHAR(255);
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);
        DECLARE msh_role VARCHAR(255);
        DECLARE notified TIMESTAMP;
        DECLARE time_stamp TIMESTAMP;
        DECLARE user_message_id_fk BIGINT;

        DECLARE calculated_msh_role_id_fk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_error_log CURSOR FOR
            SELECT EL.ID_PK,
                   EL.ERROR_CODE,
                   EL.ERROR_DETAIL,
                   EL.ERROR_SIGNAL_MESSAGE_ID,
                   EL.MESSAGE_IN_ERROR_ID,
                   EL.MSH_ROLE,
                   EL.NOTIFIED,
                   EL.TIME_STAMP,
                   EL.CREATION_TIME,
                   EL.CREATED_BY,
                   EL.MODIFICATION_TIME,
                   EL.MODIFIED_BY,
                   UMMI.ID_PK USER_MESSAGE_ID_FK
            FROM TB_ERROR_LOG EL
                     LEFT JOIN
                 (SELECT MI.MESSAGE_ID, UM.ID_PK
                  FROM TB_MESSAGE_INFO MI,
                       TB_USER_MESSAGE UM
                  WHERE UM.MESSAGEINFO_ID_PK = MI.ID_PK) UMMI
                 ON EL.MESSAGE_IN_ERROR_ID = UMMI.MESSAGE_ID;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_ERROR_LOG';
        SET @v_tab_new := 'MIGR_TB_ERROR_LOG';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_error_log;
        read_loop: LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                    BEGIN
                        GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                        CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_error_log -> execute immediate error: ', @p2));
                    END;

                FETCH c_error_log INTO id_pk, error_code, error_detail, error_signal_message_id, message_in_error_id,
                        msh_role, notified, time_stamp, creation_time, created_by, modification_time, modified_by,
                        user_message_id_fk;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                CALL MIGRATE_42_TO_50_get_tb_d_msh_role_rec(msh_role, calculated_msh_role_id_fk);

                INSERT INTO MIGR_TB_ERROR_LOG (ID_PK, ERROR_CODE, ERROR_DETAIL, ERROR_SIGNAL_MESSAGE_ID,
                        MESSAGE_IN_ERROR_ID, MSH_ROLE_ID_FK, NOTIFIED, TIME_STAMP, USER_MESSAGE_ID_FK, CREATION_TIME,
                        CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (id_pk,
                        error_code,
                        error_detail,
                        error_signal_message_id,
                        message_in_error_id,
                        calculated_msh_role_id_fk,
                        notified,
                        time_stamp,
                        user_message_id_fk,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_error_log;

        -- check counts
        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

/**- TB_MESSAGE_ACKNW data migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_message_acknw()
    BEGIN
        DECLARE acknowledge_date TIMESTAMP;
        DECLARE created_by VARCHAR(255);
        DECLARE creation_time TIMESTAMP;
        DECLARE from_value VARCHAR(255);
        DECLARE id_pk BIGINT;
        DECLARE modification_time TIMESTAMP;
        DECLARE modified_by VARCHAR(255);
        DECLARE to_value VARCHAR(255);
        DECLARE user_message_id_fk BIGINT;

        DECLARE done INT DEFAULT FALSE;
        DECLARE migration_status BOOLEAN;

        DECLARE c_message_acknw CURSOR FOR
            SELECT MA.ID_PK,
                   MA.FROM_VALUE,
                   MA.TO_VALUE,
                   MA.ACKNOWLEDGE_DATE,
                   MA.CREATION_TIME,
                   MA.CREATED_BY,
                   MA.MODIFICATION_TIME,
                   MA.MODIFIED_BY,
                   UM.ID_PK USER_MESSAGE_ID_FK
            FROM
                TB_MESSAGE_ACKNW MA,
                TB_MESSAGE_INFO MI,
                TB_USER_MESSAGE UM
            WHERE UM.MESSAGEINFO_ID_PK = MI.ID_PK
              AND MI.MESSAGE_ID = MA.MESSAGE_ID;

        DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

        SET @i := 0;
        SET @v_batch_no := 1;
        SET @v_tab := 'TB_MESSAGE_ACKNW';
        SET @v_tab_new := 'MIGR_TB_MESSAGE_ACKNW';

        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

        OPEN c_message_acknw;
        read_loop: LOOP
            BEGIN
                DECLARE EXIT HANDLER FOR SQLEXCEPTION
                    BEGIN
                        GET DIAGNOSTICS CONDITION 1
                            @p2 = MESSAGE_TEXT;
                        CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_message_acknw -> execute immediate error: ', @p2));
                    END;

                FETCH c_message_acknw INTO id_pk, from_value, to_value, acknowledge_date, creation_time, created_by,
                        modification_time, modified_by, user_message_id_fk;

                IF done THEN
                    LEAVE read_loop;
                END IF;

                INSERT INTO MIGR_TB_MESSAGE_ACKNW (ID_PK, FROM_VALUE, TO_VALUE, ACKNOWLEDGE_DATE, USER_MESSAGE_ID_FK,
                        CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
                VALUES (id_pk,
                        from_value,
                        to_value,
                        acknowledge_date,
                        user_message_id_fk,
                        creation_time,
                        created_by,
                        modification_time,
                        modified_by);

                SET @i = @i + 1;
                IF @i MOD @BATCH_SIZE = 0 THEN
                    COMMIT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no, ' records'));
                    SET @v_batch_no := @v_batch_no + 1;
                END IF;
            END;
        END LOOP read_loop;
        COMMIT;

        CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
        CLOSE c_message_acknw;

        -- check counts
        CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
        IF migration_status THEN
            CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
        END IF;
    END
//

/**- TB_SEND_ATTEMPT data migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate_send_attempt()

BEGIN
    DECLARE status VARCHAR(255);
    DECLARE error VARCHAR(255);
    DECLARE created_by VARCHAR(255);
    DECLARE creation_time TIMESTAMP;
    DECLARE start_date TIMESTAMP;
    DECLARE id_pk BIGINT;
    DECLARE modification_time TIMESTAMP;
    DECLARE modified_by VARCHAR(255);
    DECLARE end_date TIMESTAMP;
    DECLARE user_message_id_fk BIGINT;

    DECLARE done INT DEFAULT FALSE;
    DECLARE migration_status BOOLEAN;

    DECLARE c_send_attempt CURSOR FOR
        SELECT SA.ID_PK,
               SA.START_DATE,
               SA.END_DATE,
               SA.STATUS,
               SA.ERROR,
               SA.CREATION_TIME,
               SA.CREATED_BY,
               SA.MODIFICATION_TIME,
               SA.MODIFIED_BY,
               UM.ID_PK USER_MESSAGE_ID_FK
        FROM TB_SEND_ATTEMPT SA,
             TB_MESSAGE_INFO MI,
             TB_USER_MESSAGE UM
        WHERE UM.MESSAGEINFO_ID_PK = MI.ID_PK
          AND MI.MESSAGE_ID = SA.MESSAGE_ID;

    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done := TRUE;

    SET @i := 0;
    SET @v_batch_no := 1;
    SET @v_tab := 'TB_SEND_ATTEMPT';
    SET @v_tab_new := 'MIGR_TB_SEND_ATTEMPT';

    CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration started...'));

    OPEN c_send_attempt;
    read_loop:
    LOOP
        BEGIN
            DECLARE EXIT HANDLER FOR SQLEXCEPTION
                BEGIN
                    GET DIAGNOSTICS CONDITION 1
                        @p2 = MESSAGE_TEXT;
                    CALL MIGRATE_42_TO_50_trace(CONCAT('migrate_send_attempt -> execute immediate error: ', @p2));
                END;

            FETCH c_send_attempt INTO id_pk, start_date, end_date, status, error, creation_time, created_by,
                modification_time, modified_by, user_message_id_fk;

            IF done THEN
                LEAVE read_loop;
            END IF;

            INSERT INTO MIGR_TB_SEND_ATTEMPT (ID_PK, START_DATE, END_DATE, STATUS, ERROR, USER_MESSAGE_ID_FK,
                                              CREATION_TIME, CREATED_BY, MODIFICATION_TIME, MODIFIED_BY)
            VALUES (id_pk,
                    start_date,
                    end_date,
                    status,
                    error,
                    user_message_id_fk,
                    creation_time,
                    created_by,
                    modification_time,
                    modified_by);

            SET @i = @i + 1;
            IF @i MOD @BATCH_SIZE = 0 THEN
                COMMIT;
                CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab_new, ': Commit after ', @BATCH_SIZE * @v_batch_no,
                                                   ' records'));
                SET @v_batch_no := @v_batch_no + 1;
            END IF;
        END;
    END LOOP read_loop;
    COMMIT;

    CALL MIGRATE_42_TO_50_trace(CONCAT('Migrated ', @i, ' records in total into ', @v_tab_new));
    CLOSE c_send_attempt;

    -- check counts
    CALL MIGRATE_42_TO_50_check_counts(@v_tab, @v_tab_new, migration_status);
    IF migration_status THEN
        CALL MIGRATE_42_TO_50_trace(CONCAT(@v_tab, ' migration is done'));
    END IF;

END
//

/**-- main entry point for running the migration --*/
CREATE PROCEDURE MIGRATE_42_TO_50_migrate()
    BEGIN
        -- keep it in this order
        CALL MIGRATE_42_TO_50_migrate_user_message;
        CALL MIGRATE_42_TO_50_migrate_message_fragment;
        CALL MIGRATE_42_TO_50_migrate_message_group;
        CALL MIGRATE_42_TO_50_migrate_message_header;

        CALL MIGRATE_42_TO_50_migrate_signal_receipt;
        CALL MIGRATE_42_TO_50_migrate_message_log;

        CALL MIGRATE_42_TO_50_migrate_raw_envelope_log;

        CALL MIGRATE_42_TO_50_migrate_property;
        CALL MIGRATE_42_TO_50_migrate_part_info_user;
        CALL MIGRATE_42_TO_50_migrate_part_info_property;

        CALL MIGRATE_42_TO_50_migrate_error_log;
        CALL MIGRATE_42_TO_50_migrate_message_acknw;
        CALL MIGRATE_42_TO_50_migrate_send_attempt;
    END
//