CREATE OR REPLACE FUNCTION generate_partition_id(p_date IN DATE)
    RETURN NUMBER IS
    p_id NUMBER;
BEGIN
    DECLARE
        date_format CONSTANT STRING(10) := 'YYMMDDHH24';
    BEGIN
        SELECT to_number(to_char(p_date, date_format))
        INTO p_id
        FROM dual;
        RETURN p_id;
    END;
END;
/


CREATE OR REPLACE PROCEDURE LOG_DOMIBUS(message IN VARCHAR2) IS
BEGIN
    DECLARE
        time_format VARCHAR2(100) := 'HH24:MI:SS';
    BEGIN
        DBMS_OUTPUT.put_line(TO_CHAR(SYSDATE, time_format) || ' ' || message);
    END;
END;
/

CREATE OR REPLACE PROCEDURE DO_FIRST_PARTITIONS(part_one_name IN VARCHAR2, part_one_high IN NUMBER, part_two_name IN VARCHAR2 DEFAULT NULL, part_two_high IN NUMBER DEFAULT NULL)
AS
    SUFFIX              NUMBER := 10000000000;
    high_one            NUMBER;
    high_two            NUMBER;
    str_part_two        VARCHAR2(100) := '';
    ddl                 VARCHAR2(500);
BEGIN
    high_one := part_one_high * SUFFIX;
    
    IF part_two_name IS NOT NULL THEN
        high_two := part_two_high * SUFFIX;
        str_part_two := ', PARTITION ' || part_two_name || ' VALUES LESS THAN (' || high_two || ') ';
    END IF;
    
    ddl := 'ALTER TABLE TB_USER_MESSAGE MODIFY PARTITION BY RANGE (ID_PK) ' ||
        '(PARTITION ' || part_one_name || ' VALUES LESS THAN (' || high_one || ')' ||
        str_part_two ||
        ') UPDATE INDEXES ( IDX_USER_MSG_MESSAGE_ID LOCAL, IDX_USER_MSG_ACTION_ID LOCAL, IDX_USER_MSG_AGREEMENT_ID LOCAL, IDX_USER_MSG_SERVICE_ID LOCAL, IDX_USER_MSG_MPC_ID LOCAL, IDX_FROM_ROLE_ID LOCAL, IDX_USER_MSG_TO_PARTY_ID LOCAL, IDX_TO_ROLE_ID LOCAL, IDX_USER_MSG_FROM_PARTY_ID LOCAL, IDX_TEST_MESSAGE LOCAL )';
    LOG_DOMIBUS(ddl);

    EXECUTE IMMEDIATE ddl;
END;
/


CREATE OR REPLACE FUNCTION get_date_hour_as_number(plus_days IN NUMBER, hour IN NUMBER)
    RETURN NUMBER IS
    p_id NUMBER;
BEGIN
    select generate_partition_id(TRUNC(sysdate + plus_days) + hour / 24) into p_id from dual;
    RETURN p_id;
END;
/

-- creates partitions for the day sysdate + d
CREATE OR REPLACE PROCEDURE PARTITIONSGEN(
    d IN NUMBER default 7,
    table_name IN VARCHAR2 DEFAULT 'TB_USER_MESSAGE'
)
AS
BEGIN
    DECLARE
        number_of_partitions CONSTANT NUMBER := 24; -- one partition every hour
        p_id                          NUMBER;
        p_name                        VARCHAR2(20);
        p_high                        NUMBER;
    BEGIN
        FOR p_number IN 0 .. (number_of_partitions - 1)
            LOOP
                LOG_DOMIBUS('[PARTITIONSGEN] ' || 'd=' || d || ' table_name=' || table_name || ' p_number=' || p_number);
                p_id := get_date_hour_as_number(d, p_number);
                p_name := 'P' || p_id;
                p_high := p_id || '0000000000';
                EXECUTE IMMEDIATE 'ALTER TABLE ' || table_name || ' ADD PARTITION ' || p_name ||
                                  ' VALUES LESS THAN (' || p_high || ')';
                dbms_lock.sleep(10);
            END LOOP;
    END;
END;
/

CREATE OR REPLACE PROCEDURE drop_partition (partition_name IN VARCHAR2) IS
BEGIN
    execute immediate 'ALTER TABLE TB_USER_MESSAGE DROP PARTITION ' || partition_name || ' UPDATE INDEXES';
END;
/

CREATE OR REPLACE PROCEDURE MIGRATE_5_0_1_PARTITIONED_TO_5_0_2 IS
BEGIN
    DO_FIRST_PARTITIONS('P22000000', get_date_hour_as_number(0, 23));
    FOR days_in_future IN 1 .. 7
    LOOP
        PARTITIONSGEN(days_in_future);
    END LOOP;
END;
/

CREATE OR REPLACE PROCEDURE MIGRATE_5_0_1_UNPARTITIONED_TO_5_0_2 IS
BEGIN
    MIGRATE_5_0_1_PARTITIONED_TO_5_0_2;
END;
/

CREATE OR REPLACE PROCEDURE MIGRATE_5_0_2_UNPARTITIONED_TO_5_0_3 IS
BEGIN
    DO_FIRST_PARTITIONS('P1970', 1970000, 'P22000000', get_date_hour_as_number(0, 23));
    FOR days_in_future IN 1 .. 7
    LOOP
        PARTITIONSGEN(days_in_future);
    END LOOP;
END;
/

CREATE OR REPLACE PROCEDURE MIGRATE_5_0_2_PARTITIONED_TO_5_0_3 IS
BEGIN
    execute immediate 'ALTER TABLE TB_USER_MESSAGE SPLIT PARTITION P22000000 AT (19700000000000000) INTO (PARTITION P1970, PARTITION P22000000) UPDATE GLOBAL INDEXES';
    execute immediate 'ALTER INDEX PK_PART_INFO REBUILD';
    execute immediate 'ALTER INDEX IDX_MSG_DOWNLOADED REBUILD';
    FOR ddl IN (
        select 'ALTER INDEX ' || INDEX_NAME || ' REBUILD PARTITION ' || PARTITION_NAME || ' TABLESPACE ' || TABLESPACE_NAME as statement
        from user_ind_partitions 
        where status='UNUSABLE'
    )
    LOOP
        LOG_DOMIBUS(ddl.statement);
        EXECUTE IMMEDIATE ddl.statement;
    END LOOP;
END;
/

CREATE OR REPLACE PROCEDURE MIGRATE_5_0_3_UNPARTITIONED_TO_5_0_4 IS
BEGIN
    MIGRATE_5_0_2_UNPARTITIONED_TO_5_0_3;
END;
/

CREATE OR REPLACE PROCEDURE MIGRATE_5_0_4_PARTITIONED_TO_5_0_5 IS
BEGIN
    dbms_scheduler.drop_job(job_name => 'GENERATE_PARTITIONS_JOB');
    EXECUTE IMMEDIATE 'ALTER TABLE TB_USER_MESSAGE SET INTERVAL (10000000000)';
END;
/