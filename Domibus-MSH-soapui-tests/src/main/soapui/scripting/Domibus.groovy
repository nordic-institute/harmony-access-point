/**
 * Created by testTeam on 16/09/2016.
 */


import com.eviware.soapui.impl.wsdl.teststeps.WsdlTestRequestStep
import groovy.io.FileType
import groovy.sql.Sql

import javax.swing.JOptionPane
import java.sql.SQLException

import static javax.swing.JOptionPane.showConfirmDialog
import com.eviware.soapui.support.GroovyUtils
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import org.apache.commons.io.FileUtils


class Domibus{
    def messageExchange = null
    def context = null
    def log = null

    def allDomainsProperties = null

    // sleepDelay value is increased to 10 s because of execution in Docker containers
    def sleepDelay = 10_000

    def dbConnections = [:]
    def blueDomainID = null //"C2Default"checkLogFile
    def redDomainID = null //"C3Default"
    def greenDomainID = null //"thirdDefault"
    def thirdGateway = "false"
    def multitenancyModeC2 = 0
    def multitenancyModeC3 = 0

    static def defaultPluginAdminC2Default = "pluginAdminC2Default"
    static def defaultAdminDefaultPassword = "adminDefaultPassword"
    static def FS_DEF_MAP = [FS_DEF_SENDER:"domibus-blue",FS_DEF_P_TYPE:"urn:oasis:names:tc:ebcore:partyid-type:unregistered",FS_DEF_S_ROLE:"http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator",FS_DEF_RECEIVER:"domibus-red",FS_DEF_R_ROLE:"http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder",FS_DEF_AGR_TYPE:"DUM",FS_DEF_AGR:"DummyAgr",FS_DEF_SRV_TYPE:"tc20",FS_DEF_SRV:"bdx:noprocess",FS_DEF_ACTION:"TC20Leg1",FS_DEF_CID:"cid:message",FS_DEF_PAY_NAME:"PayloadName.xml",FS_DEF_MIME:"text/xml",FS_DEF_OR_SENDER:"urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1",FS_DEF_FIN_RECEIVER:"urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4"]



    static def backup_file_suffix = "_backup_for_soapui_tests"
    static def DEFAULT_LOG_LEVEL = 1
    static def SUPER_USER = "super"
    static def SUPER_USER_PWD = "123456"
    static def DEFAULT_ADMIN_USER = "admin"
    static def DEFAULT_ADMIN_USER_PWD = "123456"
    static def TRUSTSTORE_PASSWORD = "test123"
    static def XSFRTOKEN_C2 = null
    static def XSFRTOKEN_C3 = null
    static def XSFRTOKEN_C_Other = null
    static def CLEAR_CACHE_COMMAND_TOMCAT = $/rmdir /S /Q ..\work & rmdir /S /Q ..\logs & del /S /Q ..\temp\* & FOR /D %p IN ("..\temp\*.*") DO rmdir /s /q "%p"  & rmdir /S /Q ..\webapps\domibus & rmdir /S /Q ..\conf\domibus\work/$

    // Short constructor of the Domibus Class
    Domibus(log, messageExchange, context) {
        this.log = log
        this.messageExchange = messageExchange
        this.context = context
        this.allDomainsProperties = parseDomainProperties(context.expand('${#Project#allDomainsProperties}'))
        this.thirdGateway = context.expand('${#Project#thirdGateway}')
        this.blueDomainID = context.expand('${#Project#defaultBlueDomainID}')
        this.redDomainID = context.expand('${#Project#defaultRedDomainId}')
        this.greenDomainID = context.expand('${#Project#defaultGreenDomainID}')

/* Still not added as previous values was used in static context
            this.SUPER_USER = context.expand('${#Project#superAdminUsername}')
        this.SUPER_USER_PWD = context.expand('${#Project#superAdminPassword}')
        this.DEFAULT_ADMIN_USER = context.expand('${#Project#defaultAdminUsername}')
        this.DEFAULT_ADMIN_USER_PWD = context.expand('${#Project#defaultAdminPassword}')
*/
        this.multitenancyModeC2 = getMultitenancyMode(context.expand('${#Project#multitenancyModeC2}'), log)
        this.multitenancyModeC3 = getMultitenancyMode(context.expand('${#Project#multitenancyModeC3}'), log)
    }

