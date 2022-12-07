package eu.domibus.api.jms;

import eu.domibus.api.validators.CustomWhiteListed;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author Ion Perpegel
 * @version 5.1
 */

public class JmsFilterRequest {

    private String source;

    private String originalQueue;

    private String jmsType;

    private Date fromDate;

    private Date toDate;

    private String selector;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getOriginalQueue() {
        return originalQueue;
    }

    public void setOriginalQueue(String originalQueue) {
        this.originalQueue = originalQueue;
    }

    public String getJmsType() {
        return jmsType;
    }

    public void setJmsType(String jmsType) {
        this.jmsType = jmsType;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

}
