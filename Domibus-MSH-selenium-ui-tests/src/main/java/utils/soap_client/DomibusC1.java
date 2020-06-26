package utils.soap_client;


import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.MessageInfo;
import eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704.Messaging;
import eu.domibus.plugin.webService.generated.BackendInterface;
import eu.domibus.plugin.webService.generated.BackendService11;
import eu.domibus.plugin.webService.generated.SubmitRequest;
import eu.domibus.plugin.webService.generated.SubmitResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import utils.TestRunData;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class DomibusC1 {
	private static final Log LOG = LogFactory.getLog(DomibusC1.class);
	
	private static final String TEST_SUBMIT_MESSAGE_SUBMITREQUEST = "src/main/resources/eu/domibus/example/ws/submitMessage_submitRequest.xml";
	private static final String TEST_SUBMIT_MESSAGE_MESSAGING = "src/main/resources/eu/domibus/example/ws/submitMessage_messaging.xml";
	
	private static final String DEFAULT_WEBSERVICE_LOCATION = new TestRunData().getUiBaseUrl() + "services/backend?wsdl";
	
	private static JAXBContext jaxbMessagingContext;
	private static JAXBContext jaxbWebserviceContext;
	
	
	private String wsdl;
	
	
	public DomibusC1() {
		this(DEFAULT_WEBSERVICE_LOCATION);
		
		try {
			jaxbMessagingContext = JAXBContext.newInstance("eu.domibus.common.model.org.oasis_open.docs.ebxml_msg.ebms.v3_0.ns.core._200704");
			jaxbWebserviceContext = JAXBContext.newInstance("eu.domibus.plugin.webService.generated");
		} catch (JAXBException e) {
			throw new RuntimeException("Initialization of Helper class failed.");
		}
		
		
	}
	
	public DomibusC1(String webserviceLocation) {
		this.wsdl = webserviceLocation;
	}
	
	private static <E> E parseSendRequestXML(final String uriSendRequestXML, Class<E> requestType) throws Exception {
		return (E) jaxbWebserviceContext.createUnmarshaller().unmarshal(new File(uriSendRequestXML));
	}
	
	private static Messaging parseMessagingXML(String uriMessagingXML) throws Exception {
		return ((JAXBElement<Messaging>) jaxbMessagingContext.createUnmarshaller().unmarshal(new File(uriMessagingXML))).getValue();
	}
	
	public BackendInterface getPort(String username, String password) throws MalformedURLException {
		if (wsdl == null || wsdl.isEmpty()) {
			throw new IllegalArgumentException("No webservice location specified");
		}
		
		BackendService11 backendService = new BackendService11(new URL(wsdl), new QName("http://org.ecodex.backend/1_1/", "BackendService_1_1"));
		BackendInterface backendPort = backendService.getBACKENDPORT();
		
		//enable chunking
		BindingProvider bindingProvider = (BindingProvider) backendPort;
		if (username != null && !username.isEmpty()) {
			
			bindingProvider.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, username);
		}
		if (password != null && !password.isEmpty()) {
			bindingProvider.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
		}
		
		Map<String, Object> ctxt = bindingProvider.getRequestContext();
		ctxt.put("com.sun.xml.internal.ws.transport.http.client.streaming.chunk.size", 8192);
		
		SOAPBinding binding = (SOAPBinding) bindingProvider.getBinding();
		binding.setMTOMEnabled(true);
		
		
		//comment the following lines if sending large files
		List<Handler> handlers = bindingProvider.getBinding().getHandlerChain();
		
		bindingProvider.getBinding().setHandlerChain(handlers);
		
		return backendPort;
	}
	
	public String sendMessage(String pluginU, String password, String messageRefID, String conversationID) throws Exception {
		BackendInterface backendInterface = getPort(pluginU, password);
		
		
		SubmitRequest submitRequest = parseSendRequestXML(TEST_SUBMIT_MESSAGE_SUBMITREQUEST, SubmitRequest.class);
		Messaging messaging = parseMessagingXML(TEST_SUBMIT_MESSAGE_MESSAGING);
		
		if (null != messageRefID) {
			MessageInfo info = new MessageInfo();
			info.setRefToMessageId(messageRefID);
			messaging.getUserMessage().setMessageInfo(info);
		}
		
		if (null != conversationID) {
			messaging.getUserMessage().getCollaborationInfo().setConversationId(conversationID);
		}
		
		SubmitResponse result = backendInterface.submitMessage(submitRequest, messaging);
		
		if (null != result.getMessageID()) {
			return result.getMessageID().get(0);
		}
		System.out.println(result);
		throw new Exception("Could not send message");
	}
	
	
}
