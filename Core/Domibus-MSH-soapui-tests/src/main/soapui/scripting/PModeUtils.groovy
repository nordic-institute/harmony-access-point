import groovy.json.JsonOutput
import groovy.json.JsonSlurper

/**
 * This class contains all the functions related to PMode files processing
 */
class PModeUtils {
    def context = null
    def log = null

    PModeUtils(log, context) {
        this.context = context
        this.log = log
    }

    /**
     * The function uploads a pmode file into specified side(sender/receiver)
     * @param side
     * @param baseFilePath
     * @param extFilePath
     * @param context
     * @param log
     * @param domainValue
     * @param outcome
     * @param message
     * @param authUser
     * @param authPwd
     */
    //---------------------------------------------------------------------------------------------------------------------------------
    static def uploadPmode(String side, String baseFilePath, String extFilePath, context, log, String domainValue = "Default", String outcome = "successfully", String message = null, String authUser = null, authPwd = null){
        LogUtils.debugLog("  ====  Calling \"uploadPmode\".", log)
        log.info "  uploadPmode  [][]  Start upload PMode for Domibus \"" + side + "\"."
        def pmDescription = "SoapUI sample test description for PMode upload."
        def authenticationUser = authUser
        def authenticationPwd = authPwd
        String pmodeFile = Domibus.computePathRessources(baseFilePath, extFilePath, context, log)

        log.info "  uploadPmode  [][]  PMODE FILE PATH: " + pmodeFile

        try{
            (authenticationUser, authenticationPwd) = Domibus.retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)


            def commandString = ["curl", Domibus.urlToDomibus(side, log, context) + "/rest/pmode",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H","X-XSRF-TOKEN: " + Domibus.returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                 "-F", "description=" + pmDescription,
                                 "-F", "file=@" + pmodeFile,
                                 "-v"]
            def commandResult = Domibus.runCommandInShell(commandString, log)
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
            Domibus.resetAuthTokens(log)
        }
    }
