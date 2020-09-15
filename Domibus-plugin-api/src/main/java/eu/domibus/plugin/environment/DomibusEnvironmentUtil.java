package eu.domibus.plugin.environment;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.core.env.Environment;

/**
 * Utility class, used in the {@link Condition) classes, to determine the environment in which Domibus is running
 *
 * @author Cosmin Baciu
 * @since 4.2
 */
public class DomibusEnvironmentUtil {

    public static DomibusEnvironmentUtil INSTANCE = new DomibusEnvironmentUtil();

    private DomibusEnvironmentUtil() {
    }

    /**
     * Return true in case the current server is matching the provided one
     *
     * @param environment environment see {@link Environment}
     * @param serverName  the provided server value
     * @return true in case the current server is matching the provided one
     */
    public boolean isServer(Environment environment, String serverName) {
        String currentServer = environment.getProperty(DomibusEnvironmentConstants.DOMIBUS_ENVIRONMENT_SERVER_NAME);
        return StringUtils.equalsIgnoreCase(serverName, currentServer);
    }

    /**
     * Returns true if the current server is WebLogic of WildFly
     *
     * @param environment environment see {@link Environment}
     * @return true if the current server is WebLogic of WildFly
     */
    public boolean isApplicationServer(Environment environment) {
        return isWebLogic(environment) || isWildFly(environment);
    }

    /**
     * Returns true if the current server is WebLogic
     *
     * @param environment environment see {@link Environment}
     * @return true if the current server is WebLogic
     */
    public boolean isWebLogic(Environment environment) {
        return isServer(environment, DomibusEnvironmentConstants.DOMIBUS_ENVIRONMENT_SERVER_WEBLOGIC);
    }

    /**
     * Returns true if the current server is WildFly
     *
     * @param environment environment see {@link Environment}
     * @return true if the current server is WildFly
     */
    public boolean isWildFly(Environment environment) {
        return isServer(environment, DomibusEnvironmentConstants.DOMIBUS_ENVIRONMENT_SERVER_WILDFLY);
    }

    /**
     * Returns true if the current server is Tomcat
     *
     * @param environment environment see {@link Environment}
     * @return true if the current server is Tomcat
     */
    public boolean isTomcat(Environment environment) {
        return isServer(environment, DomibusEnvironmentConstants.DOMIBUS_ENVIRONMENT_SERVER_TOMCAT);
    }
}
