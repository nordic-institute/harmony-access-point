DECLARE
    table_does_not_exist exception;
    PRAGMA EXCEPTION_INIT(table_does_not_exist, -942);
BEGIN
    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_TIMEZONE_OFFSET';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_TIMEZONE_OFFSET: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_USER_MESSAGE';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_USER_MESSAGE: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_MESSAGE_INFO';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_MESSAGE_INFO: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_MESSAGE_GROUP';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_MESSAGE_GROUP: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PART_INFO';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PART_INFO: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_MESSAGE_ACKNW';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_MESSAGE_ACKNW: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_SEND_ATTEMPT';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_SEND_ATTEMPT: table does not exist');
    END;

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
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_EVENT';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_EVENT: table does not exist');
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
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_ROUTING_CRITERIA';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_ROUTING_CRITERIA: table does not exist');
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
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_BUSINESS_PROC';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_BUSINESS_PROC: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_ACTION';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_ACTION: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_AGREEMENT';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_AGREEMENT: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_ERROR_HANDLING';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_ERROR_HANDLING: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_MEP';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_MEP: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_MEP_BINDING';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_MEP_BINDING: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_MESSAGE_PROP';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_MESSAGE_PROP: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_MSG_PROP_SET';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_MSG_PROP_SET: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_PARTY';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_PARTY: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_CONFIGURATION';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_CONFIGURATION: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_MPC';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_MPC: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_PARTY_ID_TYPE';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_PARTY_ID_TYPE: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_PARTY_ID';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_PARTY_ID: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_PAYLOAD';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_PAYLOAD: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_PAYLOAD_PROF';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_PAYLOAD_PROF: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_RECEPTN_AWARNS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_RECEPTN_AWARNS: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_RELIABILITY';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_RELIABILITY: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_ROLE';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_ROLE: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_SECURITY';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_SECURITY: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_SERVICE';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_SERVICE: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_SPLITTING';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_SPLITTING: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_LEG';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_LEG: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_PROCESS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_PROCESS: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_PM_CONF_RAW';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_PM_CONF_RAW: table does not exist');
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
        EXECUTE IMMEDIATE 'TRUNCATE TABLE MIGR_TB_PKS_REV_INFO';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot truncate table MIGR_TB_PKS_REV_INFO: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_TIMEZONE_OFFSET CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_TIMEZONE_OFFSET: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_USER_MESSAGE CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_USER_MESSAGE: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_MESSAGE_INFO CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_MESSAGE_INFO: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_MESSAGE_GROUP CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_MESSAGE_GROUP: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PART_INFO CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PART_INFO: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_MESSAGE_ACKNW CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_MESSAGE_ACKNW: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_SEND_ATTEMPT CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_SEND_ATTEMPT: table does not exist');
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
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_EVENT CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_EVENT: table does not exist');
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
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_ROUTING_CRITERIA CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_ROUTING_CRITERIA: table does not exist');
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
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_BUSINESS_PROC CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_BUSINESS_PROC: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_ACTION CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_ACTION: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_AGREEMENT CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_AGREEMENT: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_ERROR_HANDLING CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_ERROR_HANDLING: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_MEP CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_MEP: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_MEP_BINDING CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_MEP_BINDING: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_MESSAGE_PROP CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_MESSAGE_PROP: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_MSG_PROP_SET CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_MSG_PROP_SET: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_PARTY CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_PARTY: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_CONFIGURATION CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_CONFIGURATION: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_MPC CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_MPC: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_PARTY_ID_TYPE CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_PARTY_ID_TYPE: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_PARTY_ID CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_PARTY_ID: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_PAYLOAD CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_PAYLOAD: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_PAYLOAD_PROF CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_PAYLOAD_PROF: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_RECEPTN_AWARNS CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_RECEPTN_AWARNS: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_RELIABILITY CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_RELIABILITY: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_ROLE CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_ROLE: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_SECURITY CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_SECURITY: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_SERVICE CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_SERVICE: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_SPLITTING CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_SPLITTING: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_LEG CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_LEG: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_PROCESS CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_PROCESS: table does not exist');
    END;

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_PM_CONF_RAW CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_PM_CONF_RAW: table does not exist');
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

    BEGIN
        EXECUTE IMMEDIATE 'DROP TABLE MIGR_TB_PKS_REV_INFO CASCADE CONSTRAINTS';
    EXCEPTION
        WHEN table_does_not_exist THEN
            DBMS_OUTPUT.PUT_LINE('Cannot drop table MIGR_TB_PKS_REV_INFO: table does not exist');
    END;
END;
/