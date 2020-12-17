package eu.domibus.core.ebms3.sender.interceptor;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import org.apache.cxf.message.Message;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Catalin Enache
 * @since 4.2
 */
public class HttpHeaderOutInterceptorTest {

    @Tested
    HttpHeaderInInterceptor httpHeaderInInterceptor;

    @Test
    public void test_handleMessage_UserAgentPresentApache(final @Mocked Message message) {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("user-agent", Collections.singletonList("Apache-CXF/3.2"));
        headers.put("cache-control", Collections.singletonList("no-cache"));
        headers.put("connection", Collections.singletonList("keep-alive"));
        headers.put("content-type", Collections.singletonList("multipart/related; type=\"application/soap+xml\"; boundary=\"uuid:4f015876-24a6-48fe-88c7-23dc84886eca\"; start=\"<root.message@cxf.apache.org>\"; start-info=\"application/soap+xml\""));

        new Expectations() {{
            message.get(Message.PROTOCOL_HEADERS);
            result = headers;
        }};

        //tested method
        httpHeaderInInterceptor.handleMessage(message);

        Assert.assertNull(headers.get("user-agent"));
        Assert.assertNotNull(headers.get("cache-control"));
    }

    @Test
    public void test_handleMessage_UserAgentNotPresent(final @Mocked Message message) {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("accept", Collections.singletonList("*/*"));
        headers.put("connection", Collections.singletonList("keep-alive"));

        new Expectations() {{
            message.get(Message.PROTOCOL_HEADERS);
            result = headers;
        }};

        //tested method
        httpHeaderInInterceptor.handleMessage(message);

        Assert.assertTrue(headers != null && headers.size() == 2);
        Assert.assertNotNull(headers.get("connection"));

    }
}