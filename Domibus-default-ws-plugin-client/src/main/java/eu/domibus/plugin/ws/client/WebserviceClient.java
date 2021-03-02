package eu.domibus.plugin.ws.client;

import eu.domibus.plugin.ws.generated.WebServicePlugin;
import eu.domibus.plugin.ws.generated.WebServicePluginInterface;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;


public class WebserviceClient {

    public static final String DEFAULT_WEBSERVICE_LOCATION = "http://localhost:8080/domibus/services/wsplugin?wsdl";

    private static final Log LOG = LogFactory.getLog(WebserviceClient.class);

    private final String wsdl;

    private final boolean logMessages;

    public WebserviceClient() {
        this(DEFAULT_WEBSERVICE_LOCATION, false);
    }

    public WebserviceClient(String webserviceLocation)  {
        this(webserviceLocation, false);
    }

    public WebserviceClient(String webserviceLocation, boolean logMessages)  {
        this.wsdl = webserviceLocation;
        this.logMessages = logMessages;
    }

    public WebServicePluginInterface getPort() throws MalformedURLException {
        return getPort(null, null);
    }

    public WebServicePluginInterface getPort(String username, String password) throws MalformedURLException {
        if (wsdl == null || wsdl.isEmpty()) {
            throw new IllegalArgumentException("No webservice location specified");
        }

        WebServicePlugin backendService = new WebServicePlugin(new URL(wsdl),  new QName("http://eu.domibus.wsplugin/", "WebServicePlugin"));
        WebServicePluginInterface backendPort = backendService.getWEBSERVICEPLUGINPORT();

        //enable chunking
        BindingProvider bindingProvider = (BindingProvider) backendPort;
        bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, wsdl);

        if(username != null && !username.isEmpty()) {
            LOG.debug("Adding username [" + username + "] to the requestContext");
            bindingProvider.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, username);
        }
        if(password != null && !password.isEmpty()) {
            LOG.debug("Adding password to the requestContext");
            bindingProvider.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
        }

        Map<String, Object> ctxt = bindingProvider.getRequestContext();
        ctxt.put("com.sun.xml.ws.transport.http.client.streaming.chunk.size", 8192);
        //enable MTOM
        SOAPBinding binding = (SOAPBinding)bindingProvider.getBinding();
        binding.setMTOMEnabled(true);

        if(logMessages) {
            List<Handler> handlers = bindingProvider.getBinding().getHandlerChain();
            handlers.add(new MessageLoggingHandler());
            bindingProvider.getBinding().setHandlerChain(handlers);
        }

        return backendPort;
    }

}