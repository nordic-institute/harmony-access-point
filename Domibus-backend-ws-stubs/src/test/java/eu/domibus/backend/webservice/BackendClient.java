package eu.domibus.backend.webservice;

import eu.domibus.backend.webservice.generated.BackendInterface;
import eu.domibus.backend.webservice.generated.SendSuccessFault;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import javax.xml.ws.BindingType;
import javax.xml.ws.soap.SOAPBinding;
import java.util.List;


@javax.jws.WebService(
        serviceName = "BackendService",
        portName = "BACKEND_PORT",
        targetNamespace = "http://org.ecodex.backend/",
        endpointInterface = "eu.domibus.backend.webservice.generated.BackendInterface")
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class BackendClient implements BackendInterface {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendClient.class);

    @Override
    public List<String> sendSuccess(String messageID) throws SendSuccessFault {
        LOG.info("SendSuccess received for id: [{}]", messageID);
        return null;
    }
}
