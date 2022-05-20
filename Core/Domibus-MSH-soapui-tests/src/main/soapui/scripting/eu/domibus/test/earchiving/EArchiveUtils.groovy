package eu.domibus.test.earchiving

import eu.domibus.test.utils.DockerUtils
import eu.domibus.test.utils.DomibusSoapUIConstants
import eu.domibus.test.utils.LogUtils
import eu.domibus.test.utils.ShellUtils
import eu.domibus.test.utils.SoapUIPropertyUtils
import groovy.io.FileType
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import org.apache.log4j.Logger

import java.nio.file.Paths

class EArchiveUtils {
    def static LOG = Logger.getLogger(LogUtils.SOAPUI_LOGGER_NAME)

    def context = null


    EArchiveUtils(context) {
        this.context = context
    }

    // Class destructor
    void finalize() {
        LOG.debug "EArchiveUtils class not needed longer."
    }

    /**
     * Clean earchive folders. The cleaning is done by the internal script
     * located in:  eArchivingFolderPath+"/scripts/clean-script.sh"
     * @param eArchivingFolderPath - e-archive root path where domibus extract the batches
     * @param log - soapui log4j object
     * @return script result: [outputStream, errorStream]
     */
    def static resetArchiveFolders(String eArchivingFolderPath) {
        def commandString = [eArchivingFolderPath + "/scripts/clean-script.sh"]
        return ShellUtils.runCommandInShell(commandString)
    }

    /**
     * Return Batch json file
     * @param eArchivingFolderPath
     * @param batchId
     * @return
     */
    def static getBatchDataFolder(String eArchivingFolderPath, String batchId) {
        return Paths.get(eArchivingFolderPath, batchId,
                EArchiveConstants.FOLDER_REPRESENTATIONS,
                EArchiveConstants.FOLDER_REPRESENTATION1,
                EArchiveConstants.FOLDER_DATA).toFile()
    }

    /**
     * Return exported batch folder names -> exported messageIDs
     * @param eArchivingFolderPath
     * @param batchId
     * @return
     */
    def static getBatchExportedMessageFolders(String eArchivingFolderPath, String batchId) {
        def dataFolder = getBatchDataFolder(eArchivingFolderPath, batchId)
        def batchExportedMessageList = []
        dataFolder.eachFile(FileType.DIRECTORIES) { file ->
            batchExportedMessageList << file.getName()
        }
        return batchExportedMessageList;
    }

    /**
     * Return Batch json file
     * @param eArchivingFolderPath
     * @param batchId
     * @return
     */
    def static getBatchJsonFile(String eArchivingFolderPath, String batchId) {
        return Paths.get(eArchivingFolderPath, batchId,
                EArchiveConstants.FOLDER_REPRESENTATIONS,
                EArchiveConstants.FOLDER_REPRESENTATION1,
                EArchiveConstants.FOLDER_DATA,
                EArchiveConstants.BATCH_JSON_NAME).toFile()
    }

    /**
     * Return Batch JSON file
     * @param eArchivingFolderPath
     * @param batchId
     * @return
     */
    def static parseBatchJsonObject(File jsonFile) {
        return (new JsonSlurper()).parse(new FileReader(jsonFile))
    }

    /**
     * Return Batch METS file
     * @param eArchivingFolderPath
     * @param batchId
     * @return
     */
    def static parseBatchMETSObject(File xmlFile) {
        return (new XmlSlurper()).parse(new FileReader(xmlFile))
    }

    /**
     * Return Batch METS file
     * @param eArchivingFolderPath
     * @param batchId
     * @return
     */
    def static getBatchMETSFile(String eArchivingFolderPath, String batchId) {
        return Paths.get(eArchivingFolderPath, batchId,
                EArchiveConstants.BATCH_METS_NAME).toFile()
    }

    /**
     * print json file
     * @param json file
     * @return
     */
    def static printBatchJsonFile(def jsonContent, batchId) {
        LOG.info("  getBatchMetadataFromJson  [][]  ==========================================")
        LOG.info("  getBatchMetadataFromJson  [][]  Metadata for batch \"$batchId\"")
        LOG.info("  getBatchMetadataFromJson  [][]  ==========================================")
        LOG.info("  getBatchMetadataFromJson  [][]  version: " + jsonContent.version)
        LOG.info("  getBatchMetadataFromJson  [][]  requestType: " + jsonContent.requestType)
        LOG.info("  getBatchMetadataFromJson  [][]  status: " + jsonContent.status)
        LOG.info("  getBatchMetadataFromJson  [][]  errorCode: " + jsonContent.errorCode)
        LOG.info("  getBatchMetadataFromJson  [][]  errorDescription: " + jsonContent.errorDescription)
        LOG.info("  getBatchMetadataFromJson  [][]  timestamp: " + jsonContent.timestamp)
        LOG.info("  getBatchMetadataFromJson  [][]  messageStartId: " + jsonContent.messageStartId)
        LOG.info("  getBatchMetadataFromJson  [][]  messageEndId: " + jsonContent.messageEndId)
        LOG.info("  getBatchMetadataFromJson  [][]  manifestChecksum: " + jsonContent.manifestChecksum)
        LOG.info("  getBatchMetadataFromJson  [][]  messages: " + jsonContent.messages)
        LOG.info("  getBatchMetadataFromJson  [][]  ==========================================")
        LOG.info("  ====  \"getBatchMetadataFromJson\" DONE.")
    }


