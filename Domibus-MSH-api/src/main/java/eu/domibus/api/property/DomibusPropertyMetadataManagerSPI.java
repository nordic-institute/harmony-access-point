package eu.domibus.api.property;

import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 * <p>
 * The interface implemented by MSH to expose metadata for all of the configuration properties
 */
public interface DomibusPropertyMetadataManagerSPI {

    String DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_PREFIX = "domibus.alert.user.account_disabled.";
    String DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_PREFIX = "domibus.alert.user.account_enabled.";
    String DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_PREFIX = "domibus.alert.cert.imminent_expiration.";
    String DOMIBUS_ALERT_CERT_EXPIRED_PREFIX = "domibus.alert.cert.expired.";
    String DOMIBUS_ALERT_USER_LOGIN_FAILURE_PREFIX = "domibus.alert.user.login_failure.";
    String DOMIBUS_ALERT_SENDER_SMTP_PREFIX = "domibus.alert.sender.smtp.";
    String DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_PREFIX = "domibus.alert.msg.communication_failure.";
    String DOMIBUS_ALERT_PASSWORD_EXPIRED_PREFIX = "domibus.alert.password.expired"; //NOSONAR
    String DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_PREFIX = "domibus.alert.password.imminent_expiration"; //NOSONAR
    String DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_PREFIX = "domibus.alert.plugin.user.account_disabled.";
    String DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_PREFIX = "domibus.alert.plugin.user.account_enabled.";
    String DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_PREFIX = "domibus.alert.plugin.user.login_failure.";
    String DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_PREFIX = "domibus.alert.plugin_password.expired"; //NOSONAR
    String DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_PREFIX = "domibus.alert.plugin_password.imminent_expiration"; //NOSONAR
    String DOMIBUS_SECURITY_KEYSTORE_PREFIX = "domibus.security.keystore.";
    String DOMIBUS_SECURITY_TRUSTSTORE_PREFIX = "domibus.security.truststore.";
    String DOMIBUS_PROXY_PREFIX = "domibus.proxy.";

