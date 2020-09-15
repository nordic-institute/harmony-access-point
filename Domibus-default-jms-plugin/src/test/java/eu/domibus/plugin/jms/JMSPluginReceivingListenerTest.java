package eu.domibus.plugin.jms;

import eu.domibus.ext.services.AuthenticationExtService;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.logging.DomibusLogger;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * @author Cosmin Baciu
 * @since 4.1
 */
@RunWith(JMockit.class)
public class JMSPluginReceivingListenerTest {

    @Injectable
    protected JMSPluginImpl backendJMS;

    @Injectable
    protected DomibusPropertyExtService domibusPropertyExtService;

    @Injectable
    protected DomainContextExtService domainContextExtService;

    @Injectable
    protected AuthenticationExtService authenticationExtService;

    @Tested
    JMSPluginReceivingListener JMSPluginReceivingListener;

    @Test
    public void receiveMessage(@Injectable MapMessage map, @Mocked DomibusLogger LOG) {
        new Expectations(JMSPluginReceivingListener) {{
            authenticationExtService.isUnsecureLoginAllowed();
            result = false;

            JMSPluginReceivingListener.authenticate(map);
        }};
        JMSPluginReceivingListener.receiveMessage(map);

        new FullVerificationsInOrder() {{
            LOG.debug("Performing authentication");
            LOG.clearCustomKeys();
            JMSPluginReceivingListener.authenticate(map);
            backendJMS.receiveMessage(map);
        }};
    }

    @Test
    public void authenticate(@Injectable MapMessage map, @Mocked DomibusLogger LOG) throws JMSException {
        String username = "cosmin";
        String password = "mypass";
        new Expectations() {{
            map.getStringProperty(JMSMessageConstants.USERNAME);
            result = username;

            map.getStringProperty(JMSMessageConstants.PASSWORD);
            result = password;
        }};

        JMSPluginReceivingListener.authenticate(map);

        new FullVerifications() {{
            authenticationExtService.basicAuthenticate(username, password);
        }};
    }

    @Test(expected = DefaultJmsPluginException.class)
    public void authenticateWithMissingUsername(@Injectable MapMessage map, @Mocked DomibusLogger LOG) throws JMSException {
        new Expectations() {{
            map.getStringProperty(JMSMessageConstants.USERNAME);
            result = null;
        }};

        JMSPluginReceivingListener.authenticate(map);

        new FullVerifications() {{
            authenticationExtService.basicAuthenticate(anyString, anyString);
            times = 0;
        }};
    }

    @Test(expected = DefaultJmsPluginException.class)
    public void authenticateWithMissingPassword(@Injectable MapMessage map, @Mocked DomibusLogger LOG) throws JMSException {
        String username = "cosmin";
        String password = null;
        new Expectations() {{
            map.getStringProperty(JMSMessageConstants.USERNAME);
            result = username;

            map.getStringProperty(JMSMessageConstants.PASSWORD);
            result = password;
        }};

        JMSPluginReceivingListener.authenticate(map);

        new FullVerifications() {{
            authenticationExtService.basicAuthenticate(anyString, anyString);
            times = 0;
        }};
    }
}