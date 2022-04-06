-- *****************************************************************************************************
-- Domibus 4.2 to 5.0 data time migration to UTC package
--
-- Main entry point is the procedure 'migrate'. To be executed into a begin/end; block
--
-- Parameters to be provided:
--  TIMEZONE: the timezone ID in which the date time values have been previously saved (e.g. 'Europe/Brussels')
-- *****************************************************************************************************
CREATE OR REPLACE PACKAGE MIGRATE_42_TO_50_utc_conversion IS

    PROCEDURE migrate(TIMEZONE IN VARCHAR2);

END MIGRATE_42_TO_50_utc_conversion;
/

CREATE OR REPLACE PACKAGE BODY MIGRATE_42_TO_50_utc_conversion IS

    PROCEDURE migrate(TIMEZONE IN VARCHAR2) IS
    BEGIN

        UPDATE TB_ACTION_AUDIT
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_ACTION_AUDIT
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_ACTION_AUDIT
        SET REVISION_DATE = SYS_EXTRACT_UTC(FROM_TZ(REVISION_DATE, TIMEZONE))
        WHERE REVISION_DATE IS NOT NULL;

        UPDATE TB_ALERT
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_ALERT
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_ALERT
        SET NEXT_ATTEMPT = SYS_EXTRACT_UTC(FROM_TZ(NEXT_ATTEMPT, TIMEZONE))
        WHERE NEXT_ATTEMPT IS NOT NULL;

        UPDATE TB_ALERT
        SET PROCESSED_TIME = SYS_EXTRACT_UTC(FROM_TZ(PROCESSED_TIME, TIMEZONE))
        WHERE PROCESSED_TIME IS NOT NULL;

        UPDATE TB_ALERT
        SET REPORTING_TIME = SYS_EXTRACT_UTC(FROM_TZ(REPORTING_TIME, TIMEZONE))
        WHERE REPORTING_TIME IS NOT NULL;

        UPDATE TB_ALERT
        SET REPORTING_TIME_FAILURE = SYS_EXTRACT_UTC(FROM_TZ(REPORTING_TIME_FAILURE, TIMEZONE))
        WHERE REPORTING_TIME_FAILURE IS NOT NULL;

        UPDATE TB_AUTHENTICATION_ENTRY
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_AUTHENTICATION_ENTRY
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_AUTHENTICATION_ENTRY
        SET PASSWORD_CHANGE_DATE = SYS_EXTRACT_UTC(FROM_TZ(PASSWORD_CHANGE_DATE, TIMEZONE))
        WHERE PASSWORD_CHANGE_DATE IS NOT NULL;

        UPDATE TB_AUTHENTICATION_ENTRY
        SET SUSPENSION_DATE = SYS_EXTRACT_UTC(FROM_TZ(SUSPENSION_DATE, TIMEZONE))
        WHERE SUSPENSION_DATE IS NOT NULL;

        UPDATE TB_AUTHENTICATION_ENTRY_AUD
        SET PASSWORD_CHANGE_DATE = SYS_EXTRACT_UTC(FROM_TZ(PASSWORD_CHANGE_DATE, TIMEZONE))
        WHERE PASSWORD_CHANGE_DATE IS NOT NULL;

        UPDATE TB_BACKEND_FILTER
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_BACKEND_FILTER
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_CERTIFICATE
        SET ALERT_EXP_NOTIFICATION_DATE = SYS_EXTRACT_UTC(FROM_TZ(ALERT_EXP_NOTIFICATION_DATE, TIMEZONE))
        WHERE ALERT_EXP_NOTIFICATION_DATE IS NOT NULL;

        UPDATE TB_CERTIFICATE
        SET ALERT_IMM_NOTIFICATION_DATE = SYS_EXTRACT_UTC(FROM_TZ(ALERT_IMM_NOTIFICATION_DATE, TIMEZONE))
        WHERE ALERT_IMM_NOTIFICATION_DATE IS NOT NULL;

        UPDATE TB_CERTIFICATE
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_CERTIFICATE
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_CERTIFICATE
        SET REVOKE_NOTIFICATION_DATE = SYS_EXTRACT_UTC(FROM_TZ(REVOKE_NOTIFICATION_DATE, TIMEZONE))
        WHERE REVOKE_NOTIFICATION_DATE IS NOT NULL;

        UPDATE TB_CERTIFICATE_AUD
        SET ALERT_EXP_NOTIFICATION_DATE = SYS_EXTRACT_UTC(FROM_TZ(ALERT_EXP_NOTIFICATION_DATE, TIMEZONE))
        WHERE ALERT_EXP_NOTIFICATION_DATE IS NOT NULL;

        UPDATE TB_CERTIFICATE_AUD
        SET ALERT_IMM_NOTIFICATION_DATE = SYS_EXTRACT_UTC(FROM_TZ(ALERT_IMM_NOTIFICATION_DATE, TIMEZONE))
        WHERE ALERT_IMM_NOTIFICATION_DATE IS NOT NULL;

        UPDATE TB_CERTIFICATE_AUD
        SET REVOKE_NOTIFICATION_DATE = SYS_EXTRACT_UTC(FROM_TZ(REVOKE_NOTIFICATION_DATE, TIMEZONE))
        WHERE REVOKE_NOTIFICATION_DATE IS NOT NULL;

        UPDATE TB_COMMAND
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_COMMAND
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_COMMAND_PROPERTY
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_COMMAND_PROPERTY
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_D_ACTION
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_D_ACTION
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_D_AGREEMENT
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_D_AGREEMENT
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_D_MESSAGE_PROPERTY
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_D_MESSAGE_PROPERTY
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_D_MESSAGE_STATUS
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_D_MESSAGE_STATUS
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_D_MPC
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_D_MPC
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_D_MSH_ROLE
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_D_MSH_ROLE
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_D_NOTIFICATION_STATUS
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_D_NOTIFICATION_STATUS
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_D_PART_PROPERTY
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_D_PART_PROPERTY
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_D_PARTY
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_D_PARTY
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_D_ROLE
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_D_ROLE
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_D_SERVICE
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_D_SERVICE
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_ENCRYPTION_KEY
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_ENCRYPTION_KEY
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_ERROR_LOG
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_ERROR_LOG
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_ERROR_LOG
        SET NOTIFIED = SYS_EXTRACT_UTC(FROM_TZ(NOTIFIED, TIMEZONE))
        WHERE NOTIFIED IS NOT NULL;

        UPDATE TB_ERROR_LOG
        SET TIME_STAMP = SYS_EXTRACT_UTC(FROM_TZ(TIME_STAMP, TIMEZONE))
        WHERE TIME_STAMP IS NOT NULL;

        UPDATE TB_EVENT
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_EVENT
        SET LAST_ALERT_DATE = SYS_EXTRACT_UTC(FROM_TZ(LAST_ALERT_DATE, TIMEZONE))
        WHERE LAST_ALERT_DATE IS NOT NULL;

        UPDATE TB_EVENT
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_EVENT
        SET REPORTING_TIME = SYS_EXTRACT_UTC(FROM_TZ(REPORTING_TIME, TIMEZONE))
        WHERE REPORTING_TIME IS NOT NULL;

        UPDATE TB_EVENT_ALERT
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_EVENT_ALERT
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_EVENT_PROPERTY
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_EVENT_PROPERTY
        SET DATE_VALUE = SYS_EXTRACT_UTC(FROM_TZ(DATE_VALUE, TIMEZONE))
        WHERE DATE_VALUE IS NOT NULL;

        UPDATE TB_EVENT_PROPERTY
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_MESSAGE_ACKNW
        SET ACKNOWLEDGE_DATE = SYS_EXTRACT_UTC(FROM_TZ(ACKNOWLEDGE_DATE, TIMEZONE))
        WHERE ACKNOWLEDGE_DATE IS NOT NULL;

        UPDATE TB_MESSAGE_ACKNW
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_MESSAGE_ACKNW
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_MESSAGE_ACKNW_PROP
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_MESSAGE_ACKNW_PROP
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_MESSAGE_PROPERTIES
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_MESSAGE_PROPERTIES
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_MESSAGING_LOCK
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_MESSAGING_LOCK
        SET MESSAGE_RECEIVED = SYS_EXTRACT_UTC(FROM_TZ(MESSAGE_RECEIVED, TIMEZONE))
        WHERE MESSAGE_RECEIVED IS NOT NULL;

        UPDATE TB_MESSAGING_LOCK
        SET MESSAGE_STALED = SYS_EXTRACT_UTC(FROM_TZ(MESSAGE_STALED, TIMEZONE))
        WHERE MESSAGE_STALED IS NOT NULL;

        UPDATE TB_MESSAGING_LOCK
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_MESSAGING_LOCK
        SET NEXT_ATTEMPT = SYS_EXTRACT_UTC(FROM_TZ(NEXT_ATTEMPT, TIMEZONE))
        WHERE NEXT_ATTEMPT IS NOT NULL;

        UPDATE TB_PART_INFO
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PART_INFO
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PART_PROPERTIES
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PART_PROPERTIES
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PLUGIN_USER_PASSWD_HISTORY
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PLUGIN_USER_PASSWD_HISTORY
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PLUGIN_USER_PASSWD_HISTORY
        SET PASSWORD_CHANGE_DATE = SYS_EXTRACT_UTC(FROM_TZ(PASSWORD_CHANGE_DATE, TIMEZONE))
        WHERE PASSWORD_CHANGE_DATE IS NOT NULL;

        UPDATE TB_PM_ACTION
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_ACTION
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_AGREEMENT
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_AGREEMENT
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_BUSINESS_PROCESS
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_BUSINESS_PROCESS
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_CONFIGURATION
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_CONFIGURATION
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_CONFIGURATION_RAW
        SET CONFIGURATION_DATE = SYS_EXTRACT_UTC(FROM_TZ(CONFIGURATION_DATE, TIMEZONE))
        WHERE CONFIGURATION_DATE IS NOT NULL;

        UPDATE TB_PM_CONFIGURATION_RAW
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_CONFIGURATION_RAW
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_CONFIGURATION_RAW_AUD
        SET CONFIGURATION_DATE = SYS_EXTRACT_UTC(FROM_TZ(CONFIGURATION_DATE, TIMEZONE))
        WHERE CONFIGURATION_DATE IS NOT NULL;

        UPDATE TB_PM_ERROR_HANDLING
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_ERROR_HANDLING
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_JOIN_PAYLOAD_PROFILE
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_JOIN_PAYLOAD_PROFILE
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_JOIN_PROCESS_INIT_PARTY
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_JOIN_PROCESS_INIT_PARTY
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_JOIN_PROCESS_LEG
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_JOIN_PROCESS_LEG
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_JOIN_PROCESS_RESP_PARTY
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_JOIN_PROCESS_RESP_PARTY
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_JOIN_PROPERTY_SET
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_JOIN_PROPERTY_SET
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_LEG
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_LEG
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_LEG_MPC
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_LEG_MPC
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_MEP
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_MEP
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_MEP_BINDING
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_MEP_BINDING
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_MESSAGE_PROPERTY
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_MESSAGE_PROPERTY
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_MESSAGE_PROPERTY_SET
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_MESSAGE_PROPERTY_SET
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_MPC
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_MPC
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_PARTY
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_PARTY
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_PARTY_ID_TYPE
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_PARTY_ID_TYPE
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_PARTY_IDENTIFIER
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_PARTY_IDENTIFIER
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_PAYLOAD
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_PAYLOAD
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_PAYLOAD_PROFILE
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_PAYLOAD_PROFILE
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_PROCESS
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_PROCESS
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_RECEPTION_AWARENESS
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_RECEPTION_AWARENESS
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_RELIABILITY
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_RELIABILITY
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_ROLE
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_ROLE
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_SECURITY
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_SECURITY
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_SERVICE
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_SERVICE
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_PM_SPLITTING
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_PM_SPLITTING
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_RECEIPT
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_RECEIPT
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_REV_CHANGES
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_REV_CHANGES
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_REV_INFO
        SET REVISION_DATE = SYS_EXTRACT_UTC(FROM_TZ(REVISION_DATE, TIMEZONE))
        WHERE REVISION_DATE IS NOT NULL;

        UPDATE TB_ROUTING_CRITERIA
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_ROUTING_CRITERIA
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_SEND_ATTEMPT
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_SEND_ATTEMPT
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_SIGNAL_MESSAGE
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_SIGNAL_MESSAGE
        SET EBMS3_TIMESTAMP = SYS_EXTRACT_UTC(FROM_TZ(EBMS3_TIMESTAMP, TIMEZONE))
        WHERE EBMS3_TIMESTAMP IS NOT NULL;

        UPDATE TB_SIGNAL_MESSAGE
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_SIGNAL_MESSAGE_LOG
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_SIGNAL_MESSAGE_LOG
        SET DELETED = SYS_EXTRACT_UTC(FROM_TZ(DELETED, TIMEZONE))
        WHERE DELETED IS NOT NULL;

        UPDATE TB_SIGNAL_MESSAGE_LOG
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_SIGNAL_MESSAGE_LOG
        SET RECEIVED = SYS_EXTRACT_UTC(FROM_TZ(RECEIVED, TIMEZONE))
        WHERE RECEIVED IS NOT NULL;

        UPDATE TB_SIGNAL_MESSAGE_RAW
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_SIGNAL_MESSAGE_RAW
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_SJ_MESSAGE_FRAGMENT
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_SJ_MESSAGE_FRAGMENT
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_SJ_MESSAGE_GROUP
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_SJ_MESSAGE_GROUP
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_SJ_MESSAGE_HEADER
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_SJ_MESSAGE_HEADER
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_USER
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_USER
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_USER
        SET PASSWORD_CHANGE_DATE = SYS_EXTRACT_UTC(FROM_TZ(PASSWORD_CHANGE_DATE, TIMEZONE))
        WHERE PASSWORD_CHANGE_DATE IS NOT NULL;

        UPDATE TB_USER
        SET SUSPENSION_DATE = SYS_EXTRACT_UTC(FROM_TZ(SUSPENSION_DATE, TIMEZONE))
        WHERE SUSPENSION_DATE IS NOT NULL;

        UPDATE TB_USER_AUD
        SET PASSWORD_CHANGE_DATE = SYS_EXTRACT_UTC(FROM_TZ(PASSWORD_CHANGE_DATE, TIMEZONE))
        WHERE PASSWORD_CHANGE_DATE IS NOT NULL;

        UPDATE TB_USER_MESSAGE
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_USER_MESSAGE
        SET EBMS3_TIMESTAMP = SYS_EXTRACT_UTC(FROM_TZ(EBMS3_TIMESTAMP, TIMEZONE))
        WHERE EBMS3_TIMESTAMP IS NOT NULL;

        UPDATE TB_USER_MESSAGE
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_USER_MESSAGE_LOG
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_USER_MESSAGE_LOG
        SET DELETED = SYS_EXTRACT_UTC(FROM_TZ(DELETED, TIMEZONE))
        WHERE DELETED IS NOT NULL;

        UPDATE TB_USER_MESSAGE_LOG
        SET DOWNLOADED = SYS_EXTRACT_UTC(FROM_TZ(DOWNLOADED, TIMEZONE))
        WHERE DOWNLOADED IS NOT NULL;

        UPDATE TB_USER_MESSAGE_LOG
        SET FAILED = SYS_EXTRACT_UTC(FROM_TZ(FAILED, TIMEZONE))
        WHERE FAILED IS NOT NULL;

        UPDATE TB_USER_MESSAGE_LOG
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_USER_MESSAGE_LOG
        SET NEXT_ATTEMPT = SYS_EXTRACT_UTC(FROM_TZ(NEXT_ATTEMPT, TIMEZONE))
        WHERE NEXT_ATTEMPT IS NOT NULL;

        UPDATE TB_USER_MESSAGE_LOG
        SET RECEIVED = SYS_EXTRACT_UTC(FROM_TZ(RECEIVED, TIMEZONE))
        WHERE RECEIVED IS NOT NULL;

        UPDATE TB_USER_MESSAGE_LOG
        SET RESTORED = SYS_EXTRACT_UTC(FROM_TZ(RESTORED, TIMEZONE))
        WHERE RESTORED IS NOT NULL;

        UPDATE TB_USER_MESSAGE_RAW
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_USER_MESSAGE_RAW
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_USER_PASSWORD_HISTORY
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_USER_PASSWORD_HISTORY
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_USER_PASSWORD_HISTORY
        SET PASSWORD_CHANGE_DATE = SYS_EXTRACT_UTC(FROM_TZ(PASSWORD_CHANGE_DATE, TIMEZONE))
        WHERE PASSWORD_CHANGE_DATE IS NOT NULL;

        UPDATE TB_USER_ROLE
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_USER_ROLE
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_USER_ROLES
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE TB_USER_ROLES
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE TB_VERSION
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE WS_PLUGIN_TB_BACKEND_MSG_LOG
        SET CREATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(CREATION_TIME, TIMEZONE))
        WHERE CREATION_TIME IS NOT NULL;

        UPDATE WS_PLUGIN_TB_BACKEND_MSG_LOG
        SET MODIFICATION_TIME = SYS_EXTRACT_UTC(FROM_TZ(MODIFICATION_TIME, TIMEZONE))
        WHERE MODIFICATION_TIME IS NOT NULL;

        UPDATE WS_PLUGIN_TB_MESSAGE_LOG
        SET RECEIVED = SYS_EXTRACT_UTC(FROM_TZ(RECEIVED, TIMEZONE))
        WHERE RECEIVED IS NOT NULL;

        COMMIT;
    END migrate;

END MIGRATE_42_TO_50_utc_conversion;
/