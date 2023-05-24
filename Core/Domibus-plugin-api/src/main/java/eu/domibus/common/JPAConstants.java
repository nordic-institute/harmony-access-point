package eu.domibus.common;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public final class JPAConstants {

    public static final String PERSISTENCE_UNIT_NAME = "domibusEM";

    public static final String DOMIBUS_SCALABLE_SEQUENCE = "DOMIBUS_SCALABLE_SEQUENCE";

    public static final String DATE_PREFIXED_SEQUENCE_ID_GENERATOR = "eu.domibus.api.model.DatePrefixedGenericSequenceIdGenerator";

    public static final String SEQUENCE_INCREMENT_SIZE = "50";

    private JPAConstants() {

    }
}