    String DOMIBUS_UI_TITLE_NAME = "domibus.UI.title.name";
    String DOMIBUS_UI_REPLICATION_ENABLED = "domibus.ui.replication.enabled";
    String DOMIBUS_UI_REPLICATION_QUEUE_CONCURENCY = "domibus.ui.replication.queue.concurency";
    String DOMIBUS_UI_SUPPORT_TEAM_NAME = "domibus.ui.support.team.name";
    String DOMIBUS_UI_SUPPORT_TEAM_EMAIL = "domibus.ui.support.team.email";
    String DOMIBUS_UI_CSV_MAX_ROWS = "domibus.ui.csv.rows.max";
    String DOMIBUS_SECURITY_KEYSTORE_LOCATION = DOMIBUS_SECURITY_KEYSTORE_PREFIX + "location";
    String DOMIBUS_SECURITY_KEYSTORE_TYPE = DOMIBUS_SECURITY_KEYSTORE_PREFIX + "type";
    String DOMIBUS_SECURITY_KEYSTORE_PASSWORD = DOMIBUS_SECURITY_KEYSTORE_PREFIX + "password";//NOSONAR
    String DOMIBUS_SECURITY_KEY_PRIVATE_ALIAS = "domibus.security.key.private.alias";
    String DOMIBUS_SECURITY_TRUSTSTORE_LOCATION = DOMIBUS_SECURITY_TRUSTSTORE_PREFIX + "location";
    String DOMIBUS_SECURITY_TRUSTSTORE_TYPE = DOMIBUS_SECURITY_TRUSTSTORE_PREFIX + "type";
    String DOMIBUS_SECURITY_TRUSTSTORE_PASSWORD = DOMIBUS_SECURITY_TRUSTSTORE_PREFIX + "password";//NOSONAR
    String DOMIBUS_AUTH_UNSECURE_LOGIN_ALLOWED = "domibus.auth.unsecureLoginAllowed";
    String DOMIBUS_CONSOLE_LOGIN_MAXIMUM_ATTEMPT = "domibus.console.login.maximum.attempt";
    String DOMIBUS_CONSOLE_LOGIN_SUSPENSION_TIME = "domibus.console.login.suspension.time";
    String DOMIBUS_CERTIFICATE_REVOCATION_OFFSET = "domibus.certificate.revocation.offset";
    String DOMIBUS_CERTIFICATE_CRL_EXCLUDED_PROTOCOLS = "domibus.certificate.crl.excludedProtocols";
    String DOMIBUS_PLUGIN_LOGIN_MAXIMUM_ATTEMPT = "domibus.plugin.login.maximum.attempt";
    String DOMIBUS_PLUGIN_LOGIN_SUSPENSION_TIME = "domibus.plugin.login.suspension.time";
    String DOMIBUS_PASSWORD_POLICY_PATTERN = "domibus.passwordPolicy.pattern";//NOSONAR
    String DOMIBUS_PASSWORD_POLICY_VALIDATION_MESSAGE = "domibus.passwordPolicy.validationMessage";//NOSONAR
    String DOMIBUS_PASSWORD_POLICY_EXPIRATION = "domibus.passwordPolicy.expiration";//NOSONAR
    String DOMIBUS_PASSWORD_POLICY_DEFAULT_PASSWORD_EXPIRATION = "domibus.passwordPolicy.defaultPasswordExpiration";//NOSONAR
    String DOMIBUS_PASSWORD_POLICY_WARNING_BEFORE_EXPIRATION = "domibus.passwordPolicy.warning.beforeExpiration";//NOSONAR
    String DOMIBUS_PASSWORD_POLICY_DONT_REUSE_LAST = "domibus.passwordPolicy.dontReuseLast";//NOSONAR
    String DOMIBUS_PASSWORD_POLICY_CHECK_DEFAULT_PASSWORD = "domibus.passwordPolicy.checkDefaultPassword";//NOSONAR
    String DOMIBUS_PLUGIN_PASSWORD_POLICY_PATTERN = "domibus.plugin.passwordPolicy.pattern";//NOSONAR
    String DOMIBUS_PLUGIN_PASSWORD_POLICY_VALIDATION_MESSAGE = "domibus.plugin.passwordPolicy.validationMessage";//NOSONAR
    String DOMIBUS_PASSWORD_POLICY_PLUGIN_EXPIRATION = "domibus.plugin.passwordPolicy.expiration";//NOSONAR
    String DOMIBUS_PASSWORD_POLICY_PLUGIN_DEFAULT_PASSWORD_EXPIRATION = "domibus.plugin.passwordPolicy.defaultPasswordExpiration";//NOSONAR
    String DOMIBUS_PASSWORD_POLICY_PLUGIN_DONT_REUSE_LAST = "domibus.plugin.passwordPolicy.dontReuseLast";//NOSONAR
    String DOMIBUS_ATTACHMENT_STORAGE_LOCATION = "domibus.attachment.storage.location";
    String DOMIBUS_PAYLOAD_ENCRYPTION_ACTIVE = "domibus.payload.encryption.active";
    String DOMIBUS_MSH_MESSAGEID_SUFFIX = "domibus.msh.messageid.suffix";
    String DOMIBUS_MSH_RETRY_MESSAGE_EXPIRATION_DELAY = "domibus.msh.retry.messageExpirationDelay";
    String DOMIBUS_DYNAMICDISCOVERY_USE_DYNAMIC_DISCOVERY = "domibus.dynamicdiscovery.useDynamicDiscovery";
    String DOMIBUS_SMLZONE = "domibus.smlzone";
    String DOMIBUS_DYNAMICDISCOVERY_CLIENT_SPECIFICATION = "domibus.dynamicdiscovery.client.specification";
    String DOMIBUS_DYNAMICDISCOVERY_PEPPOLCLIENT_MODE = "domibus.dynamicdiscovery.peppolclient.mode";
    String DOMIBUS_DYNAMICDISCOVERY_OASISCLIENT_REGEX_CERTIFICATE_SUBJECT_VALIDATION = "domibus.dynamicdiscovery.oasisclient.regexCertificateSubjectValidation";
    String DOMIBUS_DYNAMICDISCOVERY_PARTYID_RESPONDER_ROLE = "domibus.dynamicdiscovery.partyid.responder.role";
    String DOMIBUS_DYNAMICDISCOVERY_PARTYID_TYPE = "domibus.dynamicdiscovery.partyid.type";
    String DOMIBUS_DYNAMICDISCOVERY_TRANSPORTPROFILEAS_4 = "domibus.dynamicdiscovery.transportprofileas4";
    String DOMIBUS_LIST_PENDING_MESSAGES_MAX_COUNT = "domibus.listPendingMessages.maxCount";
    String DOMIBUS_JMS_CONNECTION_FACTORY_SESSION_CACHE_SIZE = "domibus.jms.connectionFactory.session.cache.size";
    String DOMIBUS_JMS_QUEUE_MAX_BROWSE_SIZE = "domibus.jms.queue.maxBrowseSize";
    String DOMIBUS_JMS_INTERNAL_QUEUE_EXPRESSION = "domibus.jms.internalQueue.expression";
    String DOMIBUS_RECEIVER_CERTIFICATE_VALIDATION_ONSENDING = "domibus.receiver.certificate.validation.onsending";
    String DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONSENDING = "domibus.sender.certificate.validation.onsending";
    String DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONRECEIVING = "domibus.sender.certificate.validation.onreceiving";
    String DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING = "domibus.sender.trust.validation.onreceiving";
    String DOMIBUS_SENDER_TRUST_VALIDATION_EXPRESSION = "domibus.sender.trust.validation.expression";
    String DOMIBUS_SENDER_CERTIFICATE_SUBJECT_CHECK = "domibus.sender.certificate.subject.check";
    String DOMIBUS_SENDER_TRUST_VALIDATION_TRUSTSTORE_ALIAS = "domibus.sender.trust.validation.truststore_alias";
    String DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN = "domibus.sendMessage.messageIdPattern";
    String DOMIBUS_DISPATCHER_CONNECTION_TIMEOUT = "domibus.dispatcher.connectionTimeout";
    String DOMIBUS_DISPATCHER_RECEIVE_TIMEOUT = "domibus.dispatcher.receiveTimeout";
    String DOMIBUS_DISPATCHER_ALLOW_CHUNKING = "domibus.dispatcher.allowChunking";
    String DOMIBUS_DISPATCHER_CHUNKING_THRESHOLD = "domibus.dispatcher.chunkingThreshold";
    String DOMIBUS_DISPATCHER_CONCURENCY = "domibus.dispatcher.concurency";
    String DOMIBUS_DISPATCHER_LARGE_FILES_CONCURRENCY = "domibus.dispatcher.largeFiles.concurrency";
    String DOMIBUS_DISPATCHER_CACHEABLE = "domibus.dispatcher.cacheable";
    String DOMIBUS_DISPATCHER_CONNECTION_KEEP_ALIVE = "domibus.dispatcher.connection.keepAlive";
    String DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_DOWNLOADED_MAX_DELETE = "domibus.retentionWorker.message.retention.downloaded.max.delete";
    String DOMIBUS_RETENTION_WORKER_MESSAGE_RETENTION_NOT_DOWNLOADED_MAX_DELETE = "domibus.retentionWorker.message.retention.not_downloaded.max.delete";
    String DOMIBUS_RETENTION_JMS_CONCURRENCY = "domibus.retention.jms.concurrency";
    String DOMIBUS_DISPATCH_EBMS_ERROR_UNRECOVERABLE_RETRY = "domibus.dispatch.ebms.error.unrecoverable.retry";
    String DOMIBUS_PROXY_ENABLED = DOMIBUS_PROXY_PREFIX + "enabled";
    String DOMIBUS_PROXY_HTTP_HOST = DOMIBUS_PROXY_PREFIX + "http.host";
    String DOMIBUS_PROXY_HTTP_PORT = DOMIBUS_PROXY_PREFIX + "http.port";
    String DOMIBUS_PROXY_USER = DOMIBUS_PROXY_PREFIX + "user";
    String DOMIBUS_PROXY_PASSWORD = DOMIBUS_PROXY_PREFIX + "password"; //NOSONAR:
    String DOMIBUS_PROXY_NON_PROXY_HOSTS = DOMIBUS_PROXY_PREFIX + "nonProxyHosts";
    String DOMIBUS_UI_REPLICATION_SYNC_CRON_MAX_ROWS = "domibus.ui.replication.sync.cron.max.rows";
    String DOMIBUS_PLUGIN_NOTIFICATION_ACTIVE = "domibus.plugin.notification.active";
    String DOMIBUS_NONREPUDIATION_AUDIT_ACTIVE = "domibus.nonrepudiation.audit.active";
    String DOMIBUS_SEND_MESSAGE_FAILURE_DELETE_PAYLOAD = "domibus.sendMessage.failure.delete.payload";
    String DOMIBUS_SEND_MESSAGE_ATTEMPT_AUDIT_ACTIVE = "domibus.sendMessage.attempt.audit.active";
    String DOMIBUS_FOURCORNERMODEL_ENABLED = "domibus.fourcornermodel.enabled";
    String DOMIBUS_LOGGING_PAYLOAD_PRINT = "domibus.logging.payload.print";
    String DOMIBUS_LOGGING_CXF_LIMIT = "domibus.logging.cxf.limit";
    String DOMIBUS_ATTACHMENT_TEMP_STORAGE_LOCATION = "domibus.attachment.temp.storage.location";
    String DOMIBUS_DISPATCHER_SPLIT_AND_JOIN_CONCURRENCY = "domibus.dispatcher.splitAndJoin.concurrency";
    String DOMIBUS_DISPATCHER_SPLIT_AND_JOIN_PAYLOADS_SCHEDULE_THRESHOLD = "domibus.dispatcher.splitAndJoin.payloads.schedule.threshold";
    String DOMAIN_TITLE = "domain.title";
    String DOMIBUS_USER_INPUT_BLACK_LIST = "domibus.userInput.blackList";
    String DOMIBUS_USER_INPUT_WHITE_LIST = "domibus.userInput.whiteList";
    String DOMIBUS_ACCOUNT_UNLOCK_CRON = "domibus.account.unlock.cron";
    String DOMIBUS_CERTIFICATE_CHECK_CRON = "domibus.certificate.check.cron";
    String DOMIBUS_PLUGIN_ACCOUNT_UNLOCK_CRON = "domibus.plugin.account.unlock.cron";
    String DOMIBUS_PASSWORD_POLICIES_CHECK_CRON = "domibus.passwordPolicies.check.cron";//NOSONAR
    String DOMIBUS_PLUGIN_PASSWORD_POLICIES_CHECK_CRON = "domibus.plugin_passwordPolicies.check.cron";//NOSONAR
    String DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_CRON = "domibus.payload.temp.job.retention.cron";
    String DOMIBUS_MSH_RETRY_CRON = "domibus.msh.retry.cron";
    String DOMIBUS_RETENTION_WORKER_CRON_EXPRESSION = "domibus.retentionWorker.cronExpression";
    String DOMIBUS_MSH_PULL_CRON = "domibus.msh.pull.cron";
    String DOMIBUS_PULL_RETRY_CRON = "domibus.pull.retry.cron";
    String DOMIBUS_ALERT_CLEANER_CRON = "domibus.alert.cleaner.cron";
    String DOMIBUS_ALERT_RETRY_CRON = "domibus.alert.retry.cron";
    String DOMIBUS_UI_REPLICATION_SYNC_CRON = "domibus.ui.replication.sync.cron";
    String DOMIBUS_SPLIT_AND_JOIN_RECEIVE_EXPIRATION_CRON = "domibus.splitAndJoin.receive.expiration.cron";
    String DOMIBUS_MONITORING_CONNECTION_CRON = "domibus.monitoring.connection.cron";
    String DOMIBUS_MONITORING_CONNECTION_PARTY_ENABLED = "domibus.monitoring.connection.party.enabled";
    String DOMIBUS_ALERT_ACTIVE = "domibus.alert.active";
    String DOMIBUS_ALERT_MAIL_SENDING_ACTIVE = "domibus.alert.mail.sending.active";
    String DOMIBUS_ALERT_MAIL_SMTP_TIMEOUT = "domibus.alert.mail.smtp.timeout";
    String DOMIBUS_ALERT_SENDER_SMTP_URL = DOMIBUS_ALERT_SENDER_SMTP_PREFIX + "url";
    String DOMIBUS_ALERT_SENDER_SMTP_PORT = DOMIBUS_ALERT_SENDER_SMTP_PREFIX + "port";
    String DOMIBUS_ALERT_SENDER_SMTP_USER = DOMIBUS_ALERT_SENDER_SMTP_PREFIX + "user";
    String DOMIBUS_ALERT_SENDER_SMTP_PASSWORD = DOMIBUS_ALERT_SENDER_SMTP_PREFIX + "password";//NOSONAR
    String DOMIBUS_ALERT_SENDER_EMAIL = "domibus.alert.sender.email";
    String DOMIBUS_ALERT_RECEIVER_EMAIL = "domibus.alert.receiver.email";
    String DOMIBUS_ALERT_CLEANER_ALERT_LIFETIME = "domibus.alert.cleaner.alert.lifetime";
    String DOMIBUS_ALERT_RETRY_TIME = "domibus.alert.retry.time";
    String DOMIBUS_ALERT_RETRY_MAX_ATTEMPTS = "domibus.alert.retry.max_attempts";
    String DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_ACTIVE = DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_PREFIX + "active";
    String DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_STATES = DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_PREFIX + "states";
    String DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_LEVEL = DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_PREFIX + "level";
    String DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_MAIL_SUBJECT = DOMIBUS_ALERT_MSG_COMMUNICATION_FAILURE_PREFIX + "mail.subject";
    String DOMIBUS_ALERT_USER_LOGIN_FAILURE_ACTIVE = DOMIBUS_ALERT_USER_LOGIN_FAILURE_PREFIX + "active";
    String DOMIBUS_ALERT_USER_LOGIN_FAILURE_LEVEL = DOMIBUS_ALERT_USER_LOGIN_FAILURE_PREFIX + "level";
    String DOMIBUS_ALERT_USER_LOGIN_FAILURE_MAIL_SUBJECT = DOMIBUS_ALERT_USER_LOGIN_FAILURE_PREFIX + "mail.subject";
    String DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_ACTIVE = DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_PREFIX + "active";
    String DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_LEVEL = DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_PREFIX + "level";
    String DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_MOMENT = DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_PREFIX + "moment";
    String DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_SUBJECT = DOMIBUS_ALERT_USER_ACCOUNT_DISABLED_PREFIX + "subject";
    String DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_ACTIVE = DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_PREFIX + "active";
    String DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_LEVEL = DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_PREFIX + "level";
    String DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_SUBJECT = DOMIBUS_ALERT_USER_ACCOUNT_ENABLED_PREFIX + "subject";
    String DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_ACTIVE = DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_PREFIX + "active";
    String DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_DELAY_DAYS = DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_PREFIX + "delay_days";
    String DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_FREQUENCY_DAYS = DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_PREFIX + "frequency_days";
    String DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_LEVEL = DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_PREFIX + "level";
    String DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_MAIL_SUBJECT = DOMIBUS_ALERT_CERT_IMMINENT_EXPIRATION_PREFIX + "mail.subject";
    String DOMIBUS_ALERT_CERT_EXPIRED_ACTIVE = DOMIBUS_ALERT_CERT_EXPIRED_PREFIX + "active";
    String DOMIBUS_ALERT_CERT_EXPIRED_FREQUENCY_DAYS = DOMIBUS_ALERT_CERT_EXPIRED_PREFIX + "frequency_days";
    String DOMIBUS_ALERT_CERT_EXPIRED_DURATION_DAYS = DOMIBUS_ALERT_CERT_EXPIRED_PREFIX + "duration_days";
    String DOMIBUS_ALERT_CERT_EXPIRED_LEVEL = DOMIBUS_ALERT_CERT_EXPIRED_PREFIX + "level";
    String DOMIBUS_ALERT_CERT_EXPIRED_MAIL_SUBJECT = DOMIBUS_ALERT_CERT_EXPIRED_PREFIX + "mail.subject";
    String DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_ACTIVE = DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_PREFIX + ".active";//NOSONAR
    String DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_DELAY_DAYS = DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_PREFIX + ".delay_days";//NOSONAR
    String DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_FREQUENCY_DAYS = DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_PREFIX + ".frequency_days";//NOSONAR
    String DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_LEVEL = DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_PREFIX + ".level";//NOSONAR
    String DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_MAIL_SUBJECT = DOMIBUS_ALERT_PASSWORD_IMMINENT_EXPIRATION_PREFIX + ".mail.subject";//NOSONAR
    String DOMIBUS_ALERT_PASSWORD_EXPIRED_ACTIVE = DOMIBUS_ALERT_PASSWORD_EXPIRED_PREFIX + ".active";//NOSONAR
    String DOMIBUS_ALERT_PASSWORD_EXPIRED_DELAY_DAYS = DOMIBUS_ALERT_PASSWORD_EXPIRED_PREFIX + ".delay_days";//NOSONAR
    String DOMIBUS_ALERT_PASSWORD_EXPIRED_FREQUENCY_DAYS = DOMIBUS_ALERT_PASSWORD_EXPIRED_PREFIX + ".frequency_days";//NOSONAR
    String DOMIBUS_ALERT_PASSWORD_EXPIRED_LEVEL = DOMIBUS_ALERT_PASSWORD_EXPIRED_PREFIX + ".level";//NOSONAR
    String DOMIBUS_ALERT_PASSWORD_EXPIRED_MAIL_SUBJECT = DOMIBUS_ALERT_PASSWORD_EXPIRED_PREFIX + ".mail.subject";//NOSONAR
    String DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_ACTIVE = DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_PREFIX + ".active";//NOSONAR
    String DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_DELAY_DAYS = DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_PREFIX + ".delay_days";//NOSONAR
    String DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_FREQUENCY_DAYS = DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_PREFIX + ".frequency_days";//NOSONAR
    String DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_LEVEL = DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_PREFIX + ".level";//NOSONAR
    String DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_MAIL_SUBJECT = DOMIBUS_ALERT_PLUGIN_PASSWORD_IMMINENT_EXPIRATION_PREFIX + ".mail.subject";//NOSONAR
    String DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_ACTIVE = DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_PREFIX + ".active";//NOSONAR
    String DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_DELAY_DAYS = DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_PREFIX + ".delay_days";//NOSONAR
    String DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_FREQUENCY_DAYS = DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_PREFIX + ".frequency_days";//NOSONAR
    String DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_LEVEL = DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_PREFIX + ".level";//NOSONAR
    String DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_MAIL_SUBJECT = DOMIBUS_ALERT_PLUGIN_PASSWORD_EXPIRED_PREFIX + ".mail.subject";//NOSONAR
    String DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_ACTIVE = DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_PREFIX + "active";
    String DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_LEVEL = DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_PREFIX + "level";
    String DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_MAIL_SUBJECT = DOMIBUS_ALERT_PLUGIN_USER_LOGIN_FAILURE_PREFIX + "mail.subject";
    String DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_ACTIVE = DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_PREFIX + "active";
    String DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_LEVEL = DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_PREFIX + "level";
    String DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_MOMENT = DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_PREFIX + "moment";
    String DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_SUBJECT = DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_DISABLED_PREFIX + "subject";
    String DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_ACTIVE = DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_PREFIX + "active";
    String DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_LEVEL = DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_PREFIX + "level";
    String DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_SUBJECT = DOMIBUS_ALERT_PLUGIN_USER_ACCOUNT_ENABLED_PREFIX + "subject";