    /**
     *  Returns a list with all the batch IDs present in the configured eArchive directory
     * @param context
     * @param log
     * @param batchFolder
     * @return
     */
    def getBatchIDListFromArchiveFolder(String eArchivingFolderPath, context, log) {
        LogUtils.debugLog("  ====  Calling \"getBatchIDListFromArchiveFolder\" for folder:[" + eArchivingFolderPath + "]!", log)
        def batchList = []
        def eArchivingFolder = null

        // Get the eArchiving folder location
        eArchivingFolder = new File(eArchivingFolderPath)

        // Extract the list of batches present
        eArchivingFolder.eachDir {
            if (isBatchFolder(log, eArchivingFolder.toString() + "/" + it.name + "/representations")) {
                batchList << it.name
            }
        }
        LogUtils.debugLog("  getBatchList  [][]  batch List: " + batchList, log)
        LogUtils.debugLog("  ====  \"getBatchList\" DONE.", log)
        // Return batch list
        return batchList
    }


    /**
     *
     * Metadata is obtained by browsing the batch file "batch.json"
     **/
    /**
     *
     * @param eArchivingFolderPath
     * @param batchID
     * @param context
     * @param log
     * @return Returns batch.json the metadata of a batch identified by "batchID".
     */
    def getBatchMetadataFromJson(String eArchivingFolderPath, String batchID, context, log) {
        LogUtils.debugLog("  ====  Calling \"getBatchMetadataFromJson\" for batch id [" + batchID + "].", log)

        def appendPath = "/" + batchID + "/representations/representation1/data"
        def jsonFile = null
        def jsonSlurper = new JsonSlurper()
        // Get the eArchiving folder location
        def eArchivingFolder = new File(SoapUIPropertyUtils.formatPathSlashes(eArchivingFolderPath + appendPath))
        // Retrieve the batch.json file
        eArchivingFolder.eachFile {
            if (it.name.toLowerCase().equals("batch.json")) {
                jsonFile = it
            }
        }

        if (jsonFile == null) {
            LogUtils.debugLog("  getBatchMetadataFromJson  [][]  Error:getBatchMetadataFromJson: The file \"batch.json\" was not found !", log)
            return null;
        }

        def jsonContent = jsonSlurper.parse(new FileReader(jsonFile))
        assert (jsonContent != null), "Error:getBatchMetadataFromJson: Could not parse file \"batch.json\" !"

        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  ==========================================", log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  Metadata for batch \"$batchID\"", log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  ==========================================", log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  version: " + jsonContent.version, log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  requestType: " + jsonContent.requestType, log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  status: " + jsonContent.status, log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  errorCode: " + jsonContent.errorCode, log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  errorDescription: " + jsonContent.errorDescription, log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  timestamp: " + jsonContent.timestamp, log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  messageStartId: " + jsonContent.messageStartId, log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  messageEndId: " + jsonContent.messageEndId, log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  manifestChecksum: " + jsonContent.manifestChecksum, log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  messages: " + jsonContent.messages, log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  ==========================================", log)
        LogUtils.debugLog("  ====  \"getBatchMetadataFromJson\" DONE.", log)

