-- ##### execute as sysdba #####
-- GRANT connect, resource TO domibus_user IDENTIFIED BY domibus_passwd;
-- GRANT aq_user_role TO domibus_user;
-- GRANT execute ON sys.dbms_aqadm TO domibus_user;
-- GRANT execute ON sys.dbms_aq TO domibus_user;
-- GRANT execute ON sys.dbms_aqin TO domibus_user;
-- GRANT execute ON sys.dbms_aqjms TO domibus_user;
-- alter user domibus_user quota unlimited on USERS;
-- disconnect

-- ##### This is an SQL example script to create all Domibus queues in the Oracle AQ database #####
BEGIN
    -- 1. AQ TABLE QUEUES
    -- 1.1 BACKEND
    dbms_aqadm.create_queue_table (queue_table => 'TBQ_BCK_ERR_NOTIF_CONS', queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);
    dbms_aqadm.create_queue_table (queue_table => 'TBQ_BCK_ERR_NOTIF_PROD', queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);
    dbms_aqadm.create_queue_table (queue_table => 'TBQ_BCK_MSG_IN', queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);
    dbms_aqadm.create_queue_table (queue_table => 'TBQ_BCK_MSG_OUT',  queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);
    dbms_aqadm.create_queue_table (queue_table => 'TBQ_BCK_REPLY', queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);
    -- 1.2 INTERNAL
    dbms_aqadm.create_queue_table (queue_table => 'TBQ_INT_ALERT', queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);
    dbms_aqadm.create_queue_table (queue_table => 'TBQ_INT_DISPATCH', queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);
    dbms_aqadm.create_queue_table (queue_table => 'TBQ_INT_DLQ', queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);
    dbms_aqadm.create_queue_table (queue_table => 'TBQ_INT_EARCHIVE', queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);
    dbms_aqadm.create_queue_table (queue_table => 'TBQ_INT_EARCHIVE_NOTIF', queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);
    dbms_aqadm.create_queue_table (queue_table => 'TBQ_INT_EARCHIVE_NOTIF_DLQ', queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);
    dbms_aqadm.create_queue_table (queue_table => 'TBQ_INT_LARGE_MSG', queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);
    dbms_aqadm.create_queue_table (queue_table => 'TBQ_INT_NOTIF', queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);
    dbms_aqadm.create_queue_table (queue_table => 'TBQ_INT_NOTIF_UNKNOWN', queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);
    dbms_aqadm.create_queue_table (queue_table => 'TBQ_INT_PULL', queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);
    dbms_aqadm.create_queue_table (queue_table => 'TBQ_INT_PULL_RECEIPT', queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);
    dbms_aqadm.create_queue_table (queue_table => 'TBQ_INT_RETENTION_MSG', queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);
    dbms_aqadm.create_queue_table (queue_table => 'TBQ_INT_SPLIT_AND_JOIN', queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);
    -- 1.3 PLUGIN NOTIFICATION
    dbms_aqadm.create_queue_table (queue_table => 'TBQ_NOTIF_FS', queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);
    dbms_aqadm.create_queue_table (queue_table => 'TBQ_NOTIF_JMS', queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);
    dbms_aqadm.create_queue_table (queue_table => 'TBQ_NOTIF_WS', queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);
    -- 1.4 PLUGIN SEND
    dbms_aqadm.create_queue_table (queue_table => 'TBQ_FSPLUGIN_SEND', queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);
    dbms_aqadm.create_queue_table (queue_table => 'TBQ_WSPLUGIN_SEND', queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);
    -- 1.5 COMMAND TOPIC
    dbms_aqadm.create_queue_table (queue_table => 'TBT_COMMAND', queue_payload_type => 'sys.aq$_jms_message', multiple_consumers => false);

    -- 2. AQ QUEUES
    dbms_aqadm.create_queue (queue_name => 'Q_BCK_ERR_NOTIF_CONS', queue_table => 'TBQ_BCK_ERR_NOTIF_CONS');
    dbms_aqadm.create_queue (queue_name => 'Q_BCK_ERR_NOTIF_PROD', queue_table => 'TBQ_BCK_ERR_NOTIF_PROD');
    dbms_aqadm.create_queue (queue_name => 'Q_BCK_MSG_IN', queue_table => 'TBQ_BCK_MSG_IN');
    dbms_aqadm.create_queue (queue_name => 'Q_BCK_MSG_OUT', queue_table => 'TBQ_BCK_MSG_OUT');
    dbms_aqadm.create_queue (queue_name => 'Q_BCK_REPLY', queue_table => 'TBQ_BCK_REPLY');
    --
    dbms_aqadm.create_queue (queue_name => 'Q_INT_ALERT', queue_table => 'TBQ_INT_ALERT');
    dbms_aqadm.create_queue (queue_name => 'Q_INT_DISPATCH', queue_table => 'TBQ_INT_DISPATCH');
    dbms_aqadm.create_queue (queue_name => 'Q_INT_DLQ', queue_table => 'TBQ_INT_DLQ');
    dbms_aqadm.create_queue (queue_name => 'Q_INT_EARCHIVE', queue_table => 'TBQ_INT_EARCHIVE');
    dbms_aqadm.create_queue (queue_name => 'Q_INT_EARCHIVE_NOTIF', queue_table => 'TBQ_INT_EARCHIVE_NOTIF');
    dbms_aqadm.create_queue (queue_name => 'Q_INT_EARCHIVE_NOTIF_DLQ', queue_table => 'TBQ_INT_EARCHIVE_NOTIF_DLQ');
    dbms_aqadm.create_queue (queue_name => 'Q_INT_LARGE_MSG', queue_table => 'TBQ_INT_LARGE_MSG');
    dbms_aqadm.create_queue (queue_name => 'Q_INT_NOTIF', queue_table => 'TBQ_INT_NOTIF');
    dbms_aqadm.create_queue (queue_name => 'Q_INT_NOTIF_UNKNOWN', queue_table => 'TBQ_INT_NOTIF_UNKNOWN');
    dbms_aqadm.create_queue (queue_name => 'Q_INT_PULL', queue_table => 'TBQ_INT_PULL');
    dbms_aqadm.create_queue (queue_name => 'Q_INT_PULL_RECEIPT', queue_table => 'TBQ_INT_PULL_RECEIPT');
    dbms_aqadm.create_queue (queue_name => 'Q_INT_RETENTION_MSG', queue_table => 'TBQ_INT_RETENTION_MSG');
    dbms_aqadm.create_queue (queue_name => 'Q_INT_SPLIT_AND_JOIN', queue_table => 'TBQ_INT_SPLIT_AND_JOIN');
    --
    dbms_aqadm.create_queue (queue_name => 'Q_NOTIF_FS', queue_table => 'TBQ_NOTIF_FS');
    dbms_aqadm.create_queue (queue_name => 'Q_NOTIF_JMS', queue_table => 'TBQ_NOTIF_JMS');
    dbms_aqadm.create_queue (queue_name => 'Q_NOTIF_WS', queue_table => 'TBQ_NOTIF_WS');
    --
    dbms_aqadm.create_queue (queue_name => 'Q_FSPLUGIN_SEND', queue_table => 'TBQ_FSPLUGIN_SEND');
    dbms_aqadm.create_queue (queue_name => 'Q_WSPLUGIN_SEND', queue_table => 'TBQ_WSPLUGIN_SEND');
    --
    dbms_aqadm.create_queue (queue_name => 'T_COMMAND', queue_table => 'TBT_COMMAND');

    -- 3. START AQ QUEUES
    dbms_aqadm.start_queue (queue_name => 'Q_BCK_ERR_NOTIF_CONS');
    dbms_aqadm.start_queue (queue_name => 'Q_BCK_ERR_NOTIF_PROD');
    dbms_aqadm.start_queue (queue_name => 'Q_BCK_MSG_IN');
    dbms_aqadm.start_queue (queue_name => 'Q_BCK_MSG_OUT');
    dbms_aqadm.start_queue (queue_name => 'Q_BCK_REPLY');
    --
    dbms_aqadm.start_queue (queue_name => 'Q_INT_ALERT');
    dbms_aqadm.start_queue (queue_name => 'Q_INT_DISPATCH');
    dbms_aqadm.start_queue (queue_name => 'Q_INT_DLQ');
    dbms_aqadm.start_queue (queue_name => 'Q_INT_EARCHIVE');
    dbms_aqadm.start_queue (queue_name => 'Q_INT_EARCHIVE_NOTIF');
    dbms_aqadm.start_queue (queue_name => 'Q_INT_EARCHIVE_NOTIF_DLQ');
    dbms_aqadm.start_queue (queue_name => 'Q_INT_LARGE_MSG');
    dbms_aqadm.start_queue (queue_name => 'Q_INT_NOTIF');
    dbms_aqadm.start_queue (queue_name => 'Q_INT_NOTIF_UNKNOWN');
    dbms_aqadm.start_queue (queue_name => 'Q_INT_PULL');
    dbms_aqadm.start_queue (queue_name => 'Q_INT_PULL_RECEIPT');
    dbms_aqadm.start_queue (queue_name => 'Q_INT_RETENTION_MSG');
    dbms_aqadm.start_queue (queue_name => 'Q_INT_SPLIT_AND_JOIN');
    --
    dbms_aqadm.start_queue (queue_name => 'Q_NOTIF_FS');
    dbms_aqadm.start_queue (queue_name => 'Q_NOTIF_JMS');
    dbms_aqadm.start_queue (queue_name => 'Q_NOTIF_WS');
    --
    dbms_aqadm.start_queue (queue_name => 'Q_FSPLUGIN_SEND');
    dbms_aqadm.start_queue (queue_name => 'Q_WSPLUGIN_SEND');
    --
    dbms_aqadm.start_queue (queue_name => 'T_COMMAND');
END;

-- ##### ORA-12519, TNS:no appropriate service handler found
-- ##### for cluster execute as sysdba and restart
-- alter system set processes=150 scope=spfile;
-- commit;