    String DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE = "domibus.pull.request.send.per.job.cycle";
    String DOMIBUS_PULL_REQUEST_FREQUENCY_RECOVERY_TIME = "domibus.pull.request.frequency.recovery.time";
    String DOMIBUS_PULL_REQUEST_FREQUENCY_ERROR_COUNT = "domibus.pull.request.frequency.error.count";
    String DOMIBUS_PULL_DYNAMIC_INITIATOR = "domibus.pull.dynamic.initiator";
    String DOMIBUS_PULL_MULTIPLE_LEGS = "domibus.pull.multiple_legs";
    String DOMIBUS_PULL_FORCE_BY_MPC = "domibus.pull.force_by_mpc";
    String DOMIBUS_PULL_MPC_INITIATOR_SEPARATOR = "domibus.pull.mpc_initiator_separator";
    String DOMIBUS_PULL_RECEIPT_QUEUE_CONCURRENCY = "domibus.pull.receipt.queue.concurrency";
    String DOMIBUS_PULL_QUEUE_CONCURENCY = "domibus.pull.queue.concurency";

    String DOMIBUS_EXTENSION_IAM_AUTHENTICATION_IDENTIFIER = "domibus.extension.iam.authentication.identifier";
    String DOMIBUS_EXTENSION_IAM_AUTHORIZATION_IDENTIFIER = "domibus.extension.iam.authorization.identifier";
    String DOMIBUS_EXCEPTIONS_REST_ENABLE = "domibus.exceptions.rest.enable";

