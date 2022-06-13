package eu.domibus.plugin.jms;

import org.apache.commons.lang3.StringUtils;

public enum JMSPluginAttachmentReferenceType {

    FILE,
    URL;

    public static JMSPluginAttachmentReferenceType fromValue(String value) {
        final JMSPluginAttachmentReferenceType[] values = JMSPluginAttachmentReferenceType.values();
        for (JMSPluginAttachmentReferenceType jmsPluginAttachmentReferenceType : values) {
            if(StringUtils.equalsIgnoreCase(jmsPluginAttachmentReferenceType.name(), value)) {
                return jmsPluginAttachmentReferenceType;
            }
        }
        return null;
    }
}