        // Return Metadata
        return jsonContent
    }


    /**
     * Returns a list with all messages present with batch from json content.
     * @param jsonContent
     * @param context
     * @param log
     * @return list
     */
    def getMessageListFromJsonFile(def batchMetadata, String batchID, context, log) {
        LogUtils.debugLog("  ====  Calling \"getMessageListFromJsonFile\".", log)
        def messageList = batchMetadata.messages
        LogUtils.debugLog("  getMessageListFromJsonFile  [][]  Message list for batch \"$batchID\": " + messageList, log)
        LogUtils.debugLog("  ====  \"getMessageListFromJsonFile\" DONE.", log)
        // Return messageList list
        return messageList
    }

    /**
     * Returns the export status of a batch identified by "batchID".
     * The status is obtained by browsing the batch file "batch.json"
     **/
    def getBatchStatus(def batchMetadata, String batchID, context, log) {
        LogUtils.debugLog("  ====  Calling \"getBatchStatus\".", log)
        LogUtils.debugLog("  getBatchStatus  [][]  batch \"$batchID\" status: " + batchMetadata.status, log)
        LogUtils.debugLog("  ====  \"getBatchStatus\" DONE.", log)
        return batchMetadata.status
    }


    /**
     * Verifies if the folder present in the export directory is a batch folder or not (dummy folder)
     **/
    def isBatchFolder(log, folderPath) {
        LogUtils.debugLog("  ====  Calling \"isBatchFolder\".", log)
        def eArchiveFolder = null
        def outcome = true
        def formattedPath = SoapUIPropertyUtils.formatPathSlashes(folderPath)

        LogUtils.debugLog("  isBatchFolder  [][]  Checking: " + formattedPath, log)
        try {
            eArchiveFolder = new File(formattedPath)
            if (!eArchiveFolder.exists()) {
                outcome = false
            }
        } catch (Exception ex) {
            outcome = false
        }

        LogUtils.debugLog("  ====  \"isBatchFolder\" DONE.", log)
        return outcome

    }

    /**
     * Returns the data of a batch identified by "batchID".
     * Data is obtained by browsing the batch file "METS.xml"
     **/
    def getBatchDataFromMETS(String eArchivingFolderPath, String batchID, context, log) {
        LogUtils.debugLog("  ====  Calling \"getBatchDataFromMETS\".", log)

        def xmlFile = null
        def appendPath = "/" + batchID
        def metsFileContent = null
        def dataList = [:]
        def dataTemp = [:]

        // Get the eArchiving folder location
        def eArchivingFolder = new File(SoapUIPropertyUtils.formatPathSlashes(eArchivingFolderPath + appendPath))

        // Retrieve the METS.xml file
        eArchivingFolder.eachFile {
            if (it.name.toLowerCase().equals("mets.xml")) {
                xmlFile = it
            }
        }
        if (xmlFile == null) {
            LogUtils.debugLog("  getBatchDataFromMETS  [][]  Error:getBatchMetadataFromJson: The file \"METS.xml\" was not found !", log)
            return null
        }

        metsFileContent = new XmlSlurper().parse(new FileReader(xmlFile))

        metsFileContent.depthFirst().each {
            if (it.name().equals("file")) {
                if (!(it.FLocat.'@xlink:href'.text().contains("data/batch.json"))) {
                    dataTemp = it.FLocat.'@xlink:href'.text().split('/');
                    assert (dataTemp.size() == 3), "Error:getBatchDataFromMETS: Error parsing reference:" + it.FLocat.'@xlink:href'.text()
                    if (dataList[dataTemp[1]] == null) {
                        dataList[dataTemp[1]] = []
                    }
                    dataList[dataTemp[1]] << ["PAYLOADNAME": dataTemp[2], "MIMETYPE": it.'@MIMETYPE'.text(), "CREATED": it.'@CREATED'.text(), "SIZE": it.'@SIZE'.text()]
                    dataTemp = [:]
                }
            }
        }

        LogUtils.debugLog("  getBatchDataFromMETS  [][]  ==========================================", log)
        LogUtils.debugLog("  getBatchDataFromMETS  [][]  Data for batch \"$batchID\"", log)
        LogUtils.debugLog("  getBatchDataFromMETS  [][]  ==========================================", log)
        dataList.each { k, v -> LogUtils.debugLog("  getBatchDataFromMETS  [][]  ${k}:${v}", log) }
        LogUtils.debugLog("  getBatchDataFromMETS  [][]  ==========================================", log)
        LogUtils.debugLog("  ====  \"getBatchDataFromMETS\" DONE.", log)

        // Return Metadata
        return dataList
    }


