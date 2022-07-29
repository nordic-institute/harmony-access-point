import org.apache.commons.io.FileUtils

import java.nio.file.Files
import java.nio.file.Paths

class FSPluginUtils {
    def context = null
    def log = null

    static def FS_DEF_MAP = [FS_DEF_SENDER:"domibus-blue",FS_DEF_P_TYPE:"urn:oasis:names:tc:ebcore:partyid-type:unregistered",FS_DEF_S_ROLE:"http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator",FS_DEF_RECEIVER:"domibus-red",FS_DEF_R_ROLE:"http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder",FS_DEF_AGR_TYPE:"DUM",FS_DEF_AGR:"DummyAgr",FS_DEF_SRV_TYPE:"tc20",FS_DEF_SRV:"bdx:noprocess",FS_DEF_ACTION:"TC20Leg1",FS_DEF_CID:"cid:message",FS_DEF_PAY_NAME:"PayloadName.xml",FS_DEF_MIME:"text/xml",FS_DEF_OR_SENDER:"urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1",FS_DEF_FIN_RECEIVER:"urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4",FS_DEF_PROC_TYPE:"PUSH",FS_DEF_MPC:"http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/mpcPushStandard"]

    /**
     *
     * @param log
     * @param context
     */
    FSPluginUtils(log, context) {
        this.context = context
        this.log = log
    }

    //---------------------------------------------------------------------------------------------------------------------------------
    /**
     * Copy metadata + payload files to submit fs plugin messages
     * parametersMap keys must be: [SENDER:"...",RECEIVER:"...",AGR_TYPE:"...",AGR:"...",SRV_TYPE:"...",SRV:"...",ACTION:"...",CID:"...",PAY_NAME:"...",MIME:"...",OR_SENDER:"...",FIN_RECEIVER:"..."]
     * @param side
     * @param context
     * @param log
     * @param testRunner
     * @param configuration
     * @param domain
     * @param parametersMap
     * @param twoFiles
     * @param destSuffix
     * @param subFolder
     * @return
     */
    def static submitFSmessage(String side, context, log, testRunner, String configuration = "standard", String domain = "default",parametersMap = [], boolean twoFiles = true, String destSuffix="", String subFolder = ""){
        LogUtils.debugLog("  ====  Calling \"submitFSmessage\".", log)
        def messageMetadata = null
        def fspluginPath
        def source
        def dest
        def metadataFile

        /*def multitenancyOn = getMultitenancyFromSide(side, context, log)
        if(multitenancyOn){
            messageLocationPropertyName = domain + ".fsplugin.messages.location"
        }*/

        // Extract the suitable template for metadata.xml file
        switch (configuration.toLowerCase()) {
            case  "standard":
                messageMetadata = Domibus.getProjectCustProp("fsMetadataStandard",context,log,testRunner)
                break
            case "withmime":
                messageMetadata = Domibus.getProjectCustProp("fsMetadataWithMimeType",context,log,testRunner)
                break
            case "withpname":
                messageMetadata = Domibus.getProjectCustProp("fsMetadataWithPayloadName",context,log,testRunner)
                break
            case "withptype":
                messageMetadata = Domibus.getProjectCustProp("fsMetadataWithProcessingType",context,log,testRunner)
                break
            default:
                log.warn "Unknown type of configuration: assume standard ..."
                messageMetadata = Domibus.getProjectCustProp("fsPluginPrototype",context,log,testRunner)
                break
        }

        // Update the targeted values in the template
        parametersMap.each { entry ->
            messageMetadata = messageMetadata.replace(FS_DEF_MAP["FS_DEF_" + entry.key],entry.value)
        }

        // Get the path to the fsplugin sending location
        switch (side.toLowerCase()) {
            case  "c2":
                fspluginPath = Domibus.getProjectCustProp("fsFilesPathBlue",context,log,testRunner)
                break
            case "c3":
                fspluginPath = Domibus.getProjectCustProp("fsFilesPathRed",context,log,testRunner)
                break
            case "c3green":
                fspluginPath = Domibus.getProjectCustProp("fsFilesPathGreen",context,log,testRunner)
                break
            default:
                log.warn "Unknown side: assume it is C2 ..."
                fspluginPath = Domibus.getProjectCustProp("fsFilesPathBlue",context,log,testRunner)
                break
        }
        def multitenancyOn = Domibus.getMultitenancyFromSide(side, context, log)
        if (!multitenancyOn) {
            fspluginPath = fspluginPath + "/OUT/"
        } else {
            fspluginPath = fspluginPath + "/$domain" + "/OUT/"
        }
        if (Files.exists(Paths.get(fspluginPath))) {
            LogUtils.debugLog("  submitFSmessage  [][]  fspluginPath = \"$fspluginPath\" is a valid path", log)
            if(subFolder != ""){
                fspluginPath = fspluginPath + subFolder + "/"
            }
            fspluginPath = Domibus.formatPathSlashes(fspluginPath)

            LogUtils.debugLog("  submitFSmessage  [][]  fspluginPath = \"$fspluginPath\"", log)

            // Copy the file
            source = Domibus.formatPathSlashes(context.expand('${projectDir}') + "/resources/PModesandKeystoresSpecialTests/fsPlugin/standard/Test_file.xml")
            dest = fspluginPath + "Test_file" + destSuffix + ".xml"
            Domibus.copyFile(source,dest,log)

            // Copy a second file in case needed
            if(twoFiles){
                source = Domibus.formatPathSlashes(context.expand('${projectDir}') + "/resources/PModesandKeystoresSpecialTests/fsPlugin/standard/fileSmall.pdf")
                dest = fspluginPath + "fileSmall" + destSuffix + ".pdf"
                Domibus.copyFile(source,dest,log)
            }


            metadataFile = new File(fspluginPath + "metadata.xml")
            metadataFile.newWriter().withWriter { w ->
                w << messageMetadata
            }
        } else {
            LogUtils.debugLog("  submitFSmessage  [][]  fspluginPath = \"$fspluginPath\" is not a valid path", log)
        }

        LogUtils.debugLog("  ====  \"submitFSmessage\" DONE.", log)

    }

