#echo "Cleaning wildfly queues"
#/opt/wildfly12/domibus/bin/jboss-cli.sh --connect --commands=/subsystem=messaging-activemq/server=default/jms-queue=DomibusNotifyBackendWebServiceQueue:remove-messages,/subsystem=messaging-activemq/server=default/jms-queue=DomibusSendMessageQueue:remove-messages,/subsystem=messaging-activemq/server=default/jms-queue=DomibusPullMessageQueue:remove-messages,/subsystem=messaging-activemq/server=default/jms-queue=DomibusDLQ:remove-messages,/subsystem=messaging-activemq/server=default/jms-queue=DomibusNotifyBackendWebServiceQueue:remove-messages
echo "Cleaning mysql"
mysql -h localhost -u root --password=Taxud_C2Db domibus < /home/edelivery/delete_all_msgs.txt
echo "Done!"
