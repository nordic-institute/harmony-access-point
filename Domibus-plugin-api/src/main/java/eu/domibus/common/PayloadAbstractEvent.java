package eu.domibus.common;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
public abstract class PayloadAbstractEvent implements Serializable, MessageEvent {

    protected String messageId;

    protected String cid;

    protected String mime;

    protected String fileName;

    protected Map<String, String> properties = new HashMap<>(); //NOSONAR

    @Override
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public void addProperty(String key, String value) {
        properties.put(key, value);
    }

    @Override
    public Map<String, String> getProps() {
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Needed for backward compatibility between 4.2 and 5.0
     * @deprecated Use instead {@link MessageEvent#getProps()}
     */
    @Deprecated
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getMime() {
        return mime;
    }

    public void setMime(String mime) {
        this.mime = mime;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }


}
