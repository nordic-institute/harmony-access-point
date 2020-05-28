##### ORA-12519, TNS:no appropriate service handler found
##### for cluster execute as sysdba and restart
# alter system set processes=150 scope=spfile;
# commit;

BEGIN
dbms_aqadm.create_queue_table (queue_table => 'OUTQUEUE',  queue_payload_type => 'sys.aq$_jms_message',multiple_consumers => false);
dbms_aqadm.create_queue_table (queue_table => 'REPLYQUEUE', queue_payload_type => 'sys.aq$_jms_message',multiple_consumers => false);
dbms_aqadm.create_queue_table (queue_table => 'ERRORNOTIFYCONSUMER', queue_payload_type => 'sys.aq$_jms_message',multiple_consumers => false);
dbms_aqadm.create_queue_table (queue_table => 'ERRORNOTIFYPRODUCER', queue_payload_type => 'sys.aq$_jms_message',multiple_consumers => false);
dbms_aqadm.create_queue_table (queue_table => 'INQUEUE', queue_payload_type => 'sys.aq$_jms_message',multiple_consumers => false);
dbms_aqadm.create_queue_table (queue_table => 'NOTIFJMSQUEUE', queue_payload_type => 'sys.aq$_jms_message',multiple_consumers => false);
dbms_aqadm.create_queue_table (queue_table => 'DISPATCHQUEUE', queue_payload_type => 'sys.aq$_jms_message',multiple_consumers => false);
dbms_aqadm.create_queue_table (queue_table => 'NOTIFWSQUEUE', queue_payload_type => 'sys.aq$_jms_message',multiple_consumers => false);
dbms_aqadm.create_queue_table (queue_table => 'UNKNOWNQUEUE', queue_payload_type => 'sys.aq$_jms_message',multiple_consumers => false);
dbms_aqadm.create_queue_table (queue_table => 'INTNOTIFQUEUE', queue_payload_type => 'sys.aq$_jms_message',multiple_consumers => false);
dbms_aqadm.create_queue_table (queue_table => 'INTPULLQUEUE', queue_payload_type => 'sys.aq$_jms_message',multiple_consumers => false);
dbms_aqadm.create_queue_table (queue_table => 'FSSENDQUEUE', queue_payload_type => 'sys.aq$_jms_message',multiple_consumers => false);
dbms_aqadm.create_queue_table (queue_table => 'ALERTQUEUE', queue_payload_type => 'sys.aq$_jms_message',multiple_consumers => false);
dbms_aqadm.create_queue_table (queue_table => 'UIREPQUEUE', queue_payload_type => 'sys.aq$_jms_message',multiple_consumers => false);
dbms_aqadm.create_queue_table (queue_table => 'LMQUEUE', queue_payload_type => 'sys.aq$_jms_message',multiple_consumers => false);
dbms_aqadm.create_queue_table (queue_table => 'SPLITQUEUE', queue_payload_type => 'sys.aq$_jms_message',multiple_consumers => false);
dbms_aqadm.create_queue_table (queue_table => 'PULLRECEIPTQUEUE', queue_payload_type => 'sys.aq$_jms_message',multiple_consumers => false);
dbms_aqadm.create_queue_table (queue_table => 'RETENTIONQUEUE', queue_payload_type => 'sys.aq$_jms_message',multiple_consumers => false);
dbms_aqadm.create_queue_table (queue_table => 'FSQUEUE', queue_payload_type => 'sys.aq$_jms_message',multiple_consumers => false);
dbms_aqadm.create_queue_table (queue_table => 'DLQQUEUE', queue_payload_type => 'sys.aq$_jms_message',multiple_consumers => false);
dbms_aqadm.create_queue_table (queue_table => 'TOPICQUEUE', queue_payload_type => 'sys.aq$_jms_message',multiple_consumers => false);