//---------------------------------------------------------------------------------------------------------------------------------
    /**
     * This method uploads a pmode file into specified side without using authentication
     * @param side
     * @param baseFilePath
     * @param extFilePath
     * @param context
     * @param log
     * @param outcome
     * @param message
     */
    static def uploadPmodeWithoutToken(String side, String baseFilePath, String extFilePath, context, log, String outcome = "successfully", String message =null){
        LogUtils.debugLog("  ====  Calling \"uploadPmodeWithoutToken\".", log)
        log.info "  uploadPmodeWithoutToken  [][]  Start upload PMode for Domibus \"" + side + "\"."
        def pmDescription = "Dummy"

//        String output = fetchCookieHeader(side, context, log)
        String pmodeFile = Domibus.computePathRessources(baseFilePath, extFilePath, context, log)

        def commandString = ["curl", Domibus.urlToDomibus(side, log, context) + "/rest/pmode",
                             "-F", "description=" + pmDescription,
                             "-F", "file=@" + pmodeFile,
                             "-v"]
        def commandResult = Domibus.runCommandInShell(commandString, log)
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
    // Methods handling Pmode properties overwriting
    /**
     * This method is used for creating a new file from existing one
     * @param log
     * @param file
     * @param newFileSuffix
     * @param processText
     * @return
     */
    static def processFile(log, file, String newFileSuffix, Closure processText) {
        def text = file.text
        LogUtils.debugLog("New file to be created: " + file.path.toString() + newFileSuffix, log)
        def outputTextFile = new File(file.path + newFileSuffix)
        outputTextFile.write(processText(text))
        if (outputTextFile.text == text)
            log.warn "processFile method returned file with same content! filePath = ${file.path}, newFileSuffix = ${newFileSuffix}."
    }

    /**
     * This method is used for updating configuration file
     * @param log
     * @param testRunner
     * @param filePath
     * @param newFileSuffix
     * @param processText
     * @return
     */
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
    /**
     * This method upaates pmode enpoints(sender/receiver)
     * @param log
     * @param context
     * @param testRunner
     * @param filePath
     * @param newFileSuffix
     * @return
     */
    static def updatePmodeEndpoints(log, context, testRunner, filePath, newFileSuffix) {
        def defaultEndpointBlue = 'http://localhost:8080/domibus'
        def newEndpointBlue = context.expand('${#Project#localUrl}')
        def defaultEndpointRed = 'http://localhost:8180/domibus'
        def newEndpointRed = context.expand('${#Project#remoteUrl}')

        LogUtils.debugLog("For file: ${filePath} change endpoint value ${defaultEndpointBlue} to ${newEndpointBlue} and change endpoint value: ${defaultEndpointRed} to ${newEndpointRed} value", log)
        changeConfigurationFile(log, testRunner, filePath, newFileSuffix) { text ->
            text = text.replaceAll("${defaultEndpointBlue}", "${newEndpointBlue}")
            text.replaceAll("${defaultEndpointRed}", "${newEndpointRed}")
        }
    }

    //---------------------------------------------------------------------------------------------------------------------------------
    /**
     * This method updates the specified parameter from pmode file
     * @param log
     * @param context
     * @param testRunner
     * @param currentValue
     * @param newValue
     * @param filePath
     * @param newFileSuffix
     * @return
     */
    def static updatePmodeParameter(log, context, testRunner,currentValue,newValue,filePath, newFileSuffix){
        LogUtils.debugLog("  ====  Calling \"updatePmodeParameter\".", log)
        def i = 0
        def swap = null

        LogUtils.debugLog("For file: ${filePath} change values:", log)
        changeConfigurationFile(log, testRunner, filePath, newFileSuffix) { text ->
            swap = text
            for(i = 0;i<currentValue.size;i++){
                LogUtils.debugLog("== \"${currentValue[i]}\" to \"${newValue[i]}\" ", log)
                swap = swap.replaceAll("${currentValue[i]}", "${newValue[i]}")
            }
            text = swap
        }

        LogUtils.debugLog("  ====  \"updatePmodeParameter\" DONE.", log)
    }

    //---------------------------------------------------------------------------------------------------------------------------------

    /**
     * In case of step failure or skip, the original pmode file is uploaded
     * @param log
     * @param context
     * @param testRunner
     * @param testStepToCheckName
     * @param pmodeUploadStepToExecuteName
     * @return
     */
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

    //---------------------------------------------------------------------------------------------------------------------------------
    /**
     * This method returns the pmode ID
     * @param side
     * @param context
     * @param log
     * @param testRunner
     * @param domainValue
     * @param authUser
     * @param authPwd
     * @return
     */
    def static getCurrentPmodeID(String side,context,log,testRunner,String domainValue = "default",String authUser = null, authPwd = null){
        LogUtils.debugLog("  ====  Calling \"getCurrentPmodeID\".", log)
        def commandResult = ""
        def authenticationUser = authUser
        def authenticationPwd = authPwd


        try{
            (authenticationUser, authenticationPwd) = Domibus.retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)


            def commandString = ["curl", Domibus.urlToDomibus(side, log, context) + "/rest/pmode/current",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H", "Content-Type: application/json",
                                 "-H", "X-XSRF-TOKEN: " + Domibus.returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                 "-v"]
            commandResult = Domibus.runCommandInShell(commandString, log)
            assert((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)||(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/)),"Error:getCurrentPmodeID: Error in the getCurrentPmodeID response."
        } finally {
            Domibus.resetAuthTokens(log)
        }

        assert(commandResult[0]!= null),"Error:getCurrentPmodeID: getCurrentPmodeID returned null value."
        assert(commandResult[0].size() >= 5),"Error:getCurrentPmodeID: getCurrentPmodeID returned wrong value: " + commandResult[0]
        def jsonSlurper = new JsonSlurper()
        def pmodeMap = jsonSlurper.parseText(commandResult[0].substring(5))
        assert(pmodeMap.id != null),"Error:getCurrentPmodeID: Pmode data is corrupted: $pmodeMap."
        LogUtils.debugLog("  ====  \"getCurrentPmodeID\" DONE.", log)
        return pmodeMap.id
    }
