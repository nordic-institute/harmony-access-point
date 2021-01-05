package eu.domibus.plugin.webService;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.model.Messaging;
import eu.domibus.core.ebms3.mapper.Ebms3Converter;
import eu.domibus.core.util.MessageUtil;
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

    @Autowired
    Ebms3Converter ebms3Converter;

    @Override
    protected Messaging getMessaging() {
        return messaging;
    }

    @Override
    public SOAPMessage invoke(SOAPMessage request) {
        try {
            Ebms3Messaging ebms3Messaging = messageUtil.getMessaging(request);
            messaging = ebms3Converter.convertFromEbms3(ebms3Messaging);
        } catch (Exception e) {
            throw new WebServiceException("Error getting Messaging", e);
        }
        return super.invoke(request);
    }
}
