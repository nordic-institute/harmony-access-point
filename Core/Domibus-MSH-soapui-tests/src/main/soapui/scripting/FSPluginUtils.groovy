import org.apache.commons.io.FileUtils

import java.nio.file.Files
import java.nio.file.Paths

class FSPluginUtils {
    def context = null
    def log = null

    static def FS_DEF_MAP = [SENDER: 'domibus-blue',
                             P_TYPE: "urn:oasis:names:tc:ebcore:partyid-type:unregistered",
                             S_ROLE: "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator",
                             RECEIVER: "domibus-red",
                             R_ROLE: "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder",
                             AGR_TYPE: "DUM",
                             AGR: "DummyAgr",
                             SRV_TYPE:'(?<=<Service type=")([^"]+)(?=">)',
                             SRV: "bdx:noprocess",
                             ACTION: '(?<=<Action>)(.+)(?=</Action>)',
                             CID: "cid:message",
                             PAY_NAME: "PayloadName.xml",
                             MIME: "text/xml",
                             OR_SENDER: "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1",
                             FIN_RECEIVER: "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4",
                             PROC_TYPE: "PUSH",
                             MPC: '(?<=mpc=")([^"]+)(?=")']
    static def PATH_TO_FS_TEST_FILES = "/resources/PModesandKeystoresSpecialTests/fsPlugin/standard/"
    static def FS_PLUGIN_MESSAGE_LOCATION_PROPERTY_NAME = "fsplugin.messages.location"
    /**
     *
     * @param log
     * @param context
     */
    FSPluginUtils(log, context) {
        this.context = context
        this.log = log
    }

    // Extract the suitable template for metadata.xml file
    def static extractSuitableTemplate(log, context, testRunner, configuration) {
        def messageMetadata
        switch (configuration.toLowerCase()) {
            case "standard":
                messageMetadata = Domibus.getProjectCustProp("fsMetadataStandard", context, log, testRunner)
                break
            case "withmime":
                messageMetadata = Domibus.getProjectCustProp("fsMetadataWithMimeType", context, log, testRunner)
                break
            case "withpname":
                messageMetadata = Domibus.getProjectCustProp("fsMetadataWithPayloadName", context, log, testRunner)
                break
            case "withptype":
                messageMetadata = Domibus.getProjectCustProp("fsMetadataWithProcessingType", context, log, testRunner)
                break
            case "splitstandard":
                messageMetadata = Domibus.getProjectCustProp("splitMetadataStandard", context, log, testRunner)
                break
            default:
                log.warn "Unknown type of configuration: assume standard ..."
                messageMetadata = Domibus.getProjectCustProp("fsPluginPrototype", context, log, testRunner)
                break
        }
        return messageMetadata
    }

