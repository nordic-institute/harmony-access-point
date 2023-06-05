package eu.domibus.core.ebms3.sender.interceptor;

import eu.domibus.logging.DomibusLogger;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstract Interceptor for Apache CXF Http headers
 *
 * @author Catalin Enache
 * @since 4.2
 */
public abstract class HttpHeaderAbstractInterceptor extends AbstractPhaseInterceptor<Message> {
    static final String USER_AGENT_HTTP_HEADER_KEY = "user-agent";
    static final String USER_AGENT_HTTP_HEADER_VALUE_APACHE_CXF = "Apache-CXF";

    public HttpHeaderAbstractInterceptor(String phase) {
        super(phase);
    }

    /**
     * It removes the user-agent header if contains Apache-CXF information
     *
     * @param message
     * @throws Fault
     */
    @Override
    public void handleMessage(Message message) throws Fault {
        //get the headers
        Map<String, List<String>> headers = (Map<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);

        if (headers == null) {
            getLogger().debug("no http headers to intercept");
            return;
        }

        boolean removed = headers.entrySet()
                .removeIf(e -> USER_AGENT_HTTP_HEADER_KEY.equalsIgnoreCase(e.getKey())
                        && StringUtils.containsIgnoreCase(Arrays.deepToString(e.getValue().toArray()), USER_AGENT_HTTP_HEADER_VALUE_APACHE_CXF)
                );

        getLogger().debug("httpHeader=[{}] {}", USER_AGENT_HTTP_HEADER_KEY, (removed ? " was successfully removed" : " not present or value not removed"));

        //logging of the remaining headers
        getLogger().debug("httpHeaders are: {}", httpHeadersToString(headers));
    }

    protected abstract DomibusLogger getLogger();

    private String httpHeadersToString(Map<String, List<String>> headers) {
        return headers.keySet().stream()
                .map(key -> key + "=" + Arrays.deepToString(headers.get(key).toArray()))
                .collect(Collectors.joining(", ", "{", "}"));
    }
}
