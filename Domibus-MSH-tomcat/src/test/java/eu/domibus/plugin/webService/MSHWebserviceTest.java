package eu.domibus.plugin.webService;

import eu.domibus.core.util.MessageUtil;
import eu.domibus.ebms3.common.model.Messaging;
import eu.domibus.core.ebms3.receiver.MSHWebservice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;

@Primary
@Service
public class MSHWebserviceTest extends MSHWebservice {

    protected Messaging messaging;

    @Autowired
    MessageUtil messageUtil;

    @Override
    protected Messaging getMessaging() {
        return messaging;
    }

    @Override
    public SOAPMessage invoke(SOAPMessage request) {
        try {
            messaging = messageUtil.getMessaging(request);
        } catch (Exception e) {
            throw new WebServiceException("Error getting Messaging");
        }
        return super.invoke(request);
    }
}