    String DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_EXCLUDE_REGEX = "domibus.payload.temp.job.retention.exclude.regex";
    String DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_EXPIRATION = "domibus.payload.temp.job.retention.expiration";
    String DOMIBUS_PAYLOAD_TEMP_JOB_RETENTION_DIRECTORIES = "domibus.payload.temp.job.retention.directories";
    String DOMIBUS_INSTANCE_NAME = "domibus.instance.name";

    String DOMIBUS_CONFIG_LOCATION = "domibus.config.location";
    String DOMIBUS_DEPLOYMENT_CLUSTERED = "domibus.deployment.clustered";
    String DOMIBUS_SECURITY_KEY_PRIVATE_PASSWORD = "domibus.security.key.private.password";//NOSONAR
    String DOMIBUS_DATABASE_GENERAL_SCHEMA = "domibus.database.general.schema";
    String DOMIBUS_DATABASE_SCHEMA = "domibus.database.schema";
    String DOMIBUS_DATASOURCE_XA_XA_DATA_SOURCE_CLASS_NAME = "domibus.datasource.xa.xaDataSourceClassName";
    String DOMIBUS_DATASOURCE_XA_MAX_LIFETIME = "domibus.datasource.xa.maxLifetime";
    String DOMIBUS_DATASOURCE_XA_MIN_POOL_SIZE = "domibus.datasource.xa.minPoolSize";
    String DOMIBUS_DATASOURCE_XA_MAX_POOL_SIZE = "domibus.datasource.xa.maxPoolSize";
    String DOMIBUS_DATASOURCE_XA_BORROW_CONNECTION_TIMEOUT = "domibus.datasource.xa.borrowConnectionTimeout";
    String DOMIBUS_DATASOURCE_XA_REAP_TIMEOUT = "domibus.datasource.xa.reapTimeout";
    String DOMIBUS_DATASOURCE_XA_MAX_IDLE_TIME = "domibus.datasource.xa.maxIdleTime";
    String DOMIBUS_DATASOURCE_XA_MAINTENANCE_INTERVAL = "domibus.datasource.xa.maintenanceInterval";
    String DOMIBUS_DATASOURCE_XA_PROPERTY_USER = "domibus.datasource.xa.property.user";
    String DOMIBUS_DATASOURCE_XA_PROPERTY_PASSWORD = "domibus.datasource.xa.property.password"; //NOSONAR
    String DOMIBUS_DATASOURCE_XA_PROPERTY_url = "domibus.datasource.xa.property.url";
    String DOMIBUS_DATASOURCE_XA_PROPERTY_URL = "domibus.datasource.xa.property.URL";
    String DOMIBUS_DATASOURCE_DRIVER_CLASS_NAME = "domibus.datasource.driverClassName";
    String DOMIBUS_DATASOURCE_URL = "domibus.datasource.url";
    String DOMIBUS_DATASOURCE_USER = "domibus.datasource.user";
    String DOMIBUS_DATASOURCE_PASSWORD = "domibus.datasource.password";//NOSONAR
    String DOMIBUS_DATASOURCE_MAX_LIFETIME = "domibus.datasource.maxLifetime";
    String DOMIBUS_DATASOURCE_MIN_POOL_SIZE = "domibus.datasource.minPoolSize";
    String DOMIBUS_DATASOURCE_MAX_POOL_SIZE = "domibus.datasource.maxPoolSize";
    String DOMIBUS_ENTITY_MANAGER_FACTORY_PACKAGES_TO_SCAN = "domibus.entityManagerFactory.packagesToScan";
    String DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY = "domibus.entityManagerFactory.jpaProperty";
    String DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_CONNECTION_DRIVER_CLASS = "domibus.entityManagerFactory.jpaProperty.hibernate.connection.driver_class";
    String DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_DIALECT = "domibus.entityManagerFactory.jpaProperty.hibernate.dialect";
    String DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_ID_NEW_GENERATOR_MAPPINGS = "domibus.entityManagerFactory.jpaProperty.hibernate.id.new_generator_mappings";
    String DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_FORMAT_SQL = "domibus.entityManagerFactory.jpaProperty.hibernate.format_sql";
    String DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_TRANSACTION_FACTORY_CLASS = "domibus.entityManagerFactory.jpaProperty.hibernate.transaction.factory_class";
    String DOMIBUS_ENTITY_MANAGER_FACTORY_JPA_PROPERTY_HIBERNATE_TRANSACTION_JTA_PLATFORM = "domibus.entityManagerFactory.jpaProperty.hibernate.transaction.jta.platform";
    String DOMIBUS_PASSWORD_ENCRYPTION_ACTIVE = "domibus.password.encryption.active"; //NOSONAR
    String DOMIBUS_PASSWORD_ENCRYPTION_PROPERTIES = "domibus.password.encryption.properties"; //NOSONAR
    String DOMIBUS_PASSWORD_ENCRYPTION_KEY_LOCATION = "domibus.password.encryption.key.location";//NOSONAR
    String DOMIBUS_JMS_QUEUE_PULL = "domibus.jms.queue.pull";
    String DOMIBUS_JMS_QUEUE_UI_REPLICATION = "domibus.jms.queue.ui.replication";
    String DOMIBUS_JMS_XACONNECTION_FACTORY_MAX_POOL_SIZE = "domibus.jms.XAConnectionFactory.maxPoolSize";
    String DOMIBUS_JMS_QUEUE_ALERT = "domibus.jms.queue.alert";
    String DOMIBUS_TASK_EXECUTOR_THREAD_COUNT = "domibus.taskExecutor.threadCount";
    String COM_ATOMIKOS_ICATCH_OUTPUT_DIR = "com.atomikos.icatch.output_dir";
    String COM_ATOMIKOS_ICATCH_LOG_BASE_DIR = "com.atomikos.icatch.log_base_dir";
    String COM_ATOMIKOS_ICATCH_DEFAULT_JTA_TIMEOUT = "com.atomikos.icatch.default_jta_timeout";
    String COM_ATOMIKOS_ICATCH_MAX_TIMEOUT = "com.atomikos.icatch.max_timeout";
    String COM_ATOMIKOS_ICATCH_MAX_ACTIVES = "com.atomikos.icatch.max_actives";
    String ACTIVE_MQ_BROKER_HOST = "activeMQ.broker.host";
    String ACTIVE_MQ_BROKER_NAME = "activeMQ.brokerName";
    String ACTIVE_MQ_EMBEDDED_CONFIGURATION_FILE = "activeMQ.embedded.configurationFile";
    String ACTIVE_MQ_JMXURL = "activeMQ.JMXURL";
    String ACTIVE_MQ_CONNECTOR_PORT = "activeMQ.connectorPort";
    String ACTIVE_MQ_TRANSPORT_CONNECTOR_URI = "activeMQ.transportConnector.uri";
    String ACTIVE_MQ_USERNAME = "activeMQ.username";
    String ACTIVE_MQ_PASSWORD = "activeMQ.password";//NOSONAR
    String ACTIVE_MQ_PERSISTENT = "activeMQ.persistent";
    String ACTIVE_MQ_CONNECTION_CLOSE_TIMEOUT = "activeMQ.connection.closeTimeout";
    String ACTIVE_MQ_CONNECTION_CONNECT_RESPONSE_TIMEOUT = "activeMQ.connection.connectResponseTimeout";
    String ACTIVE_MQ_ARTEMIS_BROKER = "domibus.jms.activemq.artemis.broker";
    String DOMIBUS_ALERT_QUEUE_CONCURRENCY = "domibus.alert.queue.concurrency";
    String MESSAGE_FACTORY_CLASS = "messageFactoryClass";
    String COMPRESSION_BLACKLIST = "compressionBlacklist";
    String DOMIBUS_JMS_INTERNAL_COMMAND_CONCURENCY = "domibus.jms.internal.command.concurrency";
    String DOMIBUS_INTERNAL_QUEUE_CONCURENCY = "domibus.internal.queue.concurency";
    String DOMIBUS_METRICS_JMX_REPORTER_ENABLE = "domibus.metrics.jmx.reporter.enable";
    String DOMIBUS_METRICS_SL_4_J_REPORTER_ENABLE = "domibus.metrics.sl4j.reporter.enable";
    String DOMIBUS_METRICS_SL_4_J_REPORTER_PERIOD_TIME_UNIT = "domibus.metrics.sl4j.reporter.period.time.unit";
    String DOMIBUS_METRICS_SL_4_J_REPORTER_PERIOD_NUMBER = "domibus.metrics.sl4j.reporter.period.number";
    String DOMIBUS_METRICS_MONITOR_MEMORY = "domibus.metrics.monitor.memory";
    String DOMIBUS_METRICS_MONITOR_GC = "domibus.metrics.monitor.gc";
    String DOMIBUS_METRICS_MONITOR_CACHED_THREADS = "domibus.metrics.monitor.cached.threads";
    String DOMIBUS_METRICS_MONITOR_JMS_QUEUES = "domibus.metrics.monitor.jms.queues";
    String DOMIBUS_SECURITY_EXT_AUTH_PROVIDER_ENABLED = "domibus.security.ext.auth.provider.enabled";
    String DOMIBUS_JMX_PASSWORD = "domibus.jmx.password"; //NOSONAR
    String DOMIBUS_JMX_USER = "domibus.jmx.user";
    String WEBLOGIC_MANAGEMENT_SERVER = "weblogic.management.server";
    String DOMIBUS_CLUSTER_COMMAND_CRON_EXPRESSION = "domibus.cluster.command.cronExpression";
    String DOMIBUS_PULL_REQUEST_SEND_PER_JOB_CYCLE_PER_MPC = "domibus.pull.request.send.per.job.cycle.per.mpc";
    String DOMIBUS_FILE_UPLOAD_MAX_SIZE = "domibus.file.upload.maxSize";
    String DOMIBUS_JDBC_DATASOURCE_JNDI_NAME = "domibus.jdbc.datasource.jndi.name";
    String DOMIBUS_JDBC_DATASOURCE_QUARTZ_JNDI_NAME = "domibus.jdbc.datasource.quartz.jndi.name";
    String DOMIBUS_METRICS_MONITOR_JMS_QUEUES_REFRESH_PERIOD = "domibus.metrics.monitor.jms.queues.refresh.period";
    String DOMIBUS_METRICS_MONITOR_JMS_QUEUES_SHOW_DLQ_ONLY = "domibus.metrics.monitor.jms.queues.show.dlq.only";
    /**
     * Get all the properties metadata that support changing at runtime
     *
     * @return properties as metadata
     */
    Map<String, DomibusPropertyMetadata> getKnownProperties();

    /**
     * True if the manager handles the specified property
     *
     * @param name the name of the property
     * @return
     */
    boolean hasKnownProperty(String name);
}
