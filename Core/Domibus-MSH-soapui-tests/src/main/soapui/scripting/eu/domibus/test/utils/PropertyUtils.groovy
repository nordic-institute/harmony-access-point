package eu.domibus.test.utils

import groovy.json.JsonSlurper
import org.apache.log4j.Logger

class PropertyUtils {
    def static LOG = Logger.getLogger(LogUtils.SOAPUI_LOGGER_NAME)
    //---------------------------------------------------------------------------------------------------------------------------------
    // Return url to specific domibus
    static String urlToDomibus(side, context) {
        LOG.debug("  ====  Calling \"urlToDomibus\".")
        // Return url to specific domibus base on the "side"
        def propName = ""
        switch (side.toLowerCase()) {
            case "c2":
            case "blue":
            case "sender":
            case "c2default":
                propName = "localUrl"
                break
            case "c3":
            case "red":
            case "receiver":
            case "c3default":
                propName = "remoteUrl"
                break
            case "green":
            case "receivergreen":
            case "thirddefault":
                propName = "greenUrl"
                break
            case "testEnv":
                propName = "testEnvUrl"
                break
            default:
                assert (false), "Unknown side. Supported values: sender, receiver, receivergreen and testEnv"
        }
        return context.expand("\${#Project#${propName}}")
    }

    /**
     * Return domibus container name
     * @param side
     * @param log
     * @param context
     * @return
     */
    static String getSTDomibusContainerName(side, context) {
        return getSTProperty(side, "serverContainerName", context)
    }

    static String getSTDatabaseContainerName(side, context) {
        return getSTProperty(side, "databaseContainerName", context)
    }

    static String getSTProperty(String side, String propertyName, context) {
        LOG.debug("  ====  Calling \"domibusContainerName\".")
        def allDomainsProperties = context.expand("\${#Project#allDomainsProperties}")
        def jsonSlurper = new JsonSlurper()
        def result = jsonSlurper.parseText(allDomainsProperties)
        def property = result[side.toUpperCase() + "Default"][propertyName];
        LOG.info "  ====  Got \"property\" " + propertyName + ": " + property
        return property;
    }
}