    // Class destructor
    void finalize() {
        closeAllDbConnections()
        log.debug "Domibus class not needed longer."
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Log information wrapper
    static void  debugLog(logMsg, log,  logLevel = DEFAULT_LOG_LEVEL) {
        if (logLevel.toString() == "1" || logLevel.toString() == "true") log.info(logMsg)
    }

//---------------------------------------------------------------------------------------------------------------------------------
// Parse domain properties
    def parseDomainProperties(allDomainsPropertiesString) {
        debugLog("  ====  Calling \"returnDBproperties\".", log)
        debugLog("  parseDomainProperties  [][]  Parse properties for connection.", log)
        debugLog("  parseDomainProperties  [][]  All domain custom properties before parsing $allDomainsPropertiesString.", log)
        def mandatoryProperties = ["site", "domainName", "domNo", "dbType", "dbDriver", "dbJdbcUrl", "dbUser", "dbPassword"]

        def jsonSlurper = new JsonSlurper()
        def domPropMap = jsonSlurper.parseText(allDomainsPropertiesString)
        assert domPropMap != null
        // it's possible that the response wasn't in proper JSON format and is deserialized as empty
        assert !domPropMap.isEmpty()

        debugLog("  parseDomainProperties  [][]  Mandatory logs are: ${mandatoryProperties}.", log)

        domPropMap.each { domain, properties ->
            debugLog("  parseDomainProperties  [][]  Check mandatory properties are not null for domain ID: ${domain}", log)
            mandatoryProperties.each { propertyName ->
                assert(properties[propertyName] != null),"Error:returnDBproperties: \"${propertyName}\" property couldn't be retrieved for domain ID \"$domain\"." }
        }
        debugLog("  parseDomainProperties  [][]  DONE.", log)
        return domPropMap
    }
//---------------------------------------------------------------------------------------------------------------------------------
// Update Number of Domains for each site base on
// -------------------------------------------------------------------------------------------------------------------------------
    def updateNumberOfDomain() {
        def numOfDomain = 0
        ["C2", "C3", "Third"].each {site ->
            numOfDomain = findNumberOfDomain(site)
            log.info "For ${site} number of defined additional domain is: ${numOfDomain}"
            context.testCase.testSuite.project.setPropertyValue("multitenancyMode${site}", numOfDomain as String)
        }
    }


    def findNumberOfDomain(String inputSite) {
        def count = 0
        debugLog( "  findNumberOfDomain  [][]  for site ID: ${inputSite}", log)
        allDomainsProperties.each { domain, properties ->
            if ((properties["site"].toLowerCase() == inputSite.toLowerCase()) && (properties["domNo"] != 0))
                count++
        }
        return count
    }


//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  DB Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Connect to a schema
    def connectTo(String database, String driver, String url, String dbUser, String dbPassword) {
        debugLog("  ====  Calling \"connectTo\".", log)
        debugLog("  connectTo  [][]  Open connection to || DB: " + database + " || Url: " + url + " || Driver: " + driver + " ||", log)
        def sql

        try {
            switch (database.toLowerCase()) {
                case  "mysql":
                    GroovyUtils.registerJdbcDriver("com.mysql.cj.jdbc.Driver")
                    sql = Sql.newInstance(url, dbUser, dbPassword, driver)
                    break
                case "oracle":
                    GroovyUtils.registerJdbcDriver("oracle.jdbc.driver.OracleDriver")
                    sql = Sql.newInstance(url, dbUser, dbPassword, driver)
                    break
                default:
                    log.warn "Unknown type of DB"
                    sql = Sql.newInstance(url, driver)
            }
            debugLog("  connectTo  [][]  Connection opened with success", log)
            return sql
        } catch (SQLException ex) {
            log.error "  connectTo  [][]  Connection failed"
            assert 0,"SQLException occurred: " + ex
        }
    }



//---------------------------------------------------------------------------------------------------------------------------------
    // Open all DB connections
    def openAllDbConnections() {
        debugLog("  ====  Calling \"openAllDbConnections\".", log)
        openDbConnections(allDomainsProperties.keySet())
    }

//---------------------------------------------------------------------------------------------------------------------------------
    // Open DB connections for provided list of domain defined by domain IDs
    def openDbConnections(domainIdList) {
        debugLog("  ====  Calling \"openDbConnections\" ${domainIdList}.", log)

        domainIdList.each { domainName ->
            def domain = retrieveDomainId(domainName)
            if (!dbConnections.containsKey(domain)) {
                debugLog("  openConnection  [][]  Open DB connection for domain ID: ${domain}", log)
                this.dbConnections[domain] = connectTo(allDomainsProperties[domain].dbType,
                        allDomainsProperties[domain].dbDriver,
                        allDomainsProperties[domain].dbJdbcUrl,
                        allDomainsProperties[domain].dbUser,
                        allDomainsProperties[domain].dbPassword)
            } else debugLog("  openConnection  [][]  DB connection for domain ID: ${domain} already open.", log)
        }
    }

//---------------------------------------------------------------------------------------------------------------------------------
    // Close all DB connections opened previously
    def closeAllDbConnections() {
        debugLog("  ====  Calling \"closeAllDbConnections\".", log)
        closeDbConnections(allDomainsProperties.keySet())
    }

//---------------------------------------------------------------------------------------------------------------------------------
    // Close all DB connections opened previously
    def closeDbConnections(domainIdList) {
        debugLog("  ====  Calling \"closeConnection\".", log)

        for (domainName in domainIdList) {
            def domID = retrieveDomainId(domainName)
            if (dbConnections.containsKey(domID)) {
                debugLog("  closeConnection  [][]  Close DB connection for domain ID: ${domID}", log)
                dbConnections[domID].connection.close()
                dbConnections.remove(domID)
            } else debugLog("  closeConnection  [][]  DB connection for domain ID: ${domID} was NOT open.", log)
        }
    }

//---------------------------------------------------------------------------------------------------------------------------------
    def executeListOfQueriesOnAllDB(String[] sqlQueriesList) {
        debugLog("  ====  Calling \"executeListOfQueriesOnAllDB\".", log)
        dbConnections.each { domainId, connection ->
            executeListOfSqlQueries(sqlQueriesList, domainId)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // to be removed - invoked in SoapUI
    def executeListOfQueriesOnBlue(String[] sqlQueriesList) {
        debugLog("  ====  Calling \"executeListOfQueriesOnBlue\".", log)
        log.info "  executeListOfQueriesOnBlue  [][]  Executing SQL queries on sender/Blue"
        executeListOfSqlQueries(sqlQueriesList, blueDomainID)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // to be removed  - invoked in SoapUI
    def executeListOfQueriesOnRed(String[] sqlQueriesList) {
        debugLog("  ====  Calling \"executeListOfQueriesOnRed\".", log)
        log.info "  executeListOfQueriesOnRed  [][]  Executing SQL queries on receiver/Red"
        executeListOfSqlQueries(sqlQueriesList, redDomainID)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // to be removed  - NOT invoked in SoapUI
    def executeListOfQueriesOnGreen(String[] sqlQueriesList) {
        debugLog("  ====  Calling \"executeListOfQueriesOnGreen\".", log)
        log.info "  executeListOfQueriesOnGreen  [][]  Executing SQL queries on Third/Green"
        executeListOfSqlQueries(sqlQueriesList, greenDomainID)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    def executeListOfQueriesOnMany(String[] sqlQueriesList, executeOnDomainIDList) {
        debugLog("  ====  Calling \"executeListOfQueriesOnMany\".", log)
        executeOnDomainIDList.each { domainID ->
            executeListOfSqlQueries(sqlQueriesList, domainID)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    def executeListOfSqlQueries(String[] sqlQueriesList, String inputTargetDomainID) {
        debugLog("  ====  Calling \"executeListOfSqlQueries\".", log)
        def connectionOpenedInsideMethod = false
        def targetDomainID = retrieveDomainId(inputTargetDomainID)

        if (!dbConnections.containsKey(targetDomainID)) {
            debugLog("  executeListOfSqlQueries  [][]  Method executed without DB connections open - try to open connection", log)
            openDbConnections([targetDomainID])
            connectionOpenedInsideMethod = true
        }

        for (query in sqlQueriesList) {
            debugLog("  executeListOfSqlQueries  [][]  Executing SQL query: " + query + " on domibus: " + targetDomainID, log)
            try {
                dbConnections[targetDomainID].execute query
            } catch (SQLException ex) {
                closeAllDbConnections()
                assert 0,"SQLException occurred: " + ex
            }
        }

        // Maybe this part is not needed as connection would be always close in class destructor
        if (connectionOpenedInsideMethod) {
            debugLog("  executeListOfSqlQueries  [][]  Connection to DB opened during method execution - close opened connection", log)
            closeDbConnections([targetDomainID])
        }
    }

//---------------------------------------------------------------------------------------------------------------------------------
    // Retrieve domain ID reference from provided name. When name exists use it
    def retrieveDomainId(String inputName) {
        debugLog("  ====  Calling \"retrieveDomainId\". With inputName: \"${inputName}\"", log)
        def domID = null

        // For Backward compatibility
        if (allDomainsProperties.containsKey(inputName)) {
            domID = inputName
        } else {
            switch (inputName.toUpperCase()) {
                case "C3":
                case "RED":
                case "RECEIVER":
                    domID = this.redDomainID
                    break
                case "C2":
                case "BLUE":
                case "SENDER":
                    domID = this.blueDomainID
                    break
                case "GREEN":
                    assert(thirdGateway.toLowerCase().trim() == "true"), "\"GREEN\" schema is not active. Please set soapui project custom property \"thirdGateway\" to \"true\"."
                    domID = this.greenDomainID
                    break
                default:
                    assert false, "Not supported domain ID ${inputName} provide for retrieveDomainId method. Not able to found it in allDomainsProperties nor common names list. "
                    break
            }
        }
        debugLog("   retrieveDomainId  [][]  Input value ${inputName} translated to following domain ID: ${domID}", log)

        return domID as String
    }

//---------------------------------------------------------------------------------------------------------------------------------
    // Clean all the messages from all defined for domains databases
    def cleanDatabaseAll() {
        debugLog("  ====  Calling \"cleanDatabaseAll\".", log)
        openAllDbConnections()
        cleanDatabaseForDomains(allDomainsProperties.keySet())
        closeAllDbConnections()
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Clean certificate from table
    def cleanCertificateEntries(String domainName, String certAlias) {
        debugLog("  ====  Calling \"cleanCertificateEntries\".", log)

        debugLog("   cleanCertificateEntries  [][]  Target domain: " + domainName, log)
        debugLog("   cleanCertificateEntries  [][]  Target certificate alias: " + certAlias, log)

        def sqlQueriesList = [
                "delete from TB_CERTIFICATE where LOWER(CERTIFICATE_ALIAS) = LOWER('${certAlias}')"
        ] as String[]

        openAllDbConnections()
        def domain = retrieveDomainId(domainName)
        debugLog("  cleanCertificateEntries  [][]  Clean certificate ${certAlias} for domain ID: ${domain}", log)
        executeListOfSqlQueries(sqlQueriesList, domain)
        closeAllDbConnections()

    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Clean all the messages from the DB for provided list of domain defined by domain IDs
    def cleanDatabaseForDomains(domainIdList) {
        debugLog("  ====  Calling \"cleanDatabaseForDomains\" ${domainIdList}.", log)
        def sqlQueriesList = [
                "delete from TB_RAWENVELOPE_LOG",
                "delete from TB_RECEIPT_DATA",
                "delete from TB_PROPERTY",
                "delete from TB_PART_INFO",
                "delete from TB_PARTY_ID",
                "delete from TB_MESSAGING",
                "delete from TB_ERROR",
                "delete from TB_USER_MESSAGE",
                "delete from TB_SIGNAL_MESSAGE",
                "delete from TB_RECEIPT",
                "delete from TB_MESSAGE_INFO",
                "delete from TB_ERROR_LOG",
                "delete from TB_SEND_ATTEMPT",
                "delete from TB_MESSAGE_ACKNW_PROP",
                "delete from TB_MESSAGE_ACKNW",
                "delete from TB_MESSAGING_LOCK",
                "delete from TB_MESSAGE_LOG",
                "delete from TB_MESSAGE_UI"
        ] as String[]



        domainIdList.each { domainName ->
            def domain = retrieveDomainId(domainName)
            debugLog("  cleanDatabaseForDomains  [][]  Clean DB for domain ID: ${domain}", log)
            executeListOfSqlQueries(sqlQueriesList, domain)
        }

        log.info "  cleanDatabaseAll  [][]  Cleaning Done"
    }



//---------------------------------------------------------------------------------------------------------------------------------
    // Clean single message identified by messageID starting with provided value from ALL defined DBs
    def cleanDBMessageIDStartsWith(String messageID) {
        debugLog("  ====  Calling \"cleanDBMessageIDStartsWith\".", log)
        cleanDBMessageID(messageID, true)
    }

//---------------------------------------------------------------------------------------------------------------------------------
    // Clean single message identified by messageID starting with provided value from provided list of domains
    def cleanDBMessageIDStartsWithForDomains(String messageID, domainIdList) {
        debugLog("  ====  Calling \"cleanDBMessageIDStartsWith\".", log)
        cleanDBMessageIDForDomains(messageID, domainIdList, true)
    }

//---------------------------------------------------------------------------------------------------------------------------------
    // Clean single message identified by ID
    def cleanDBMessageID(String messageID, boolean  messageIDStartWithProvidedValue = false) {
        debugLog("  ====  Calling \"cleanDBMessageID\".", log)
        log.info "  cleanDBMessageID  [][]  Clean from DB information related to the message with ID: " + messageID
        openAllDbConnections()
        cleanDBMessageIDForDomains(messageID, allDomainsProperties.keySet(), messageIDStartWithProvidedValue)
        closeAllDbConnections()
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Clean single message identified by ID
    def cleanDBMessageIDForDomains(String messageID, domainIdList, boolean  messgaeIDStartWithProvidedValue = false) {
        debugLog("  ====  Calling \"cleanDBMessageIDForDomains\".", log)
        log.info "  cleanDBMessageIDForDomains  [][]  Clean from DB information related to the message with ID: " + messageID

        def messageIDCheck = "= '${messageID}'" //default comparison method use equal operator
        if (messgaeIDStartWithProvidedValue) messageIDCheck = "like '${messageID}%'" //if cleanDBMessageIDStartsWith method was called change method for comparison

        def select_ID_PK = "select ID_PK from TB_MESSAGE_INFO where MESSAGE_ID ${messageIDCheck}" //extracted as common part of queries below
        def sqlQueriesList = [
                "delete from TB_RAWENVELOPE_LOG where USERMESSAGE_ID_FK IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + ")) or SIGNALMESSAGE_ID_FK IN (select ID_PK from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + " OR REF_TO_MESSAGE_ID " + messageIDCheck + ")) or MESSAGE_ID ${messageIDCheck}",
                "delete from TB_RECEIPT_DATA where RECEIPT_ID IN (select ID_PK from TB_RECEIPT where ID_PK IN(select receipt_ID_PK from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + ") or messageInfo_ID_PK IN (select ID_PK from TB_MESSAGE_INFO where REF_TO_MESSAGE_ID ${messageIDCheck})))",
                "delete from TB_PROPERTY where MESSAGEPROPERTIES_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + ")) or PARTPROPERTIES_ID IN (select ID_PK from TB_PART_INFO where PAYLOADINFO_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + ")))",
                "delete from TB_PART_INFO where PAYLOADINFO_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + "))",
                "delete from TB_PARTY_ID where FROM_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + ")) or TO_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + "))",
                "delete from TB_MESSAGING where (SIGNAL_MESSAGE_ID IN (select ID_PK from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + "))) OR (USER_MESSAGE_ID IN (select ID_PK from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + ")))",
                "delete from TB_ERROR where SIGNALMESSAGE_ID IN (select ID_PK from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + "))",
                "delete from TB_USER_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + ")",
                "delete from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + " OR REF_TO_MESSAGE_ID " + messageIDCheck + ")",
                "delete from TB_RECEIPT where ID_PK IN(select receipt_ID_PK from TB_SIGNAL_MESSAGE where messageInfo_ID_PK IN (" + select_ID_PK + "))",
                "delete from TB_MESSAGE_INFO where MESSAGE_ID " + messageIDCheck + " OR REF_TO_MESSAGE_ID " + messageIDCheck + "",
                "delete from TB_SEND_ATTEMPT where MESSAGE_ID " + messageIDCheck + "",
                "delete from TB_MESSAGE_ACKNW_PROP where FK_MSG_ACKNOWLEDGE IN (select ID_PK from TB_MESSAGE_ACKNW where MESSAGE_ID " + messageIDCheck + ")",
                "delete from TB_MESSAGE_ACKNW where MESSAGE_ID " + messageIDCheck + "",
                "delete from TB_MESSAGING_LOCK where MESSAGE_ID " + messageIDCheck + "",
                "delete from TB_MESSAGE_LOG where MESSAGE_ID " + messageIDCheck + "",
                "delete from WS_PLUGIN_TB_MESSAGE_LOG where MESSAGE_ID " + messageIDCheck + "",
                "delete from TB_MESSAGE_UI where MESSAGE_ID " + messageIDCheck + ""
        ] as String[]

        domainIdList.each { domainName ->
            def domain = retrieveDomainId(domainName)
            debugLog("  cleanDBMessageIDForDomains  [][]  Clean DB for domain ID: ${domain}", log)
            executeListOfSqlQueries(sqlQueriesList, domain)
        }

    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Messages Info Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Extract messageID from the request if it exists
    String findGivenMessageID() {
        debugLog("  ====  Calling \"findGivenMessageID\".", log)
        def messageID = null
        def requestContent = messageExchange.getRequestContentAsXml()
        def requestFile = new XmlSlurper().parseText(requestContent)
        requestFile.depthFirst().each {
            if (it.name() == "MessageId") {
                messageID = it.text().toLowerCase().trim()
            }
        }
        return (messageID)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Extract messageID from the response
    String findReturnedMessageID() {
        debugLog("  ====  Calling \"findReturnedMessageID\".", log)
        def messageID = null
        def responseContent = messageExchange.getResponseContentAsXml()
        def responseFile = new XmlSlurper().parseText(responseContent)
        responseFile.depthFirst().each {
            if (it.name() == "messageID") {
                messageID = it.text()
            }
        }
        assert(messageID != null),locateTest(context) + "Error:findReturnedMessageID: The message ID is not found in the response"
        if ( (findGivenMessageID() != null) && (findGivenMessageID().trim() != "") ) {
            //if(findGivenMessageID()!= null){
            assert(messageID.toLowerCase() == findGivenMessageID().toLowerCase()),locateTest(context) + "Error:findReturnedMessageID: The message ID returned is (" + messageID + "), the message ID provided is (" + findGivenMessageID() + ")."
        }
        return (messageID.toLowerCase().trim())
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Verification of message existence
    // TODO: change this to wait in loop
    def verifyMessagePresence(int presence1, int presence2, String IDMes = null, String senderDomainId = blueDomainID, String receiverDomainId =  redDomainID) {
        debugLog("  ====  Calling \"verifyMessagePresence\".", log)
        def messageID
        def sqlSender
        sleep(sleepDelay)

        if (IDMes != null) {
            messageID = IDMes
        } else {
            messageID = findReturnedMessageID()
        }
        def total = 0
        debugLog("  verifyMessagePresence  [][]  senderDomainId = " + senderDomainId + " receiverDomaindId = " + receiverDomainId, log)
        sqlSender = retrieveSqlConnectionRefFromDomainId(senderDomainId)
        def sqlReceiver = retrieveSqlConnectionRefFromDomainId(receiverDomainId)
        def usedDomains = [senderDomainId, receiverDomainId]
        openDbConnections(usedDomains)

        // Sender DB
        sqlSender.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where REPLACE(LOWER(MESSAGE_ID),' ','') = REPLACE(LOWER(${messageID}),' ','')") {
            total = it.lignes
        }
        if (presence1 == 1) {
            //log.info "total = " + total
            assert(total > 0),locateTest(context) + "Error:verifyMessagePresence: Message with ID " + messageID + " is not found in sender side."
        }
        if (presence1 == 0) {
            assert(total == 0),locateTest(context) + "Error:verifyMessagePresence: Message with ID " + messageID + " is found in sender side."
        }

        // Receiver DB
        total = 0
        sleep(sleepDelay)
        sqlReceiver.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where REPLACE(LOWER(MESSAGE_ID),' ','') = REPLACE(LOWER(${messageID}),' ','')") {
            total = it.lignes
        }
        if (presence2 == 1) {
            assert(total > 0),locateTest(context) + "Error:verifyMessagePresence: Message with ID " + messageID + " is not found in receiver side."
        }
        if (presence2 == 0) {
            assert(total == 0),locateTest(context) + "Error:verifyMessagePresence: Message with ID " + messageID + " is found in receiver side."
        }

        closeDbConnections(usedDomains)
    }

    def verifyMessagePresenceWithoutWaiting(int presence, String IDMes = null, String domainId = blueDomainID) {
        debugLog("  ====  Calling \"verifyMessagePresenceWithoutWaiting\".", log)
        def messageID

        if (IDMes != null) {
            messageID = IDMes
        } else {
            messageID = findReturnedMessageID()
        }
        def total = 0
        debugLog("  verifyMessagePresenceWithoutWaiting  [][]  domainId = " + domainId, log)
        def sql = retrieveSqlConnectionRefFromDomainId(domainId)

        // Sender DB
        sql.eachRow("Select count(*) number_of_lines from TB_MESSAGE_LOG where REPLACE(LOWER(MESSAGE_ID),' ','') = REPLACE(LOWER(${messageID}),' ','')") {
            total = it.number_of_lines
        }
        if (presence == 1) {
            assert(total > 0),locateTest(context) + "Error: verifyMessagePresenceWithoutWaiting: Message with ID ${messageID} was not found in ${domainId} domain."
        }
        if (presence == 0) {
            assert(total == 0),locateTest(context) + "Error: verifyMessagePresenceWithoutWaiting: Message with ID ${messageID} was found in ${domainId} domain."
        }
        closeDbConnections([domainId])
        log.info("  ==== returning from \"verifyMessagePresenceWithoutWaiting\" method. Confirmed that message ${messageID} ${presence==0?'not ':''}exists in domain ${domainId} .")
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Verification of message unicity
    def verifyMessageUnicity(String IDMes = null, String senderDomainId = blueDomainID, String receiverDomainId =  redDomainID) {
        debugLog("  ====  Calling \"verifyMessageUnicity\".", log)
        sleep(sleepDelay)
        def messageID
        def total = 0

        if (IDMes != null) {
            messageID = IDMes
        } else {
            messageID = findReturnedMessageID()
        }
        debugLog("  verifyMessageUnicity  [][]  senderDomainId = " + senderDomainId + " receiverDomainId = " + receiverDomainId, log)
        def sqlSender = retrieveSqlConnectionRefFromDomainId(senderDomainId)
        def sqlReceiver = retrieveSqlConnectionRefFromDomainId(receiverDomainId)
        def usedDomains = [senderDomainId, receiverDomainId]
        openDbConnections(usedDomains)

        sqlSender.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where REPLACE(LOWER(MESSAGE_ID),' ','') = REPLACE(LOWER(${messageID}),' ','')") {
            total = it.lignes
        }
        assert(total == 1),locateTest(context) + "Error:verifyMessageUnicity: Message found " + total + " times in sender side."
        sleep(sleepDelay)
        sqlReceiver.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where REPLACE(LOWER(MESSAGE_ID),' ','') = REPLACE(LOWER(${messageID}),' ','')") {
            total = it.lignes
        }
        assert(total == 1),locateTest(context) + "Error:verifyMessageUnicity: Message found " + total + " times in receiver side."
        closeDbConnections(usedDomains)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Wait until status or timer expire
    def waitForStatus(String SMSH = null, String RMSH = null, String IDMes = null, String bonusTimeForSender = null, String bonusTimeForReceiver = null, String senderDomainId = blueDomainID, String receiverDomainId =  redDomainID) {
        debugLog("  ====  Calling \"waitForStatus\".", log)
        def MAX_WAIT_TIME = 100_000 // Maximum time to wait to check the message status.
        def RECEIVER_MAX_WAIT_TIME = 60_000
        def RECEIVER_MAX_WAIT_TIME_EXTENDED = 120_000
        def STEP_WAIT_TIME = 2_000 // Time to wait before re-checking the message status.
        def messageID
        def numberAttempts = 0
        def maxNumberAttempts = 5
        def messageStatus = "INIT"
        def wait = false

        if (IDMes != null) {
            messageID = IDMes
        } else {
            messageID = findReturnedMessageID()
        }

        log.info "  waitForStatus  [][]  params: messageID: " + messageID + " SMSH: " + SMSH + " RMSH: " + RMSH + " IDMes: " + IDMes + " bonusTimeForSender: " + bonusTimeForSender + " bonusTimeForReceiver: " + bonusTimeForReceiver

        if (bonusTimeForSender) {
            if (bonusTimeForSender.isInteger()) MAX_WAIT_TIME = (bonusTimeForSender as Integer) * 1000
            else MAX_WAIT_TIME = 500_000

            log.info "  waitForStatus  [][]  Waiting time for Sender extended to ${MAX_WAIT_TIME/1000} seconds"
        }

        debugLog("  waitForStatus  [][]  senderDomainId = " + senderDomainId + " receiverDomaindId = " + receiverDomainId, log)
        def sqlSender = retrieveSqlConnectionRefFromDomainId(senderDomainId)
        def sqlReceiver = retrieveSqlConnectionRefFromDomainId(receiverDomainId)
        def usedDomains = [senderDomainId, receiverDomainId]
        openDbConnections(usedDomains)

        if (SMSH) {
            while ( ( (messageStatus != SMSH) && (MAX_WAIT_TIME > 0) ) || (wait) ) {
                sleep(STEP_WAIT_TIME)
                if (MAX_WAIT_TIME > 0) {
                    MAX_WAIT_TIME = MAX_WAIT_TIME - STEP_WAIT_TIME
                }
                log.info "  waitForStatus  [][]  WAIT: " + MAX_WAIT_TIME
                sqlSender.eachRow("Select * from TB_MESSAGE_LOG where REPLACE(LOWER(MESSAGE_ID),' ','') = REPLACE(LOWER(${messageID}),' ','')") {
                    messageStatus = it.MESSAGE_STATUS
                    numberAttempts = it.SEND_ATTEMPTS
                }
                log.info "|MSG_ID: " + messageID + " | SENDER: Expected MSG Status =" + SMSH + "-- Current MSG Status = " + messageStatus + " | maxNumbAttempts: " + maxNumberAttempts + "-- numbAttempts: " + numberAttempts
                if (SMSH == "SEND_FAILURE") {
                    if (messageStatus == "WAITING_FOR_RETRY") {
                        if ( ( (maxNumberAttempts - numberAttempts) > 0) && (!wait) ) {
                            wait = true
                        }
                        if ( (maxNumberAttempts - numberAttempts) <= 0) {
                            wait = false
                        }
                    } else {
                        if (messageStatus == SMSH) {
                            wait = false
                        }
                    }
                }
            }
            log.info "  waitForStatus  [][]  finished checking sender, messageStatus: " + messageStatus + " MAX_WAIT_TIME: " + MAX_WAIT_TIME

            assert(messageStatus != "INIT"),locateTest(context) + "Error:waitForStatus: Message " + messageID + " is not present in the sender side."
            assert(messageStatus.toLowerCase() == SMSH.toLowerCase()),locateTest(context) + "Error:waitForStatus: Message in the sender side has status " + messageStatus + " instead of " + SMSH + "."
        }
        if (bonusTimeForReceiver) {
            if (bonusTimeForReceiver.isInteger()) MAX_WAIT_TIME = (bonusTimeForReceiver as Integer) * 1000
            else MAX_WAIT_TIME = RECEIVER_MAX_WAIT_TIME_EXTENDED

            log.info "  waitForStatus  [][]  Waiting time for Receiver extended to ${MAX_WAIT_TIME/1000} seconds"

        } else {
            MAX_WAIT_TIME = RECEIVER_MAX_WAIT_TIME
        }
        messageStatus = "INIT"
        if (RMSH) {
            while ( (messageStatus != RMSH) && (MAX_WAIT_TIME > 0) ) {
                sleep(STEP_WAIT_TIME)
                MAX_WAIT_TIME = MAX_WAIT_TIME - STEP_WAIT_TIME
                sqlReceiver.eachRow("Select * from TB_MESSAGE_LOG where REPLACE(LOWER(MESSAGE_ID),' ','') = REPLACE(LOWER(${messageID}),' ','')") {
                    messageStatus = it.MESSAGE_STATUS
                }
                log.info "  waitForStatus  [][]  W:" + MAX_WAIT_TIME + " M:" + messageStatus
            }
            log.info "  waitForStatus  [][]  finished checking receiver, messageStatus: " + messageStatus + " MAX_WAIT_TIME: " + MAX_WAIT_TIME
            assert(messageStatus != "INIT"),locateTest(context) + "Error:waitForStatus: Message " + messageID + " is not present in the receiver side."
            assert(messageStatus.toLowerCase() == RMSH.toLowerCase()),locateTest(context) + "Error:waitForStatus: Message in the receiver side has status " + messageStatus + " instead of " + RMSH + "."
        }
        closeDbConnections(usedDomains)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Check that an entry is created in the table TB_SEND_ATTEMPT
    def checkSendAttempt(String messageID, String targetSchema = "BLUE"){
        debugLog("  ====  Calling \"checkSendAttempt\".", log)
        def MAX_WAIT_TIME = 50_000
        def STEP_WAIT_TIME = 2000
        int total = 0
        openAllDbConnections()

        def sqlSender = retrieveSqlConnectionRefFromDomainId(targetSchema)

        while ( (MAX_WAIT_TIME > 0) && (total == 0) ) {
            sqlSender.eachRow("Select count(*) lignes from tb_send_attempt where REPLACE(LOWER(MESSAGE_ID),' ','') = REPLACE(LOWER(${messageID}),' ','')") {
                total = it.lignes
            }
            log.info "  checkSendAttempt  [][]  W: " + MAX_WAIT_TIME
            sleep(STEP_WAIT_TIME)
            MAX_WAIT_TIME = MAX_WAIT_TIME - STEP_WAIT_TIME
        }
        assert(total > 0),locateTest(context) + "Error: Message " + messageID + " is not present in the table tb_send_attempt."
        closeAllDbConnections()
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def getStatusRetriveStatus(log, context, messageExchange) {
        debugLog("  ====  Calling \"getStatusRetriveStatus\".", log)
        def outStatus = null
        def responseContent = messageExchange.getResponseContentAsXml()
        def requestFile = new XmlSlurper().parseText(responseContent)
        requestFile.depthFirst().each {
            if (it.name() == "getMessageStatusResponse") {
                outStatus = it.text()
            }
        }
        assert(outStatus != null),locateTest(context) + "Error:getStatusRetriveStatus: Not able to return status from response message"
        return outStatus
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Compare payloads order
    static def checkPayloadOrder(submitRequest, log, context, messageExchange){
        debugLog("  ====  Calling \"checkPayloadOrder\".", log)
        def requestAtts = []
        def responseAtts = []
        def i = 0
        //def requestContent = messageExchange.getRequestContentAsXml()
        def requestContent = submitRequest
        def responseContent = messageExchange.getResponseContentAsXml()
        assert(requestContent != null),locateTest(context) + "Error: request is empty."
        assert(responseContent != null),locateTest(context) + "Error: response is empty."
        def parserFile = new XmlSlurper().parseText(requestContent)
        debugLog("===========================================", log)
        debugLog("  checkPayloadOrder  [][]  Attachments in request: ", log)
        parserFile.depthFirst().each {
            if (it.name() == "PartInfo") {
                requestAtts[i] = it.@href.text()
                debugLog("  checkPayloadOrder  [][]  Attachment: " + requestAtts[i] + " in position " + (i + 1) + ".", log)
                i++
            }
        }
        debugLog("===========================================", log)
        debugLog("  checkPayloadOrder  [][]  Attachments in response: ", log)
        i = 0
        parserFile = new XmlSlurper().parseText(responseContent)
        parserFile.depthFirst().each {
            if (it.name() == "PartInfo") {
                responseAtts[i] = it.@href.text()
                debugLog("  checkPayloadOrder  [][]  Attachment: " + responseAtts[i] + " in position " + (i + 1) + ".", log)
                i++
            }
        }
        debugLog("===========================================", log)
        assert(requestAtts.size() == responseAtts.size()),locateTest(context) + "Error: request has " + requestAtts.size() + " attachments whereas response has " + responseAtts.size() + " attachments."
        for (i = 0; i < requestAtts.size(); i++) {
            assert(requestAtts[i] == responseAtts[i]),locateTest(context) + "Error: in position " + (i + 1) + " request has attachment " + requestAtts[i] + " whereas response has attachment " + responseAtts[i] + "."
        }
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  PopUP Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    static def showPopUpForManualCheck(messagePrefix, log, testRunner) {
        debugLog("  ====  Calling \"showPopUpForManualCheck\".", log)
        def message = messagePrefix + """

		After the manual check of the expected result:
		- Click 'Yes' when result is correct.
		- Click 'No' when result is incorrect. 
		- Click 'Cancel' to skip this check."""

        def result = showConfirmDialog(null, message)
        if (result == JOptionPane.YES_OPTION)
        {
            log.info "PASS MANUAL TEST STEP: Result as expected, continuing the test."
        } else if (result == JOptionPane.NO_OPTION)
        {
            log.info "FAIL MANUAL TEST STEP: Manual check unsuccessful."
            testRunner.fail("Manual check indicated as unsuccessful by user.")
        } else if (result == JOptionPane.CANCEL_OPTION)
        {
            log.info "SKIP MANUAL TEST STEP: Check skipped bu user."
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def showPopUpForManualConfigurationChange(messagePrefix, log, testRunner) {
        debugLog("  ====  Calling \"showPopUpForManualConfigurationChange\".", log)
        def message = messagePrefix + """

		Did configuration was changed?
		- Click 'Yes' when configuration was changed.
		- Click 'No' when configuration was not changed, this test step would be marked as failed.
		- Click 'Cancel' to skip this configuration change, the test would be continue from next test step."""

        def result = showConfirmDialog(null, message)
        if (result == JOptionPane.YES_OPTION)
        {
            log.info "User indicated configuration was changed as described in test step, continuing the test."
        } else if (result == JOptionPane.NO_OPTION)
        {
            log.info "User indicated configuration wasn't changed, this test step would be marked as failed."
            testRunner.fail("User indicated configuration wasn't changed, this test step would be marked as failed.")
        } else if (result == JOptionPane.CANCEL_OPTION)
        {
            log.info "This configuration changed was skipped, continue with next test step."
        }
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Domibus Administration Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Ping Gateway
    static String pingMSH(String side, context, log) {
        debugLog("  ====  Calling \"pingMSH\".", log)

        def commandString = "curl -s -o /dev/null -w \"%{http_code}\" --noproxy localhost " + urlToDomibus(side, log, context) + "/"
        def commandResult = runCommandInShell(commandString, log)

        debugLog("  ====  ENDING \"pingMSH\".", log)
        return commandResult[0].trim()
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Clear domibus cache
    static def clearCache(String side, context, log, String server = "tomcat") {
        debugLog("  ====  Calling \"clearCache\".", log)
        log.info "Cleaning cache for domibus " + side + " ..."
        def outputCatcher = new StringBuffer()
        def errorCatcher = new StringBuffer()
        def pathS = context.expand('${#Project#pathExeSender}')
        def pathR = context.expand('${#Project#pathExeReceiver}')
        def pathRG = context.expand('${#Project#pathExeGreen}')
        def commandToRun = null
        switch (server.toLowerCase()) {
            case "tomcat":
                switch (side.toLowerCase()) {
                    case "sender":
                        log.info "PATH = " + pathS
                        commandToRun = "cmd /c cd ${pathS} && " + CLEAR_CACHE_COMMAND_TOMCAT
                        break
                    case "receiver":
                        log.info "PATH = " + pathR
                        commandToRun = "cmd /c cd ${pathR} && " + CLEAR_CACHE_COMMAND_TOMCAT
                        break
                    case "receivergreen":
                        log.info "PATH = " + pathRG
                        commandToRun = "cmd /c cd ${pathRG} && " + CLEAR_CACHE_COMMAND_TOMCAT
                        break
                    default:
                        assert(false), "Unknown side."
                }
                break
            case "weblogic":
                log.info "  clearCache  [][]  I don't know how to clean in weblogic yet."
                break
            case "wildfly":
                log.info "  clearCache  [][]  I don't know how to clean in wildfly yet."
                break
            default:
                assert(false), "Unknown server."
        }
        if (commandToRun) {
            def proc = commandToRun.execute()
            if (proc != null) {
                proc.consumeProcessOutput(outputCatcher, errorCatcher)
                proc.waitForProcessOutput(outputCatcher, errorCatcher)
            }
            debugLog("  clearCache  [][]  commandToRun = " + commandToRun, log)
            debugLog("  clearCache  [][]  outputCatcher = " + outputCatcher, log)
            debugLog("  clearCache  [][]  errorCatcher = " + errorCatcher, log)
            log.info "  clearCache  [][]  Cleaning should be done."
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Start several gateways
    static def startSetMSHs(int dom1, int dom2, int dom3, context, log) {
        debugLog("  ====  Calling \"startSetMSHs\".", log)
        if (dom1 > 0) {
            startMSH("sender", context, log)
        }
        if (dom2 > 0) {
            startMSH("receiver", context, log)
        }
        if (dom3 > 0) {
            startMSH("receivergreen", context, log)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Start Gateway
    static def startMSH(String side, context, log){
        debugLog("  ====  Calling \"startMSH\".", log)
        def MAX_WAIT_TIME = 150_000 // Maximum time to wait for the domibus to start.
        def STEP_WAIT_TIME = 2_000 // Time to wait before re-checking the domibus status.
        def outputCatcher = new StringBuffer()
        def errorCatcher = new StringBuffer()
        def pathS = context.expand('${#Project#pathExeSender}')
        def pathR = context.expand('${#Project#pathExeReceiver}')
        def pathRG = context.expand('${#Project#pathExeGreen}')
        def path = null

        if (pingMSH(side, context, log) == "200") {
            log.info "  startMSH  [][]  " + side.toUpperCase() + " is already running!"
        } else {
            log.info "  startMSH  [][]  Trying to start the " + side.toUpperCase()
            switch (side.toLowerCase()) {
                case "sender":
                case "c2":
                case "blue":
                    path = pathS
                    break

                case "receiver":
                case "c3":
                case "red":
                    path = pathR
                    break

                case "receivergreen":
                case "green":
                    path = pathRG
                    break

                default:
                    assert(false), "Unknown side."
            }
            def proc = "cmd /c cd ${path} && startup.bat".execute()
            if (proc != null) {
                //proc.consumeProcessOutput(outputCatcher, errorCatcher)
                //proc.waitForProcessOutput(outputCatcher, errorCatcher)
                proc.consumeProcessOutput(outputCatcher, errorCatcher)
                proc.waitFor()
            }
            debugLog("  startMSH  [][]  outputCatcher: " + outputCatcher.toString(), log)
            debugLog("  startMSH  [][]  errorCatcher: " + errorCatcher.toString(), log)
            assert((!errorCatcher) && (proc != null)), locateTest(context) + "Error:startMSH: Error while trying to start the MSH."
            while ( pingMSH(side, context, log) != "200" && (MAX_WAIT_TIME > 0) ) {
                debugLog("  startMSH  [][]  WAITING " + MAX_WAIT_TIME, log)
                MAX_WAIT_TIME = MAX_WAIT_TIME - STEP_WAIT_TIME
                sleep(STEP_WAIT_TIME)
            }
            assert pingMSH(side, context, log) == "200",locateTest(context) + "Error:startMSH: Error while trying to start the MSH."
            log.info "  startMSH  [][]  DONE - " + side.toUpperCase() + " started."
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Stop several gateways
    static def stopSetMSHs(int dom1, int dom2, int dom3, context, log) {
        debugLog("  ====  Calling \"stopSetMSHs\".", log)
        if (dom1 > 0) {
            stopMSH("sender", context, log)
        }
        if (dom2 > 0) {
            stopMSH("receiver", context, log)
        }
        if (dom3 > 0) {
            stopMSH("receivergreen", context, log)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Stop Gateway
    static def stopMSH(String side, context, log){
        debugLog("  ====  Calling \"stopMSH\".", log)
        def MAX_WAIT_TIME = 180_000 // Maximum time to wait for the domibus to stop.
        def STEP_WAIT_TIME = 2_000 // Time to wait before re-checking the domibus status.
        def outputCatcher = new StringBuffer()
        def errorCatcher = new StringBuffer()
        def pathS = context.expand('${#Project#pathExeSender}')
        def pathR = context.expand('${#Project#pathExeReceiver}')
        def pathRG = context.expand('${#Project#pathExeGreen}')
        def path = null
        def passedDuration = 0

        if (pingMSH(side, context, log) != "200") {
            log.info "  stopMSH  [][]  " + side.toUpperCase() + " is not running!"
        } else {
            log.info "  stopMSH  [][]  Trying to stop the " + side.toUpperCase()
            switch (side.toLowerCase()) {
                case "sender":
                case "c2":
                case "blue":
                    path = pathS
                    break
                case "receiver":
                case "c3":
                case "red":
                    path = pathR
                    break
                case "receivergreen":
                case "green":
                    path = pathRG
                    break
                default:
                    assert(false), "Unknown side."
            }
            def proc = "cmd /c cd ${path} && shutdown.bat".execute()

            if (proc != null) {
                proc.consumeProcessOutput(outputCatcher, errorCatcher)
                proc.waitForProcessOutput(outputCatcher, errorCatcher)
            }
            assert((!errorCatcher) && (proc != null)),locateTest(context) + "Error:stopMSH: Error while trying to stop the MSH."
            while ( pingMSH(side, context, log) == "200" && (passedDuration < MAX_WAIT_TIME) ) {
                debugLog("  stopMSH  [][]  WAITING " + MAX_WAIT_TIME, log)
                passedDuration = passedDuration + STEP_WAIT_TIME
                sleep(STEP_WAIT_TIME)
            }
            assert pingMSH(side, context, log) != "200",locateTest(context) + "Error:stopMSH: Error while trying to stop the MSH."
            log.info "  stopMSH  [][]  Sleeping for 1 min ..."
            sleep(60000)
            log.info "  stopMSH  [][]  END sleeping for 1 min."
            log.info "  stopMSH  [][]  DONE - " + side.toUpperCase() + " stopped."
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def uploadPmode(String side, String baseFilePath, String extFilePath, context, log, String domainValue = "Default", String outcome = "successfully", String message = null, String authUser = null, authPwd = null){
        debugLog("  ====  Calling \"uploadPmode\".", log)
        log.info "  uploadPmode  [][]  Start upload PMode for Domibus \"" + side + "\"."
        def pmDescription = "SoapUI sample test description for PMode upload."
        def authenticationUser = authUser
        def authenticationPwd = authPwd
        String pmodeFile = computePathRessources(baseFilePath, extFilePath, context, log)

        log.info "  uploadPmode  [][]  PMODE FILE PATH: " + pmodeFile

        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)


            def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/pmode",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H","X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                 "-F", "description=" + pmDescription,
                                 "-F", "file=@" + pmodeFile,
                                 "-v"]
            def commandResult = runCommandInShell(commandString, log)
            assert(commandResult[0].contains(outcome)),"Error:uploadPmode: Error while trying to upload the PMode: response doesn't contain the expected outcome \"" + outcome + "\"."
            if (outcome.toLowerCase() == "successfully") {
                log.info "  uploadPmode  [][]  " + commandResult[0] + " Domibus: \"" + side + "\"."
                if (message != null) {
                    assert(commandResult[0].contains(message)),"Error:uploadPmode: Upload done but expected message \"" + message + "\" was not returned."
                }
            } else {
                log.info "  uploadPmode  [][]  Upload PMode was not done for Domibus: \"" + side + "\"."
                if (message != null) {
                    assert(commandResult[0].contains(message)),"Error:uploadPmode: Upload was not done but expected message \"" + message + "\" was not returned.\n Result:"  + commandResult[0]
                }
            }
        } finally {
            resetAuthTokens(log)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def uploadPmodeWithoutToken(String side, String baseFilePath, String extFilePath, context, log, String outcome = "successfully", String message =null){
        debugLog("  ====  Calling \"uploadPmodeWithoutToken\".", log)
        log.info "  uploadPmodeWithoutToken  [][]  Start upload PMode for Domibus \"" + side + "\"."
        def pmDescription = "Dummy"

//        String output = fetchCookieHeader(side, context, log)
        String pmodeFile = computePathRessources(baseFilePath, extFilePath, context, log)

        def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/pmode",
                             "-F", "description=" + pmDescription,
                             "-F", "file=@" + pmodeFile,
                             "-v"]
        def commandResult = runCommandInShell(commandString, log)
        assert(commandResult[0].contains(outcome)),"Error:uploadPmode: Error while trying to connect to domibus."
        if (outcome.toLowerCase() == "successfully") {
            log.info "  uploadPmodeWithoutToken  [][]  " + commandResult[0] + " Domibus: \"" + side + "\"."
            if (message != null) {
                assert(commandResult[0].contains(message)),"Error:uploadPmode: Upload done but expected message \"" + message + "\" was not returned."
            }
        } else {
            log.info "  uploadPmodeWithoutToken  [][]  Upload PMode was not done for Domibus: \"" + side + "\"."
            if (message != null) {
                assert(commandResult[0].contains(message)),"Error:uploadPmode: Upload was not done but expected message \"" + message + "\" was not returned."
            }
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def uploadTruststore(String side, String baseFilePath, String extFilePath, context, log, String domainValue = "Default", String outcome = "successfully", String tsPassword = TRUSTSTORE_PASSWORD, String authUser = null, authPwd = null){
        debugLog("  ====  Calling \"uploadTruststore\".", log)
        log.info "  uploadTruststore  [][]  Start upload truststore for Domibus \"" + side + "\"."
        def authenticationUser = authUser
        def authenticationPwd = authPwd

        try{
            debugLog("  uploadTruststore  [][]  Fetch multitenancy mode on domibus $side.", log)
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

            String truststoreFile = computePathRessources(baseFilePath,extFilePath,context,log)

            def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/truststore/save",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H","X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                 "-F", "password=" + tsPassword,
                                 "-F", "file=@" + truststoreFile,
                                 "-v"]
            def commandResult = runCommandInShell(commandString, log)

            assert(commandResult[0].contains(outcome)),"Error:uploadTruststore: Error while trying to upload the truststore to domibus. Returned: " + commandResult[0]
            log.info "  uploadTruststore  [][]  " + commandResult[0] + " Domibus: \"" + side + "\"."
        } finally {
            resetAuthTokens(log)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Change Domibus configuration file
    static void changeDomibusProperties(color, propValueDict, log, context, testRunner){
        debugLog("  ====  Calling \"changeDomibusProperties\".", log)
        // Check that properties file exist and if yes create backup_file
        // For all properties name and new value pairs change value in file
        // to restore configuration use method restoreDomibusPropertiesFromBackup(domibusPath,  log, context, testRunner)
        def pathToPropertyFile = pathToDomibus(color, log, context) + context.expand('${#Project#subPathToDomibusProperties}')

        // Check file exists
        def testFile = new File(pathToPropertyFile)
        if (!testFile.exists()) {
            testRunner.fail("File [${pathToPropertyFile}] does not exist. Can't change value.")
            return
        } else log.info "  changeDomibusProperties  [][]  File [${pathToPropertyFile}] exists."

        // Create backup file if already not created
        def backupFileName = "${pathToPropertyFile}${backup_file_suffix}"
        def backupFile = new File(backupFileName)
        if (backupFile.exists()) {
            log.info "  changeDomibusProperties  [][]  File [${backupFileName}] already exists and would not be overwrite - old backup file would be preserved."
        } else  {
            copyFile(pathToPropertyFile, backupFileName, log)
            log.info "  changeDomibusProperties  [][]  Backup copy of config file created: [${backupFile}]"
        }

        def fileContent = testFile.text
        //run in loop for all properties key values pairs
        for(item in propValueDict){
            def propertyToChangeName = item.key
            def newValueToAssign = item.value

            // Check that property exist in config file
            def found = 0
            def foundInCommentedRow = 0
            testFile.eachLine {
                line, n ->
                    n++
                    if(line =~ /^\s*${propertyToChangeName} = /) {
                        log.info "  changeDomibusProperties  [][]  In line $n searched property was found. Line value is: $line"
                        found++
                    }
                    if(line =~ ~/# + \s*${propertyToChangeName} = .*/) {
                        log.info "  changeDomibusProperties  [][]  In line $n commented searched property was found. Line value is: $line"
                        foundInCommentedRow++
                    }
            }

            if (found > 1) {
                testRunner.fail("The search string ($propertyToChangeName = ) was found ${found} times in file [${pathToPropertyFile}]. Expect only one assigment - check if configuration file is not corrupted.")
                return
            }
            // If property is present in file change it value
            if (found)
                fileContent = fileContent.replaceAll(/(?m)^\s*($ { propertyToChangeName }
                                                           = )(.*)/) { all, paramName, value -> "${paramName}${newValueToAssign}" } else
            if (foundInCommentedRow)
                fileContent = fileContent.replaceFirst(/(?m)^# + \s*($ { propertyToChangeName }
                                                               = )(.*)/) { all, paramName, value -> "${paramName}${newValueToAssign}" } else {
                testRunner.fail("The search string ($propertyToChangeName) was not found in file [${pathToPropertyFile}]. No changes would be applied - properties file restored.")
                return
            }
            log.info "  changeDomibusProperties  [][]  In [${pathToPropertyFile}] file property ${propertyToChangeName} was changed to value ${newValueToAssign}"
        } //loop end

        // Store new content of properties file after all changes
        testFile.text = fileContent
        log.info "  changeDomibusProperties  [][]  Property file [${pathToPropertyFile}] amended"
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Restore Domibus configuration file
    static void  restoreDomibusPropertiesFromBackup(color, log, context, testRunner) {
        debugLog("  ====  Calling \"restoreDomibusPropertiesFromBackup\".", log)
        // Restore from backup file domibus.properties file
        def pathToPropertyFile = pathToDomibus(color, log, context) + context.expand('${#Project#subPathToDomibusProperties}')
        def backupFile = "${pathToPropertyFile}${backup_file_suffix}"

        // Check backup file exists
        def backupFileHandler = new File(backupFile)
        if (!backupFileHandler.exists()) {
            testRunner.fail("CRITICAL ERROR: File [${backupFile}] does not exist.")
        } else {
            log.info "  restoreDomibusPropertiesFromBackup  [][]  Restore properties file from existing backup"
            copyFile(backupFile, pathToPropertyFile, log)
            if (backupFileHandler.delete()) {
                log.info "  restoreDomibusPropertiesFromBackup  [][]  Successfully restore configuration from backup file and backup file was removed"
            } else {
                testRunner.fail "Not able to delete configuration backup file"
            }
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def domibusHealthMonitor(String side, context, log, String domainValue = "Default",String pluginUsername = "user",String pluginPassword = "Domibus-123",String userRole = "ROLE_ADMIN",authorizedUser = true,outcomesMap = [], String authUser = null, String authPwd = null){
        debugLog("  ====  Calling \"domibusHealthMonitor\".", log)
        log.info "  domibusHealthMonitor  [][]  Checking the health of Domibus $side."
        def jsonSlurper = new JsonSlurper()
        def i = 0

        // Create plugin user for authentication
        if(pluginUsername.toLowerCase() == "user"){
            pluginUsername = "userPl" + (new Date().format("ddHHmmss"))
            addPluginUser(side, context, log, domainValue, userRole, pluginUsername, pluginPassword,"urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1",true,authUser,authPwd)
        }


        try{
            log.info "  domibusHealthMonitor  [][]  =================================="
            log.info "  domibusHealthMonitor  [][]  Checking the general status ..."

            def commandString = ["curl", urlToDomibus(side, log, context) + "/ext/monitoring/application/status",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H", "Content-Type: application/json",
                                 "-u", pluginUsername + ":" + pluginPassword,
                                 "-v"]
            def commandResult = runCommandInShell(commandString, log)
            if(!authorizedUser){
                assert(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*403.*/),"Error:domibusHealthMonitor: Error in the expected response (http 403). Returned: " + commandResult[1]
                log.info "  domibusHealthMonitor  [][]  User \"$pluginUsername\" is not authorized to check the status."
            }else{
                assert(commandResult[0]!= null),"Error:domibusHealthMonitor: Error while trying to retrieve domibus health status. Returned: null."
                assert commandResult[0].trim() != "","Error:domibusHealthMonitor: Error while trying to retrieve domibus health status. Returned an empty response."
                assert(commandResult[0].contains("Database") && commandResult[0].contains("JMSBroker") && commandResult[0].contains("Quartz Trigger")),"Error:domibusHealthMonitor: Error while trying to retrieve domibus health status. Returned: " + commandResult[0]
                def dataMap = jsonSlurper.parseText(commandResult[0])
                while (i < dataMap.services.size()){
                    assert(dataMap.services[i] != null),"Error:domibusHealthMonitor: Error while parsing the components status."
                    log.info "  domibusHealthMonitor  [][]  " + dataMap.services[i].name.toUpperCase().padRight(30-dataMap.services[i].name.length()) + "          " + dataMap.services[i].status.toUpperCase()
                    if(outcomesMap){
                        outcomesMap.each { entry ->
                            if(entry.key.toUpperCase().equals(dataMap.services[i].name.toUpperCase())){
                                assert(dataMap.services[i].status.toUpperCase().equals(entry.value.toUpperCase())),"Error:domibusHealthMonitor: Error for service \"" + dataMap.services[i].name + "\". Expecting status \"" + entry.value + "\" but received status \"" + dataMap.services[i].status + "\""
                            }
                        }
                    }
                    i++
                }
            }
            log.info "  domibusHealthMonitor  [][]  =================================="
            log.info "\n\n"

            log.info "  domibusHealthMonitor  [][]  =================================="
            log.info "  domibusHealthMonitor  [][]  Checking the Database ..."

            commandString = ["curl", urlToDomibus(side, log, context) + "/ext/monitoring/application/status?filter = db",
                             "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                             "-H", "Content-Type: application/json",
                             "-u", pluginUsername + ":" + pluginPassword,
                             "-v"]
            commandResult = runCommandInShell(commandString, log)
            if(!authorizedUser){
                assert(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*403.*/),"Error:domibusHealthMonitor: Error in the expected response (http 403). Returned: " + commandResult[1]
                log.info "  domibusHealthMonitor  [][]  User \"$pluginUsername\" is not authorized to check the \"Database\" status."
            }else{
                assert(commandResult[0]!= null),"Error:domibusHealthMonitor: Error while trying to retrieve domibus DB status. Returned: null."
                assert commandResult[0].trim() != "","Error:domibusHealthMonitor: Error while trying to retrieve domibus DB status. Returned an empty response."
                assert(commandResult[0].contains("Database")),"Error:domibusHealthMonitor: Error while trying to retrieve domibus DB status. Returned: " + commandResult[0]
                def dataMap = jsonSlurper.parseText(commandResult[0])
                assert(dataMap.services != null),"Error:domibusHealthMonitor: Error while parsing the DB status."
                assert(dataMap.services.size() == 1),"Error:domibusHealthMonitor: Error while parsing the DB status: must return only the DB status. Returned: " + commandResult[0]
                log.info "  domibusHealthMonitor  [][]  STATUS:" + "          " + dataMap.services[0].status.toUpperCase()
                if(outcomesMap){
                    outcomesMap.each { entry ->
                        if(entry.key.toUpperCase().equals(dataMap.services[0].name.toUpperCase())){
                            assert(dataMap.services[0].status.toUpperCase().equals(entry.value.toUpperCase())),"Error:domibusHealthMonitor: Error for service \"" + dataMap.services[0].name + "\". Expecting status \"" + entry.value + "\" but received status \"" + dataMap.services[0].status + "\""
                        }
                    }
                }
            }
            log.info "  domibusHealthMonitor  [][]  =================================="
            log.info "\n\n"

            log.info "  domibusHealthMonitor  [][]  =================================="
            log.info "  domibusHealthMonitor  [][]  Checking the quartz trigger ..."

            commandString = ["curl", urlToDomibus(side, log, context) + "/ext/monitoring/application/status?filter = quartzTrigger",
                             "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                             "-H", "Content-Type: application/json",
                             "-u", pluginUsername + ":" + pluginPassword,
                             "-v"]
            commandResult = runCommandInShell(commandString, log)
            if(!authorizedUser){
                assert(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*403.*/),"Error:domibusHealthMonitor: Error in the expected response (http 403). Returned: " + commandResult[1]
                log.info "  domibusHealthMonitor  [][]  User \"$pluginUsername\" is not authorized to check the \"quartz trigger\" status."
            }else{
                assert(commandResult[0]!= null),"Error:domibusHealthMonitor: Error while trying to retrieve domibus Quartz Trigger status. Returned: null."
                assert commandResult[0].trim() != "","Error:domibusHealthMonitor: Error while trying to retrieve domibus Quartz Trigger status. Returned an empty response."
                assert(commandResult[0].contains("Quartz Trigger")),"Error:domibusHealthMonitor: Error while trying to retrieve domibus Quartz Trigger status. Returned: " + commandResult[0]
                def dataMap = jsonSlurper.parseText(commandResult[0])
                assert(dataMap.services != null),"Error:domibusHealthMonitor: Error while parsing the Quartz Trigger status."
                assert(dataMap.services.size() == 1),"Error:domibusHealthMonitor: Error while parsing the Quartz Trigger status: must return only the Quartz Trigger status. Returned: " + commandResult[0]
                log.info "  domibusHealthMonitor  [][]  STATUS:" + "          " + dataMap.services[0].status.toUpperCase()
                if(outcomesMap){
                    outcomesMap.each { entry ->
                        if(entry.key.toUpperCase().equals(dataMap.services[0].name.toUpperCase())){
                            assert(dataMap.services[0].status.toUpperCase().equals(entry.value.toUpperCase())),"Error:domibusHealthMonitor: Error for service \"" + dataMap.services[0].name + "\". Expecting status \"" + entry.value + "\" but received status \"" + dataMap.services[0].status + "\""
                        }
                    }
                }
            }
            log.info "  domibusHealthMonitor  [][]  =================================="
            log.info "\n\n"
            log.info "  domibusHealthMonitor  [][]  =================================="
            log.info "  domibusHealthMonitor  [][]  Checking the jms broker ..."
            commandString = ["curl", urlToDomibus(side, log, context) + "/ext/monitoring/application/status?filter = jmsBroker",
                             "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                             "-H", "Content-Type: application/json",
                             "-u", pluginUsername + ":" + pluginPassword,
                             "-v"]
            commandResult = runCommandInShell(commandString, log)
            if(!authorizedUser){
                assert(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*403.*/),"Error:domibusHealthMonitor: Error in the expected response (http 403). Returned: " + commandResult[1]
                log.info "  domibusHealthMonitor  [][]  User \"$pluginUsername\" is not authorized to check the \"jms broker\" status."
            }else{
                assert(commandResult[0]!= null),"Error:domibusHealthMonitor: Error while trying to retrieve domibus JMSBroker status. Returned: null."
                assert commandResult[0].trim() != "","Error:domibusHealthMonitor: Error while trying to retrieve domibus JMSBroker status. Returned an empty response."
                assert(commandResult[0].contains("JMSBroker")),"Error:domibusHealthMonitor: Error while trying to retrieve domibus JMSBroker status. Returned: " + commandResult[0]
                def dataMap = jsonSlurper.parseText(commandResult[0])
                assert(dataMap.services != null),"Error:domibusHealthMonitor: Error while parsing the JMSBroker status."
                assert(dataMap.services.size() == 1),"Error:domibusHealthMonitor: Error while parsing the JMSBroker status: must return only the JMSBroker status. Returned: " + commandResult[0]
                log.info "  domibusHealthMonitor  [][]  STATUS:" + "          " + dataMap.services[0].status.toUpperCase()
                if(outcomesMap){
                    outcomesMap.each { entry ->
                        if(entry.key.toUpperCase().equals(dataMap.services[0].name.toUpperCase())){
                            assert(dataMap.services[0].status.toUpperCase().equals(entry.value.toUpperCase())),"Error:domibusHealthMonitor: Error for service \"" + dataMap.services[0].name + "\". Expecting status \"" + entry.value + "\" but received status \"" + dataMap.services[0].status + "\""
                        }
                    }
                }
            }
            log.info "  domibusHealthMonitor  [][]  =================================="
            log.info "\n\n"

        } finally {
            resetAuthTokens(log)
        }

        // Remove created plugin user
        removePluginUser(side, context, log, domainValue, pluginUsername,authUser,authPwd)
        debugLog("  ====  END \"domibusHealthMonitor\".", log)
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Domain Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    static def isFourCornerEnabled(String side, context, log, String userLogin = null, String passwordLogin = null) {
        debugLog("  ====  Calling \"isFourCornerEnabled\".", log)

        log.info "  isFourCornerEnabled  [][]  Get four corner mode Domibus $side."
        def authenticationUser = userLogin
        def authenticationPwd = passwordLogin

        (authenticationUser, authenticationPwd) = retriveAdminCredentials(context, log, side, authenticationUser, authenticationPwd)

        def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/application/fourcornerenabled",
                             "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                             "-H", "Content-Type: application/json",
                             "-H", "X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                             "-v"]
        def commandResult = runCommandInShell(commandString, log)
        assert((commandResult[1] ==~ /(?s).*HTTP\/\d.\d\s*204.*/)||(commandResult[1] ==~ /(?s).*HTTP\/\d.\d\s*200.*/)),"Error:isFourCornerEnabled: Error in the isFourCornerEnabled response."
        debugLog("  ====  END \"isFourCornerEnabled\".", log)
        return commandResult[0].substring(5).trim() == "true"
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def isMultitenancy(String side, context, log, String userLogin = SUPER_USER, String passwordLogin = SUPER_USER_PWD) {
        debugLog("  ====  Calling \"isMultitenancy\".", log)

        log.info "  isMultitenancy  [][]  Checking if Domibus is deployed in multitenancy for Domibus \"$side\"."

        def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/application/multitenancy",
                             "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                             "-H", "Content-Type: application/json",
                             "-H", "X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, userLogin, passwordLogin),
                             "-v"]
        def commandResult = runCommandInShell(commandString, log)
        resetAuthTokens(log)
        assert((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)||(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/)),"Error:isMultitenancy: Error in the isMultitenancy response."
        debugLog("  ====  END \"isMultitenancy\".", log)
        return commandResult[0].substring(5).trim() == "true"
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def getUItitle(String side, context, log,domainValue = "default" ,String userLogin = null, String passwordLogin = null) {
        debugLog("  ====  Calling \"getUItitle\".", log)

        log.info "  getUItitle  [][]  Get current UI title for Domibus $side."
        def authenticationUser = userLogin
        def authenticationPwd = passwordLogin

        (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

        def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/application/name",
                             "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                             "-H", "Content-Type: application/json",
                             "-H", "X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                             "-v"]
        def commandResult = runCommandInShell(commandString, log)
        assert((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)||(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/)),"Error:getUItitle: Error in the getUItitle response."
        debugLog("  ====  END \"getUItitle\".", log)
        return commandResult[0].substring(5).replace("\"", "").trim()
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def getDomain(String side, context, log,infoType = "code" ,String userLogin = SUPER_USER, String passwordLogin = SUPER_USER_PWD) {
        debugLog("  ====  Calling \"getDomain\".", log)

        log.info "  getDomain  [][]  Get current domain for Domibus $side."
        def jsonSlurper = new JsonSlurper()

        // If multitenancy is on no need to continue
        if (!getMultitenancyFromSide(side, context, log)) {
            debugLog("  getDomain  [][]  Singletenancy deployment: Return \"default\" value.", log)
            return "default"
        }

        def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/security/user/domain",
                             "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                             "-H", "Content-Type: application/json",
                             "-H", "X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, userLogin, passwordLogin),
                             "-v"]
        def commandResult = runCommandInShell(commandString, log)
        assert ((commandResult[1] ==~ /(?s).*HTTP\/\d.\d\s*204.*/) || (commandResult[1] ==~ /(?s).*HTTP\/\d.\d\s*200.*/)), "Error:getDomain: Error in the getDomain response."
        debugLog("  ====  END \"getDomain\".", log)
        def responseOutput = commandResult[0].substring(5)
        def dataMap = jsonSlurper.parseText(responseOutput)
        if (infoType == "code") {
            return dataMap.code
        } else {
            return dataMap.name
        }
    }

//---------------------------------------------------------------------------------------------------------------------------------
    static def setDomain(String side, context, log, String domainValue, String userLogin = SUPER_USER, String passwordLogin = SUPER_USER_PWD){
        debugLog("  ====  Calling \"setDomain\".", log)
        debugLog("  setDomain  [][]  Set domain for Domibus $side.", log)
        if (domainValue == getDomain(side, context, log)) {
            debugLog("  setDomain  [][]  Requested domain is equal to the current value: no action needed", log)
        } else {
            debugLog("  setDomain  [][]  Calling curl command to switch to domain \"$domainValue\"", log)
            def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/security/user/domain",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H", "Content-Type: text/plain",
                                 "-H", "X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, userLogin, passwordLogin),
                                 "-X", "PUT","-v",
                                 "--data-binary", "$domainValue"]
            def commandResult = runCommandInShell(commandString, log)
            assert((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)||(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/)),"Error:setDomain: Error while trying to set the domain: verify that domain $domainValue is correctly configured."
            debugLog("  setDomain  [][]  Domain set to $domainValue.",log)
        }
        debugLog("  ====  END \"setDomain\".", log)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Return number of domains
    static int getMultitenancyMode(String inputValue, log) {
        debugLog("  ====  Calling \"getMultitenancyMode\".", log)
        if ( (inputValue == null) || (inputValue == "") ) {
            return 0
        }
        if (inputValue.trim().isInteger()) {
            return (inputValue as Integer)
        } else {
            return 0
        }
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Users Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    static def getAdminConsoleUsers(String side, context, log, String authUser = null, String authPwd = null){
        debugLog("  ====  Calling \"getAdminConsoleUsers\".", log)
        debugLog("  getAdminConsoleUsers  [][]  Get Admin Console users for Domibus \"$side\".", log)
        def authenticationUser = authUser
        def authenticationPwd = authPwd

        (authenticationUser, authenticationPwd) = retriveAdminCredentials(context, log, side, authenticationUser, authenticationPwd)

        def commandString = "curl " + urlToDomibus(side, log, context) + "/rest/user/users -b " + context.expand( '${projectDir}')+ File.separator + "cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: "+ returnXsfrToken(side,context,log,authenticationUser,authenticationPwd) +"\" -X GET "
        def commandResult = runCommandInShell(commandString, log)
        assert((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/) || commandResult[1].contains("successfully")),"Error:getAdminConsoleUsers: Error while trying to connect to domibus."
        return commandResult[0].substring(5)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def addAdminConsoleUser(String side, context, log, String domainValue = "Default", String userRole = "ROLE_ADMIN", String userAC, String passwordAC = "Domibus-123", String authUser = null, String authPwd = null,success = true){
        debugLog("  ====  Calling \"addAdminConsoleUser\".", log)
        def jsonSlurper = new JsonSlurper()
        def authenticationUser = authUser
        def authenticationPwd = authPwd

        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

            debugLog("  addAdminConsoleUser  [][]  Fetch users list and verify that user \"$userAC\" doesn't already exist.",log)
            def usersMap = jsonSlurper.parseText(getAdminConsoleUsers(side, context, log))
            if (userExists(usersMap, userAC, log, false)) {
                log.error "Error:addAdminConsoleUser: Admin Console user \"$userAC\" already exist: usernames must be unique."
            } else {
                debugLog("  addAdminConsoleUser  [][]  Users list before the update: " + usersMap, log)
                debugLog("  addAdminConsoleUser  [][]  Prepare user \"$userAC\" details to be added.", log)
                def curlParams = "[ { \"roles\": \"$userRole\",\"domain\": \"$domainValue\", \"userName\": \"$userAC\", \"password\": \"$passwordAC\", \"status\": \"NEW\", \"active\": true, \"suspended\": false, \"authorities\": [], \"deleted\": false } ]"
                debugLog("  addAdminConsoleUser  [][]  Inserting user \"$userAC\" in list.", log)
                debugLog("  addAdminConsoleUser  [][]  User \"$userAC\" parameters: $curlParams.", log)
                def commandString = ["curl ",urlToDomibus(side, log, context) + "/rest/user/users",
                                     "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                     "-H", "Content-Type: application/json",
                                     "-H", "X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                     "-X", "PUT",
                                     "--data-binary", formatJsonForCurl(curlParams, log),
                                     "-v"]
                def commandResult = runCommandInShell(commandString, log)
                if(success){
                    assert((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/)||(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)),"Error:addAdminConsoleUser: Error while trying to add a user \"$userAC\"."
                    log.info "  addAdminConsoleUser  [][]  Admin Console user \"$userAC\" added."
                }else{
                    assert((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*500.*/)&&(commandResult[0]==~ /(?s).*password of.*user.*not meet the minimum complexity requirements.*/)),"Error:addAdminConsoleUser: user \"$userAC\" was created ."
                    log.info "  addAdminConsoleUser  [][]  Admin Console user \"$userAC\" was not added."
                }
            }
        } finally {
            resetAuthTokens(log)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def removeAdminConsoleUser(String side, context, log, String domainValue = "Default", String userAC, String authUser = null, String authPwd = null){
        debugLog("  ====  Calling \"removeAdminConsoleUser\".", log)
        def jsonSlurper = new JsonSlurper()
        def authenticationUser = authUser
        def authenticationPwd = authPwd
        def roleAC = null
        def userDeleted = false
        int i = 0

        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

            debugLog("  removeAdminConsoleUser  [][]  Fetch users list and verify that user \"$userAC\" exists.",log)
            def usersMap = jsonSlurper.parseText(getAdminConsoleUsers(side, context, log))
            if (!userExists(usersMap, userAC, log, false)) {
                log.info "  removeAdminConsoleUser  [][]  Admin console user \"$userAC\" doesn't exist. No action needed."
            } else {
                while (i < usersMap.size()) {
                    assert(usersMap[i] != null),"Error:removeAdminConsoleUser: Error while parsing the list of admin console users."
                    if (usersMap[i].userName == userAC) {
                        roleAC = usersMap[i].roles
                        userDeleted = usersMap[i].deleted
                        i = usersMap.size()
                    }
                    i++
                }
                assert(roleAC != null),"Error:removeAdminConsoleUser: Error while fetching the role of user \"$userAC\"."
                assert(userDeleted != null),"Error:removeAdminConsoleUser: Error while fetching the \"deleted\" status of user \"$userAC\"."
                if (!userDeleted) {
                    def curlParams = "[ { \"userName\": \"$userAC\", \"roles\": \"$roleAC\", \"active\": true, \"authorities\": [ \"$roleAC\" ], \"status\": \"REMOVED\", \"suspended\": false, \"deleted\": true } ]"
                    def commandString = ["curl ", urlToDomibus(side, log, context) + "/rest/user/users", "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt", "-H", "\"Content-Type: application/json\"", "-H", "\"X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd) + "\"", "-v","-X", "PUT", "--data-binary", formatJsonForCurl(curlParams, log)]
                    def commandResult = runCommandInShell(commandString, log)
                    assert(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/),"Error:removeAdminConsoleUser: Error while trying to remove user $userAC."
                    log.info "  removeAdminConsoleUser  [][]  User \"$userAC\" Removed."
                } else {
                    log.info "  removeAdminConsoleUser  [][]  User \"$userAC\" was already deleted. No action needed."
                }
            }
        } finally {
            resetAuthTokens(log)
        }
    }
//-------------------------------------------------------------------------------------------------------------------------------
    static def loginAdminConsole(String side, context, log, String userLogin = SUPER_USER, passwordLogin = SUPER_USER_PWD,String domainValue = "Default", checkResp0 = null,checkResp1 = null,success = true){
        debugLog("  ====  Calling \"loginAdminConsole\".", log)
        def json = ifWindowsEscapeJsonString('{\"username\":\"' + "${userLogin}" + '\",\"password\":\"' + "${passwordLogin}" + '\"}')

        def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/security/authentication",
                             "-i",
                             "-v",
                             "-H",  "Content-Type: application/json",
                             "--data-binary", json, "-c", context.expand('${projectDir}') + File.separator + "cookie.txt",
                             "--trace-ascii", "-"]

        def commandResult = runCommandInShell(commandString, log)
        if(success){
            assert(commandResult[0].contains("XSRF-TOKEN")),"Error:Authenticating user: Error while trying to connect to domibus."
        }
        if(checkResp0!= null){
            assert(commandResult[0].contains(checkResp0)),"Error:Authenticating user: Error checking response for string \"$checkResp0\"."
        }
        if(checkResp1){
            assert(commandResult[0].contains(checkResp1)),"Error:Authenticating user: Error checking response for string \"$checkResp1\"."
        }
    }
//-------------------------------------------------------------------------------------------------------------------------------
    static def UpdateAdminConsoleUserPass(String side, context, log, String userLogin = SUPER_USER, oldPassword = SUPER_USER_PWD,newPassword = "Domibus-1234",String domainValue = "Default",checkResp0 = null,checkResp1 = null,success = true){
        debugLog("  ====  Calling \"loginAdminConsole\".", log)
        def authenticationUser = userLogin
        def authenticationPwd = oldPassword

        def json = ifWindowsEscapeJsonString('{\"currentPassword\":\"' + "${oldPassword}" + '\",\"newPassword\":\"' + "${newPassword}" + '\"}')


        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

            def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/security/user/password",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H",  "Content-Type: application/json",
                                 "-H","X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                 "-v",
                                 "-X", "PUT",
                                 "--data-binary", json]
            def commandResult = runCommandInShell(commandString, log)
            if(success){
                assert((commandResult[0]==~ /(?s).*HTTP\/\d.\d\s*200.*/) || (commandResult[0]==~ /(?s).*HTTP\/\d.\d\s*204.*/)),"Error:Authenticating user: Error while trying to connect to domibus."
            }
            if(checkResp0!= null){
                assert(commandResult[0].contains(checkResp0)),"Error:UpdateAdminConsoleUserPass: Error checking response for string \"$checkResp0\"."
            }
            if(checkResp1){
                assert(commandResult[0].contains(checkResp1)),"Error:UpdateAdminConsoleUserPass: Error checking response for string \"$checkResp1\"."
            }
        } finally {
            resetAuthTokens(log)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def getPluginUsers(String side, context, log, String authUser = null, String authPwd = null){
        debugLog("  ====  Calling \"getPluginUsers\".", log)
        def authenticationUser = authUser
        def authenticationPwd = authPwd

        (authenticationUser, authenticationPwd) = retriveAdminCredentials(context, log, side, authenticationUser, authenticationPwd)

        def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/plugin/users?pageSize = 10000",
                             "--cookie", context.expand( '${projectDir}')+ File.separator + "cookie.txt",
                             "-H", 'Content-Type: application/json',
                             "-H", "\"X-XSRF-TOKEN: "+ returnXsfrToken(side,context,log,authenticationUser,authenticationPwd) +"\"",
                             "-v"]
        def commandResult = runCommandInShell(commandString, log)
        assert((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/) || commandResult[1].contains("successfully")),"Error:getPluginUsers: Error while trying to connect to domibus."
        return commandResult[0].substring(5)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def addPluginUser(String side, context, log, String domainValue = "Default", String userRole = "ROLE_ADMIN", String userPl, String passwordPl = "Domibus-123", String originalUser = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1",success = true, String authUser = null, String authPwd = null){
        debugLog("  ====  Calling \"addPluginUser\".", log)
        def jsonSlurper = new JsonSlurper()
        def authenticationUser = authUser
        def authenticationPwd = authPwd

        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

            debugLog("  addPluginUser  [][]  Fetch users list and verify that user $userPl doesn't already exist.",log)
            def usersMap = jsonSlurper.parseText(getPluginUsers(side, context, log))
            if (userExists(usersMap, userPl, log, true)) {
                log.error "Error:addPluginUser: plugin user $userPl already exist: usernames must be unique."
            } else {
                debugLog("  addPluginUser  [][]  Users list before the update: " + usersMap, log)
                debugLog("  addPluginUser  [][]  Prepare user $userPl details to be added.", log)
                def curlParams = '[ { \"status\": \"NEW\", \"userName\": \"' + "${userPl}" + '\", \"authenticationType\": \"BASIC\", ' +
                        ((originalUser != null && originalUser != "") ? ' \"originalUser\": \"' + "${originalUser}" + '\", ' : '') +
                        ' \"authRoles\": \"' + "${userRole}" + '\", \"password\": \"' + "${passwordPl}" + '\", \"active\": \"true\" } ]'
                debugLog("  addPluginUser  [][]  Inserting user $userPl in the list.", log)
                debugLog("  addPluginUser  [][]  curlParams: " + curlParams, log)
                def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/plugin/users",
                                     "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                     "-H", "Content-Type: application/json",
                                     "-H", "X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                     "-X", "PUT",
                                     "--data-binary", formatJsonForCurl(curlParams, log),
                                     "-v"]
                def commandResult = runCommandInShell(commandString, log)

                if(success){
                    assert((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/)||(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)),"Error:addPluginUser: Error while trying to add a user."
                    log.info "  addPluginUser  [][]  Plugin user $userPl added."
                }else{
                    assert((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*500.*/)&&(commandResult[0]==~ /(?s).*password of.*user.*not meet the minimum complexity requirements.*/)),"Error:addAdminConsoleUser: user \"$userPl\" was created ."
                    log.info "  addPluginUser  [][]  Plugin user \"$userPl\" was not added."
                }
            }
        } finally {
            resetAuthTokens(log)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def removePluginUser(String side, context, log, String domainValue = "Default", String userPl, String authUser = null, String authPwd = null){
        debugLog("  ====  Calling \"removePluginUser\".", log)
        def jsonSlurper = new JsonSlurper()
        def authenticationUser = authUser
        def authenticationPwd = authPwd
        def rolePl = null
        def entityId = null
        def active = null
        def suspended = null
        int i = 0

        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

            debugLog("  removePluginUser  [][]  Fetch users list and verify that user $userPl exists.", log)
            def usersMap = jsonSlurper.parseText(getPluginUsers(side, context, log))
            debugLog("  removePluginUser  [][]  usersMap:	$usersMap", log)
            if (!userExists(usersMap, userPl, log, true)) {
                log.info "  removePluginUser  [][]  Plugin user $userPl doesn't exist. No action needed."
            } else {
                while (i < usersMap.entries.size()) {
                    assert(usersMap.entries[i] != null),"Error:removePluginUser: Error while parsing the list of plugin users."
                    if (usersMap.entries[i].userName == userPl) {
                        rolePl = usersMap.entries[i].authRoles
//                      originalUser = usersMap.entries[i].originalUser
                        entityId = usersMap.entries[i].entityId
                        active = usersMap.entries[i].active
                        suspended = usersMap.entries[i].suspended
                        i = usersMap.entries.size()
                    }
                    i++
                }
                assert(rolePl != null),"Error:removePluginUser: Error while fetching the role of user \"$userPl\"."
                assert(entityId != null),"Error:removePluginUser: Error while fetching the \"entityId\" of user \"$userPl\" from the user list."

                def curlParams = "[ { \"entityId\": \"$entityId\", \"userName\": \"$userPl\", \"password\": null, \"certificateId\": null,\"authRoles\": \"$rolePl\", \"authenticationType\": \"BASIC\", \"status\": \"REMOVED\", \"active\": $active, \"suspended\": $suspended } ]"

                debugLog("  removePluginUser  [][]  curlParams: " + curlParams, log)
                def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/plugin/users",
                                     "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                     "-H", "Content-Type: application/json",
                                     "-H","X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                     "-X", "PUT",
                                     "--data-binary", formatJsonForCurl(curlParams, log),
                                     "-v"]

                def commandResult = runCommandInShell(commandString, log)
                assert((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/)||(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)),"Error:removePluginUser: Error while trying to remove user $userPl."
                log.info "  removePluginUser  [][]  Plugin user $userPl removed."
            }
        } finally {
            resetAuthTokens(log)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def updatePluginUserPass(String side, context, log, String userPl, newPassword = "Domibus-1234", String domainValue = "Default",checkResp0 = null,checkResp1 = null,success = true, String authUser = null, String authPwd = null){
        debugLog("  ====  Calling \"updatePluginUser\".", log)
        def jsonSlurper = new JsonSlurper()
        def authenticationUser = authUser
        def authenticationPwd = authPwd
        def rolePl = null
        def entityId = null
        def active = null
        def suspended = null
        int i = 0

        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

            debugLog("  updatePluginUser  [][]  Fetch users list and verify that user $userPl exists.", log)
            def usersMap = jsonSlurper.parseText(getPluginUsers(side, context, log))
            debugLog("  updatePluginUser  [][]  usersMap:	$usersMap", log)
            assert(userExists(usersMap, userPl, log, true)),"Error: updatePluginUser  [][]  Plugin user $userPl doesn't exist."
            while (i < usersMap.entries.size()) {
                assert(usersMap.entries[i] != null),"Error:updatePluginUser: Error while parsing the list of plugin users."
                if (usersMap.entries[i].userName == userPl) {
                    rolePl = usersMap.entries[i].authRoles
//                  originalUser = usersMap.entries[i].originalUser
                    entityId = usersMap.entries[i].entityId
                    active = usersMap.entries[i].active
                    suspended = usersMap.entries[i].suspended
                    i = usersMap.entries.size()
                }
                i++
            }
            assert(rolePl != null),"Error:updatePluginUser: Error while fetching the role of user \"$userPl\"."
            assert(entityId != null),"Error:updatePluginUser: Error while fetching the \"entityId\" of user \"$userPl\" from the user list."

            def curlParams = "[ { \"entityId\": \"$entityId\", \"userName\": \"$userPl\", \"password\": \"$newPassword\", \"certificateId\": null,\"authRoles\": \"$rolePl\", \"authenticationType\": \"BASIC\", \"status\": \"UPDATED\", \"active\": $active, \"suspended\": $suspended } ]"

            debugLog("  updatePluginUser  [][]  curlParams: " + curlParams, log)
            def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/plugin/users",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H", "Content-Type: application/json",
                                 "-H","X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                 "-X", "PUT",
                                 "--data-binary", formatJsonForCurl(curlParams, log),
                                 "-v"]

            def commandResult = runCommandInShell(commandString, log)

            if(success){
                assert((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/)||(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)),"Error:updatePluginUser: Error while trying to update the password of the user \"$userPl\""
                log.info "  updatePluginUser  [][]  Password of plugin user \"$userPl\" was successfully updated."
            }
            if(checkResp0!= null){
                assert(commandResult[0].contains(checkResp0)),"Error:updatePluginUser: Error checking response for string \"$checkResp0\"."
            }
            if(checkResp1){
                assert(commandResult[0].contains(checkResp1)),"Error:updatePluginUser: Error checking response for string \"$checkResp1\"."
            }
        } finally {
            resetAuthTokens(log)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def userExists(usersMap, String targetedUser, log, boolean plugin = false) {
        debugLog("  ====  Calling \"userExists\".", log)
        int i = 0
        def userFound = false
        if (plugin) {
            debugLog("  userExists  [][]  Checking if plugin user \"$targetedUser\" exists.", log)
            debugLog("  userExists  [][]  Plugin users map: $usersMap.", log)
            assert(usersMap.entries != null),"Error:userExists: Error while parsing the list of plugin users."
            while ( (i < usersMap.entries.size()) && !userFound) {
                assert(usersMap.entries[i] != null),"Error:userExists: Error while parsing the list of plugin users."
                debugLog("  userExists  [][]  Iteration $i: comparing --$targetedUser--and--" + usersMap.entries[i].userName + "--.", log)
                if (usersMap.entries[i].userName == targetedUser) {
                    userFound = true
                }
                i++
            }
        } else {
            debugLog("  userExists  [][]  Checking if admin console user \"$targetedUser\" exists.", log)
            debugLog("  userExists  [][]  Admin console users map: $usersMap.", log)
            assert(usersMap != null),"Error:userExists: Error while parsing the list of admin console users."
            while ( (i < usersMap.size()) && !userFound) {
                assert(usersMap[i] != null),"Error:userExists: Error while parsing the list of admin console users."
                if (usersMap[i].userName == targetedUser) {
                    userFound = true
                }
                i++
            }
        }

        return userFound
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def resetAuthTokens(log) {
        debugLog("  ====  Calling \"resetAuthTokens\".", log)
        XSFRTOKEN_C2 = null
        XSFRTOKEN_C3 = null
        XSFRTOKEN_C_Other = null
    }

//---------------------------------------------------------------------------------------------------------------------------------
//
    static def ifWindowsEscapeJsonString(json) {
        if (System.properties['os.name'].toLowerCase().contains('windows'))
            json = json.replace("\"", "\\\"")
        return json
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Insert wrong password
    static def insertWrongPassword(String side, context, log, String username, int attempts = 1, String wrongPass = "zzzdumzzz"){
        debugLog("  ====  Calling \"insertWrongPassword\".", log)
        def jsonSlurper = new JsonSlurper()

        try{
            debugLog("  insertWrongPassword  [][]  Fetch users list and verify that user $username exists.",log)
            def usersMap = jsonSlurper.parseText(getAdminConsoleUsers(side, context, log))
            debugLog("  insertWrongPassword  [][]  usersMap:	$usersMap", log)
            assert(userExists(usersMap, username, log, false)),"Error:insertWrongPassword: user \"$username\" was not found."

            def json = ifWindowsEscapeJsonString('{\"username\":\"' + "${username}" + '\",\"password\":\"' + "${wrongPass}" + '\"}')

            // Try to login with wrong password
            def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/security/authentication",
                                 "-H",  "Content-Type: application/json",
                                 "--data-binary", json, "-c", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-i"]

            for (def i = 1; i <= attempts; i++) {
                log.info("  insertWrongPassword  [][]  Try to login with wrong password: Attempt $i.")
                def commandResult = runCommandInShell(commandString, log)
                assert((commandResult[0].contains("Bad credentials")) || (commandResult[0].contains("Suspended"))),"Error:Authenticating user: Error while trying to connect to domibus."
            }
        } finally {
            resetAuthTokens(log)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Get Admin Console user Status (suspended or active)
    static def adminConsoleUserSuspended(String side, context, log, String username, String domainValue = "Default", String authUser = null, String authPwd = null){
        debugLog("  ====  Calling \"adminConsoleUserSuspended\".", log)
        def jsonSlurper = new JsonSlurper()
        def userStatus = null
        int i = 0

        try{
            debugLog("  adminConsoleUserSuspended  [][]  Fetch users list and check user $username status: active or suspended.",log)
            def usersMap = jsonSlurper.parseText(getAdminConsoleUsers(side, context, log, authUser, authPwd))
            debugLog("  adminConsoleUserSuspended  [][]  Admin console users map: $usersMap.", log)
            assert(usersMap != null),"Error:adminConsoleUserSuspended: Error while parsing the list of admin console users."
            while ( (i < usersMap.size()) && (userStatus == null) ) {
                assert(usersMap[i] != null),"Error:adminConsoleUserSuspended: Error while parsing the list of admin console users."
                if (usersMap[i].userName == username) {
                    userStatus = usersMap[i].suspended
                }
                i++
            }
        } finally {
            resetAuthTokens(log)
        }

        assert(userStatus!= null),"Error:adminConsoleUserSuspended: Error user $username was not found."
        return userStatus
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Get plugin user Status (suspended or active)
    static def pluginUserSuspended(String side, context, log, String username, String domainValue = "Default", String authUser = null, String authPwd = null){
        debugLog("  ====  Calling \"pluginUserSuspended\".", log)
        def jsonSlurper = new JsonSlurper()
        def userStatus = null
        int i = 0

        try{
            debugLog("  pluginUserSuspended  [][]  Fetch users list and check user $username status: active or suspended.",log)
            def usersMap = jsonSlurper.parseText(getPluginUsers(side, context, log, authUser, authPwd))
            debugLog("  pluginUserSuspended  [][]  Plugin users map: $usersMap.", log)
            assert(usersMap != null),"Error:pluginUserSuspended: Error while parsing the list of plugin users."
            while ( (i < usersMap.entries.size()) && (userStatus == null) ) {
                assert(usersMap.entries[i] != null),"Error:pluginUserSuspended: Error while parsing the list of plugin users."
                debugLog("  pluginUserSuspended  [][]  Checking " + usersMap.entries[i].userName + " VS $username ...",log)
                if (usersMap.entries[i].userName == username) {
                    debugLog("  pluginUserSuspended  [][]  User $username found.",log)
                    userStatus = usersMap.entries[i].active
                }
                i++
            }

        } finally {
            resetAuthTokens(log)
        }

        assert(userStatus!= null),"Error:pluginUserSuspended: Error plugin user $username was not found."
        return userStatus
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Message filter Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    static def getMessageFilters(String side, context, log, String authUser = null, authPwd = null){
        debugLog("  ====  Calling \"getMessageFilters\".", log)
        log.info "  getMessageFilters  [][]  Get message filters for Domibus \"" + side + "\"."
        def authenticationUser = authUser
        def authenticationPwd = authPwd

        (authenticationUser, authenticationPwd) = retriveAdminCredentials(context, log, side, authenticationUser, authenticationPwd)

        def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/messagefilters",
                             "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                             "-H", "Content-Type: application/json",
                             "-H","X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                             "-X", "GET",
                             "-v"]

        def commandResult = runCommandInShell(commandString, log)
        assert(commandResult[0].contains("messageFilterEntries") || commandResult[1].contains("successfully")),"Error:getMessageFilter: Error while trying to retrieve filters."
        return commandResult[0].substring(5)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def formatFilters(filtersMap, String filterChoice, context, log, String extraCriteria = null) {
        debugLog("  ====  Calling \"formatFilters\".", log)
        log.info "  formatFilters  [][]  Analysing backends filters order ..."
        def swapBck
        def i = 0

        assert(filtersMap != null),"Error:formatFilters: Not able to get the backend details."
        debugLog("  formatFilters  [][]  FILTERS:" + filtersMap, log)

        // Single backend: no action needed
        if (filtersMap.messageFilterEntries.size() == 1) {
            return "ok"
        }
        debugLog("  formatFilters  [][]  Loop over :" + filtersMap.messageFilterEntries.size() + " backend filters.", log)
        debugLog("  formatFilters  [][]  extraCriteria = --" + extraCriteria + "--.", log)

        while (i < filtersMap.messageFilterEntries.size()) {
            assert(filtersMap.messageFilterEntries[i] != null),"Error:formatFilters: Error while parsing filter details."
            if (filtersMap.messageFilterEntries[i].backendName.toLowerCase() == filterChoice.toLowerCase()) {
                debugLog("  formatFilters  [][]  Comparing --" + filtersMap.messageFilterEntries[i].backendName + "-- and --" + filterChoice + "--", log)
                if ( (extraCriteria == null) || ( (extraCriteria != null) && filtersMap.messageFilterEntries[i].toString().contains(extraCriteria)) ) {
                    if (i == 0) {
                        return "correct"
                    }
                    debugLog("  formatFilters  [][]  switch $i element", log)
                    swapBck = filtersMap.messageFilterEntries[0]
                    filtersMap.messageFilterEntries[0] = filtersMap.messageFilterEntries[i]
                    filtersMap.messageFilterEntries[i] = swapBck
                    // swap entityId
                    def tmpEntryId = filtersMap.messageFilterEntries[i].entityId
                    filtersMap.messageFilterEntries[i].entityId = filtersMap.messageFilterEntries[0].entityId
                    filtersMap.messageFilterEntries[0].entityId = tmpEntryId
                    return filtersMap.messageFilterEntries
                }
            }
            i++
        }
        return "ko"
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def setMessageFilters(String side, String filterChoice, context, log, domainValue = "Default", String authUser = null, authPwd = null, String extraCriteria = null){
        debugLog("  ====  Calling \"setMessageFilters\".", log)
        log.info "  setMessageFilters  [][]  Start setting message filters for Domibus \"" + side + "\"."
        def authenticationUser = authUser
        def authenticationPwd = authPwd
        def jsonSlurper = new JsonSlurper()

        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

            def filtersMap = jsonSlurper.parseText(getMessageFilters(side,context,log))
            debugLog("  setMessageFilters  [][]  filtersMap:" + filtersMap, log)
            assert(filtersMap != null),"Error:setMessageFilter: Not able to get the backend details."
            assert(filtersMap.toString().toLowerCase().contains(filterChoice.toLowerCase())),"Error:setMessageFilter: The backend you want to set is not installed."
            filtersMap = formatFilters(filtersMap, filterChoice, context, log, extraCriteria)
            assert(filtersMap != "ko"),"Error:setMessageFilter: The backend you want to set is not installed."
            debugLog("  setMessageFilters  [][]  Backend filters order analyse done.", log)
            if (filtersMap == "ok") {
                log.info "  setMessageFilters  [][]  Only one backend installed: Nothing to do."
            } else {
                if (filtersMap == "correct") {
                    log.info "  setMessageFilters  [][]  The requested backend is already selected: Nothing to do."
                } else {
                    log.info "Received filtersMap: " + filtersMap


                    def curlParams = JsonOutput.toJson(filtersMap).toString()
                    log.info "-------------------------------------------------------------"
                    log.info "Received filtersMap after formatting: " +formatJsonForCurl(curlParams, log)

                    def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/messagefilters",
                                         "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                         "-H", "Content-Type: application/json",
                                         "-H","X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                         "-X", "PUT",
                                         "--data", formatJsonForCurl(curlParams, log),
                                         "-v"]

                    log.info "commandString: " + commandString
                    def commandResult = runCommandInShell(commandString, log)
                    assert((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/) || commandResult[1].contains("successfully")),"Error:setMessageFilter: Error while trying to connect to domibus. CommandResult[0]:" +commandResult[0] + "| commandResult[1]:" + commandResult[1]
                    log.info "  setMessageFilters  [][]  Message filters update done successfully for Domibus: \"" + side + "\"."
                }
            }
        } finally {
            resetAuthTokens(log)
        }
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Curl related Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    static def fetchCookieHeader(String side, context, log, String userLogin = SUPER_USER, passwordLogin = SUPER_USER_PWD) {
        debugLog("  ====  Calling \"fetchCookieHeader\".", log)
        def json = ifWindowsEscapeJsonString('{\"username\":\"' + "${userLogin}" + '\",\"password\":\"' + "${passwordLogin}" + '\"}')

        def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/security/authentication",
                             "-i",
                             "-H",  "Content-Type: application/json",
                             "--data-binary", json, "-c", context.expand('${projectDir}') + File.separator + "cookie.txt",
                             "--trace-ascii", "-"]

        def commandResult = runCommandInShell(commandString, log)
        assert(commandResult[0].contains("XSRF-TOKEN")),"Error:Authenticating user: Error while trying to connect to domibus."
        return commandResult[0]
    }

//---------------------------------------------------------------------------------------------------------------------------------
    static String returnXsfrToken(String side, context, log, String userLogin = SUPER_USER, passwordLogin = SUPER_USER_PWD) {
        debugLog("  ====  Calling \"returnXsfrToken\".", log)
        debugLog("  returnXsfrToken  [][]  Call returnXsfrToken with values: --side=$side--XSFRTOKEN_C2=$XSFRTOKEN_C2--XSFRTOKEN_C3=$XSFRTOKEN_C3.", log)
        String output

        switch (side.toLowerCase()) {
            case "c2":
            case "blue":
            case "sender":
            case "c2default":
                if (XSFRTOKEN_C2 == null) {
                    output = fetchCookieHeader(side, context, log, userLogin, passwordLogin)
                    XSFRTOKEN_C2 = output.find("XSRF-TOKEN.*;").replace("XSRF-TOKEN=", "").replace(";", "")
                }
                return XSFRTOKEN_C2
                break
            case "c3":
            case "red":
            case "receiver":
            case "c3default":
                if (XSFRTOKEN_C3 == null) {
                    output = fetchCookieHeader(side, context, log, userLogin, passwordLogin)
                    XSFRTOKEN_C3 = output.find("XSRF-TOKEN.*;").replace("XSRF-TOKEN=", "").replace(";", "")
                }
                return XSFRTOKEN_C3
                break
            case "receivergreen":
            case "green":
            case "thirddefault":
                if (XSFRTOKEN_C_Other == null) {
                    output = fetchCookieHeader(side, context, log, userLogin, passwordLogin)
                    XSFRTOKEN_C_Other = output.find("XSRF-TOKEN.*;").replace("XSRF-TOKEN=", "").replace(";", "")
                }
                return XSFRTOKEN_C_Other
                break
            default:
                assert(false), "returnXsfrToken: Unknown side. Supported values: sender, receiver, receivergreen ..."
        }
        assert(false), "returnXsfrToken: Error while retrieving XSFRTOKEN ..."
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def formatJsonForCurl(input, log) {
        debugLog("  ====  Calling \"formatJsonForCurl\".", log)
        debugLog("  +++++++++++ Run on: " + System.properties['os.name'], log)
        if (System.properties['os.name'].toLowerCase().contains('windows')) {
            assert(input != null),"Error:formatJsonForCurl: input string is null."
            assert(input.contains("[") && input.contains("]")),"Error:formatJsonForCurl: input string is corrupted."
            def intermediate = input.substring(input.indexOf("[") + 1, input.lastIndexOf("]")).replace("\"", "\"\"\"")
            return "[" + intermediate + "]"
        }
        return input
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def computePathRessources(String type, String extension, context, log) {
        debugLog("  ====  Calling \"computePathRessources\".", log)
        def basePathPropName = ""
        debugLog("Input extension: " + extension, log)

        switch (type.toLowerCase()) {
            case "special":
                basePathPropName = "specialPModesPath"
                break
            case "default":
                basePathPropName = "defaultPModesPath"
                break
            case "temp":
                basePathPropName = "tempFilesDir"
                break
            default:
                assert false, "Unknown type of path provided: ${type}. Supported types: special, default, temp."
        }

        def returnPath = (context.expand("\${#Project#${basePathPropName}}") + extension).replace("\\\\", "\\")

        debugLog("  +++++++++++ Run on: " + System.properties['os.name'], log)
        if (System.properties['os.name'].toLowerCase().contains('windows'))
            returnPath = returnPath.replace("\\", "\\\\")
        else
            returnPath = returnPath.replace("\\", "/")

        debugLog("Output computePathRessources: " + returnPath.toString(), log)
        return returnPath.toString()
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Run curl command
    static def runCommandInShell(inputCommand, log) {
        debugLog("  ====  Calling \"runCommandInShell\".", log)
        def outputCatcher = new StringBuffer()
        def errorCatcher = new StringBuffer()
        debugLog("  runCommandInShell  [][]  Run curl command: " + inputCommand, log)
        if (inputCommand) {
            def proc = inputCommand.execute()
            if (proc != null) {
                //proc.consumeProcessOutput(outputCatcher, errorCatcher)
                proc.waitForProcessOutput(outputCatcher, errorCatcher)
            }
        }
        debugLog("  runCommandInShell  [][]  outputCatcher: " + outputCatcher.toString(), log)
        debugLog("  runCommandInShell  [][]  errorCatcher: " + errorCatcher.toString(), log)
        return ([outputCatcher.toString(), errorCatcher.toString()])
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Multitenancy Functions
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Return multitenancy mode
    static def getMultitenancyFromSide(String side, context, log) {
        debugLog("  ====  Calling \"getMultitenancyFromSide\".", log)
        int mode = 0
        switch (side.toUpperCase()) {
            case "C2":
            case "BLUE":
            case "SENDER":
            case "C2DEFAULT":
                //mode = multitenancyModeC2
                mode = getMultitenancyMode(context.expand('${#Project#multitenancyModeC2}'), log)
                debugLog("  getMultitenancyFromSide  [][]  mode on domibus $side set to $mode.", log)
                break
            case "C3":
            case "RED":
            case "RECEIVER":
            case "C3DEFAULT":
                //mode = multitenancyModeC3
                mode = getMultitenancyMode(context.expand('${#Project#multitenancyModeC3}'), log)
                debugLog("  getMultitenancyFromSide  [][]  mode on domibus $side set to $mode.", log)
                break
            default:
                log.error "  getMultitenancyFromSide  [][]  ERROR:getMultitenancyFromSide: dominus $side not found."
        }
        return mode > 0
    }

// Return admin credentials for super user in multidomain configuration and admin user in single domain situation with domain provided
    static def retriveAdminCredentialsForDomain(context, log, String side, String domainValue, String authUser, authPwd) {
        debugLog("  ====  Calling \"retriveAdminCredentialsForDomain\".", log)
        def authenticationUser = authUser
        def authenticationPwd = authPwd

        def multitenancyOn = getMultitenancyFromSide(side, context, log)
        if (multitenancyOn) {
            log.info("  retriveAdminCredentialsForDomain  [][]  retriveAdminCredentialsForDomain for Domibus $side and domain $domainValue.")
            debugLog("  retriveAdminCredentialsForDomain  [][]  First, set domain to $domainValue.", log)
            setDomain(side, context, log, domainValue)
            // If authentication details are not fully provided, use default values
            if ( (authUser == null) || (authPwd == null) ) {
                authenticationUser = SUPER_USER
                authenticationPwd = SUPER_USER_PWD
            }
        } else {
            log.info("  retriveAdminCredentialsForDomain  [][]  retriveAdminCredentialsForDomain for Domibus $side.")
            // If authentication details are not fully provided, use default values
            if ( (authUser == null) || (authPwd == null) ) {
                authenticationUser = DEFAULT_ADMIN_USER
                authenticationPwd = DEFAULT_ADMIN_USER_PWD
            }
        }
        debugLog("  ====  END \"retriveAdminCredentialsForDomain\": returning credentials: ["+authenticationUser+", "+authenticationPwd+"].", log)
        return [authenticationUser, authenticationPwd]
    }

    // Return admin credentials for super user in multidomain configuration and admin user in single domaind situation without domain provided
    static def retriveAdminCredentials(context, log, String side, String authUser, authPwd) {
        def authenticationUser = authUser
        def authenticationPwd = authPwd
        // If authentication details are not fully provided, use default values
        if ( (authUser == null) || (authPwd == null) ) {
            def multitenancyOn = getMultitenancyFromSide(side, context, log)
            if (multitenancyOn) {
                authenticationUser = SUPER_USER
                authenticationPwd = SUPER_USER_PWD
            } else {
                authenticationUser = DEFAULT_ADMIN_USER
                authenticationPwd = DEFAULT_ADMIN_USER_PWD
            }
        }
        return  [authenticationUser, authenticationPwd]
    }

    //IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    //  Utilities Functions
    //IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    // Returns: "--TestCase--testStep--"
    static String locateTest(context) {
        return ("--" + context.testCase.name + "--" + context.testCase.getTestStepAt(context.getCurrentStepIndex()).getLabel() + "--  ")
    }
//---------------------------------------------------------------------------------------------------------------------------------
// Copy file from source to destination
    static void  copyFile(String source, String destination, log, overwriteOpt = true){
        debugLog("  ====  Calling \"copyFile\".",log)
        // Check that destination folder exists.
        //def destFolder = new File("${destination}")
        //assert destFolder.exists(), "Error while trying to copy file to folder " + destination + ": Destination folder doesn't exist."

        def builder = new AntBuilder()
        try {
            builder.sequential {
                copy(tofile: destination, file:source, overwrite:overwriteOpt)
            }
            log.info "  copyFile  [][]  File ${source} was successfuly copied to ${destination}"
        } catch (Exception ex) {
            log.error "  copyFile  [][]  Error while trying to copy files: " + ex
            assert 0
        }
    }

    //---------------------------------------------------------------------------------------------------------------------------------
    // replace slashes in project custom properties values
    static String formatPathSlashes(String source) {
        if ( (source != null) && (source != "") ) {
            if (System.properties['os.name'].toLowerCase().contains('windows')){
                return source.replaceAll("/", "\\\\")
            }
        }
        return source
    }

    //---------------------------------------------------------------------------------------------------------------------------------
    // Return path to domibus folder
    static String pathToDomibus(color, log, context) {
        debugLog("  ====  Calling \"pathToDomibus\".", log)
        // Return path to domibus folder base on the "color"
        def propName = ""
        switch (color.toLowerCase()) {
            case "blue":
                propName =  "pathBlue"
                break
            case "red":
                propName = "pathRed"
                break
            case "green":
                propName  = "pathGreen"
                break
            default:
                assert(false), "Unknown side color. Supported values: BLUE, RED, GREEN"
        }

        return context.expand("\${#Project#${propName}}")
    }

    //---------------------------------------------------------------------------------------------------------------------------------
    // Retrieve sql reference from domain ID if connection is not present try to open it
    def retrieveSqlConnectionRefFromDomainId(String domainName) {
        debugLog("  ====  Calling \"retrieveSqlConnectionRefFromDomainId\".", log)
        def domain = retrieveDomainId(domainName)
        openDbConnections([domain])
        assert(dbConnections.containsKey(domain) && dbConnections[domain] != null),"Error: Selecting sql references failed: Null values found."
        return dbConnections[domain]
    }

    //---------------------------------------------------------------------------------------------------------------------------------
    // Return url to specific domibus
    static String urlToDomibus(side, log, context) {
        debugLog("  ====  Calling \"urlToDomibus\".", log)
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
                propName  = "greenUrl"
                break
            case "testEnv":
                propName = "testEnvUrl"
                break
            default:
                assert(false), "Unknown side. Supported values: sender, receiver, receivergreen and testEnv"
        }
        return context.expand("\${#Project#${propName}}")
    }

    //---------------------------------------------------------------------------------------------------------------------------------
    // This methods support JMS project
    //---------------------------------------------------------------------------------------------------------------------------------
    static void  addPluginCredentialsIfNeeded(context, log, messageMap, String propPluginUsername = defaultPluginAdminC2Default, String propPluginPassword = defaultAdminDefaultPassword) {
        debugLog("  ====  Calling \"addPluginCredentialsIfNeeded\".", log)
        def unsecureLoginAllowed = context.expand("\${#Project#unsecureLoginAllowed}").toLowerCase()
        if (unsecureLoginAllowed == "false" || unsecureLoginAllowed == 0) {
            debugLog("  addPluginCredentialsIfNeeded  [][]  passed values are propPluginUsername = ${propPluginUsername} propPluginPasswor = ${propPluginPassword} ", log)
            def u = context.expand("\${#Project#${propPluginUsername}}")
            def p = context.expand("\${#Project#${propPluginPassword}}")
            debugLog("  addPluginCredentialsIfNeeded  [][]  Username|Password = " + u + "|" + p, log)
            messageMap.setStringProperty("username", u)
            messageMap.setStringProperty("password", p)
        }
    }

    static String jmsPropertiesPrefix(inputName) {
        String domId = ""
        switch (inputName.toUpperCase()) {
            case "C3":
            case "RED":
            case "RECEIVER":
                domId = "C3"
                break
            case "C2":
            case "BLUE":
            case "SENDER":
                domId = "C2"
                break
            default:
                assert false, "Not supported domain name ${inputName} provide for jmsPropertiesPrefix method. Not able to found specific properties."
                break
        }
        return domId
    }

    //---------------------------------------------------------------------------------------------------------------------------------
    // Support fast failure approche and cancel execution when one of the smoke tests fail.

    static void  initSmokeTestsResult(testSuite, log) {
        debugLog("  ====  Calling \"initSmokeTestsResult\".", log)
        testSuite.setPropertyValue("TestSuiteSmokeTestsResult", "OK")
    }

    static void  checkSmokeTestsResult(testRunner, testCase, log) {
        debugLog("  ====  Calling \"checkSmokeTestsResult\".", log)
        if (testRunner.getStatus().toString() == "FAILED") {
            testCase.testSuite.setPropertyValue("TestSuiteSmokeTestsResult", "FAILED")
            log.warn ("Test case CANCELED as one of the smoke tests failed.")
        }
    }

    static void  checkIfAnySmokeTestsFailed(testRunner, testCase, log) {
        debugLog("  ====  Calling \"checkIfAnySmokeTestsFailed\".", log)
        if (testCase.testSuite.getPropertyValue("TestSuiteSmokeTestsResult") == "FAILED") {
            debugLog("One of smoke tests failed. Now would cancel execution of all other test cases in current test suite.", log)
            testRunner.cancel( "One of smoke tests failed. Aborting whole test suite run." )
        }
    }

    //---------------------------------------------------------------------------------------------------------------------------------
    // Support enabling and disabling the authentication for SOAP requests.

    static def enableAuthenticationForTestSuite(filterForTestSuite, context, log, authProfile, authType, endpointPattern = /.*/) {
        updateAuthenticationForTestSuite(filterForTestSuite, context, log, true, authProfile, authType, endpointPattern)
    }

    static def disableAuthenticationForTestSuite(filterForTestSuite, context, log, authProfile = null, authType = null, endpointPattern = /.*/) {
        updateAuthenticationForTestSuite(filterForTestSuite, context, log, false, authProfile, authType, endpointPattern)
    }

    static def updateAuthenticationForTestSuite(filterForTestSuite, context, log, enableAuthentication, authProfile, authType, endpointPattern = /.*/) {
        debugLog("START: updateAuthenticationForTestSuite [] [] modyfication of test requests", log)
        if (enableAuthentication)
            log.info "Activating for all SOAP requests Basic Preemptive authentication in test suite ${filterForTestSuite} and endpoint matching pattern ${endpointPattern}.  Previously defined usernames and password would be used."
        else
            log.info "Disabling authentication for all SOAP requests in test suite ${filterForTestSuite} and endpoint matching pattern ${endpointPattern}."

        context.testCase.testSuite.project.getTestSuiteList().each { testSuite ->
            if (testSuite.getLabel() =~ filterForTestSuite) {
                debugLog("test suite: " + testSuite.getLabel(), log)
                testSuite.getTestCaseList().findAll{ ! it.isDisabled() }.each { testCase ->
                    debugLog("test label:" + testCase.getLabel(), log)
                    testCase.getTestStepList().findAll{ ! it.isDisabled() }.each { testStep ->
                        if (testStep instanceof WsdlTestRequestStep) {
                            debugLog("Ammending test step: " + testStep.name, log)
                            def httpRequest = testStep.getHttpRequest()
                            def endpoint = testStep.getPropertyValue("Endpoint")
                            if ( endpoint =~ endpointPattern) {
                                if (enableAuthentication)
                                    httpRequest.setSelectedAuthProfileAndAuthType(authProfile, authType)
                                else {
                                    httpRequest.setSelectedAuthProfileAndAuthType(authProfile, authType)
                                    httpRequest.removeBasicAuthenticationProfile(authProfile)
                                }
                            }
                            else
                                debugLog("Endpoint is not refering to provided patern.", log)
                        }
                    }

                }
            }
        }
        log.info "Authentication update finished"
        debugLog("END: updateAuthenticationForTestSuite [] [] Modification of authentication finished", log)
    }

    //---------------------------------------------------------------------------------------------------------------------------------
    // Start/Stop rest mock service selected by name.

    static void  stopRestMockService(String restMockServiceName,log,testRunner) {
        log.info("  ====  Calling \"stopRestMockService\".")
        debugLog("  stopRestMockService  [][]  Rest mock service name:" + restMockServiceName,log)
        def mockService = null
        try{
            mockService = testRunner.testCase.testSuite.project.getRestMockServiceByName(restMockServiceName)
        }
        catch (Exception ex) {
            log.error "  stopRestMockService  [][]  Can't find rest mock service called: " + restMockServiceName
            assert 0,"Exception occurred: " + ex
        }
        def mockRunner = mockService.getMockRunner()
        if(mockRunner!= null){
            mockRunner.stop()
            assert(!mockRunner.isRunning()),"  startRestMockService  [][]  Mock service is still running."
            mockRunner.release()
        }
        log.info ("  stopRestMockService  [][]  Rest mock service " + restMockServiceName + " is stopped.")
    }


    static void  stopAllRestMockService(log,testRunner) {
        log.info("  ====  Calling \"stopAllRestMockService\".")
        def project = testRunner.testCase.testSuite.project
        def restMockServicesCount = project.getRestMockServiceCount()
        for (i in 0..(restMockServicesCount-1)){
            // Stop each rest mock service
            def restMockServiceName = project.getRestMockServiceAt(i).getName()
            debugLog("  stopAllRestMockService  [][]  Stopping Rest service: " + restMockServiceName,log)
            stopRestMockService(restMockServiceName,log,testRunner)
        }
        log.info ("  stopAllRestMockService  [][]  All rest mock services are stopped.")
    }

    static void  startRestMockService(String restMockServiceName,log,testRunner,stopAll = 1) {
        log.info("  ====  Calling \"startRestMockService\".")
        debugLog("  startRestMockService  [][]  Rest mock service name:" + restMockServiceName,log)
        if(stopAll==1){
            stopAllRestMockService(log,testRunner)
        }else{
            stopRestMockService(restMockServiceName,log,testRunner)
        }
        def mockService = null
        try{
            mockService = testRunner.testCase.testSuite.project.getRestMockServiceByName(restMockServiceName)
        }
        catch (Exception ex) {
            log.error "  startRestMockService  [][]  Can't find rest mock service called: " + restMockServiceName
            assert 0,"Exception occurred: " + ex
        }
        mockService.start()
        def mockRunner = mockService.getMockRunner()
        assert(mockRunner!= null),"  startRestMockService  [][]  Can't get mock runner: mock service did not start."
        assert(mockRunner.isRunning()),"  startRestMockService  [][]  Mock service did not start."
        log.info ("  startRestMockService  [][]  Rest mock service " + restMockServiceName + " is running.")
    }

    //---------------------------------------------------------------------------------------------------------------------------------
    // Methods handling Pmode properties overwriting
    static def processFile(log, file, String newFileSuffix, Closure processText) {
        def text = file.text
        debugLog("New file to be created: " + file.path.toString() + newFileSuffix, log)
        def outputTextFile = new File(file.path + newFileSuffix)
        outputTextFile.write(processText(text))
        if (outputTextFile.text == text)
            log.warn "processFile method returned file with same content! filePath = ${file.path}, newFileSuffix = ${newFileSuffix}."
    }

    static def changeConfigurationFile(log, testRunner, filePath, newFileSuffix, Closure processText) {
        // Check that file exists
        def file = new File(filePath)
        if (!file.exists()) {
            testRunner.fail("File [${filePath}] does not exist. Can't change value.")
            return null
        } else log.info "  changeConfigurationFile  [][]  File [${filePath}] exists."

        processFile(log, file, newFileSuffix, processText)

        log.info "  changeDomibusProperties  [][]  Configuration file [${filePath}] amended"
    }
    static def updatePmodeEndpoints(log, context, testRunner, filePath, newFileSuffix) {
        def defaultEndpointBlue = 'http://localhost:8080/domibus'
        def newEndpointBlue = context.expand('${#Project#localUrl}')
        def defaultEndpointRed = 'http://localhost:8180/domibus'
        def newEndpointRed = context.expand('${#Project#remoteUrl}')

        debugLog("For file: ${filePath} change endpoint value ${defaultEndpointBlue} to ${newEndpointBlue} and change endpoint value: ${defaultEndpointRed} to ${newEndpointRed} value", log)
        changeConfigurationFile(log, testRunner, filePath, newFileSuffix) { text ->
            text = text.replaceAll("${defaultEndpointBlue}", "${newEndpointBlue}")
            text.replaceAll("${defaultEndpointRed}", "${newEndpointRed}")
        }
    }

    //---------------------------------------------------------------------------------------------------------------------------------
    def static updatePmodeParameter(log, context, testRunner,currentValue,newValue,filePath, newFileSuffix){
        debugLog("  ====  Calling \"updatePmodeParameter\".", log)
        def i = 0
        def swap = null

        debugLog("For file: ${filePath} change values:", log)
        changeConfigurationFile(log, testRunner, filePath, newFileSuffix) { text ->
            swap = text
            for(i = 0;i<currentValue.size;i++){
                debugLog("== \"${currentValue[i]}\" to \"${newValue[i]}\" ", log)
                swap = swap.replaceAll("${currentValue[i]}", "${newValue[i]}")
            }
            text = swap
        }

        debugLog("  ====  \"updatePmodeParameter\" DONE.", log)
    }

    //---------------------------------------------------------------------------------------------------------------------------------

    static def uploadPmodeIfStepFailedOrNotRun(log, context, testRunner, testStepToCheckName, pmodeUploadStepToExecuteName) {
        //Check status of step reverting Pmode configuration if needed run step
        Map resultOf = testRunner.getResults().collectEntries { result ->  [ (result.testStep): result.status ] }
        def myStep = context.getTestCase().getTestStepByName(testStepToCheckName)
        if (resultOf[myStep]?.toString() != "OK")  {
            log.info "As test step ${testStepToCheckName} failed or was not run reset PMode in tear down script using ${pmodeUploadStepToExecuteName} test step"
            def tStep = testRunner.testCase.testSuite.project.testSuites["Administration"].testCases["Pmode Update"].testSteps[pmodeUploadStepToExecuteName]
            tStep.run(testRunner, context)
        }
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
// Handling domibus properties at runtime
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    static def changePropertyAtRuntime(String side, String propName, String propNewValue, context, log, String domainValue = "Default", String authUser = null, authPwd = null,message = "successfully"){
        def authenticationUser = authUser
        def authenticationPwd = authPwd

        debugLog("  ====  Calling \"changePropertyAtRuntime\".", log)
        log.info "  changePropertyAtRuntime  [][]  Start procedure to change property at runtime for Domibus \"" + side + "\"."
        log.info "  changePropertyAtRuntime  [][]  Property to change: " + propName + " new value: " + propNewValue

        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

            def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/configuration/properties/" + propName,
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H",  "Content-Type: text/plain",
                                 "-H","X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                 "-X", "PUT",
                                 "-v",
                                 "--data-binary", "$propNewValue"]
            def commandResult = runCommandInShell(commandString, log)

            if(message == "successfully"){
                assert((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/) || commandResult[1].contains(message)), "Error: changePropertyAtRuntime: Error while trying to change property at runtime: response doesn't contain the expected outcome HTTP code 200.\nCommand output error: " + commandResult[1]
                log.info "  changePropertyAtRuntime  [][]  Property value was changed"
            }else{
                assert(commandResult[0].contains(message)), "Error: changePropertyAtRuntime: Error while trying to change proeprty at runtime: string $message not found in returned value."
            }

        } finally {
            resetAuthTokens(log)
        }
        debugLog("  ====  Finished \"changePropertyAtRuntime\".", log)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def getPropertyAtRuntime(String side, String propName, context, log, String domainValue = "Default", String authUser = null, authPwd = null){
        def authenticationUser = authUser
        def authenticationPwd = authPwd
        def jsonSlurper = new JsonSlurper()
        def propValue = null

        debugLog("  ====  Calling \"getPropertyAtRuntime\".", log)
        log.info "  getPropertyAtRuntime  [][]  Property to get: \"$propName\"."

        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

            def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/configuration/properties/$propName",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H",  "Content-Type: text/xml",
                                 "-H","X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                 "-v"]
            def commandResult = runCommandInShell(commandString, log)
            assert(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/),"Error:getPropertyAtRuntime: Error while fetching property of $propName."
            def propMetadata = commandResult[0].substring(5)
            debugLog("  getPropertyAtRuntime  [][]  Property get result: $propMetadata", log)
            def propMap = jsonSlurper.parseText(propMetadata)
            assert(propMap != null),"Error:getPropertyAtRuntime: Error while parsing the returned property value: null result found."
            propValue = propMap.value

            assert(propValue!= null), "Error: getPropertyAtRuntime: no property found matching name \"$propName\""
            log.info "  getPropertyAtRuntime  [][]  Property \"$propName\" value = \"$propValue\"."

        } finally {
            resetAuthTokens(log)
        }
        debugLog("  ====  Finished \"getPropertyAtRuntime\".", log)
        return propValue
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def testPropertyAtRuntime(String side, String propName, String propTestValue, context, log, String domainValue = "Default", String authUser = null, authPwd = null){
        debugLog("  ====  Calling \"testPropertyAtRuntime\".", log)
        def returnedPropValue = getPropertyAtRuntime(side,propName,context,log,domainValue,authUser,authPwd)
        debugLog("  testPropertyAtRuntime  [][]  Comparing property fetched value \"$returnedPropValue\" against input value \"$propTestValue\".",log)
        assert(returnedPropValue.equals(propTestValue)),"Error: testPropertyAtRuntime: property fetched value = \"$returnedPropValue\" instead of \"$propTestValue\""
        log.info "  testPropertyAtRuntime  [][]  Success: property fetched value \"$returnedPropValue\" and input value \"$propTestValue\" are equal."
        debugLog("  ====  Finished \"testPropertyAtRuntime\".", log)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static void  setTestCaseCustProp(custPropName,custPropValue,log,context,testRunner){
        debugLog("  ====  Calling \"setTestCaseCustProp\".", log)
        testRunner.testCase.setPropertyValue(custPropName,custPropValue)
        log.info "Test case level custom property \"$custPropName\" set to \"$custPropValue\"."
        debugLog("  ====  End \"setTestCaseCustProp\".", log)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def getTestCaseCustProp(custPropName,log, context, testRunner){
        debugLog("  ====  Calling \"getTestCaseCustProp\".", log)
        def retPropVal = testRunner.testCase.getPropertyValue(custPropName)
        assert(retPropVal!= null),"Error:getTestCaseCustProp: Couldn't fetch property \"$custPropName\" value"
        log.info "Test case level custom property fetched \"$custPropName\"= \"$retPropVal\"."
        debugLog("  ====  End \"getTestCaseCustProp\".", log)
        return retPropVal
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def getProjectCustProp(custPropName,context,log, testRunner){
        debugLog("  ====  Calling \"getProjectCustProp\".", log)
        def retPropVal = testRunner.testCase.testSuite.project.getPropertyValue(custPropName)
        assert(retPropVal!= null),"Error:getProjectCustProp: Couldn't fetch property \"$custPropName\" value"
        assert(retPropVal.trim()!= ""),"Error:getProjectCustProp: Property \"$custPropName\" returned value is empty."
        log.info "Project level custom property fetched \"$custPropName\"= \"$retPropVal\"."
        debugLog("  ====  End \"getProjectCustProp\".", log)
        return retPropVal
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def ifEmptyReturnDef(value1,value2 = "dummy"){
        if(value1==null){
            assert(value2!= "dummy"),"Error: ifEmptyReturnDef: Both values were not set."
            return value2
        }
        else{
            if(value1.trim()==""){
                assert(value2!= "dummy"),"Error: ifEmptyReturnDef: Both values were not set."
                return value2
            }
        }
        return value1
    }
//---------------------------------------------------------------------------------------------------------------------------------
//---------------------------------------------------------------------------------------------------------------------------------
// Return path to domibus folder
    static String pathToLogFiles(side, log, context) {
        debugLog("  ====  Calling \"pathToDomibus\".", log)
        // Return path to domibus folder base on the "color"
        def propName = ""
        switch (side.toLowerCase()) {
            case "c2":
            case "blue":
            case "sender":
            case "c2default":
                propName =  "logsPathBlue"
                break
            case "c3":
            case "red":
            case "receiver":
            case "c3default":
                propName = "logsPathRed"
                break
            case "receivergreen":
            case "green":
            case "thirddefault":
                propName  = "logsPathGreen"
                break
            default:
                assert(false), "Unknown side color. Supported values: BLUE, RED, GREEN"
        }
        def path = context.expand("\${#Project#${propName}}")
        return (path[-1]=='/' || path[-1]=='\\') ? path : (path + '/')
    }

//---------------------------------------------------------------------------------------------------------------------------------
    static void  checkNumberOfLinesToSkipInLogFile(side, logFileToCheck, log, context, testRunner){
        debugLog("  ====  Calling \"checkNumberOfLinesToSkipInL\".", log)
        // Before checking that some action generate specific log entry method store information how many lines already in log to not search tested log entry in old logs

        def pathToLogFile = pathToLogFiles(side, log, context) + logFileToCheck

        log.info "  checkNumberOfLinesToSkipInLogFile  [][]  skipNumberOfLines property set"

        // Check file exists
        def testFile = new File(pathToLogFile)
        if (!testFile.exists()) {
            testRunner.fail("File [${pathToLogFile}] does not exist. Can't check logs.")
            return
        } else debugLog("  checkLogFile  [][]  File [${pathToLogFile}] exists.", log)

        def lineCount = 0
        testFile.eachLine { lineCount++}
        debugLog("Line count = " + lineCount, log)

        testRunner.testCase.setPropertyValue( "skipNumberOfLines", lineCount.toString() )
        log.info "Test case level property skipNumberOfLine set to = " + lineCount
    }

//---------------------------------------------------------------------------------------------------------------------------------
    // Change Domibus configuration file
    static void  checkLogFile(side, logFileToCheck, logValueList, log, context, testRunner,checkPresent = true){
        debugLog("  ====  Calling \"checkLogFile\".", log)
        // Check that logs file contains specific entries specified in list logValueList
        // to set number of lines to skip configuration use method restoreDomibusPropertiesFromBackup(domibusPath,  log, context, testRunner)

        def pathToLogFile = pathToLogFiles(side, log, context) + logFileToCheck

        def skipNumberOfLines = context.expand('${#TestCase#skipNumberOfLines}')
        if (skipNumberOfLines == "") {
            log.info "  checkLogFile  [][]  skipNumberOfLines property not defined on the test case level would start to search on first line"
            skipNumberOfLines = 0
        } else
            skipNumberOfLines = skipNumberOfLines.toInteger()

        // Check file exists
        def testFile = new File(pathToLogFile)
        if (!testFile.exists()) {
            testRunner.fail("File [${pathToLogFile}] does not exist. Can't check logs.")
            return
        } else log.debug "  checkLogFile  [][]  File [${pathToLogFile}] exists."

        //def skipNumberOfLines = 0
        def foundTotalNumber = 0
        def fileContent = testFile.text
        log.info " checkLogFile  [][]  would skip ${skipNumberOfLines} lines"
        def logSizeInLines = fileContent.readLines().size()
        if (logSizeInLines < skipNumberOfLines) {
            log.info "Incorrect number of line to skip - it is higher than number of lines in log file (" + logSizeInLines + "). Maybe it is new log file would reset skipNumberOfLines value."
            skipNumberOfLines = 0
        }

        for(logEntryToFind  in logValueList){
            def found = false
            testFile.eachLine{
                line, lineNumber ->
                    //lineNumber++
                    if (lineNumber > skipNumberOfLines) {
                        if(line =~ logEntryToFind) {
                            log.info "  checkLogFile  [][]  In log line $lineNumber searched entry was found. Line value is: $line"
                            found = true
                        }
                    }
            }
            if (! found ){
                if(checkPresent){
                    log.warn " checkLogFile  [][]  The search string [$logEntryToFind] was NOT in file [${pathToLogFile}]"
                }
            }
            else{
                foundTotalNumber++
            }
        } //loop end
        if(checkPresent){
            if (foundTotalNumber != logValueList.size())
                testRunner.fail(" checkLogFile  [][]  Searching log file failed: Only ${foundTotalNumber} from ${logValueList.size()} entries found.")
            else
                log.info " checkLogFile  [][]  All ${logValueList.size()} entries were found in log file."
        }
        else{
            if (foundTotalNumber != 0)
                testRunner.fail(" checkLogFile  [][]  Searching log file failed: ${foundTotalNumber} from ${logValueList.size()} entries were found.")
            else
                log.info " checkLogFile  [][]  All ${logValueList.size()} entries were not found in log file."
        }
    }


//---------------------------------------------------------------------------------------------------------------------------------
// REST PUT request to test blacklisted characters
    static def userInputCheck_PUT(String side, context, log, String userAC,listType = "blacklist", String domainValue = "Default", String userRole = "ROLE_ADMIN", String passwordAC = "Domibus-123", String authUser = null, String authPwd = null){
        debugLog("  ====  Calling \"userInputCheck_PUT\".", log)
        def jsonSlurper = new JsonSlurper()
        def authenticationUser = authUser
        def authenticationPwd = authPwd

        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)
            def usersMap = jsonSlurper.parseText(getAdminConsoleUsers(side, context, log))
            if (userExists(usersMap, userAC, log, false)) {
                log.error "Error:userInputCheck_PUT: Admin Console user \"$userAC\" already exist: usernames must be unique."
            } else {
                def curlParams = "[ { \"roles\": \"$userRole\", \"userName\": \"$userAC\", \"password\": \"$passwordAC\", \"status\": \"NEW\", \"active\": true, \"suspended\": false, \"authorities\": [], \"deleted\": false } ]"
                debugLog("  userInputCheck_PUT  [][]  User \"$userAC\" parameters: $curlParams.", log)
                def commandString = ["curl ",urlToDomibus(side, log, context) + "/rest/user/users",
                                     "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                     "-H", "Content-Type: application/json",
                                     "-H", "X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                     "-X", "PUT",
                                     "--data-binary", formatJsonForCurl(curlParams, log),
                                     "-v"]
                def commandResult = runCommandInShell(commandString, log)
                if(listType.toLowerCase() == "blacklist"){
                    assert((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*400.*/)&&(commandResult[0]==~ /(?s).*Forbidden character.*detected.*/)),"Error:userInputCheck_PUT: Forbidden character not detected."
                    log.info "  userInputCheck_PUT  [][]  Forbidden character detected in value \"$userAC\"."
                }else{
                    assert(!((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*400.*/)&&(commandResult[0]==~ /(?s).*Forbidden character.*detected.*/))),"Error:userInputCheck_PUT: Forbidden character detected."
                    log.info "  userInputCheck_PUT  [][]  No forbidden characters detected in value \"$userAC\"."
                }
            }
        } finally {
            resetAuthTokens(log)
        }
    }


//---------------------------------------------------------------------------------------------------------------------------------
// REST GET request to test blacklisted/whitelisted characters
    static def userInputCheck_GET(String side, context, log, String data = "\$%25%5E%26\$%25%26\$%26\$",listType = "blacklist", domainValue = "Default", String authUser = null, String authPwd = null){
        debugLog("  ====  Calling \"userInputCheck_GET\".", log)
        debugLog("  userInputCheck_GET  [][]  Get Admin Console users for Domibus \"$side\".", log)
        def authenticationUser = authUser
        def authenticationPwd = authPwd

        try{
            //(authenticationUser, authenticationPwd) = retriveAdminCredentials(context, log, side, authenticationUser, authenticationPwd)
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)
            def commandString = "curl " + urlToDomibus(side, log, context) + "/rest/messagelog?orderBy = received&asc = false&messageId = " + data + "&messageType = USER_MESSAGE&page = 0&pageSize = 10 -b " + context.expand( '${projectDir}')+ File.separator + "cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: "+ returnXsfrToken(side,context,log,authenticationUser,authenticationPwd) + "\" -X GET "
            def commandResult = runCommandInShell(commandString, log)
            if(listType.toLowerCase() == "blacklist"){
                assert(commandResult[0]==~ /(?s).*Forbidden character.*detected.*/),"Error:userInputCheck_GET: Forbidden character not detected."
                log.info "  userInputCheck_GET  [][]  Forbidden character detected in value \"$data\"."
            }else{
                assert(!(commandResult[0]==~ /(?s).*Forbidden character.*detected.*/)),"Error:userInputCheck_GET: Forbidden character detected."
                log.info "  userInputCheck_GET  [][]  No forbidden characters detected in value \"$data\"."
            }

        } finally {
            resetAuthTokens(log)
        }
    }

//---------------------------------------------------------------------------------------------------------------------------------
// REST POST request to test blacklisted characters
    static def userInputCheck_POST(String side, context, log,listType = "blacklist" ,String userLogin = DEFAULT_ADMIN_USER, passwordLogin = DEFAULT_ADMIN_USER_PWD) {
        debugLog("  ====  Calling \"userInputCheck_POST\".", log)
        def commandResult = ""
        def json = ifWindowsEscapeJsonString('{\"username\":\"' + "${userLogin}" + '\",\"password\":\"' + "${passwordLogin}" + '\"}')

        def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/security/authentication",
                             "-i",
                             "-H",  "Content-Type: application/json",
                             "--data-binary", json, "-c", context.expand('${projectDir}') + File.separator + "cookie.txt",
                             "--trace-ascii", "-"]
        try{
            commandResult = runCommandInShell(commandString, log)
        } finally {
            resetAuthTokens(log)
        }
        if(listType.toLowerCase() == "blacklist"){
            assert(commandResult[0]==~ /(?s).*Forbidden character.*detected.*/),"Error:userInputCheck_POST: Forbidden character not detected."
            log.info "  userInputCheck_POST  [][]  Forbidden character detected in value \"$userLogin\"."
        }else{
            assert(!(commandResult[0]==~ /(?s).*Forbidden character.*detected.*/)),"Error:userInputCheck_POST: Forbidden character detected."
            log.info "  userInputCheck_POST  [][]  No forbidden characters detected in value \"$userLogin\"."
        }

    }

//---------------------------------------------------------------------------------------------------------------------------------
    static def retrieveQueueNameFromDomibus(String queuesList,queueName, context,log){
        debugLog("  ====  Calling \"retrieveQueueNameFromDomibus\".", log)
        debugLog("  retrieveQueueNameFromDomibus  [][]  Queue names list \"" + queuesList + "\".", log)
        def jsonSlurper = new JsonSlurper()
        def detailedName = null

        def queuesMap = jsonSlurper.parseText(queuesList)
        assert(queuesMap.jmsDestinations != null),"Error:retrieveQueueNameFromDomibus: Not able to get the jms queue details."
        debugLog("  retrieveQueueNameFromDomibus  [][]  queuesMap.jmsDestinations map: \"" + queuesMap.jmsDestinations + "\".", log)

        queuesMap.jmsDestinations.find{ queues ->
            queues.value.collect{ properties ->
                if(properties.key == "name"){
                    if(properties.value == queueName){
                        detailedName = properties.value
                    }
                }
            }
            if(detailedName!= null){
                return true
            }
            return false
        }
        if(detailedName!= null){
            log.info("  retrieveQueueNameFromDomibus  [][]  Retrieved queue name from domibus: \"$detailedName\"")
            return(detailedName)
        }
        else{
            log.error "  retrieveQueueNameFromDomibus  [][]  Verified queue name not found: will use input queue name."
            return(queueName)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
// Browse jms queue defined by queueName
    static def browseJmsQueue(String side, context, log,queueName = "domibus.backend.jms.errorNotifyConsumer", domainValue = "Default", String authUser = null, String authPwd = null){
        debugLog("  ====  Calling \"browseJmsQueue\".", log)
        debugLog("  browseJmsQueue  [][]  Browse jms queue \"$queueName\" for Domibus \"$side\" (Domain = \"$domainValue\").", log)
        def commandResult = ""
        def authenticationUser = authUser
        def authenticationPwd = authPwd

        (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

        try{
            // Try to retrieve the queue name from domibus to avoid problems like in case of cluster
            def commandString = "curl " + urlToDomibus(side, log, context) + "/rest/jms/destinations -b " + context.expand( '${projectDir}')+ File.separator + "cookie.txt -v -H \"Content-Type: application/json\" -H \"X-XSRF-TOKEN: "+ returnXsfrToken(side,context,log,authenticationUser,authenticationPwd) +"\" -X GET "
            commandResult = runCommandInShell(commandString, log)
            def detailedQueueName = retrieveQueueNameFromDomibus(commandResult[0].substring(5),queueName,context,log)
            debugLog("  browseJmsQueue  [][]  Queue name set to \"" + detailedQueueName + "\".", log)

            commandString = ["curl", urlToDomibus(side, log, context) + "/rest/jms/messages?source = $detailedQueueName",
                             "-H",  "Content-Type: application/json",
                             "-H", "X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                             "-X", "GET",
                             "-b", context.expand('${projectDir}') + File.separator + "cookie.txt"]

            commandResult = runCommandInShell(commandString, log)
            assert(commandResult[0].contains("{\"messages\"")),"Error:browseJmsQueue: Wrong response."
        } finally {
            resetAuthTokens(log)
        }
        return commandResult[0].substring(5)
    }

//---------------------------------------------------------------------------------------------------------------------------------

// Search for message in a jms queue identified by its ID
    static def SearchMessageJmsQueue(String side, context, log,searchKey = null,pattern = null,queueName = "domibus.backend.jms.errorNotifyConsumer", outcome = true,domainValue = "Default",String authUser = null, String authPwd = null){
        debugLog("  ====  Calling \"SearchMessageJmsQueue\".", log)
        debugLog("  SearchMessageJmsQueue  [][]  In Domibus \"$side\", search for message with key \"$searchKey\" and pattern \"$pattern\" in queue \"$queueName\" (Domain = \"$domainValue\").", log)

        def i = 0
        def found = false
        def jsonSlurper = new JsonSlurper()

        def jmsMessagesMap = jsonSlurper.parseText(browseJmsQueue(side,context,log,queueName,domainValue,authUser,authPwd))
        debugLog("  SearchMessageJmsQueue  [][]  jmsMessagesMap:" + jmsMessagesMap, log)
        assert(jmsMessagesMap != null),"Error:SearchMessageJmsQueue: Not able to get the jms queue details."
        log.info ("jmsMessagesMap size = " + jmsMessagesMap.size())

        switch(queueName.toLowerCase()){
            case "domibus.backend.jms.replyqueue":
                while ((i < jmsMessagesMap.messages.size())&&(!found)) {
                    assert(jmsMessagesMap.messages[i] != null),"Error:SearchMessageJmsQueue: Error while parsing jms queue details."
                    if(jmsMessagesMap.messages[i].customProperties.messageId!= null){
                        if (jmsMessagesMap.messages[i].customProperties.messageId.toLowerCase() == searchKey.toLowerCase()) {
                            debugLog("  SearchMessageJmsQueue  [][]  Found message ID \"" + jmsMessagesMap.messages[i].customProperties.messageId + "\".", log)
                            if(jmsMessagesMap.messages[i].customProperties.ErrorMessage!= null){
                                if(jmsMessagesMap.messages[i].customProperties.ErrorMessage.contains(pattern)){
                                    found = true
                                }
                            }
                            else{
                                log.error "  SearchMessageJmsQueue  [][]  jmsMessagesMap.messages[i] has a null ErrorMessage: not possible to use this entry ..."
                            }
                        }
                    }
                    else{
                        log.error "  SearchMessageJmsQueue  [][]  jmsMessagesMap.messages[i] has a null message ID: not possible to use this entry ..."
                    }
                    i++
                }
                break
            case "domibus.backend.jms.errornotifyconsumer":
                while ((i < jmsMessagesMap.messages.size())&&(!found)) {
                    assert(jmsMessagesMap.messages[i] != null),"Error:SearchMessageJmsQueue: Error while parsing jms queue details."
                    if(jmsMessagesMap.messages[i].customProperties.messageId!= null){
                        if (jmsMessagesMap.messages[i].customProperties.messageId.toLowerCase() == searchKey.toLowerCase()) {
                            debugLog("  SearchMessageJmsQueue  [][]  Found message ID \"" + jmsMessagesMap.messages[i].customProperties.messageId + "\".", log)
                            if(jmsMessagesMap.messages[i].customProperties.errorDetail!= null){
                                if(jmsMessagesMap.messages[i].customProperties.errorDetail.contains(pattern)){
                                    found = true
                                }
                            }
                            else{
                                log.error "  SearchMessageJmsQueue  [][]  jmsMessagesMap.messages[i] has a null errorDetail: not possible to use this entry ..."
                            }
                        }
                    }
                    else{
                        log.error "  SearchMessageJmsQueue  [][]  jmsMessagesMap.messages[i] has a null message ID: not possible to use this entry ..."
                    }
                    i++
                }
                break

        // Put here other cases (queues ...)
        // ...

            default:
                log.error "Unknown queue \"$queueName\""
        }

        if(outcome){
            assert(found),"Error:SearchMessageJmsQueue: Message with key \"$searchKey\" and pattern \"$pattern\" not found in queue \"$queueName\"."
            log.info("  SearchMessageJmsQueue  [][]  Success: Message with key \"$searchKey\" and pattern \"$pattern\" was found in queue \"$queueName\".")
        }else{
            assert(!found),"Error:SearchMessageJmsQueue: Message with key \"$searchKey\" and pattern \"$pattern\" found in queue \"$queueName\"."
            log.info("  SearchMessageJmsQueue  [][]  Success: Message with key \"$searchKey\" and pattern \"$pattern\" was not found in queue \"$queueName\".")
        }
    }

//---------------------------------------------------------------------------------------------------------------------------------
    static def setLogLevel(String side,context,log,packageName,logLevel,String domainValue = "Default", String outcome = "Success", String authUser = null, authPwd = null){
        debugLog("  ====  Calling \"setLogLevel\".", log)
        def authenticationUser = authUser
        def authenticationPwd = authPwd

        log.info "  setLogLevel  [][]  setting Log level of Package/Class \"$packageName\" for Domibus \"$side\"."
        if((logLevel==null)||(logLevel=="")||(logLevel==" ")){
            logLevel = "WARN"
        }

        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

            def json = ifWindowsEscapeJsonString('{\"name\":\"' + "${packageName}" + '\",\"level\":\"' + "${logLevel}" + '\"}')
            def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/logging/loglevel",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H","X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                 "-H",  "Content-Type: application/json",
                                 "--data-binary", json,
                                 "-v"]
            def commandResult = runCommandInShell(commandString, log)
            assert(commandResult[0].contains(outcome)),"Error:setLogLevel: Error while trying to set the log level of Package/Class \"$packageName\" for Domibus \"$side\""
            log.info "  setLogLevel  [][]  Log level successfully set to \"$logLevel\" for Package/Class \"$packageName\" in Domibus \"$side\"."
        } finally {
            resetAuthTokens(log)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def getLogLevel(String side,context,log,packageName,String domainValue = "Default", String authUser = null, authPwd = null){
        debugLog("  ====  Calling \"getLogLevel\".", log)
        def authenticationUser = authUser
        def authenticationPwd = authPwd
        def logValue = null
        def i = 0
        def jsonSlurper = new JsonSlurper()

        log.info "  getLogLevel  [][]  getting Log level of Package/Class \"$packageName\" for Domibus \"$side\"."

        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

            def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/logging/loglevel?orderBy = loggerName&asc = false&loggerName = $packageName&page = 0&pageSize = 500",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H","X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                 "-H",  "Content-Type: text/xml",
                                 "-v"]
            def commandResult = runCommandInShell(commandString, log)
            assert(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/),"Error:getLogLevel: Error while trying to connect to domibus."
            def packagesMetadata = commandResult[0].substring(5)
            debugLog("  getLogLevel  [][]  Package serach result: $packagesMetadata", log)
            def packagesMap = jsonSlurper.parseText(packagesMetadata)
            assert(packagesMap != null),"Error:getLogLevel: Error while parsing the returned packages map: null value found."
            assert(packagesMap.loggingEntries != null),"Error:getLogLevel: rror while parsing the returned packages map: empty list found."

            while ( (i < packagesMap.loggingEntries.size()) && (logValue == null) ) {
                assert(packagesMap.loggingEntries[i] != null),"Error:getLogLevel: Error while parsing the list of returned entries."
                debugLog("  getLogLevel  [][]  Iteration $i: comparing --$packageName--and--" + packagesMap.loggingEntries[i].name + "--.", log)
                if (packagesMap.loggingEntries[i].name == packageName) {
                    logValue = packagesMap.loggingEntries[i].level
                }
                i++
            }
            assert(logValue!= null), "Error: getLogLevel: no package found matching name \"$packageName\""
            log.info "  getLogLevel  [][]  Package \"$packageName\" log level value = \"$logValue\"."
        } finally {
            resetAuthTokens(log)
        }
        debugLog("  ====  Finished \"getLogLevel\".", log)
        return logValue
    }

//---------------------------------------------------------------------------------------------------------------------------------
// Alerts in DB verification
//---------------------------------------------------------------------------------------------------------------------------------
    // Verification that mesage status change alert exist for specific message_id
    def verifyMessageStatusChangeAlerts(domainId, propertyValue, eventType, alertStatus, alertLevel, expectNumberOfAlerts = 1, filterEventType = "%") {
        debugLog("  ====  Calling \"verifyMessageStatusChangeAlerts\".", log)
        genericAlertValidation(domainId, "MESSAGE_ID", propertyValue, eventType, alertStatus, alertLevel, expectNumberOfAlerts, filterEventType)
        debugLog("  ====  Ending \"verifyMessageStatusChangeAlerts\".", log)
    }

    // Verification of user iminnent expiration and expired
    def verifyUserAlerts(domainId, propertyValue, eventType, alertStatus, alertLevel, expectNumberOfAlerts = 1, filterEventType = "%") {
        debugLog("  ====  Calling \"verifyUserAlerts\".", log)
        genericAlertValidation(domainId, "USER", propertyValue, eventType, alertStatus, alertLevel, expectNumberOfAlerts, filterEventType)
        debugLog("  ====  Ending \"verifyUserAlerts\".", log)
    }

    def verifyCertAlerts(domainId, propertyValue, eventType, alertStatus, alertLevel, expectNumberOfAlerts = 1, filterEventType = "%") {
        debugLog("  ====  Calling \"verifyCertAlerts\".", log)
        genericAlertValidation(domainId, "ALIAS", propertyValue, eventType, alertStatus, alertLevel, expectNumberOfAlerts, filterEventType)
        debugLog("  ====  Ending \"verifyCertAlerts\".", log)
    }
    // Verification of user imminent expiration and expired
    def genericAlertValidation(domainId, propertyType, propertyValue, eventType, alertStatus, alertLevel, expectNumberOfAlerts = 1, filterEventType = "%") {
        debugLog("  ====  Calling \"genericAlertValidation\".", log)
        log.info"  verifyUserAlerts  [][] Alert to be found propertyType = ${propertyType}, propertyValue = ${propertyValue}, eventType = ${eventType}, alertStatus = ${alertStatus}, alertLevel = ${alertLevel}"

        def sqlHandler = retrieveSqlConnectionRefFromDomainId(domainId)

        openDbConnections([domainId])

        // Query DB
        def sqlQuery = """SELECT e.EVENT_TYPE, A.ALERT_STATUS, A.ALERT_LEVEL 
		FROM TB_EVENT_PROPERTY P 
		JOIN TB_EVENT E ON P.FK_EVENT = E.ID_PK 
		JOIN TB_ALERT A ON P.FK_EVENT = A.ID_PK 
		where P.PROPERTY_TYPE = '${propertyType}' 
		  and LOWER(P.STRING_VALUE) = LOWER('${propertyValue}')  
		  and E.EVENT_TYPE LIKE '${filterEventType}'
		  ORDER BY A.CREATION_TIME DESC"""
        List alerts = sqlHandler.rows(sqlQuery)

        assert alerts.size() == expectNumberOfAlerts, "Error:genericAlertValidation: Incorrect number for alerts expected number was ${expectNumberOfAlerts} and got ${alerts.size()} for specific property type and value ${propertyType}: ${propertyValue} "
        if (expectNumberOfAlerts == 0)
            return

        debugLog("Alert found for specific property type and value ${propertyType}: ${propertyValue}. ", log)

        // Check returned alert
        assert alerts[0].EVENT_TYPE.toUpperCase() == eventType.toUpperCase(), "Incorrect event type returned. Expected ${eventType} returned value: ${alerts[0].EVENT_TYPE}"
        assert alerts[0].ALERT_STATUS.toUpperCase() == alertStatus.toUpperCase(), "Incorrect alert status returned. Expected ${alertStatus} returned value: ${alerts[0].ALERT_STATUS}"
        assert alerts[0].ALERT_LEVEL.toUpperCase() == alertLevel.toUpperCase(), "Incorrect alert level returned. Expected ${alertLevel} returned value: ${alerts[0].ALERT_LEVEL}"

        closeDbConnections([domainId])
        log.info "Alert data checked successfully"
        debugLog("  ====  Ending \"genericAlertValidation\".", log)
    }

    // Check whenever payload in DB exist for provided message ID
    def checkPayloadDataExistInDb(domainId, message_id, expectPayload = 1) {
        debugLog("  ====  Calling \"checkPayloadDataExistInDb\".", log)
        log.info"  checkPayloadDataExistInDb  [][] Search for payload in DB for message = ${message_id}"

        def sqlHandler = retrieveSqlConnectionRefFromDomainId(domainId)
        openDbConnections([domainId])

        // Query DB
        def sqlQuery = """SELECT 
            part.BINARY_DATA is not null as DATA_EXISTS
            FROM tb_user_message msg
                join tb_message_info info on info.ID_PK = msg.MESSAGEINFO_ID_PK
                join tb_part_info part on part.PAYLOADINFO_ID = msg.id_pk
            where info.MESSAGE_ID = '${message_id}'
		"""
        List payloadExist = sqlHandler.rows(sqlQuery)

        assert payloadExist.size() > 0, "Error:checkPayloadDataExistInDb: Payload info not found for message ID ${message_id}"

        assert payloadExist[0].DATA_EXISTS == expectPayload, "Paylod data returned '${payloadExist[0].DATA_EXISTS}' and expected '${expectPayload}'"

        closeDbConnections([domainId])
        log.info "Payload data checked successfully"
        debugLog("  ====  Ending \"checkPayloadDataExistInDb\".", log)
    }

//---------------------------------------------------------------------------------------------------------------------------------
// UIreplication verification
//---------------------------------------------------------------------------------------------------------------------------------
    static def uireplicationCount(String side, context, log, enabled = true, String domainValue = "Default", String authUser = null, String authPwd = null){
        debugLog("  ====  Calling \"uireplicationCount\".", log)
        def authenticationUser = authUser
        def authenticationPwd = authPwd
        def countValue = ""
        def i = 0

        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)
            def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/uireplication/count",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H","X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                 "-H",  "Content-Type: application/json",
                                 "-v"]
            def commandResult = runCommandInShell(commandString, log)
            assert(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/),"Error:uireplicationCount: UIreplication count command returned an error."
            if(enabled){
                assert(commandResult[0].substring(7)[0].isNumber()),"Error:uireplicationCount: UIreplication count command response has an unusual format: " + commandResult[0].substring(6)
                while(commandResult[0].substring(7)[i].isNumber()){
                    countValue = countValue + commandResult[0].substring(7)[i]
                    i = i + 1
                }
            }
            else{
                assert(!(commandResult[0]==~ /(?s).*[Uu][Ii][Rr]eplication.*disabled.*/)||(commandResult[0].substring(7)[0].isNumber())),"Error:uireplicationCount: UIreplication is disabled and must not process any request."
            }
        } finally {
            resetAuthTokens(log)
        }
        debugLog("  ====  Ending \"uireplicationCount\".", log)
        return countValue
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def uireplicationCount_Check(String side, context, log, expectedValue = "0", String domainValue = "Default", String authUser = null, String authPwd = null){
        debugLog("  ====  Calling \"uireplicationCount_Check\".", log)
        def returnedValue = uireplicationCount(side,context,log,true,domainValue,authUser,authPwd)
        assert(expectedValue==returnedValue),"Error:uireplicationCount_Check: UIreplication count returned $returnedValue instead of $expectedValue."
        log.info "Number of records to be synched = $returnedValue"
        debugLog("  ====  Ending \"uireplicationCount_Check\".", log)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    static def uireplicationSync(String side, context, log, enabled = true,String domainValue = "Default", String authUser = null, String authPwd = null){
        debugLog("  ====  Calling \"uireplicationSync\".", log)

        def authenticationUser = authUser
        def authenticationPwd = authPwd

        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)
            def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/uireplication/sync",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H","X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                 "-H",  "Content-Type: application/json",
                                 "-v"]
            def commandResult = runCommandInShell(commandString, log)
            assert(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/),"Error:uireplicationSync: UIreplication sync command returned an error."
            if(enabled){
                def retCountValue = uireplicationCount(side,context,log,true,domainValue,authUser,authPwd)
                if(retCountValue=="0"){
                    assert((commandResult[0]==~ /(?s).*[Nn]o records were.*/)&&(!commandResult[0].substring(7)[0].isNumber())),"Error:uireplicationSync: UIreplication sync must not be done because the number of records to be synched is equal to 0."
                }
                else{
                    assert(commandResult[0].substring(7)[0].isNumber()),"Error:uireplicationSync: UIreplication Sync command response has an unusual format: " + commandResult[0].substring(6)
                }
                log.info commandResult[0].substring(6)
            }
            else{
                assert(commandResult[0]==~ /(?s).*[Uu][Ii][Rr]eplication.*disabled.*/),"Error:uireplicationSync: UIreplication is disabled and must not process any request."
                log.info "  uireplicationSync  [][] UIreplication is disabled."
            }
        } finally {
            resetAuthTokens(log)
        }
        debugLog("  ====  Ending \"uireplicationSync\".", log)
    }
//---------------------------------------------------------------------------------------------------------------------------------
// Load tests verification
//---------------------------------------------------------------------------------------------------------------------------------
    // Wait until all messages are submitted/received (to be used for load tests ...)
    def waitMessagesExchangedNumber(countToReachStrC2 = "0", countToReachStrC3 = "0",C2Status = "acknowledged", C3Status = "received",String senderDomainId = blueDomainID, String receiverDomanId = redDomainID,duration = 20,stepDuration = 30){
        debugLog("  ====  Calling \"waitMessagesExchangedNumber\".", log)
        def MAX_WAIT_TIME = (int)(duration * 60_000) // Maximum time to wait to check that all messages are received.
        def STEP_WAIT_TIME = (int)(stepDuration * 2_000) // Time to wait before re-checking the message status.
        def countToReachC2 = countToReachStrC2.toInteger();def countToReachC3 = countToReachStrC3.toInteger()
        def currentCount = 0

        def sqlSender = retrieveSqlConnectionRefFromDomainId(senderDomainId)
        def sqlReceiver = retrieveSqlConnectionRefFromDomainId(receiverDomanId)
        def usedDomains = [senderDomainId, receiverDomanId]
        openDbConnections(usedDomains)

        if(countToReachC2>0){
            log.info "  waitMessagesExchangedNumber  [][]  Start checking C2 for $countToReachC2 messages. MAX_WAIT_TIME: " + MAX_WAIT_TIME
            while ( (currentCount < countToReachC2) && (MAX_WAIT_TIME > 0) ) {
                sleep(STEP_WAIT_TIME)
                MAX_WAIT_TIME = MAX_WAIT_TIME - STEP_WAIT_TIME
                sqlSender.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where (LOWER(MESSAGE_TYPE) = 'user_message') and (LOWER(MESSAGE_STATUS) = ${C2Status})") {
                    currentCount = it.lignes
                }
                log.info "  waitMessagesExchangedNumber  [][]  Waiting C2:" + MAX_WAIT_TIME + " -- Current:" + currentCount + " -- Target:" + countToReachC2
            }
        }
        log.info "  waitMessagesExchangedNumber  [][]  finished checking C2 for $countToReachC2 messages. MAX_WAIT_TIME: " + MAX_WAIT_TIME
        assert(countToReachC2 == currentCount),locateTest(context) + "Error:waitMessagesExchangedNumber: Number of Messages in C2 side is $currentCount instead of $countToReachC2"

        currentCount = 0

        if(countToReachC3>0){
            log.info "  waitMessagesExchangedNumber  [][]  Start checking C3 for $countToReachC3 messages. MAX_WAIT_TIME: " + MAX_WAIT_TIME
            while ( (currentCount < countToReachC3) && (MAX_WAIT_TIME > 0) ) {
                sleep(STEP_WAIT_TIME)
                MAX_WAIT_TIME = MAX_WAIT_TIME - STEP_WAIT_TIME
                sqlReceiver.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where (LOWER(MESSAGE_TYPE) = 'user_message') and (LOWER(MESSAGE_STATUS) = ${C3Status})") {
                    currentCount = it.lignes
                }
                log.info "  waitMessagesExchangedNumber  [][]  Waiting C3:" + MAX_WAIT_TIME + " -- Current:" + currentCount + " -- Target:" + countToReachC3
            }
        }
        log.info "  waitMessagesExchangedNumber  [][]  finished checking C3 for $countToReachC3 messages. MAX_WAIT_TIME: " + MAX_WAIT_TIME
        assert(countToReachC3 == currentCount),locateTest(context) + "Error:waitMessagesExchangedNumber: Number of Messages in C3 side is $currentCount instead of $countToReachC3"

        closeDbConnections(usedDomains)
        debugLog("  ====  Ending \"waitMessagesExchangedNumber\".", log)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Count total number of messages in C2 and C3 sides (to be used for load tests ...)
    def countCurrentMessagesNumber(testRunner,C2Status = "acknowledged", C3Status = "received",String senderDomainId = blueDomainID, String receiverDomanId = redDomainID){
        debugLog("  ====  Calling \"countCurrentMessagesNumber\".", log)
        def countC2 = 0; def countC3 = 0

        def sqlSender = retrieveSqlConnectionRefFromDomainId(senderDomainId)
        def sqlReceiver = retrieveSqlConnectionRefFromDomainId(receiverDomanId)
        def usedDomains = [senderDomainId, receiverDomanId]
        openDbConnections(usedDomains)

        sqlSender.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where (LOWER(MESSAGE_TYPE) = 'user_message') and (LOWER(MESSAGE_STATUS) = ${C2Status})") {
            countC2 = it.lignes
        }

        sqlReceiver.eachRow("Select count(*) lignes from TB_MESSAGE_LOG where (LOWER(MESSAGE_TYPE) = 'user_message') and (LOWER(MESSAGE_STATUS) = ${C3Status})") {
            countC3 = it.lignes
        }

        closeDbConnections(usedDomains)

        testRunner.testCase.setPropertyValue( "propertyCountC2", countC2.toString() )
        log.info "Setting property \"propertyCountC2\" value: $countC2"
        testRunner.testCase.setPropertyValue( "propertyCountC3", countC3.toString() )
        log.info "Setting property \"propertyCountC3\" value: $countC3"
        debugLog("  ====  Ending \"countCurrentMessagesNumber\".", log)
    }
//---------------------------------------------------------------------------------------------------------------------------------
// DB operations
//---------------------------------------------------------------------------------------------------------------------------------
    // Clean Certificates to be revoked
    def cleanToBeRevCertificates(String senderDomainId = blueDomainID, String receiverDomanId =  redDomainID) {
        debugLog("  ====  Calling \"cleanToBeRevCertificates\".", log)
        def sqlSender = retrieveSqlConnectionRefFromDomainId(senderDomainId)
        def sqlReceiver = retrieveSqlConnectionRefFromDomainId(receiverDomanId)
        def usedDomains = [senderDomainId, receiverDomanId]
        openDbConnections(usedDomains)
        sqlSender.execute("DELETE FROM TB_CERTIFICATE WHERE REVOKE_NOTIFICATION_DATE IS NOT NULL")
        sqlReceiver.execute("DELETE FROM TB_CERTIFICATE WHERE REVOKE_NOTIFICATION_DATE IS NOT NULL")
        closeDbConnections(usedDomains)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Set user's password default parameter
    def setPasswordDefaultValue(String targetDomainId,String username ,valueToSet = false) {
        debugLog("  ====  Calling \"setPasswordDefaultValue\".", log)
        assert((targetDomainId!= null)&&(targetDomainId.trim()!= "")),"Error: DomainId provided must not be empty."
        def sqlDB = retrieveSqlConnectionRefFromDomainId(targetDomainId)
        def usedDomains = [targetDomainId]
        openDbConnections(usedDomains)
        if(valueToSet){
            sqlDB.execute("UPDATE TB_USER set DEFAULT_PASSWORD = 1 WHERE USER_NAME = '${username}'")
        }else{
            sqlDB.execute("UPDATE TB_USER set DEFAULT_PASSWORD = 0 WHERE USER_NAME = '${username}'")
        }
        closeDbConnections(usedDomains)
    }



    // Set plugin user's password default parameter
    def setPluginPasswordDefaultValue(String targetDomainId,String username ,valueToSet = false) {
        debugLog("  ====  Calling \"setPluginPasswordDefaultValue\".", log)
        assert((targetDomainId!= null)&&(targetDomainId.trim()!= "")),"Error: DomainId provided must not be empty."
        def sqlDB = retrieveSqlConnectionRefFromDomainId(targetDomainId)
        def usedDomains = [targetDomainId]
        openDbConnections(usedDomains)
        if(valueToSet){
            sqlDB.execute("UPDATE TB_AUTHENTICATION_ENTRY set DEFAULT_PASSWORD = 1 WHERE USERNAME = '${username}'")
        }else{
            sqlDB.execute("UPDATE TB_AUTHENTICATION_ENTRY set DEFAULT_PASSWORD = 0 WHERE USERNAME = '${username}'")
        }
        closeDbConnections(usedDomains)
    }
//---------------------------------------------------------------------------------------------------------------------------------
// Keystroes and trustores support methods
//---------------------------------------------------------------------------------------------------------------------------------
// Creates a new keystore. The name of the keystore will be "gateway_keystore.jks" unless the optional domain name
// argument is provided - in this case the name of the keystore will be "gateway_keystore_DOMAIN.jks" -.
    static def generateKeyStore(context, log, workingDirectory, keystoreAlias, keystorePassword, privateKeyPassword, validityOfKey = 300, keystoreFileName = "gateway_keystore.jks") {

        assert (keystoreAlias?.trim()), "Please provide the alias of the keystore entry as the 3rd parameter (e.g. 'red_gw', 'blue_gw'}"
        assert (keystorePassword?.trim()), "Please provide keystore password"
        assert (privateKeyPassword?.trim()), "Please provide not empty private key password"

        log.info """Generating keystore using: 
        keystoreAlias = ${keystoreAlias},  
        keystorePassword = ${keystorePassword}, 
        privateKeyPassword = ${privateKeyPassword}, 
        keystoreFileName = ${keystoreFileName}, 
        validityOfKey = ${validityOfKey}"""

        def keystoreFile = workingDirectory + keystoreFileName
        log.info keystoreFile

        def startDate = 0
        def defaultValidity = 1 // 1 days is minimal validity for Key and Certificate Management Tool - keytool
        if (validityOfKey <= 0) {
            startDate = validityOfKey - defaultValidity
            validityOfKey = defaultValidity
        }

        def commandString =  ["keytool", "-genkeypair",
                              "-dname",  "C = BE,O = eDelivery,CN = ${keystoreAlias}",
                              "-alias", "${keystoreAlias}",
                              "-keyalg", "RSA",
                              "-keysize", "2048",
                              "-keypass", "${privateKeyPassword}",
                              "-validity", validityOfKey.toString(),
                              "-storetype", "JKS",
                              "-keystore", "${keystoreFile}",
                              "-storepass", "${keystorePassword}" ,
                              "-v"]
        if (startDate != 0)
            commandString << "-startdate" << startDate.toString() + "d"

        def commandResult = runCommandInShell(commandString, log)
        assert!(commandResult[0].contains("error")),"Error: Output of keytool execution, generating key, should not contain an error. Returned message: " +  commandResult[0] + "||||" +  commandResult[1]

        def pemPath = workingDirectory + returnDefaultPemFileName(keystoreFileName, keystoreAlias)
        def pemFile = new File(pemPath)

        assert !(pemFile.exists()), "The certificate file: ${pemPath} shouldn't already exist"

        commandString =  ["keytool", "-exportcert",
                          "-alias", "${keystoreAlias}",
                          "-file", pemPath,
                          "-keystore", "${keystoreFile}",
                          "-storetype", "JKS",
                          "-storepass", "${keystorePassword}",
                          "-rfc", "-v"]

        commandResult = runCommandInShell(commandString, log)
        assert!(commandResult[0].contains("error")),"Error: Output of keytool execution, generating *.pem file, should not contain an error. Returned message: " +  commandResult[0] + "||" +  commandResult[1]

        pemFile = new File(pemPath)
        pemFile.setWritable(true)

    }

    // Shared method for creating pem filename
    static String returnDefaultPemFileName(String keystoreFileName, String keystoreAlias) {
        return "${keystoreFileName}_${keystoreAlias}.pem"
    }
    // Remove files with filenames containing filter string in it
    static void  deleteFiles(log, path, filter) {
        log.info "  deleteFiles  [][]  Delete files from [${path}] with filenames containg [${filter}] string."
        try {
            new File(path).eachFile (FileType.FILES) { file ->
                if (file.name.contains(filter)) {
                    log.info "Deleting file: " + file.name
                    file.delete()
                }
            }
        } catch (Exception ex) {
            log.error "  deleteFiles  [][]  Error while trying to delete files, exception: " + ex
            assert 0
        }
    }

// Imports an existing public-key certificate into a truststore. If the truststore is missing, it will be created. The
// name of the truststore chosen as destination will be "gateway_truststore.jks" unless the optional truststoreFileName
// argument is provided - in this case the name of the truststore used will be exactly as provided truststoreFileName
// (you need to include extension, example value "gateway_truststore_domain1.jks")
    static def updateTrustStore(context, log, workingDirectory, keystoreAlias, keystorePassword, privateKeyPassword, keystoreFileName, truststoreFileName = "gateway_truststore.jks") {

        assert (keystoreAlias?.trim()), "Please provide the alias of the keystore entry as the 3rd parameter (e.g. 'red_gw', 'blue_gw'}"
        assert (keystorePassword?.trim()), "Please provide keystore password"
        assert (privateKeyPassword?.trim()), "Please provide not empty private key password"

        log.info """Updating truststore using: 
        keystoreAlias = ${keystoreAlias}, 
        keystorePassword = ${keystorePassword}, 
        privateKeyPassword = ${privateKeyPassword}, 
        truststoreFileName = ${truststoreFileName}, 
        keystoreFileName = ${keystoreFileName}"""

        def truststoreFile = workingDirectory  + truststoreFileName
        def pemFilePath = workingDirectory  + returnDefaultPemFileName(keystoreFileName, keystoreAlias)

        def pemFile = new File(pemFilePath)
        assert (pemFile.exists()), "The certificate ${pemFile} shouldn't already exist"

        def commandString =  ["keytool", "-importcert",
                              "-alias", "${keystoreAlias}",
                              "-file", pemFilePath,
                              "-keypass", "${privateKeyPassword}",
                              "-keystore", truststoreFile,
                              "-storetype", "JKS",
                              "-storepass", "${keystorePassword}",
                              "-noprompt ", "-v"]

        def commandResult = runCommandInShell(commandString, log)
        assert!(commandResult[0].contains("error")),"Error: Output of keytool execution, importing *.pem data to truststre, should not contain an error. Returned message: " +  commandResult[0] + "||" +  commandResult[1]

        def trustFile = new File(truststoreFile)
        trustFile.setWritable(true)
    }


//---------------------------------------------------------------------------------------------------------------------------------

// 	Retrieve domain name from project custom property "allDomainsProperties" and store it in test suite level propect property for easy access
    def parseDomainsNamesIntoTSproperty(testRunner) {
        debugLog("  ====  Calling \"parseDomainsNamesIntoTSpropert\".", log)
        def domainNamesMap = [:]
        allDomainsProperties.each { domain, properties ->
            debugLog("  parseDomainsNamesIntoTSpropert  [][]  Parsing domain name for domain ID: \"${domain}\".", log)
            domainNamesMap[properties["site"] + properties["domNo"]] = properties["domainName"]
        }
        testRunner.testCase.testSuite.setPropertyValue("domainsNamesList",JsonOutput.toJson(domainNamesMap).toString())
    }
//---------------------------------------------------------------------------------------------------------------------------------

// 	Retrieve domain name from test suite custom property
    static String retDomName(side, number, testRunner) {
        String stringValue = testRunner.testCase.testSuite.getPropertyValue("domainsNamesList")
        def jsonSlurper = new JsonSlurper()
        def mapValue = jsonSlurper.parseText(stringValue)
        return mapValue[side + number]
    }
//---------------------------------------------------------------------------------------------------------------------------------
    // Copy metadata + payload files to submit fs plugin messages
    // parametersMap keys must be: [SENDER:"...",RECEIVER:"...",AGR_TYPE:"...",AGR:"...",SRV_TYPE:"...",SRV:"...",ACTION:"...",CID:"...",PAY_NAME:"...",MIME:"...",OR_SENDER:"...",FIN_RECEIVER:"..."]
    def static submitFSmessage(String side, context, log, testRunner, String configuration = "standard", String domain = "default",parametersMap = [], boolean twoFiles = true, String subFolder = ""){
        debugLog("  ====  Calling \"submitFSmessage\".", log)
        def messageMetadata = null
        def fspluginPath
        def source
        def dest
        def metadataFile
        def messageLocationPropertyName = "fsplugin.messages.location"

        def multitenancyOn = getMultitenancyFromSide(side, context, log)
        if(multitenancyOn){
            messageLocationPropertyName = "fsplugin.domains." + domain + ".messages.location"
        }

        // Extract the suitable template for metadata.xml file
        switch (configuration.toLowerCase()) {
            case  "standard":
                messageMetadata = getProjectCustProp("fsMetadataStandard",context,log,testRunner)
                break
            case "withmime":
                messageMetadata = getProjectCustProp("fsMetadataWithMimeType",context,log,testRunner)
                break
            case "withpname":
                messageMetadata = getProjectCustProp("fsMetadataWithPayloadName",context,log,testRunner)
                break
            default:
                log.warn "Unknown type of configuration: assume standard ..."
                messageMetadata = getProjectCustProp("fsPluginPrototype",context,log,testRunner)
                break
        }

        // Update the targeted values in the template
        parametersMap.each { entry ->
            messageMetadata = messageMetadata.replace(FS_DEF_MAP["FS_DEF_" + entry.key],entry.value)
        }

        // Get the path to the fsplugin sending location
        fspluginPath = getPropertyAtRuntime(side, messageLocationPropertyName, context, log, domain) + "/OUT/"
        if(subFolder != ""){
            fspluginPath = fspluginPath + subFolder + "/"
        }
        fspluginPath = formatPathSlashes(fspluginPath)

        debugLog("  submitFSmessage  [][]  fspluginPath = \"$fspluginPath\"", log)

        // Copy the file
        source = formatPathSlashes(context.expand('${projectDir}') + "/resources/PModesandKeystoresSpecialTests/fsPlugin/standard/Test_file.xml")
        dest = fspluginPath + "Test_file.xml"
        copyFile(source,dest,log)

        // Copy a second file in case needed
        if(twoFiles){
            source = formatPathSlashes(context.expand('${projectDir}') + "/resources/PModesandKeystoresSpecialTests/fsPlugin/standard/fileSmall.pdf")
            dest = fspluginPath + "fileSmall.pdf"
            copyFile(source,dest,log)
        }


        metadataFile = new File(fspluginPath + "metadata.xml")
        metadataFile.newWriter().withWriter { w ->
            w << messageMetadata
        }


        debugLog("  ====  \"submitFSmessage\" DONE.", log)

    }
//---------------------------------------------------------------------------------------------------------------------------------
    def static checkFSpayloadPresentIN(String side,String finalRecipient,String messageID,payloadName,String domain = "default",context,log,testRunner,checkTrue = true,String authUser = null, authPwd = null){
        debugLog("  ====  Calling \"checkFSpayloadPresentIN\".", log)
        def fsPayloadPath
        def testFile
        def messageLocationPrpertyName = "fsplugin.messages.location"

        def multitenancyOn = getMultitenancyFromSide(side, context, log)
        if(multitenancyOn){
            messageLocationPrpertyName = "fsplugin.domains." + domain + ".messages.location"
        }

        fsPayloadPath = getPropertyAtRuntime(side, messageLocationPrpertyName, context, log, domain) + "/IN/" + finalRecipient + "/" + messageID + "/"
        fsPayloadPath = formatPathSlashes(fsPayloadPath)
        debugLog("  checkFSpayloadPresentIN  [][]  fsPayloadPath = \"$fsPayloadPath\"", log)
        for(int i = 0;i<payloadName.size;i++){
            testFile = new File(fsPayloadPath + payloadName[i])
            if(checkTrue){
                assert(testFile.exists()),"Error: checkFSpayloadPresentIN: file \"" + payloadName[i] + "\" was not found in path \"$fsPayloadPath\" ..."
                log.info "File \"" + payloadName[i] + "\" was found in path \"$fsPayloadPath\"."
            }else{
                assert(!testFile.exists()),"Error: checkFSpayloadPresentIN: file \"" + payloadName[i] + "\" was found in path \"$fsPayloadPath\" ..."
                log.info "File \"" + payloadName[i] + "\" was not found in path \"$fsPayloadPath\"."
            }
        }

        debugLog("  ====  \"checkFSpayloadPresentIN\" DONE.", log)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    def static checkFSpayloadPresentOUT(String side,context,log,testRunner,payloadNumber = 2,String domain = "default",String authUser = null, authPwd = null){
        debugLog("  ====  Calling \"checkFSpayloadPresentOUT\".", log)
        def counter = 0
        def FS_WAIT_TIME = 60_000 // Maximum time to wait to check.
        def STEP_TIME = 1_000 // Time to wait before re-checking.
        def messageLocationPrpertyName = "fsplugin.messages.location"

        def multitenancyOn = getMultitenancyFromSide(side, context, log)
        if(multitenancyOn){
            messageLocationPrpertyName = "fsplugin.domains." + domain + ".messages.location"
        }

        def fsPayloadPath = getPropertyAtRuntime(side, messageLocationPrpertyName, context, log, domain) + "/OUT"
        fsPayloadPath = formatPathSlashes(fsPayloadPath)
        debugLog("  checkFSpayloadPresentOUT  [][]  fsPayloadPath = \"$fsPayloadPath\"", log)

        while ( (counter != payloadNumber) && (FS_WAIT_TIME > 0) ) {
            counter = new File(fsPayloadPath).listFiles().count { it.name ==~ /.*WAITING_FOR_RETRY/ }
            FS_WAIT_TIME = FS_WAIT_TIME-STEP_TIME
            log.info "  checkFSpayloadPresentOUT  [][]  Waiting $side :" + FS_WAIT_TIME + " -- Current:" + counter + " -- Target:" + payloadNumber
            sleep(STEP_TIME)
        }

        assert(counter == payloadNumber),"Error: checkFSpayloadPresentOUT: \"$counter\" messages found in \"WAITING_FOR_RETRY\" status instead of \"$payloadNumber\" ..."

        debugLog("  ====  \"checkFSpayloadPresentOUT\" DONE.", log)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    def static checkFSpayloadPresentFAILED(String side,context,log,testRunner,payloadNumber = 2,String domain = "default",providedDuration = null){
        debugLog("  ====  Calling \"checkFSpayloadPresentFAILED\".", log)
        def counter = 0
        def FS_WAIT_TIME = 60_000 // Maximum time to wait to check.
        def STEP_TIME = 1_000 // Time to wait before re-checking.
        def messageLocationPropertyName = "fsplugin.messages.location"

        def multitenancyOn = getMultitenancyFromSide(side, context, log)
        if(multitenancyOn){
            messageLocationPropertyName = "fsplugin.domains." + domain + ".messages.location"
        }

        def fsPayloadPath = getPropertyAtRuntime(side, messageLocationPropertyName, context, log, domain) + "/FAILED"
        fsPayloadPath = formatPathSlashes(fsPayloadPath)
        debugLog("  checkFSpayloadPresentFAILED  [][]  fsPayloadPath = \"$fsPayloadPath\"", log)

        if(providedDuration!= null){
            FS_WAIT_TIME = providedDuration
        }
        while ( (counter != payloadNumber) && (FS_WAIT_TIME > 0) ) {
            counter = new File(fsPayloadPath).listFiles().count { it.name ==~ /.*error/ }
            FS_WAIT_TIME = FS_WAIT_TIME-STEP_TIME
            log.info "  checkFSpayloadPresentFAILED  [][]  Waiting $side :" + FS_WAIT_TIME + " -- Current:" + counter + " -- Target:" + payloadNumber
            sleep(STEP_TIME)
        }

        assert(counter == payloadNumber),"Error: checkFSpayloadPresentFAILED: \"$counter\" messages found in \"WAITING_FOR_RETRY\" status instead of \"$payloadNumber\" ..."

        debugLog("  ====  \"checkFSpayloadPresentFAILED\" DONE.", log)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    def static cleanFSPluginFolders(String side,context,log,testRunner,String domain = "default"){
        debugLog("  ====  Calling \"cleanFSPluginFolders\".", log)

        def messageLocationPrpertyName = "fsplugin.messages.location"
        def multitenancyOn = getMultitenancyFromSide(side, context, log)
        if(multitenancyOn){
            messageLocationPrpertyName = "fsplugin.domains." + domain + ".messages.location"
        }

        def fsPayloadPathBase = getPropertyAtRuntime(side, messageLocationPrpertyName, context, log, domain)

        def fsPayloadPath = fsPayloadPathBase + "/IN"
        fsPayloadPath = formatPathSlashes(fsPayloadPath)
        debugLog("  cleanFSPluginFolders  [][]  Cleaning folder \"$fsPayloadPath\"", log)
        def folder = new File(fsPayloadPath)
        FileUtils.cleanDirectory(folder)

        fsPayloadPath = fsPayloadPathBase + "/OUT"
        fsPayloadPath = formatPathSlashes(fsPayloadPath)
        debugLog("  cleanFSPluginFolders  [][]  Cleaning folder \"$fsPayloadPath\"", log)
        folder = new File(fsPayloadPath)
        FileUtils.cleanDirectory(folder)

        fsPayloadPath = fsPayloadPathBase + "/FAILED"
        fsPayloadPath = formatPathSlashes(fsPayloadPath)
        debugLog("  cleanFSPluginFolders  [][]  Cleaning folder \"$fsPayloadPath\"", log)
        folder = new File(fsPayloadPath)
        FileUtils.cleanDirectory(folder)

        debugLog("  ====  \"cleanFSPluginFolders\" DONE.", log)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    def static getCurrentPmodeID(String side,context,log,testRunner,String domainValue = "default",String authUser = null, authPwd = null){
        debugLog("  ====  Calling \"getCurrentPmodeID\".", log)
        def commandResult = ""
        def authenticationUser = authUser
        def authenticationPwd = authPwd


        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)


            def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/pmode/current",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H", "Content-Type: application/json",
                                 "-H", "X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                 "-v"]
            commandResult = runCommandInShell(commandString, log)
            assert((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)||(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/)),"Error:getCurrentPmodeID: Error in the getCurrentPmodeID response."
        } finally {
            resetAuthTokens(log)
        }

        assert(commandResult[0]!= null),"Error:getCurrentPmodeID: getCurrentPmodeID returned null value."
        assert(commandResult[0].size() >= 5),"Error:getCurrentPmodeID: getCurrentPmodeID returned wrong value: " + commandResult[0]
        def jsonSlurper = new JsonSlurper()
        def pmodeMap = jsonSlurper.parseText(commandResult[0].substring(5))
        assert(pmodeMap.id != null),"Error:getCurrentPmodeID: Pmode data is corrupted: $pmodeMap."
        debugLog("  ====  \"getCurrentPmodeID\" DONE.", log)
        return pmodeMap.id
    }
//---------------------------------------------------------------------------------------------------------------------------------
    def static getCurrentPmodeText(String side,context,log,testRunner,String domainValue = "default",String authUser = null, authPwd = null){
        debugLog("  ====  Calling \"getCurrentPmodeText\".", log)
        def commandResult = ""
        def authenticationUser = authUser
        def authenticationPwd = authPwd

        def retrievedID = getCurrentPmodeID(side,context,log,testRunner,domainValue,authUser, authPwd)
        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)
            def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/pmode/" + retrievedID + "?noAudit = true",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H", "Content-Type: application/json",
                                 "-H", "X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                 "-v"]
            commandResult = runCommandInShell(commandString, log)
            assert((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)||(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/)),"Error:getCurrentPmodeText: Error in the getCurrentPmodeText response."
        } finally {
            resetAuthTokens(log)
        }

        debugLog("  ====  \"getCurrentPmodeText\" DONE.", log)
        return commandResult[0]
    }
//---------------------------------------------------------------------------------------------------------------------------------
    def static updatePmodeParameterRest(String side,context,log,testRunner,String domainValue = "default",target = "endpoint",targetID = "blue_gw",targetRep = "",String authUser = null, authPwd = null){
        debugLog("  ====  Calling \"updatePmodeParameter\".", log)

        def authenticationUser = authUser
        def authenticationPwd = authPwd
        (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

        String pmodeText = getCurrentPmodeText(side,context,log,testRunner,domainValue,authenticationUser,authenticationPwd)
        def pmodeFile = null
        def pmDescription = "SoapUI sample test description for PMode upload."
        def swapText = null

        // Read Pmode file
        try{
            pmodeFile = new XmlSlurper().parseText(pmodeText)
        }catch(Exception ex) {
            assert (0),"Error:updatePmodeParameter: Error parsing the pmode as xml file. " + ex
        }

        // Fetch value to change
        switch (target.toLowerCase()){
            case "endpoint":
                pmodeFile.depthFirst().each {
                    if (it.name().equals("party")){
                        if(it.@name.text().equals(targetID)){
                            swapText = it.@endpoint.text()
                        }
                    }
                }
                break

        // Put other cases here ...

            default:
                // Do nothing
                log.info "updatePmodeParameter [][] Operation not recognized."
                break

        }

        // Re-upload new Pmode file
        File tempfile = null
        try {
            // creates temporary file
            tempfile = File.createTempFile("tmp", ".xml")
            tempfile.write(pmodeText.replaceAll(swapText,targetRep))
            // deletes file when the virtual machine terminate
            tempfile.deleteOnExit()
        } catch(Exception ex) {
            // if any error occurs
            log.info "Error while creating temp file ... " + ex
        }

        try{
            def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/pmode",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H","X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                 "-F", "description = " + pmDescription,
                                 "-F", "file = @" + tempfile,
                                 "-v"]
            def commandResult = runCommandInShell(commandString, log)
            assert(commandResult[0].contains("successfully")),"Error:uploadPmode: Error while trying to upload the PMode: response doesn't contain the expected string \"successfully\"."
        }finally {
            resetAuthTokens(log)
        }

        debugLog("  ====  \"updatePmodeParameter\" DONE.", log)

    }
//---------------------------------------------------------------------------------------------------------------------------------
    def static getPartyListFromPmode(String side,context,log,testRunner,String domainValue = "default",String authUser = null, authPwd = null){
        debugLog("  ====  Calling \"getPartyListFromPmode\".", log)
        def commandResult = ""
        def authenticationUser = authUser
        def authenticationPwd = authPwd

        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)
            def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/party/list?pageSize = 100",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H", "Content-Type: application/json",
                                 "-H", "X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                 "-v"]
            commandResult = runCommandInShell(commandString, log)
            assert((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)||(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/)),"Error:getPartyListFromPmode: Error in the getPartyListFromPmode response."
        } finally {
            resetAuthTokens(log)
        }

        debugLog("  ====  \"getPartyListFromPmode\" DONE.", log)
        return commandResult[0].substring(5)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    def static addPartyMap(mainMap,extraMap,context,log){
        debugLog("  ====  Calling \"addPartyMap\".", log)
        def maxEntId = 0;def maxEntId2 = 0
        def i = 0; def j = 0;def k = 0
        def swapName

        debugLog("  addPartyMap  [][]  mainMap:" + mainMap, log)
        while (i < mainMap.size()){
            debugLog("  addPartyMap  [][]  Checking element:" + mainMap[i], log)
            debugLog("  addPartyMap  [][]  Checking value:" + mainMap[i].entityId, log)
            if(maxEntId<mainMap[i].entityId){
                maxEntId = mainMap[i].entityId
            }
            while(j<mainMap[i].processesWithPartyAsInitiator.size()){
                if(maxEntId2<mainMap[i].processesWithPartyAsInitiator[j].entityId){
                    maxEntId2 = mainMap[i].processesWithPartyAsInitiator[j].entityId
                }
                j++
            }
            j = 0
            while(j<mainMap[i].processesWithPartyAsResponder.size()){
                if(maxEntId2<mainMap[i].processesWithPartyAsResponder[j].entityId){
                    maxEntId2 = mainMap[i].processesWithPartyAsResponder[j].entityId
                }
                j++
            }
            i++
        }

        debugLog("  addPartyMap  [][]  Extracted maximum entity IDs \"$maxEntId\", \"$maxEntId2\" ...", log)

        i = 0;j = 0
        while (i < extraMap.size()){
            maxEntId++
            extraMap[i].entityId = maxEntId

            while(j<extraMap[i].processesWithPartyAsInitiator.size()){
                if(extraMap[i].processesWithPartyAsInitiator[j].entityId==0){
                    maxEntId2++
                    extraMap[i].processesWithPartyAsInitiator[j].entityId = maxEntId2
                    swapName = extraMap[i].processesWithPartyAsInitiator[j].name
                    while(k<extraMap[i].processesWithPartyAsResponder.size()){
                        if(extraMap[i].processesWithPartyAsResponder[k].name == swapName){
                            extraMap[i].processesWithPartyAsResponder[k].entityId = maxEntId2
                        }
                        k++
                    }
                    k = 0
                }
                j++
            }
            j = 0
            while(j<extraMap[i].processesWithPartyAsResponder.size()){
                if(extraMap[i].processesWithPartyAsResponder[j].entityId==0){
                    maxEntId2++
                    extraMap[i].processesWithPartyAsResponder[j].entityId = maxEntId2
                }
                j++
            }
            i++
        }

        mainMap += extraMap
        debugLog("  addPartyMap  [][]  Formatted party map:" + mainMap, log)
        debugLog("  ====  \"addPartyMap\" DONE.", log)
        return(mainMap)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    def static deletePartyMap(partyMap,partyName,context,log){
        debugLog("  ====  Calling \"deletePartyMap\".", log)
        def pArray = []
        def pIndex = 0
        def i = 0; def j = 0

        debugLog("  deletePartyMap  [][]  parties to delete :" + partyName, log)
        debugLog("  deletePartyMap  [][]  partyMap:" + partyMap, log)

        // Locate the targeted parties elements index
        while(j<partyName.size()){
            while (i < partyMap.size()){
                debugLog("  deletePartyMap  [][]  Checking element: " + partyMap[i], log)
                debugLog("  deletePartyMap  [][]  Checking value: " + partyMap[i].name, log)
                if(partyMap[i].name == partyName[j]){
                    pArray[pIndex] = i
                    pIndex++
                    i = partyMap.size()
                }
                i++
            }
            j++
        }

        debugLog("  deletePartyMap  [][]  Start deleting elements at: " + partyMap, log)
        // Delete parties found
        pIndex = 0
        while(pIndex<pArray.size()){
            partyMap.remove(pArray[pIndex])
            pIndex++
        }

        debugLog("  deletePartyMap  [][]  Formatted party map:" + partyMap, log)
        debugLog("  ====  \"deletePartyMap\" DONE.", log)
        return(partyMap)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    def static updatePartyMap(partyMap,nPartyList,context,log){
        debugLog("  ====  Calling \"updatePartyMap\".", log)
        def i = 0; def j = 0; def k = 0; def l = 0

        debugLog("  updatePartyMap  [][]  parties to update :" + nPartyList, log)
        debugLog("  updatePartyMap  [][]  partyMap:" + partyMap, log)

        // Locate the targeted parties elements index
        while(j<nPartyList.size()){
            while (i < partyMap.size()){
                debugLog("  updatePartyMap  [][]  Checking element:" + partyMap[i], log)
                debugLog("  updatePartyMap  [][]  Checking value:" + partyMap[i].name, log)
                if(partyMap[i].name == nPartyList[j].name){
                    nPartyList[j].entityId = partyMap[i].entityId
                    while(k<nPartyList[j].processesWithPartyAsInitiator.size()){
                        while(l<partyMap[i].processesWithPartyAsInitiator.size()){
                            if(nPartyList[j].processesWithPartyAsInitiator[k].name == partyMap[i].processesWithPartyAsInitiator[l].name){
                                nPartyList[j].processesWithPartyAsInitiator[k].entityId = partyMap[i].processesWithPartyAsInitiator[l].entityId
                            }
                            l++
                        }
                        k++
                    }
                    k = 0;l = 0
                    while(k<nPartyList[j].processesWithPartyAsResponder.size()){
                        while(l<partyMap[i].processesWithPartyAsResponder.size()){
                            if(nPartyList[j].processesWithPartyAsResponder[k].name == partyMap[i].processesWithPartyAsResponder[l].name){
                                nPartyList[j].processesWithPartyAsResponder[k].entityId = partyMap[i].processesWithPartyAsResponder[l].entityId
                            }
                            l++
                        }
                        k++
                    }
                    k = 0;l = 0
                    debugLog("  updatePartyMap  [][]  Replace party: " + partyMap[i], log)
                    debugLog("  updatePartyMap  [][]  with party: " + nPartyList[j], log)
                    partyMap[i] = nPartyList[j]
                }
                i++
            }
            i = 0
            j++
        }

        debugLog("  updatePartyMap  [][]  Formatted party map:" + partyMap, log)
        debugLog("  ====  \"updatePartyMap\" DONE.", log)
        return(partyMap)
    }
//---------------------------------------------------------------------------------------------------------------------------------
    def static managePartyInPmode(String side,context,log,testRunner,String operation = "add",partyParams,String domainValue = "default",outcome = "success",message = null,String authUser = null, authPwd = null){
        debugLog("  ====  Calling \"managePartyInPmode\".", log)
        def authenticationUser = authUser
        def authenticationPwd = authPwd
        def jsonSlurper= new JsonSlurper()

        try{
            (authenticationUser, authenticationPwd) = retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

            def partyMap = jsonSlurper.parseText(getPartyListFromPmode(side,context,log,testRunner,domainValue,authenticationUser, authenticationPwd))
            switch(operation.toLowerCase()){
                case "add":
                    partyMap = addPartyMap(partyMap,partyParams,context,log)
                    break
                case "delete":
                    partyMap = deletePartyMap(partyMap,partyParams,context,log)
                    break
                case "update":
                    partyMap = updatePartyMap(partyMap,partyParams,context,log)
                    break
                default:
                    assert(false),"Error:managePartyInPmode: Error in the requested operation ..."
            }


            def curlParams = JsonOutput.toJson(partyMap).toString()

            def commandString = ["curl", urlToDomibus(side, log, context) + "/rest/party/update",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H", "Content-Type: application/json",
                                 "-H", "X-XSRF-TOKEN: " + returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                 "-X", "PUT",
                                 "--data-binary", formatJsonForCurl(curlParams, log),
                                 "-v"]
            def commandResult = runCommandInShell(commandString, log)
            if(outcome.toLowerCase()=="success"){
                assert((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)||(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/)),"Error:managePartyInPmode: Error in the managePartyInPmode response."
            }else{
                assert(!(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)&& !(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/)),"Error:managePartyInPmode: Error in the managePartyInPmode response."
            }
            if(message!= null){
                assert(commandResult[0].contains(message)),"Error:managePartyInPmode: Error in the managePartyInPmode response: string \"$message\" was not found in: " + commandResult[0]
            }
        }finally {
            resetAuthTokens(log)
        }

        debugLog("  ====  \"managePartyInPmode\" DONE.", log)
    }
//---------------------------------------------------------------------------------------------------------------------------------
// Domibus text reporting
//---------------------------------------------------------------------------------------------------------------------------------

/*
	static final def TC_ID_COLUMN = 1			// Test Case ID (assuming  everything before first dash in test case name is test case ID)
	static final def TC_PROJECT_COLUMN = 2				// SoapUI project name
	static final def TC_TEST_SUITE_COLUMN = 3	// Test Suite Name
	static final def TC_NAME_COLUMN = 4			// Full Test Case Name (with test case ID)
	static final def TC_RESULT_COLUMN = 5		// Last Result of test case execution [see bellow REPORT_PASS_STRING/REPORT_FAIL_STRING]
	static final def TC_DISABLED_COLUMN = 6		// Is Test Case Disabled in SoapUI project
	static final def TC_TIME_COLUMN = 7			// Time of last Execution was Started
	static final def TC_EXEC_TIME_COLUMN = 8	// Test case execution time in seconds
	static final def TC_COMMENT_COLUMN = 9		// Collected information about failed assertion, empty for passing TCs
*/
    static final def REPORT_PASS_STRING = "PASS"
    static final def REPORT_FAIL_STRING = "FAIL"
    static final def COLUMN_LIST = ["TC_ID_COLUMN", "TC_PROJECT", "TC_TEST_SUITE_COLUMN", "TC_NAME_COLUMN", "TC_DISABLED_COLUMN", "TC_RESULT_COLUMN", "TC_TIME_COLUMN", "TC_EXEC_TIME_COLUMN", "TC_COMMENT_COLUMN"]
    static final def CSV_DELIMITER = ','
    static final def newLine = System.getProperty("line.separator")

    static def reportTestCaseCsv(testRunner, log) {
        // check update report property is not true or '1'
        def updateReport = testRunner.getRunContext().expand( '${#Project#updateReport}' )
        if (updateReport == null || updateReport.trim().isEmpty() || !(updateReport.toLowerCase().equals('true') || updateReport == '1'))
        {
            log.warn "Reporting disabled, please refer to SoapUI Project level property updateReport"
            return
        }

        //check report file exist
        log.debug "check report file exist"
        def outputReportFilePath = testRunner.getRunContext().expand( '${#Project#txtReportFilePath}' ) as String

        File file = new File(outputReportFilePath)
        if ( file.isDirectory()) {
            log.error "Error: report file is directory on path:" + outputReportFilePath
            return
        }
        File parentDir = file.getParentFile()
        if ( parentDir == null) {
            log.error "Error: parent path to report file doesn't exist. Provided path was:"  + outputReportFilePath
            return
        }
        parentDir.mkdirs()

        if ( file.createNewFile() ) { //if file does not exist it will do nothing
            log.warn "Warning: text report file doesn't exist, would create file with header:" + outputReportFilePath
            def header = COLUMN_LIST.join(CSV_DELIMITER)
            file.write(header)
        }

        // project name it should same as worksheet name
        def projectName = testRunner.testCase.testSuite.project.name
        def testSuiteName = testRunner.testCase.testSuite.getLabel()
        // extract test case ID
        String tcName = testRunner.testCase.getLabel()
        def searchedID = tcName.split("-")[0].trim()

        def tcStatus = testRunner.getStatus().toString().equals("FINISHED")? REPORT_PASS_STRING : REPORT_FAIL_STRING
        def startTime = new Date(testRunner.getStartTime()).format("dd-MM-yyyy HH:mm:ss")
        def timeTaken = testRunner.getTimeTaken()/1000 + "s"
        def comment = ""

        testRunner.getResults().each{ t->
            def stepStatus = t.status.toString()
            def stepName = t.getTestStep().getLabel()
            def stepNum = (testRunner.getResults().indexOf(t) as Integer) +1
            def executionStart = new Date(t.getTimeStamp()).format("dd-MM-yyyy HH:mm:ss")

            log.debug "Check status of step " + stepNum + " - " + stepName + " --> " + stepStatus

            if (!(stepStatus == "OK" || comment == "")) {
                comment += " || "
            }

            if (stepStatus == "FAILED")
            {
                log.debug "Found test step with FAILED status try extract error messages"
                def messages = ""
                t.getMessages().each() { msg -> messages += " || " + " |" + msg + "| " }

                comment += executionStart + ": Test case FAILED on step " + stepNum + ": " + stepName + "|| Returned error message[s]: " + messages
            }
            if (stepStatus == "CANCELED")
            {
                log.debug "Found test step with CANCELED status"
                comment += executionStart + ": Test case CANCELED on step " + stepNum + ": " + stepName
            }

        }
        //update values
        log.debug "REPORTING Test case: \"" + tcName + "\" " +  tcStatus + "ED, details in the report file"
        def row = []
        row.add(searchedID)
        row.add(projectName)
        row.add(testSuiteName)
        row.add(tcName)
        row.add(testRunner.testCase.isDisabled())
        row.add(tcStatus)
        row.add(startTime)
        row.add(timeTaken)
        row.add('"' + comment.replaceAll("\r\n|\n\r|\n|\r"," | ") + '"')

        // new row debug values
        //showResultRow(row, log)
        log.info "Reporting status for '${tcName}' from test suite: '${testSuiteName}' with result: '${tcStatus}'"

        // Write the output to a file
        def stringRow = row.join(CSV_DELIMITER)
        file.append(newLine + stringRow)
        file.finalize()

    }

} // Domibus class end