    /**
     * This functions checks payload is present in IN folder
     * @param side
     * @param finalRecipient
     * @param messageID
     * @param payloadName
     * @param domain
     * @param context
     * @param log
     * @param testRunner
     * @param checkTrue
     * @param authUser
     * @param authPwd
     * @return
     */
    def static checkFSpayloadPresentIN(String side,String finalRecipient,String messageID,payloadName,String domain = "default",context,log,testRunner,checkTrue = true,String authUser = null, authPwd = null){
        LogUtils.debugLog("  ====  Calling \"checkFSpayloadPresentIN\".", log)
        def fsPayloadPath
        def testFile
        def messageLocationPrpertyName = "fsplugin.messages.location"

        /*def multitenancyOn = getMultitenancyFromSide(side, context, log)
        if(multitenancyOn){
            messageLocationPropertyName = domain + ".fsplugin.messages.location"
        }*/


        fsPayloadPath = Domibus.getPropertyAtRuntime(side, messageLocationPrpertyName, context, log, domain) + "/IN/" + finalRecipient + "/" + messageID + "/"
        fsPayloadPath = Domibus.formatPathSlashes(fsPayloadPath)
        LogUtils.debugLog("  checkFSpayloadPresentIN  [][]  fsPayloadPath = \"$fsPayloadPath\"", log)
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

        LogUtils.debugLog("  ====  \"checkFSpayloadPresentIN\" DONE.", log)
    }

    /**
     * This functions checks payload is present in OUT folder
     * @param side
     * @param context
     * @param log
     * @param testRunner
     * @param payloadNumber
     * @param domain
     * @param authUser
     * @param authPwd
     * @return
     */
    def static checkFSpayloadPresentOUT(String side,context,log,testRunner,payloadNumber = 2,String domain = "default",String authUser = null, authPwd = null){
        LogUtils.debugLog("  ====  Calling \"checkFSpayloadPresentOUT\".", log)
        def counter = 0
        def FS_WAIT_TIME = 60_000 // Maximum time to wait to check.
        def STEP_TIME = 1_000 // Time to wait before re-checking.
        def messageLocationPrpertyName = "fsplugin.messages.location"

        /*def multitenancyOn = getMultitenancyFromSide(side, context, log)
        if(multitenancyOn){
            messageLocationPropertyName = domain + ".fsplugin.messages.location"
        }*/


        def fsPayloadPath = Domibus.getPropertyAtRuntime(side, messageLocationPrpertyName, context, log, domain) + "/OUT"
        fsPayloadPath = Domibus.formatPathSlashes(fsPayloadPath)
        LogUtils.debugLog("  checkFSpayloadPresentOUT  [][]  fsPayloadPath = \"$fsPayloadPath\"", log)

        while ( (counter != payloadNumber) && (FS_WAIT_TIME > 0) ) {
            counter = new File(fsPayloadPath).listFiles().count { it.name ==~ /.*WAITING_FOR_RETRY/ }
            FS_WAIT_TIME = FS_WAIT_TIME-STEP_TIME
            log.info "  checkFSpayloadPresentOUT  [][]  Waiting $side :" + FS_WAIT_TIME + " -- Current:" + counter + " -- Target:" + payloadNumber
            sleep(STEP_TIME)
        }

        assert(counter == payloadNumber),"Error: checkFSpayloadPresentOUT: \"$counter\" messages found in \"WAITING_FOR_RETRY\" status instead of \"$payloadNumber\" ..."

        LogUtils.debugLog("  ====  \"checkFSpayloadPresentOUT\" DONE.", log)
    }

