package eu.domibus.tomcat;

import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyMetadata.Type;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.ext.domain.Module;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Property manager for the Tomcat servers specific properties.
 */
@Service
public class TomcatPropertyMetadataManager implements DomibusPropertyMetadataManagerSPI {

    private Map<String, DomibusPropertyMetadata> knownProperties = Arrays.asList(
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_XA_DATA_SOURCE_CLASS_NAME, Type.CLASS, Module.TOMCAT),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_MAX_LIFETIME, Type.NUMERIC, Module.TOMCAT),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_MIN_POOL_SIZE, Type.NUMERIC, Module.TOMCAT),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_MAX_POOL_SIZE, Type.NUMERIC, Module.TOMCAT),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_BORROW_CONNECTION_TIMEOUT, Type.NUMERIC, Module.TOMCAT),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_REAP_TIMEOUT, Type.NUMERIC, Module.TOMCAT),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_MAX_IDLE_TIME, Type.NUMERIC, Module.TOMCAT),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_DATASOURCE_XA_MAINTENANCE_INTERVAL, Type.NUMERIC, Module.TOMCAT),

            DomibusPropertyMetadata.getReadOnlyGlobalProperty(DOMIBUS_JMS_XACONNECTION_FACTORY_MAX_POOL_SIZE, Type.NUMERIC, Module.TOMCAT), //move the usage from xml ?

            DomibusPropertyMetadata.getReadOnlyGlobalProperty(COM_ATOMIKOS_ICATCH_OUTPUT_DIR, Type.URI, Module.TOMCAT), //move the usage from xml ?
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(COM_ATOMIKOS_ICATCH_LOG_BASE_DIR, Type.URI, Module.TOMCAT), //move the usage from xml ?
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(COM_ATOMIKOS_ICATCH_DEFAULT_JTA_TIMEOUT, Type.NUMERIC, Module.TOMCAT), //move the usage from xml ?
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(COM_ATOMIKOS_ICATCH_MAX_TIMEOUT, Type.NUMERIC, Module.TOMCAT), //move the usage from xml ?
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(COM_ATOMIKOS_ICATCH_MAX_ACTIVES, Type.NUMERIC, Module.TOMCAT),

            DomibusPropertyMetadata.getReadOnlyGlobalProperty(ACTIVE_MQ_BROKER_HOST, Module.TOMCAT), //cannot find the usage
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(ACTIVE_MQ_BROKER_NAME, Module.TOMCAT), //move the usage from xml ?
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(ACTIVE_MQ_EMBEDDED_CONFIGURATION_FILE, Type.URI, Module.TOMCAT),
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(ACTIVE_MQ_JMXURL, Type.URI, Module.TOMCAT), //move the usage from xml ?
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(ACTIVE_MQ_CONNECTOR_PORT, Type.NUMERIC, Module.TOMCAT), //move the usage from xml ?
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(ACTIVE_MQ_TRANSPORT_CONNECTOR_URI, Type.URI, Module.TOMCAT), //move the usage from xml ?
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(ACTIVE_MQ_USERNAME, Module.TOMCAT), //move the usage from xml ?
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(ACTIVE_MQ_PASSWORD, Type.PASSWORD, Module.TOMCAT), //move the usage from xml ?
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(ACTIVE_MQ_PERSISTENT, Type.BOOLEAN, Module.TOMCAT), //move the usage from xml ?
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(ACTIVE_MQ_CONNECTION_CLOSE_TIMEOUT, Type.NUMERIC, Module.TOMCAT), //move the usage from xml ?
            DomibusPropertyMetadata.getReadOnlyGlobalProperty(ACTIVE_MQ_CONNECTION_CONNECT_RESPONSE_TIMEOUT, Type.NUMERIC, Module.TOMCAT) //move the usage from xml ?
    ).stream().collect(Collectors.toMap(x -> x.getName(), x -> x));

    @Override
    public Map<String, DomibusPropertyMetadata> getKnownProperties() {
        return knownProperties;
    }

    @Override
    public boolean hasKnownProperty(String name) {
        return getKnownProperties().containsKey(name);
    }
}
