DECLARE
    table_does_not_exist exception;
    PRAGMA EXCEPTION_INIT(table_does_not_exist, -942);
BEGIN
    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_ACTION_AUDIT';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_ACTION_AUDIT: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_ALERT';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_ALERT: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_AUTH_ENTRY';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_AUTH_ENTRY: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_BACKEND_FILTER';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_BACKEND_FILTER: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_CERTIFICATE';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_CERTIFICATE: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_COMMAND';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_COMMAND: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_EVENT';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_EVENT: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_CONF_RAW';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_CONF_RAW: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_CONFIGURATION';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_CONFIGURATION: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_PARTY';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_PARTY: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_PARTY_ID_TYPE';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_PARTY_ID_TYPE: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_REV_INFO';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_REV_INFO: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_ROUTING_CRITERIA';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_ROUTING_CRITERIA: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_TIMEZONE_OFFSET';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_TIMEZONE_OFFSET: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_USER';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_USER: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_USER_ROLE';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_USER_ROLE: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_ACTION_AUDIT CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_ACTION_AUDIT: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_ALERT CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_ALERT: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_AUTH_ENTRY CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_AUTH_ENTRY: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_BACKEND_FILTER CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_BACKEND_FILTER: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_CERTIFICATE CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_CERTIFICATE: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_COMMAND CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_COMMAND: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_EVENT CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_EVENT: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_CONF_RAW CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_CONF_RAW: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_CONFIGURATION CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_CONFIGURATION: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_PARTY CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_PARTY: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_PARTY_ID_TYPE CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_PARTY_ID_TYPE: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_REV_INFO CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_REV_INFO: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_ROUTING_CRITERIA CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_ROUTING_CRITERIA: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_TIMEZONE_OFFSET CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_TIMEZONE_OFFSET: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_USER CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_USER: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_USER_ROLE CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_USER_ROLE: table does not exist');
    END;
END;
/