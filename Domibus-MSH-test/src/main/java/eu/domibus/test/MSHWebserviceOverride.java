package eu.domibus.test;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.core.ebms3.mapper.Ebms3Converter;
import eu.domibus.core.ebms3.receiver.MSHWebservice;
import eu.domibus.core.util.MessageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.xml.soap.SOAPMessage;
import javax.xml.ws.WebServiceException;

@Primary
@Service
public class MSHWebserviceOverride extends MSHWebservice {

    protected Ebms3Messaging ebms3Messaging;

    @Autowired
    MessageUtil messageUtil;

    @Autowired
    Ebms3Converter ebms3Converter;

    @Override
    protected Ebms3Messaging getMessaging() {
        return ebms3Messaging;
    }

    @Override
    public SOAPMessage invoke(SOAPMessage request) {
        try {
            ebms3Messaging = messageUtil.getMessaging(request);
        } catch (Exception e) {
            throw new WebServiceException("Error getting Messaging", e);
        }
        return super.invoke(request);
    }
}