    // Get the path to the fsplugin sending location
    def static getPathToFsPluginSendingLocation(log, context, testRunner, side) {
        def fspluginPath
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
        return fspluginPath
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
     * @param inputFileName
     * @return
     */
    def static submitFSmessage(String side, context, log, testRunner, String configuration = "standard", String domain = "default",parametersMap = [], boolean twoFiles = true, String destSuffix="", String subFolder = "", String inputFileName = "Test_file.xml"){
        LogUtils.debugLog("  ====  Calling \"submitFSmessage\".", log)
        def messageMetadata = null
        def fspluginPath
        def source
        def dest
        def metadataFile

        /*def multitenancyOn = getMultitenancyFromSide(side, context, log)
        if(multitenancyOn){
            messageLocationPropertyName = domain + "." + FS_PLUGIN_MESSAGE_LOCATION_PROPERTY_NAME
        }*/
        messageMetadata = extractSuitableTemplate(log, context, testRunner, configuration)

        // Update the targeted values in the template
        parametersMap.each { entry ->
            messageMetadata = messageMetadata.replaceAll(FS_DEF_MAP[entry.key],entry.value)
        }

        fspluginPath = getPathToFsPluginSendingLocation(log, context, testRunner, side)

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
            source = Domibus.formatPathSlashes(context.expand('${projectDir}') + PATH_TO_FS_TEST_FILES + inputFileName)
            dest = fspluginPath + "Test_file" + destSuffix + ".xml"
            Domibus.copyFile(source,dest,log)

            // Copy a second file in case needed
            if(twoFiles){
                source = Domibus.formatPathSlashes(context.expand('${projectDir}') + PATH_TO_FS_TEST_FILES + "fileSmall.pdf")
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
     * Submit split and join message reusing submitFSmessage method
     * parametersMap keys must be: [SENDER:"...",RECEIVER:"...",AGR_TYPE:"...",AGR:"...",SRV_TYPE:"...",SRV:"...",ACTION:"...",CID:"...",PAY_NAME:"...",MIME:"...",OR_SENDER:"...",FIN_RECEIVER:"..."]
     * @param side
     * @param context
     * @param log
     * @param testRunner
     * @param inputFileName
     * @param parametersMap
     * @param configuration
     * @param domain
     * @param subFolder
     * @return
     */
    def static submitSplitAndJoinMessage(String side, context, log, testRunner, String inputFileName = "Test_file.xml", parametersMap = [], String configuration = "splitstandard", String domain = "default", String subFolder="") {
        def twoFiles = false
        def destSuffix = "_" + inputFileName.split(/\./)[0]
        submitFSmessage(side,
                context,
                log,
                testRunner,
                configuration,
                domain,
                parametersMap,
                twoFiles,
                destSuffix,
                subFolder,
                inputFileName)
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
        def messageLocationPropertyName = FS_PLUGIN_MESSAGE_LOCATION_PROPERTY_NAME

        /*def multitenancyOn = getMultitenancyFromSide(side, context, log)
        if(multitenancyOn){
            messageLocationPropertyName = domain + "." + FS_PLUGIN_MESSAGE_LOCATION_PROPERTY_NAME
        }*/


        fsPayloadPath = Domibus.getPropertyAtRuntime(side, messageLocationPropertyName, context, log, domain) + "/IN/" + finalRecipient + "/" + messageID + "/"
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
        def messageLocationPropertyName = FS_PLUGIN_MESSAGE_LOCATION_PROPERTY_NAME

        /*def multitenancyOn = getMultitenancyFromSide(side, context, log)
        if(multitenancyOn){
            messageLocationPropertyName = domain + "." + FS_PLUGIN_MESSAGE_LOCATION_PROPERTY_NAME
        }*/


        def fsPayloadPath = Domibus.getPropertyAtRuntime(side, messageLocationPropertyName, context, log, domain) + "/OUT"
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
        def messageLocationPropertyName = FS_PLUGIN_MESSAGE_LOCATION_PROPERTY_NAME

        /*def multitenancyOn = getMultitenancyFromSide(side, context, log)
        if(multitenancyOn){
            messageLocationPropertyName = domain + "." + FS_PLUGIN_MESSAGE_LOCATION_PROPERTY_NAME
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
            messageLocationPropertyName = domain + "." + FS_PLUGIN_MESSAGE_LOCATION_PROPERTY_NAME
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


    /**
     * Create file with random content and give size in fsPlugin test files folder
     * @param log
     * @param context
     * @param filesize
     * @param destSuffix
     * @return
     */
    def static createRandomFSFile(log, context, Integer filesize = 1024*1024, String inputFileName = "Test_file_split.xml") {
        LogUtils.debugLog("  ====  Calling \"createRandomFSFile\".", log)
        def source = Domibus.formatPathSlashes(context.expand('${projectDir}') + PATH_TO_FS_TEST_FILES + inputFileName)
        try {
            File dir = new File(source)

            byte[] bytes = new byte[filesize]
            Random rand = new Random()

            File file = new File(source)

            def fos = new FileOutputStream(file)
            def bos = new BufferedOutputStream(fos)

            rand.nextBytes(bytes)
            bos.write(bytes)

            bos.flush()
            bos.close()
            fos.flush()
            fos.close()
            LogUtils.debugLog("File ${source} create.", log)
        } catch (IOException e) {
                e.printStackTrace();
            }
        LogUtils.debugLog("  ====  \"cleanFSPluginFolders\" DONE.", log)
    }


    /**
     * Create file with random content and give size in fsPlugin test files folder - this approach is very fast
     * but file can be compressed hugely
     * @param log
     * @param context
     * @param filesize
     * @param destSuffix
     * @return
     */
    def static createRandomEmptyFSFile(log, context, Integer filesize = 1024*1024, String inputFileName = "Test_file_split.xml") {
        LogUtils.debugLog("  ====  Calling \"createRandomEmptyFSFile\".", log)
        def source = Domibus.formatPathSlashes(context.expand('${projectDir}') + PATH_TO_FS_TEST_FILES + inputFileName)
        try {
            RandomAccessFile f = new RandomAccessFile(source, "rw");
            f.setLength(filesize)
            f.close()
            LogUtils.debugLog("File ${source} create.", log)
        } catch (IOException e) {
            e.printStackTrace();
        }
        LogUtils.debugLog("  ====  \"createRandomEmptyFSFile\" DONE.", log)
    }
}