//---------------------------------------------------------------------------------------------------------------------------------
    /**
     * This method returns current pmode text
     * @param side
     * @param context
     * @param log
     * @param testRunner
     * @param domainValue
     * @param authUser
     * @param authPwd
     * @return
     */
    def static getCurrentPmodeText(String side,context,log,testRunner,String domainValue = "default",String authUser = null, authPwd = null){
        LogUtils.debugLog("  ====  Calling \"getCurrentPmodeText\".", log)
        def commandResult = ""
        def authenticationUser = authUser
        def authenticationPwd = authPwd

        def retrievedID = getCurrentPmodeID(side,context,log,testRunner,domainValue,authUser, authPwd)
        try{
            (authenticationUser, authenticationPwd) = Domibus.retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)
            def commandString = ["curl", Domibus.urlToDomibus(side, log, context) + "/rest/pmode/" + retrievedID + "?noAudit=true",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H", "Accept: application/json, text/plain, */*",
                                 "-H", "X-XSRF-TOKEN: " + Domibus.returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                 "-v"]
            commandResult = Domibus.runCommandInShell(commandString, log)
            assert((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)||(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/)),"Error:getCurrentPmodeText: Error in the getCurrentPmodeText response."
        } finally {
            Domibus.resetAuthTokens(log)
        }

        LogUtils.debugLog("  ====  \"getCurrentPmodeText\" DONE.", log)
        return commandResult[0]
    }
//---------------------------------------------------------------------------------------------------------------------------------
    /**
     * This method updated the specified parameter using REST calls
     * @param side
     * @param context
     * @param log
     * @param testRunner
     * @param domainValue
     * @param target
     * @param targetID
     * @param targetRep
     * @param authUser
     * @param authPwd
     * @return
     */
    def static updatePmodeParameterRest(String side,context,log,testRunner,String domainValue = "default",target = "endpoint",targetID = "blue_gw",targetRep = "",String authUser = null, authPwd = null){
        LogUtils.debugLog("  ====  Calling \"updatePmodeParameter\".", log)

        def authenticationUser = authUser
        def authenticationPwd = authPwd
        (authenticationUser, authenticationPwd) = Domibus.retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

        String pmodeText = getCurrentPmodeText(side,context,log,testRunner,domainValue,authenticationUser,authenticationPwd)
        LogUtils.debugLog("  updatePmodeParameterRest  [][]  Current Pmode successfully retrieved.", log)
        def pmodeFile = null
        def pmDescription = "SoapUI sample test description for PMode upload."
        def swapText = null
        def formattedPath=null

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
            assert (0),"Error while creating temp file ... " + ex
            //log.info "Error while creating temp file ... " + ex
        }
        formattedPath=tempfile.getAbsolutePath()
        if (System.properties['os.name'].toLowerCase().contains('windows'))
            formattedPath = formattedPath.replace("\\", "\\\\")
        else
            formattedPath = formattedPath.replace("\\", "/")

        LogUtils.debugLog("  updatePmodeParameterRest  [][]  formattedPath: "+formattedPath, log)

        try{
            def commandString = ["curl", Domibus.urlToDomibus(side, log, context) + "/rest/pmode",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H","X-XSRF-TOKEN: " + Domibus.returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                 "-F", "description=" + pmDescription,
                                 "-F", "file=@" + tempfile,
                                 "-v"]
            def commandResult = Domibus.runCommandInShell(commandString, log)
            assert(commandResult[0].contains("successfully")),"Error:uploadPmode: Error while trying to upload the PMode: response doesn't contain the expected string \"successfully\"."
        }finally {
            Domibus.resetAuthTokens(log)
            tempfile.delete()
        }

        LogUtils.debugLog("  ====  \"updatePmodeParameter\" DONE.", log)

    }
//---------------------------------------------------------------------------------------------------------------------------------
    /**
     * This method updates pmode using REST calls
     * @param side
     * @param context
     * @param log
     * @param testRunner
     * @param domainValue
     * @param target
     * @param targetRep
     * @param authUser
     * @param authPwd
     * @return
     */
    def static updatePmodeStringRest(String side,context,log,testRunner,String domainValue = "default",target="",targetRep = "",String authUser = null, authPwd = null){
        LogUtils.debugLog("  ====  Calling \"updatePmodeStringRest\".", log)

        def authenticationUser = authUser
        def authenticationPwd = authPwd
        (authenticationUser, authenticationPwd) = Domibus.retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

        String pmodeText = getCurrentPmodeText(side,context,log,testRunner,domainValue,authenticationUser,authenticationPwd)

        LogUtils.debugLog("  updatePmodeStringRest  [][]  Current Pmode successfully retrieved.", log)
        def pmodeFile = null
        def pmDescription = "SoapUI sample test description for PMode upload."
        def formattedPath=null

        // Re-upload new Pmode file
        File tempfile = null
        try {
            // creates temporary file
            tempfile = File.createTempFile("tmp", ".xml")
            tempfile.write(pmodeText.replaceAll(target,targetRep))
            // deletes file when the virtual machine terminate
            tempfile.deleteOnExit()
        } catch(Exception ex) {
            // if any error occurs
            assert (0),"Error while creating temp file ... " + ex
        }
        formattedPath=tempfile.getAbsolutePath()
        if (System.properties['os.name'].toLowerCase().contains('windows'))
            formattedPath = formattedPath.replace("\\", "\\\\")
        else
            formattedPath = formattedPath.replace("\\", "/")

        LogUtils.debugLog("  updatePmodeStringRest  [][]  formattedPath: "+formattedPath, log)

        (authenticationUser, authenticationPwd) = Domibus.retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

        try{
            def commandString = ["curl", Domibus.urlToDomibus(side, log, context) + "/rest/pmode",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H","X-XSRF-TOKEN: " + Domibus.returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                 "-F", "description=" + pmDescription,
                                 "-F", "file=@" + tempfile,
                                 "-v"]
            def commandResult = Domibus.runCommandInShell(commandString, log)
            assert(commandResult[0].contains("successfully")),"Error:uploadPmode: Error while trying to upload the PMode: response doesn't contain the expected string \"successfully\"."
        }finally {
            Domibus.resetAuthTokens(log)
            tempfile.delete()
        }

        LogUtils.debugLog("  ====  \"updatePmodeStringRest\" DONE.", log)

    }
//---------------------------------------------------------------------------------------------------------------------------------
    /**
     * This function returns the list of parties from pmode file
     * @param side
     * @param context
     * @param log
     * @param testRunner
     * @param domainValue
     * @param authUser
     * @param authPwd
     * @return
     */
    def static getPartyListFromPmode(String side,context,log,testRunner,String domainValue = "default",String authUser = null, authPwd = null){
        LogUtils.debugLog("  ====  Calling \"getPartyListFromPmode\".", log)
        def commandResult = ""
        def authenticationUser = authUser
        def authenticationPwd = authPwd

        try{
            (authenticationUser, authenticationPwd) = Domibus.retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)
            def commandString = ["curl", Domibus.urlToDomibus(side, log, context) + "/rest/party/list?pageSize=100",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H", "Content-Type: application/json",
                                 "-H", "X-XSRF-TOKEN: " + Domibus.returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                 "-v"]
            commandResult = Domibus.runCommandInShell(commandString, log)
            assert((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)||(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/)),"Error:getPartyListFromPmode: Error in the getPartyListFromPmode response."
        } finally {
            Domibus.resetAuthTokens(log)
        }

        LogUtils.debugLog("  ====  \"getPartyListFromPmode\" DONE.", log)
        return commandResult[0].substring(5)
    }

    //---------------------------------------------------------------------------------------------------------------------------------
    /**
     * This method is used to handle(add/delete/update) the parties in pmode file
     * @param side
     * @param context
     * @param log
     * @param testRunner
     * @param operation
     * @param partyParams
     * @param domainValue
     * @param outcome
     * @param message
     * @param authUser
     * @param authPwd
     * @return
     */
    def static managePartyInPmode(String side,context,log,testRunner,String operation = "add",partyParams,String domainValue = "default",outcome = "success",message = null,String authUser = null, authPwd = null){
        LogUtils.debugLog("  ====  Calling \"managePartyInPmode\".", log)
        def authenticationUser = authUser
        def authenticationPwd = authPwd
        def jsonSlurper= new JsonSlurper()

        try{
            (authenticationUser, authenticationPwd) = Domibus.retriveAdminCredentialsForDomain(context, log, side, domainValue, authenticationUser, authenticationPwd)

            def partyMap = jsonSlurper.parseText(getPartyListFromPmode(side,context,log,testRunner,domainValue,authenticationUser, authenticationPwd))
            switch(operation.toLowerCase()){
                case "add":
                    partyMap = Domibus.addPartyMap(partyMap,partyParams,context,log)
                    break
                case "delete":
                    partyMap = Domibus.deletePartyMap(partyMap,partyParams,context,log)
                    break
                case "update":
                    partyMap = Domibus.updatePartyMap(partyMap,partyParams,context,log)
                    break
                default:
                    assert(false),"Error:managePartyInPmode: Error in the requested operation ..."
            }


            def curlParams = JsonOutput.toJson(partyMap).toString()

            def commandString = ["curl", Domibus.urlToDomibus(side, log, context) + "/rest/party/update",
                                 "--cookie", context.expand('${projectDir}') + File.separator + "cookie.txt",
                                 "-H", "Content-Type: application/json",
                                 "-H", "X-XSRF-TOKEN: " + Domibus.returnXsfrToken(side, context, log, authenticationUser, authenticationPwd),
                                 "-X", "PUT",
                                 "--data-binary", Domibus.formatJsonForCurl(curlParams, log),
                                 "-v"]
            def commandResult = Domibus.runCommandInShell(commandString, log)
            if(outcome.toLowerCase()=="success"){
                assert((commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)||(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/)),"Error:managePartyInPmode: Error in the managePartyInPmode response."
            }else{
                assert(!(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*204.*/)&& !(commandResult[1]==~ /(?s).*HTTP\/\d.\d\s*200.*/)),"Error:managePartyInPmode: Error in the managePartyInPmode response."
            }
            if(message!= null){
                assert(commandResult[0].contains(message)),"Error:managePartyInPmode: Error in the managePartyInPmode response: string \"$message\" was not found in: " + commandResult[0]
            }
        }finally {
            Domibus.resetAuthTokens(log)
        }

        LogUtils.debugLog("  ====  \"managePartyInPmode\" DONE.", log)
    }
}
