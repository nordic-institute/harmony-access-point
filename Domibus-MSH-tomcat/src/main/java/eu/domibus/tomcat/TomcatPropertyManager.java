package eu.domibus.tomcat;

import eu.domibus.api.property.DomibusPropertyManager;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyServiceDelegateAbstract;
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
@Service("serverPropertyManager")
public class TomcatPropertyManager extends DomibusPropertyServiceDelegateAbstract
        implements DomibusPropertyManager {

    private Map<String, DomibusPropertyMetadata> knownProperties = Arrays.stream(new String[]{
            DOMIBUS_DATASOURCE_XA_XA_DATA_SOURCE_CLASS_NAME,
            DOMIBUS_DATASOURCE_XA_MAX_LIFETIME,
            DOMIBUS_DATASOURCE_XA_MIN_POOL_SIZE,
            DOMIBUS_DATASOURCE_XA_MAX_POOL_SIZE,
            DOMIBUS_DATASOURCE_XA_BORROW_CONNECTION_TIMEOUT,
            DOMIBUS_DATASOURCE_XA_REAP_TIMEOUT,
            DOMIBUS_DATASOURCE_XA_MAX_IDLE_TIME,
            DOMIBUS_DATASOURCE_XA_MAINTENANCE_INTERVAL,

            DOMIBUS_JMS_XACONNECTION_FACTORY_MAX_POOL_SIZE, //move the usage from xml ?

            COM_ATOMIKOS_ICATCH_OUTPUT_DIR, //move the usage from xml ?
            COM_ATOMIKOS_ICATCH_LOG_BASE_DIR, //move the usage from xml ?
            COM_ATOMIKOS_ICATCH_DEFAULT_JTA_TIMEOUT, //move the usage from xml ?
            COM_ATOMIKOS_ICATCH_MAX_TIMEOUT, //move the usage from xml ?
            COM_ATOMIKOS_ICATCH_MAX_ACTIVES,

            ACTIVE_MQ_BROKER_HOST, //cannot find the usage
            ACTIVE_MQ_BROKER_NAME, //move the usage from xml ?
            ACTIVE_MQ_EMBEDDED_CONFIGURATION_FILE,
            ACTIVE_MQ_JMXURL, //move the usage from xml ?
            ACTIVE_MQ_CONNECTOR_PORT, //move the usage from xml ?
            ACTIVE_MQ_TRANSPORT_CONNECTOR_URI, //move the usage from xml ?
            ACTIVE_MQ_USERNAME, //move the usage from xml ?
            ACTIVE_MQ_PASSWORD, //move the usage from xml ?
            ACTIVE_MQ_PERSISTENT, //move the usage from xml ?
            ACTIVE_MQ_CONNECTION_CLOSE_TIMEOUT, //move the usage from xml ?
            ACTIVE_MQ_CONNECTION_CONNECT_RESPONSE_TIMEOUT, //move the usage from xml ?

    })
            .map(name -> DomibusPropertyMetadata.getReadOnlyGlobalProperty(name, Module.TOMCAT))
            .collect(Collectors.toMap(x -> x.getName(), x -> x));

    @Override
    public Map<String, DomibusPropertyMetadata> getKnownProperties() {
        return knownProperties;
    }
}
