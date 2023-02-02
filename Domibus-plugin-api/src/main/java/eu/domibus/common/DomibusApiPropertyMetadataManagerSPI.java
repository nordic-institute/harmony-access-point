package eu.domibus.common;

/**
 * The interface implemented by MSH to expose metadata for all of the configuration properties to plugins
 *
 * @author François Gautier
 * @since 5.0.5
 */
public interface DomibusApiPropertyMetadataManagerSPI {

    String DOMIBUS_LOGGING_PAYLOAD_PRINT = "domibus.logging.payload.print";
    String DOMIBUS_LOGGING_METADATA_PRINT = "domibus.logging.metadata.print";
    String DOMIBUS_LOGGING_CXF_LIMIT = "domibus.logging.cxf.limit";

}
