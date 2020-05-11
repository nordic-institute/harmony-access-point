package eu.domibus.jms.weblogic;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Hashtable;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ForeignServerTest {

    private static MBeanServerConnection connection;
    private static JMXConnector connector;
    private static final ObjectName service;

    // Initializing the object name for DomainRuntimeServiceMBean
    // so it can be used throughout the class.
    static {
        try {
            service = new ObjectName(
                    "com.bea:Name=DomainRuntimeService,Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean");
        } catch (MalformedObjectNameException e) {
            throw new AssertionError(e.getMessage());
        }
    }

    /*
     * Initialize connection to the Domain Runtime MBean Server
     */
    public static void initConnection(String hostname, String portString,
                                      String username, String password) throws IOException,
            MalformedURLException {
        String protocol = "t3";
        Integer portInteger = Integer.valueOf(portString);
        int port = portInteger.intValue();
        String jndiroot = "/jndi/";
        String mserver = "weblogic.management.mbeanservers.domainruntime";
        JMXServiceURL serviceURL = new JMXServiceURL(protocol, hostname,
                port, jndiroot + mserver);
        Hashtable h = new Hashtable();
        h.put(Context.SECURITY_PRINCIPAL, username);
        h.put(Context.SECURITY_CREDENTIALS, password);
        h.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES,
                "weblogic.management.remote");
        connector = JMXConnectorFactory.connect(serviceURL, h);
        connection = connector.getMBeanServerConnection();
    }

    /*
     * Print an array of ServerRuntimeMBeans.
     * This MBean is the root of the runtime MBean hierarchy, and
     * each server in the domain hosts its own instance.
     */
    public static ObjectName[] getServerRuntimes() throws Exception {
        return (ObjectName[]) connection.getAttribute(service,
                "ServerRuntimes");
    }


    public static void main(String[] args) throws Exception {
        String hostname = "localhost";
        String portString = "7001";
        String username = "jmsManager";
        String password = "jms_Manager1";

        ForeignServerTest s = new ForeignServerTest();
        initConnection(hostname, portString, username, password);
        s.printNameAndState();
        connector.close();

//        connector = getJMXConnector();
//        connection = connector.getMBeanServerConnection();
//        ForeignServerTest s = new ForeignServerTest();
//        s.printNameAndState();
    }

    public static MBeanServer getDomainRuntimeMBeanServer() throws NamingException {
        String username = "jmsManager";
        String password = "jms_Manager1";

        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.PROVIDER_URL, "t3://localhost:7011");
        env.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");
        env.put(Context.SECURITY_PRINCIPAL, username);
        env.put(Context.SECURITY_CREDENTIALS, password);
        InitialContext ic = new InitialContext(env);


        //return (MBeanServer) ic.lookup("java:comp/env/jmx/domainRuntime");
        return (MBeanServer) ic.lookup("jndi/weblogic.management.mbeanservers.domainruntime");
    }

    public static ObjectName getDomainRuntimeService() throws MalformedObjectNameException {
        return new ObjectName("com.bea:Name=DomainRuntimeService,Type=weblogic.management.mbeanservers.domainruntime.DomainRuntimeServiceMBean");
    }


    public static JMXConnector getJMXConnector() throws Exception {
        String adminUrl = System.getProperty("weblogic.management.server");
        if (adminUrl == null) {
            // We must be on admin server...build remote url by introspecting admin server mbean
            adminUrl = "t3://";
            String adminHost = null;
            Integer adminPort = null;

            ObjectName drs = getDomainRuntimeService();
            MBeanServer mbs = getDomainRuntimeMBeanServer();
            ObjectName[] servers = (ObjectName[]) mbs.getAttribute(drs, "ServerRuntimes");
            for (ObjectName server : servers) {
                adminHost = (String) mbs.getAttribute(server, "AdminServerHost");
                adminPort = (Integer) mbs.getAttribute(server, "AdminServerListenPort");
                if (adminHost != null && adminPort != null) {
                    break;
                }
            }
            adminUrl = adminUrl + adminHost + ":" + adminPort;
        }
        // Build JMX service url
        String protocol = adminUrl.substring(0, adminUrl.indexOf(":"));
        if (!"t3".equals(protocol)) {
            protocol = "t3"; // Enforce t3 to prevent connectivity issues
        }
        String host = adminUrl.substring(adminUrl.indexOf(":") + 3, adminUrl.lastIndexOf(":"));
        String port = adminUrl.substring(adminUrl.lastIndexOf(":") + 1);
        JMXServiceURL serviceURL = new JMXServiceURL(protocol, host, Integer.parseInt(port), "/jndi/weblogic.management.mbeanservers.domainruntime");
        // Build security context to connect with
        String username = "jmsManager";
        String password = "jms_Manager1";
        Hashtable<String, String> ctx = new Hashtable<String, String>();
        ctx.put(Context.SECURITY_PRINCIPAL, username);
        ctx.put(Context.SECURITY_CREDENTIALS, password);
        ctx.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "weblogic.management.remote");
        JMXConnector connector = JMXConnectorFactory.connect(serviceURL, ctx);
        return connector;
    }

    public static MBeanServerConnection getDomainRuntimeMBeanServerConnection(JMXConnector connector) throws IOException {
        return connector.getMBeanServerConnection();
    }

    /*
     * Iterate through ServerRuntimeMBeans and get the name and state
     */
    public void printNameAndState() throws Exception {
        ObjectName[] serverRT = getServerRuntimes();
        System.out.println("got server runtimes");
        int length = (int) serverRT.length;
        for (int i = 0; i < length; i++) {
            String name = (String) connection.getAttribute(serverRT[i],
                    "Name");
            String state = (String) connection.getAttribute(serverRT[i],
                    "State");
            System.out.println("Server name: " + name + ".   Server state: "
                    + state);
        }
    }


}