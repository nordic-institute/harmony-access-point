package eu.domibus.test.utils

import eu.domibus.test.utils.LogUtils;
import groovy.json.JsonSlurper;

/**
 * This is utility class for parsing and validation the
 * SOAP ui  properties
 *
 */
class PropertyParser {
    static def final  JMS_MANDATORY_PROPERTIES = [
            "site",
            "jmsClientType",
            "jmsUrl",
            "jmsServerUsername",
            "jmsServerPassword",
            "jmsQueue"]

    /**
     * Method parses the JSON type string and property object. Method also validats if json cotains all required data
     *
     * @param allDomainsPropertiesString
     * @return
     */
    static def parseJMSDomainProperties(allJMSProperties, log) {

        LogUtils.debugLog("  ====  Calling \"returnDBproperties\".", log)
        LogUtils.debugLog("  parseDomainProperties  [][]  Parse properties for connection.", log)
        LogUtils.debugLog("  parseDomainProperties  [][]  All domain custom properties before parsing $allJMSProperties.", log)
        def mandatoryProperties = ["site", "domainName", "domNo", "dbType", "dbDriver", "dbJdbcUrl", "dbUser", "dbPassword"]

        def jsonSlurper = new JsonSlurper()
        def domPropMap = jsonSlurper.parseText(allJMSProperties)
        assert domPropMap != null
        // it's possible that the response wasn't in proper JSON format and is deserialize as empty
        assert !domPropMap.isEmpty()

        LogUtils.debugLog("  parseDomainProperties  [][]  Mandatory logs are: ${JMS_MANDATORY_PROPERTIES}.", log)

        domPropMap.each { domain, properties ->
            LogUtils.debugLog("  parseDomainProperties  [][]  Check mandatory properties are not null for domain ID: ${domain}", log)
            JMS_MANDATORY_PROPERTIES.each { propertyName ->
                assert (properties[propertyName] != null), "Error:returnDBproperties: \"${propertyName}\" property couldn't be retrieved for domain ID \"$domain\"."
            }
        }
        LogUtils.debugLog("  parseDomainProperties  [][]  DONE.", log)
        return domPropMap
    }

}