dbms_aqadm.create_queue (queue_name => 'OUTQUEUE',queue_table => 'OUTQUEUE');
dbms_aqadm.create_queue (queue_name => 'REPLYQUEUE',queue_table => 'REPLYQUEUE');
dbms_aqadm.create_queue (queue_name => 'ERRORNOTIFYCONSUMER',queue_table => 'ERRORNOTIFYCONSUMER');
dbms_aqadm.create_queue (queue_name => 'ERRORNOTIFYPRODUCER',queue_table => 'ERRORNOTIFYPRODUCER');
dbms_aqadm.create_queue (queue_name => 'INQUEUE',queue_table => 'INQUEUE');
dbms_aqadm.create_queue (queue_name => 'NOTIFJMSQUEUE',queue_table => 'NOTIFJMSQUEUE');
dbms_aqadm.create_queue (queue_name => 'DISPATCHQUEUE',queue_table => 'DISPATCHQUEUE');
dbms_aqadm.create_queue (queue_name => 'NOTIFWSQUEUE',queue_table => 'NOTIFWSQUEUE');
dbms_aqadm.create_queue (queue_name => 'UNKNOWNQUEUE',queue_table => 'UNKNOWNQUEUE');
dbms_aqadm.create_queue (queue_name => 'INTNOTIFQUEUE',queue_table => 'INTNOTIFQUEUE');
dbms_aqadm.create_queue (queue_name => 'INTPULLQUEUE',queue_table => 'INTPULLQUEUE');
dbms_aqadm.create_queue (queue_name => 'FSSENDQUEUE',queue_table => 'FSSENDQUEUE');
dbms_aqadm.create_queue (queue_name => 'ALERTQUEUE',queue_table => 'ALERTQUEUE');
dbms_aqadm.create_queue (queue_name => 'UIREPQUEUE',queue_table => 'UIREPQUEUE');
dbms_aqadm.create_queue (queue_name => 'LMQUEUE',queue_table => 'LMQUEUE');
dbms_aqadm.create_queue (queue_name => 'SPLITQUEUE',queue_table => 'SPLITQUEUE');
dbms_aqadm.create_queue (queue_name => 'PULLRECEIPTQUEUE',queue_table => 'PULLRECEIPTQUEUE');
dbms_aqadm.create_queue (queue_name => 'RETENTIONQUEUE',queue_table => 'RETENTIONQUEUE');
dbms_aqadm.create_queue (queue_name => 'FSQUEUE',queue_table => 'FSQUEUE');
dbms_aqadm.create_queue (queue_name => 'DLQQUEUE',queue_table => 'DLQQUEUE');
dbms_aqadm.create_queue (queue_name => 'TOPICQUEUE',queue_table => 'TOPICQUEUE');

dbms_aqadm.start_queue (queue_name => 'OUTQUEUE');
dbms_aqadm.start_queue (queue_name => 'REPLYQUEUE');
dbms_aqadm.start_queue (queue_name => 'ERRORNOTIFYCONSUMER');
dbms_aqadm.start_queue (queue_name => 'ERRORNOTIFYPRODUCER');
dbms_aqadm.start_queue (queue_name => 'INQUEUE');
dbms_aqadm.start_queue (queue_name => 'NOTIFJMSQUEUE');
dbms_aqadm.start_queue (queue_name => 'DISPATCHQUEUE');
dbms_aqadm.start_queue (queue_name => 'NOTIFWSQUEUE');
dbms_aqadm.start_queue (queue_name => 'UNKNOWNQUEUE');
dbms_aqadm.start_queue (queue_name => 'INTNOTIFQUEUE');
dbms_aqadm.start_queue (queue_name => 'INTPULLQUEUE');
dbms_aqadm.start_queue (queue_name => 'FSSENDQUEUE');
dbms_aqadm.start_queue (queue_name => 'ALERTQUEUE');
dbms_aqadm.start_queue (queue_name => 'UIREPQUEUE');
dbms_aqadm.start_queue (queue_name => 'LMQUEUE');
dbms_aqadm.start_queue (queue_name => 'SPLITQUEUE');
dbms_aqadm.start_queue (queue_name => 'PULLRECEIPTQUEUE');
dbms_aqadm.start_queue (queue_name => 'RETENTIONQUEUE');
dbms_aqadm.start_queue (queue_name => 'FSQUEUE');
dbms_aqadm.start_queue (queue_name => 'DLQQUEUE');
dbms_aqadm.start_queue (queue_name => 'TOPICQUEUE');
END;

##### ORA-12519, TNS:no appropriate service handler found
##### for cluster execute as sysdba and restart
# alter system set processes=150 scope=spfile;
# commit;
