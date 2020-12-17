package eu.domibus.core.clustering;

import eu.domibus.api.cluster.CommandExecutorService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.messaging.MessageConstants;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @author Ion Perpegel
 * @since 4.2
 */
@RunWith(JMockit.class)
public class ControllerListenerServiceTest {

    @Tested
    ControllerListenerService controllerListenerService;

    @Injectable
    protected CommandExecutorService commandExecutorService;

    @Injectable
    protected DomainService domainService;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Test
    public void testHandleMessageDomainWhenNoDomainWasProvided(@Mocked Message message) {
        boolean handled = controllerListenerService.handleMessageDomain(message);
        Assert.assertTrue(handled);
        new Verifications() {{
            domainContextProvider.clearCurrentDomain();
        }};
    }

    @Test
    public void testHandleMessageDomainWhenADomainWasProvided(@Mocked Message message) throws JMSException {
        String domainCode = "domain1";
        Domain domain = new Domain(domainCode, domainCode);
        new Expectations() {{
            message.getStringProperty(MessageConstants.DOMAIN);
            result = domainCode;
            domainService.getDomain(domainCode);
            result = domain;
        }};
        boolean handled = controllerListenerService.handleMessageDomain(message);
        Assert.assertTrue(handled);
        new Verifications() {{
            domainContextProvider.setCurrentDomain(domainCode);
        }};
    }

    @Test
    public void testHandleMessageDomainWhenInvalidDomainWasProvided(@Mocked Message message) throws JMSException {
        String domainCode = "domain1";
        new Expectations() {{
            message.getStringProperty(MessageConstants.DOMAIN);
            result = domainCode;
            domainService.getDomain(domainCode);
            result = null;
        }};
        boolean handled = controllerListenerService.handleMessageDomain(message);
        Assert.assertFalse(handled);
        new Verifications() {{
            domainContextProvider.setCurrentDomain(domainCode); times=0;
        }};
    }

}