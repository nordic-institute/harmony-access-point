package eu.domibus.test.utils

class DomibusSoapUIConstants {
    
    public static final def PROP_GLOBAL_JMS_ALL_PROPERTIES = 'allJMSDomainsProperties'
    // JMS JSON properties
    public static final def JSON_JMS_TYPE = 'jmsClientType';
    public static final def JSON_JMS_URL = 'jmsUrl';
    public static final def JSON_JMS_QUEUE = 'jmsQueue';
    public static final def JSON_JMS_CF_JNDI = 'jmsConnectionFactoryJNDI';
    public static final def JSON_JMS_SRV_USERNAME = 'jmsServerUsername';
    public static final def JSON_JMS_SRV_PASSWORD = 'jmsServerPassword';
    public static final def JSON_JMS_PLG_USERNAME = 'jmsPluginUsername';
    public static final def JSON_JMS_PLG_PASSWORD = 'jmsPluginPassword';
	public static final def BATCH_SUCCESS_STATUS="success" // Status value assigned to a batch in case of a successfull export
	public static final def BATCH_FAILURE_STATUS="failure" // Status value assigned to a batch in case of a failed export
}