// ----------------------------------------------
// old functions

    /**
     * Returns the full path to a targeted earchiving folder: it is built from the export folder configured in the domibus properties
     * "appendPath" parameter is used to point to the location desired in the export directory tree
     * If "appendPath" parameter is empty, it will return the path to the export folder (where the batch folders are present)
     **/
    def static getArchivingFolder(String side, context, log, String appendPath = "", String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"getArchivingFolder\".", log)

        def earStorLocaProp = "domibus.earchive.storage.location"
        def earStorLoca = null
        def eArchiveFolder = null

        // Get the eArchiving folder location
        earStorLoca = SoapUIPropertyUtils.getPropertyAtRuntime(side, earStorLocaProp, context, log, domainValue)
        assert (earStorLoca != null), "Error:getArchivingFolder: The extracted eArchiving storage location is null !"
        earStorLoca = SoapUIPropertyUtils.formatPathSlashes(earStorLoca + appendPath)
        LogUtils.debugLog("  getArchivingFolder  [][]  eArchiving folder path: " + earStorLoca, log)
        try {
            eArchiveFolder = new File(earStorLoca)
        } catch (Exception ex) {
            assert 0, "  getArchivingFolder  [][]  exception occurred: " + ex
        }
        assert (eArchiveFolder != null), "Error:getArchivingFolder: The eArchiving folder path is not correct (could not be converted to a folder) !"
        assert (eArchiveFolder.exists()), "Error:getArchivingFolder: The folder \"" + eArchiveFolder + "\" doesn't exist."

        LogUtils.debugLog("  ====  \"getArchivingFolder\" DONE.", log)

        // Return eArchiving folder
        return eArchiveFolder
    }


    /**
     * Returns a list with all the batch IDs present in the configured earchiving directory
     **/
    def static getBatchList(String side, context, log, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"getBatchList\".", log)

        def batchList = []
        def eArchivingFolder = null

        // Get the eArchiving folder location
        eArchivingFolder = getArchivingFolder(side, context, log, "", domainValue)

        // Extract the list of batches present
        eArchivingFolder.eachDir {
            if (isBatchFolder(log, eArchivingFolder.toString() + "/" + it.name + "/representations")) {
                batchList << it.name
            }
        }

        LogUtils.debugLog("  getBatchList  [][]  batch List: " + batchList, log)

        LogUtils.debugLog("  ====  \"getBatchList\" DONE.", log)

        // Return batch list
        return batchList
    }

    /**
     * Returns a list with all messages present with batch identified by "batchID".
     * List is obtained by browsing the messages files present in the batch directory
     **/
    def static getMessageListFromBatchDir(String side, context, log, String batchID, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"getMessageListFromBatchDir\".", log)

        def eArchivingFolder = null
        def appendPath = "/" + batchID + "/representations/representation1/data"
        def messageList = []

        // Get the eArchiving folder location
        eArchivingFolder = getArchivingFolder(side, context, log, appendPath, domainValue)

        // Extract the list of messages present
        eArchivingFolder.eachDir {
            messageList << it.name
        }

        LogUtils.debugLog("  getMessageListFromBatchDir  [][]  Message list for batch \"$batchID\": " + messageList, log)

        LogUtils.debugLog("  ====  \"getMessageListFromBatchDir\" DONE.", log)

        // Return messageList list
        return messageList
    }

    /**
     * Returns the metadata of a batch identified by "batchID".
     * Metadata is obtained by browsing the batch file "batch.json"
     **/
    def static getBatchMetadataFromJson(String side, context, log, String batchID, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"getBatchMetadataFromJson\".", log)

        def eArchivingFolder = null
        def appendPath = "/" + batchID + "/representations/representation1/data"
        def jsonFile = null
        def jsonContent = null
        def batchMetadata = [:]
        def jsonSlurper = new JsonSlurper()

        // Get the eArchiving folder location
        eArchivingFolder = getArchivingFolder(side, context, log, appendPath, domainValue)

        // Retrieve the batch.json file
        eArchivingFolder.eachFile {
            if (it.name.toLowerCase().equals("batch.json")) {
                jsonFile = it
            }
        }
        assert (jsonFile != null), "Error:getBatchMetadataFromJson: The file \"batch.json\" was not found !"

        jsonContent = jsonSlurper.parse(jsonFile)
        assert (jsonContent != null), "Error:getBatchMetadataFromJson: Could not parse file \"batch.json\" !"
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  ==========================================", log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  Metadata for batch \"$batchID\"", log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  ==========================================", log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  version: " + jsonContent.version, log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  requestType: " + jsonContent.requestType, log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  status: " + jsonContent.status, log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  errorCode: " + jsonContent.errorCode, log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  errorDescription: " + jsonContent.errorDescription, log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  timestamp: " + jsonContent.timestamp, log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  messageStartId: " + jsonContent.messageStartId, log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  messageEndId: " + jsonContent.messageEndId, log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  manifestChecksum: " + jsonContent.manifestChecksum, log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  messages: " + jsonContent.messages, log)
        LogUtils.debugLog("  getBatchMetadataFromJson  [][]  ==========================================", log)

        LogUtils.debugLog("  ====  \"getBatchMetadataFromJson\" DONE.", log)

        // Return Metadata
        return jsonContent
    }


    /**
     * Returns a list with all messages present with batch identified by "batchID".
     * List is obtained by browsing the "batch.json" file
     **/
    def static getMessageListFromJsonFile(String side, context, log, String batchID, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"getMessageListFromJsonFile\".", log)

        def batchMetadata = null
        def messageList = []

        batchMetadata = getBatchMetadataFromJson(side, context, log, batchID, domainValue)
        assert (batchMetadata != null), "Error:getMessageListFromJsonFile: Could not retrieve metadata for batch \"$batchID\" !"

        messageList = batchMetadata.messages
        LogUtils.debugLog("  getMessageListFromJsonFile  [][]  Message list for batch \"$batchID\": " + messageList, log)

        LogUtils.debugLog("  ====  \"getMessageListFromJsonFile\" DONE.", log)

        // Return messageList list
        return messageList
    }


    /**
     * Returns the export status of a batch identified by "batchID".
     * The status is obtained by browsing the batch file "batch.json"
     **/
    def static getBatchStatus(String side, context, log, String batchID, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"getBatchStatus\".", log)

        def batchMetadata = null


        batchMetadata = getBatchMetadataFromJson(side, context, log, batchID, domainValue)
        LogUtils.debugLog("  getBatchStatus  [][]  batch \"$batchID\" status: " + batchMetadata.status, log)
        LogUtils.debugLog("  ====  \"getBatchStatus\" DONE.", log)

        return batchMetadata.status
    }

    /**
     * Browses input list of batches and returns the list of the successfully exported ones.
     **/
    def static getBatchExportSuccessFromList(String side, context, log, batchIdList, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"getBatchExportSuccessFromList\".", log)

        def batchList = []
        def returnedStatus = null

        batchIdList.each {
            k ->
                returnedStatus = getBatchStatus(side, context, log, k, domainValue)
                if (returnedStatus.toLowerCase().equals(DomibusSoapUIConstants.BATCH_SUCCESS_STATUS.toLowerCase())) {
                    batchList << k
                }
        }
        LogUtils.debugLog("  getBatchExportSuccessFromList  [][]  Batch list with successfull status: " + batchList, log)
        LogUtils.debugLog("  ====  \"getBatchExportSuccessFromList\" DONE.", log)
        return batchList
    }


    /**
     * Returns the list of all the successfully exported batches present.
     **/
    def static getAllBatchExportSuccess(String side, context, log, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"getAllBatchExportSuccess\".", log)

        def batchList = []
        def returnedBatchList = []

        batchList = getBatchList(side, context, log, domainValue)
        returnedBatchList = getBatchExportSuccessFromList(side, context, log, batchList, domainValue)

        LogUtils.debugLog("  getAllBatchExportSuccess  [][]  Batch list with success status: " + returnedBatchList, log)
        LogUtils.debugLog("  ====  \"getAllBatchExportSuccess\" DONE.", log)
        return returnedBatchList
    }


    /**
     * Browses input list of batches and returns the list of the ones with failed export status.
     **/
    def static getBatchExportFailureFromList(String side, context, log, batchIdList, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"getBatchExportSuccessFromList\".", log)

        def batchList = []
        def returnedStatus = null

        batchIdList.each {
            k ->
                returnedStatus = getBatchStatus(side, context, log, k, domainValue)
                if (returnedStatus.toLowerCase().equals(DomibusSoapUIConstants.BATCH_FAILURE_STATUS.toLowerCase())) {
                    batchList << k
                }
        }
        LogUtils.debugLog("  getBatchExportSuccessFromList  [][]  Batch list with failed status: " + batchList, log)
        LogUtils.debugLog("  ====  \"getBatchExportSuccessFromList\" DONE.", log)
        return batchList
    }


    /**
     * Returns the list of all the batches present with failed export status.
     **/
    def static getAllBatchExportFailure(String side, context, log, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"getAllBatchExportFailure\".", log)

        def batchList = []
        def returnedBatchList = []

        batchList = getBatchList(side, context, log, domainValue)
        returnedBatchList = getBatchExportFailureFromList(side, context, log, batchList, domainValue)

        LogUtils.debugLog("  getAllBatchExportFailure  [][]  Batch list with failed status: " + returnedBatchList, log)
        LogUtils.debugLog("  ====  \"getAllBatchExportFailure\" DONE.", log)
        return returnedBatchList
    }


    /**
     * Returns the data of a batch identified by "batchID".
     * Data is obtained by browsing the batch file "METS.xml"
     **/
    def static getBatchDataFromMETS(String side, context, log, String batchID, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"getBatchDataFromMETS\".", log)

        def eArchivingFolder = null
        def xmlFile = null
        def appendPath = "/" + batchID
        def metsFileContent = null
        def dataList = [:]
        def dataTemp = [:]

        // Get the eArchiving folder location
        eArchivingFolder = getArchivingFolder(side, context, log, appendPath, domainValue)

        // Retrieve the METS.xml file
        eArchivingFolder.eachFile {
            if (it.name.toLowerCase().equals("mets.xml")) {
                xmlFile = it
            }
        }
        assert (xmlFile != null), "Error:getBatchDataFromMETS: The file \"METS.xml\" was not found !"

        metsFileContent = new XmlSlurper().parse(xmlFile)
        metsFileContent.depthFirst().each {
            if (it.name().equals("file")) {
                if (!(it.FLocat.'@xlink:href'.text().contains("data/batch.json"))) {
                    dataTemp = it.FLocat.'@xlink:href'.text().split('/');
                    assert (dataTemp.size() == 3), "Error:getBatchDataFromMETS: Error parsing reference:" + it.FLocat.'@xlink:href'.text()
                    if (dataList[dataTemp[1]] == null) {
                        dataList[dataTemp[1]] = []
                    }
                    dataList[dataTemp[1]] << ["PAYLOADNAME": dataTemp[2], "MIMETYPE": it.'@MIMETYPE'.text(), "CREATED": it.'@CREATED'.text(), "SIZE": it.'@SIZE'.text()]
                    dataTemp = [:]
                }
            }
        }

        LogUtils.debugLog("  getBatchDataFromMETS  [][]  ==========================================", log)
        LogUtils.debugLog("  getBatchDataFromMETS  [][]  Data for batch \"$batchID\"", log)
        LogUtils.debugLog("  getBatchDataFromMETS  [][]  ==========================================", log)
        dataList.each { k, v -> LogUtils.debugLog("  getBatchDataFromMETS  [][]  ${k}:${v}", log) }
        LogUtils.debugLog("  getBatchDataFromMETS  [][]  ==========================================", log)
        LogUtils.debugLog("  ====  \"getBatchDataFromMETS\" DONE.", log)

        // Return Metadata
        return dataList
    }


    /**
     * Returns a list with all messages present with batch identified by "batchID".
     * List is obtained by browsing the "METS.xml" file
     **/
    def static getMessageListFromMetsFile(String side, context, log, String batchID, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"getMessageListFromMetsFile\".", log)

        def batchData = null
        def messageList = []

        batchData = getBatchDataFromMETS(side, context, log, batchID, domainValue)
        assert (batchData != null), "Error:getMessageListFromMetsFile: Could not retrieve data for batch \"$batchID\" !"

        batchData.each {
            k, v -> messageList << k
        }
        LogUtils.debugLog("  getMessageListFromMetsFile  [][]  Message list for batch \"$batchID\": " + messageList, log)
        LogUtils.debugLog("  ====  \"getMessageListFromMetsFile\" DONE.", log)

        // Return messageList list
        return messageList
    }

    /**
     * Returns a list with all messages present within an input batch list.
     * The message list will be obtained from the messages directories names.
     * If parameter "removeDuplicates" is set to true, all duplicate messages IDs are removed
     * If parameter "validate" is set to true, the messages list is validated against the lists obtained from "METS.xml" and "batch.json" files
     **/
    def static getAllMessagesInBatchList(String side, context, log, batchIdList, removeDuplicates = true, validate = false, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"getAllMessagesInBatchList\".", log)

        def messageList = []
        def messageListJson = []
        def messageListMets = []

        batchIdList.each {
            k -> messageList.addAll(getMessageListFromBatchDir(side, context, log, k, domainValue))
        }
        LogUtils.debugLog("  getAllMessagesInBatchList  [][]  Message list retrieved from batch dirs: " + messageList, log)
        if (validate) {
            batchIdList.each {
                k -> messageListJson.addAll(getMessageListFromJsonFile(side, context, log, k, domainValue))
            }
            batchIdList.each {
                k -> messageListMets.addAll(getMessageListFromMetsFile(side, context, log, k, domainValue))
            }
            LogUtils.debugLog("  getAllMessagesInBatchList  [][]  Message list retrieved from json files: " + messageListJson, log)
            LogUtils.debugLog("  getAllMessagesInBatchList  [][]  Message list retrieved from mets files: " + messageListMets, log)
            assert ((messageList as Set == messageListJson as Set) && (messageList as Set == messageListMets as Set)), "Error:getAllMessagesInBatchList: Messages retrieved with different method are not equal ! "
        }
        if (removeDuplicates) {
            LogUtils.debugLog("  getAllMessagesInBatchList  [][]  Removing duplicate messages in case present ...", log)
            messageList.unique()
            LogUtils.debugLog("  getAllMessagesInBatchList  [][]  Message list after removing duplicates: " + messageList, log)
        }
        LogUtils.debugLog("  ====  \"getAllMessagesInBatchList\" DONE.", log)

        // Return messageList list
        return messageList
    }


    /**
     * Returns a list with all messages present in the export folder.
     * The message list will be obtained from the messages directories names.
     * If parameter "removeDuplicates" is set to true, all duplicate messages IDs are removed
     * If parameter "validate" is set to true, the messages list is validated against the lists obtained from "METS.xml" and "batch.json" files
     **/
    def static getAllExportedMessages(String side, context, log, removeDuplicates = true, validate = false, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"getAllExportedMessages\".", log)

        def batchList = []
        def messageList = []
        batchList = getBatchList(side, context, log, domainValue)
        messageList = getAllMessagesInBatchList(side, context, log, batchList, removeDuplicates, validate, domainValue)
        LogUtils.debugLog("  getAllExportedMessages  [][]  Message list of all exported messages: " + messageList, log)
        LogUtils.debugLog("  ====  \"getAllExportedMessages\" DONE.", log)

        // Return messageList list
        return messageList
    }


    /**
     * Returns a list of all the messages belonging to a successfully exported batch from a list a batches.
     * The message list will be obtained from the messages directories names.
     * If parameter "removeDuplicates" is set to true, all duplicate messages IDs are removed
     * If parameter "validate" is set to true, the messages list is validated against the lists obtained from "METS.xml" and "batch.json" files
     **/
    def static getMessagesExportedWithSuccess(String side, context, log, batchIdList, removeDuplicates = true, validate = false, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"getMessagesExportedWithSuccess\".", log)

        def batchList = []
        def messageList = []
        batchList = getBatchExportSuccessFromList(side, context, log, batchIdList, domainValue)
        messageList = getAllMessagesInBatchList(side, context, log, batchList, removeDuplicates, validate, domainValue)
        LogUtils.debugLog("  getMessagesExportedWithSuccess  [][]  Message list of messages exported with success: " + messageList, log)
        LogUtils.debugLog("  ====  \"getMessagesExportedWithSuccess\" DONE.", log)

        // Return messageList list
        return messageList
    }


    /**
     * Returns a list of all the successfully exported messages present in the export folder.
     * The message list will be obtained from the messages directories names.
     * If parameter "removeDuplicates" is set to true, all duplicate messages IDs are removed
     * If parameter "validate" is set to true, the messages list is validated against the lists obtained from "METS.xml" and "batch.json" files
     **/
    def static getAllMessagesExportedWithSuccess(String side, context, log, removeDuplicates = true, validate = false, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"getAllMessagesExportedWithSuccess\".", log)

        def batchList = []
        def messageList = []
        batchList = getAllBatchExportSuccess(side, context, log, domainValue)
        messageList = getAllMessagesInBatchList(side, context, log, batchList, removeDuplicates, validate, domainValue)
        LogUtils.debugLog("  getAllMessagesExportedWithSuccess  [][]  Message list of all messages exported with success: " + messageList, log)
        LogUtils.debugLog("  ====  \"getAllMessagesExportedWithSuccess\" DONE.", log)

        // Return messageList list
        return messageList
    }


    /**
     * Returns a list of all the messages, belonging to a failed batches, present in a batch list.
     * The message list will be obtained from the messages directories names.
     * If parameter "removeDuplicates" is set to true, all duplicate messages IDs are removed
     * If parameter "validate" is set to true, the messages list is validated against the lists obtained from "METS.xml" and "batch.json" files
     **/
    def static getMessagesExportedWithFailure(String side, context, log, batchIdList, removeDuplicates = true, validate = false, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"getMessagesExportedWithFailure\".", log)

        def batchList = []
        def messageList = []
        batchList = getBatchExportFailureFromList(side, context, log, batchIdList, domainValue)
        messageList = getAllMessagesInBatchList(side, context, log, batchList, removeDuplicates, validate, domainValue)
        LogUtils.debugLog("  getMessagesExportedWithFailure  [][]  Message list of messages exported with failure: " + messageList, log)
        LogUtils.debugLog("  ====  \"getMessagesExportedWithFailure\" DONE.", log)

        // Return messageList list
        return messageList
    }


    /**
     * Returns a list of all the messages, belonging to a failed batches, present in the export folder.
     * The message list will be obtained from the messages directories names.
     * If parameter "removeDuplicates" is set to true, all duplicate messages IDs are removed
     * If parameter "validate" is set to true, the messages list is validated against the lists obtained from "METS.xml" and "batch.json" files
     **/
    def static getAllMessagesExportedWithFailure(String side, context, log, removeDuplicates = true, validate = false, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"getAllMessagesExportedWithFailure\".", log)

        def batchList = []
        def messageList = []
        batchList = getAllBatchExportFailure(side, context, log, domainValue)
        messageList = getAllMessagesInBatchList(side, context, log, batchList, removeDuplicates, validate, domainValue)
        LogUtils.debugLog("  getAllMessagesExportedWithFailure  [][]  Message list of all messages exported with failure: " + messageList, log)
        LogUtils.debugLog("  ====  \"getAllMessagesExportedWithFailure\" DONE.", log)

        // Return messageList list
        return messageList
    }

    /**
     * Returns number of number of messages exported in batch defined by "batchID".
     * The message list will be obtained from the batch file "batch.json".
     **/
    def static countMessagesInBatchFromJson(String side, context, log, String batchID, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"countMessagesInBatchFromJson\".", log)
        def messageList = []
        def count = 0
        messageList = getMessageListFromJsonFile(side, context, log, batchID, domainValue)
        count = messageList.size
        LogUtils.debugLog("  countMessagesInBatchFromJson  [][]  Number of messages for batch \"$batchID\": " + count, log)
        LogUtils.debugLog("  ====  \"countMessagesInBatchFromJson\" DONE.", log)
        return count
    }


    /**
     * Returns number of number of messages exported in batch defined by "batchID".
     * The message list will be obtained from the batch file "METS.xml".
     **/
    def static countMessagesInBatchFromMets(String side, context, log, String batchID, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"countMessagesInBatchFromMets\".", log)
        def messageList = []
        def count = 0
        messageList = getMessageListFromMetsFile(side, context, log, batchID, domainValue)
        count = messageList.size
        LogUtils.debugLog("  countMessagesInBatchFromMets  [][]  Number of messages for batch \"$batchID\": " + count, log)
        LogUtils.debugLog("  ====  \"countMessagesInBatchFromMets\" DONE.", log)
        return count
    }


    /**
     * Returns number of messages exported in batch defined by "batchID".
     * The message list will be obtained from the messages directories names.
     * If parameter "validate" is set to true, the count is validated against the data obtained from "METS.xml" and "batch.json" files
     **/
    def static countMessagesInBatch(String side, context, log, String batchID, validate = false, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"countMessagesInBatch\".", log)
        def messageList = []
        def countFromDir = 0
        def countFromJson = 0
        def countFromMetz = 0

        messageList = getMessageListFromBatchDir(side, context, log, batchID, domainValue)
        countFromDir = messageList.size

        if (validate) {
            countFromJson = countMessagesInBatchFromJson(side, context, log, batchID, domainValue)
            countFromMetz = countMessagesInBatchFromMets(side, context, log, batchID, domainValue)
            assert ((countFromDir + countFromJson + countFromMetz) == (3 * countFromDir)), "Error:countMessagesInBatch: BATCH \"$batchID\" Messages counted from batch directory is \"$countFromDir\", messages counted from json file is \"$countFromJson\", messages counted from mets file is \"$countFromMetz\" "
        }

        LogUtils.debugLog("  countMessagesInBatch  [][]  Number of messages for batch \"$batchID\": " + countFromDir, log)
        LogUtils.debugLog("  ====  \"countMessagesInBatch\" DONE.", log)
        return countFromDir
    }


    /**
     * Returns number of all exported messages.
     * The message list will be obtained from the messages directories names.
     * If parameter "validate" is set to true, the count is validated against the data obtained from "METS.xml" and "batch.json" files
     **/
    def static countAllExportedMessages(String side, context, log, removeDuplicates = true, validate = false, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"countAllExportedMessages\".", log)

        def countFromDir = 0
        def messageList = []

        messageList = getAllExportedMessages(side, context, log, removeDuplicates, validate, domainValue)
        countFromDir = messageList.size

        LogUtils.debugLog("  countAllExportedMessages  [][]  Number of all exported messages: " + countFromDir, log)
        LogUtils.debugLog("  ====  \"countAllExportedMessages\" DONE.", log)

        return countFromDir
    }

    /**
     * Returns number of all messages exported with success.
     * The message list will be obtained from the messages directories names.
     * If parameter "validate" is set to true, the count is validated against the data obtained from "METS.xml" and "batch.json" files
     **/
    def static countAllExportedMessagesSuccess(String side, context, log, removeDuplicates = true, validate = false, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"countAllExportedMessagesSuccess\".", log)

        def countFromDir = 0
        def messageList = []

        messageList = getAllMessagesExportedWithSuccess(side, context, log, removeDuplicates, validate, domainValue)
        countFromDir = messageList.size

        LogUtils.debugLog("  countAllExportedMessagesSuccess  [][]  Number of all messages exported with success: " + countFromDir, log)
        LogUtils.debugLog("  ====  \"countAllExportedMessagesSuccess\" DONE.", log)

        return countFromDir
    }


    /**
     * Returns number of all messages exported with success from a batch list.
     * The message list will be obtained from the messages directories names.
     * If parameter "validate" is set to true, the count is validated against the data obtained from "METS.xml" and "batch.json" files
     **/
    def static countMessagesExpSuccessInBatchList(String side, context, log, batchIdList, removeDuplicates = true, validate = false, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"countMessagesExpSuccessInBatchList\".", log)

        def countFromDir = 0
        def messageList = []

        messageList = getMessagesExportedWithSuccess(side, context, log, batchIdList, removeDuplicates, validate, domainValue)
        countFromDir = messageList.size

        LogUtils.debugLog("  countMessagesExpSuccessInBatchList  [][]  Number of all messages exported with success within the batch list: " + countFromDir, log)
        LogUtils.debugLog("  ====  \"countMessagesExpSuccessInBatchList\" DONE.", log)

        return countFromDir
    }


    /**
     * Returns number of all messages exported with failure.
     * The message list will be obtained from the messages directories names.
     * If parameter "validate" is set to true, the count is validated against the data obtained from "METS.xml" and "batch.json" files
     **/
    def static countAllExportedMessagesFailure(String side, context, log, removeDuplicates = true, validate = false, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"countAllExportedMessagesFailure\".", log)

        def countFromDir = 0
        def messageList = []

        messageList = getAllMessagesExportedWithFailure(side, context, log, removeDuplicates, validate, domainValue)
        countFromDir = messageList.size

        LogUtils.debugLog("  countAllExportedMessagesFailure  [][]  Number of all messages exported with failure: " + countFromDir, log)
        LogUtils.debugLog("  ====  \"countAllExportedMessagesFailure\" DONE.", log)

        return countFromDir
    }


    /**
     * Returns number of all messages exported with failure from a batch list.
     * The message list will be obtained from the messages directories names.
     * If parameter "validate" is set to true, the count is validated against the data obtained from "METS.xml" and "batch.json" files
     **/
    def static countMessagesExpFailureInBatchList(String side, context, log, batchIdList, removeDuplicates = true, validate = false, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"countMessagesExpFailureInBatchList\".", log)

        def countFromDir = 0
        def messageList = []

        messageList = getMessagesExportedWithFailure(side, context, log, batchIdList, removeDuplicates, validate, domainValue)
        countFromDir = messageList.size

        LogUtils.debugLog("  countMessagesExpFailureInBatchList  [][]  Number of all messages exported with failure within the batch list: " + countFromDir, log)
        LogUtils.debugLog("  ====  \"countMessagesExpFailureInBatchList\" DONE.", log)

        return countFromDir
    }


    /**
     * Returns number of all messages within a batch list.
     * The message list will be obtained from the messages directories names.
     * If parameter "validate" is set to true, the count is validated against the data obtained from "METS.xml" and "batch.json" files
     **/
    def static countAllMessagesInBatchList(String side, context, log, batchIdList, removeDuplicates = true, validate = false, String domainValue = "default") {
        LogUtils.debugLog("  ====  Calling \"countAllMessagesInBatchList\".", log)

        def countFromDir = 0
        def messageList = []

        messageList = getAllMessagesInBatchList(side, context, log, batchIdList, removeDuplicates, validate, domainValue)
        countFromDir = messageList.size

        LogUtils.debugLog("  countAllMessagesInBatchList  [][]  Number of all messages within the batch list: " + countFromDir, log)
        LogUtils.debugLog("  ====  \"countAllMessagesInBatchList\" DONE.", log)

        return countFromDir
    }

}