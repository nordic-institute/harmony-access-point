rm -Rf /opt/wildfly12/domibus/standalone/data/*
rm -Rf /opt/wildfly12/domibus/standalone/deployments/*
rm -Rf /opt/wildfly12/domibus/standalone/tmp/*
rm -Rf /opt/wildfly12/domibus/standalone/configuration/standalone_xml_history/*
/home/edelivery/clearLogs.sh
cp /home/edelivery/domibus-MSH-wildfly12-4.2-SNAPSHOT.war /opt/wildfly12/domibus/standalone/deployments/
#mv /datadrive/domibus/payload /datadrive/domibus/oldPayload
#mkdir /datadrive/domibus/payload
