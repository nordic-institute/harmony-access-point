package eu.domibus.messaging.jms;


/**
 * DTO to store a pair of jmsMessageID and domain code
 *
 * @author Catalin Enache
 * @since 4.2
 */
public class JMSMessageDomainDTO {

    private String jmsMessageId;

    private String domainCode;

    public JMSMessageDomainDTO(String jmsMessageId, String domainCode) {
        this.jmsMessageId = jmsMessageId;
        this.domainCode = domainCode;
    }

    public String getJmsMessageId() {
        return jmsMessageId;
    }

    public String getDomainCode() {
        return domainCode;
    }
}
