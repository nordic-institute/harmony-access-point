package eu.domibus.test.utils

import groovy.json.JsonSlurper

/**
 * This is utility class for parsing and validation the
 * SOAP ui  properties
 *
 */
class SoapUIPropertyUtils {
    static def final  JMS_MANDATORY_PROPERTIES = [
            "site",
            "jmsClientType",
            "jmsUrl",
            "jmsServerUsername",
            "jmsServerPassword",
            "jmsQueue"]

    /**
     * Method parses the JSON type string and property object. Method also validates if json contains all required jms data
     * for the domain
     * @param allJMSProperties - json string for jms data
     * @param log  - logger object
     * @return "double" hashmap of properties values[domain][property]
     */
    static def parseJMSDomainProperties(allJMSProperties, log) {
        LogUtils.debugLog("  parseJMSDomainProperties  [][]  All domain custom properties before parsing $allJMSProperties.", log)
        return parseProperties(allJMSProperties, JMS_MANDATORY_PROPERTIES, log)
    }

    /**
     * Method parses the JSON type string and property object. Method also validates if json contains all required data
     *  for the domain. The required parameters are given in the list argument mandatoryProperties.
     * @param allProperties - json string of parameters
     * @param mandatoryProperties - required list of parameters for the domain
     * @param log  - logger object
     * @return "double" hashmap of properties values[domain][property]
     */
    static def parseProperties(allProperties, mandatoryProperties, log) {
        LogUtils.debugLog("  parseProperties  [][]  All domain custom properties before parsing $allProperties.", log)

        def jsonSlurper = new JsonSlurper()
        def domPropMap = jsonSlurper.parseText(allProperties)
        assert domPropMap != null
        // it's possible that the response wasn't in proper JSON format and is deserialize as empty
        assert !domPropMap.isEmpty()

        LogUtils.debugLog("  parseProperties  [][]  Mandatory properties are: ${mandatoryProperties}.", log)

        domPropMap.each { domain, properties ->
            LogUtils.debugLog("  parseProperties  [][]  Check mandatory properties are not null for domain ID: ${domain}", log)
            mandatoryProperties.each { propertyName ->
                assert (properties[propertyName] != null), "Error: parseProperties: \"${propertyName}\" property couldn't be retrieved for domain ID \"$domain\"."
            }
        }
        LogUtils.debugLog("  parseProperties  [][]  DONE.", log)
        return domPropMap
    }

    /**
     * Method returns parameter value for the domain and property name.
     * First it check if there is a global SoapUI property for the propertyDomain.propertyName. If it does not exists
     * then it returns value from given "double hashmap" (defPropertyValues[domain][property] )
     *
     * @param context - the soapui context object
     * @param defPropertyValues - default double hashmap values (defPropertyValues[domain][property] )
     * @param propertyDomain - domain properties
     * @param propertyName - property name
     * @param log - logger
     */
    static def getDomainProperty(context,defPropertyValues, propertyDomain, propertyName, log) {
        log.debug "Get project property: " +propertyDomain+"."+propertyName
        String value  = context.expand('${#Project#' +propertyDomain+"."+propertyName + '}')
        if (value == null || value.trim().isEmpty()) {
            log.debug "Project property: " +propertyDomain+"."+propertyName + " is empty! get it from initial json property definition"
            value = defPropertyValues[propertyDomain][propertyName]
            log.debug "Got value "+ value + " for property: " +propertyDomain+"."+propertyName + "."
        }
        return value
    }
}