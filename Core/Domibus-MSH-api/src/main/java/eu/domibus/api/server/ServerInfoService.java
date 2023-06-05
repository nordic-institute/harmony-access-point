package eu.domibus.api.server;

/**
 * Helper methods for each type of server (Tomcat, Weblogic, Wildfly)
 *
 * @author Catalin Enache
 * @since 4.0.1
 */
public interface ServerInfoService {

    /**
     * Returns the server name
     *
     * To be used in a cluster environment or non clustered environment
     *
     *
     * @return server name
     */
    String getServerName();

}