    /**
     * This functions checks payload is present in FAILED folder
     * @param side
     * @param context
     * @param log
     * @param testRunner
     * @param payloadNumber
     * @param domain
     * @param providedDuration
     * @return
     */
    def static checkFSpayloadPresentFAILED(String side,context,log,testRunner,payloadNumber = 2,String domain = "default",providedDuration = null){
        LogUtils.debugLog("  ====  Calling \"checkFSpayloadPresentFAILED\".", log)
        def counter = 0
        def FS_WAIT_TIME = 60_000 // Maximum time to wait to check.
        def STEP_TIME = 1_000 // Time to wait before re-checking.
        def messageLocationPropertyName = "fsplugin.messages.location"

        /*def multitenancyOn = getMultitenancyFromSide(side, context, log)
        if(multitenancyOn){
            messageLocationPropertyName = domain + ".fsplugin.messages.location"
        }*/


        def fsPayloadPath = Domibus.getPropertyAtRuntime(side, messageLocationPropertyName, context, log, domain) + "/FAILED"
        fsPayloadPath = Domibus.formatPathSlashes(fsPayloadPath)
        LogUtils.debugLog("  checkFSpayloadPresentFAILED  [][]  fsPayloadPath = \"$fsPayloadPath\"", log)

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

        LogUtils.debugLog("  ====  \"checkFSpayloadPresentFAILED\" DONE.", log)
    }

    /**
     * This function is used to clean files from IN, OUT and FAILED folders
     * @param side
     * @param context
     * @param log
     * @param testRunner
     * @param domain
     * @return
     */
    def static cleanFSPluginFolders(String side,context,log,testRunner,String domain = "default"){
        LogUtils.debugLog("  ====  Calling \"cleanFSPluginFolders\".", log)
        def fsPayloadPathBase = ""
        /*def multitenancyOn = getMultitenancyFromSide(side, context, log)
        if(multitenancyOn){
            messageLocationPropertyName = domain + ".fsplugin.messages.location"
        }*/

        switch (side.toLowerCase()) {
            case  "c2":
                fsPayloadPathBase = Domibus.getProjectCustProp("fsFilesPathBlue",context,log,testRunner)
                break
            case "c3":
                fsPayloadPathBase = Domibus.getProjectCustProp("fsFilesPathRed",context,log,testRunner)
                break
            case "c3green":
                fsPayloadPathBase = Domibus.getProjectCustProp("fsFilesPathGreen",context,log,testRunner)
                break
            default:
                log.warn "Unknown side: assume it is C2 ..."
                fsPayloadPathBase = Domibus.getProjectCustProp("fsFilesPathBlue",context,log,testRunner)
                break
        }
        def multitenancyOn = Domibus.getMultitenancyFromSide(side, context, log)
        if (multitenancyOn) {
            fsPayloadPathBase = fsPayloadPathBase + "/$domain/"
        }
        def fsPayloadPath = fsPayloadPathBase + "/IN"
        fsPayloadPath = Domibus.formatPathSlashes(fsPayloadPath)
        LogUtils.debugLog("  cleanFSPluginFolders  [][]  Cleaning folder \"$fsPayloadPath\"", log)
        def folder = new File(fsPayloadPath)
        try{
            FileUtils.cleanDirectory(folder)
        }catch(Exception ex){
            log.error "Not possible to clean directory: "+fsPayloadPath
            log.error "encountered exception: "+ex
        }

        fsPayloadPath = fsPayloadPathBase + "/OUT"
        fsPayloadPath = Domibus.formatPathSlashes(fsPayloadPath)
        LogUtils.debugLog("  cleanFSPluginFolders  [][]  Cleaning folder \"$fsPayloadPath\"", log)
        folder = new File(fsPayloadPath)
        try{
            FileUtils.cleanDirectory(folder)
        }catch(Exception ex){
            log.error "Not possible to clean directory: "+fsPayloadPath
            log.error "encountered exception: "+ex
        }

        fsPayloadPath = fsPayloadPathBase + "/FAILED"
        fsPayloadPath = Domibus.formatPathSlashes(fsPayloadPath)
        LogUtils.debugLog("  cleanFSPluginFolders  [][]  Cleaning folder \"$fsPayloadPath\"", log)
        folder = new File(fsPayloadPath)
        try{
            FileUtils.cleanDirectory(folder)
        }catch(Exception ex){
            log.error "Not possible to clean directory: "+fsPayloadPath
            log.error "encountered exception: "+ex
        }

        LogUtils.debugLog("  ====  \"cleanFSPluginFolders\" DONE.", log)
    }

}
