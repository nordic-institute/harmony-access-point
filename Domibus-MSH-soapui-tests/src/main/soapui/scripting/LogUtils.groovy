
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import com.google.common.io.BaseEncoding
import java.security.MessageDigest

/**
 * This is utility class for Logging
 *
 */
class LogUtils {

	def context = null
    def log = null

    LogUtils(log, context) {
        this.context = context
		this.log = log
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
// Print logs in debug level
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    /**
     * Method check the log level and logs the message
     * @param logMsg - message to be logged
     * @param log - logger objects
     * @param logLevel - if log level is 1 or true, then message is logged with info level, else message is ignored
     */
    static void debugLog(logMsg, log, logLevel = DomibusConstants.DEFAULT_LOG_LEVEL) {
        if (logLevel.toString() == "1" || logLevel.toString() == "true")
            log.info(logMsg)
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
// Evaluate the outcome of the search results
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
	static void evaluateSearchOutcome(searchResultMap,logValueList,checkPresent,testRunner,log){
		debugLog("  ====  Calling \"evaluateSearchOutcome\".", log)
		def foundTotalNumber=0
		logValueList.each{
			value ->
			if(searchResultMap[hashValue(value)]==1){
				log.info "  evaluateSearchOutcome  [][]  String \"$value\" was found in the log file(s)..."
			}else{
				log.info "  evaluateSearchOutcome  [][]  String \"$value\" was not found in the log file(s)..."
			}
			foundTotalNumber=foundTotalNumber+searchResultMap[hashValue(value)]
		}
        if(checkPresent){
            if (foundTotalNumber != logValueList.size())
                testRunner.fail(" evaluateSearchOutcome  [][]  Searching log file failed: ${foundTotalNumber} from ${logValueList.size()} entries found.")
            else
                log.info " evaluateSearchOutcome  [][]  All ${logValueList.size()} entries were found in log file."
        }
        else{
            if (foundTotalNumber != 0)
                testRunner.fail(" checkLogFile  [][]  Searching log file failed: ${foundTotalNumber} from ${logValueList.size()} entries were found.")
            else
                log.info " checkLogFile  [][]  All ${logValueList.size()} entries were not found in log file."
        }
		debugLog("  ====  \"evaluateSearchOutcome\" DONE.", log)		
	}
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
// Hash String value
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    static String hashValue(String stringToHash, String algoName="MD5", boolean toBase32=false){
        def String result = null;
        if(toBase32){
            BaseEncoding base32 = BaseEncoding.base32().omitPadding();
            result=	base32.encode(MessageDigest.getInstance(algoName).digest(stringToHash.toLowerCase(Locale.US).bytes))
        }
        else{
            result = MessageDigest.getInstance(algoName).digest(stringToHash.toLowerCase(Locale.US).bytes).encodeHex().toString()
        }
        return result;
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
// Check if a folder contains a log file
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII 	
    static isLogFolder(String folderPath,log) {
        debugLog("  ====  Calling \"isLogFolder\".", log)
        def logFolder = null
        def outcome=true
        def formattedPath = Domibus.formatPathSlashes(folderPath)
		
		if(folderPath.contains("Admin") || folderPath.contains("admin")){
			return false
		}

        debugLog("  isLogFolder  [][]  Checking: " + formattedPath, log)
        try {
            logFolder=new File(formattedPath)
            if (!logFolder.exists()) {
                outcome = false
            }
        } catch (Exception ex) {
            outcome = false
        }

        debugLog("  ====  \"isLogFolder\" DONE.", log)
        return outcome

    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
// Return path to domibus folder
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII 
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
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
// Check the number of lines to skip in the log file
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII 	
    static  String returnNumberOfLinesToSkip(side, pathToLogFile, log, context, testRunner){

		def testFile=null 
        // Check file exists
		try{
			testFile = new File(pathToLogFile)
		}catch (Exception ex) {
            log.error "  returnNumberOfLinesToSkip  [][]  Not possible to open file"+pathToLogFile
            assert 0,"Exception occurred: " + ex
        }
        if (!testFile.exists()) {
            testRunner.fail("File [${pathToLogFile}] does not exist. Can't check logs.")
            return
        } else debugLog("  returnNumberOfLinesToSkip  [][]  File [${pathToLogFile}] exists.", log)

        def lineCount = 0
        testFile.eachLine { lineCount++}
        debugLog(" returnNumberOfLinesToSkip  [][]  Line count for $pathToLogFile = " + lineCount, log)
		
		return lineCount.toString()
	
	}
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
// Check the number of lines to skip in all the domibus log files
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII 	
    static void  checkNumberOfLinesToSkipInLogFile(side, logFileToCheck, log, context, testRunner,domainValue = "default",String authUser = null, authPwd = null){
        debugLog("  ====  Calling \"checkNumberOfLinesToSkipInLogFile\".", log)
        // Before checking that some action generate specific log entry method store information how many lines already in log to not search tested log entry in old logs
		def pathToLogFile=null
		def pathToDir=null
		def cluster=false
		def lineCount=0
		def formattedPath=null
		def mainLogFolder=null
		def dirList=[]
		def logLineNumbMap=[:]
		
		// Check if the domibus deployment is clustered or not		
		cluster=Domibus.isClustered(side,context,log, domainValue, authUser, authPwd)

		// Get the path to the logs main directory
		pathToDir=pathToLogFiles(side, log, context)

		// In case of clustered deployment, store all the managed servers logs directories in a list
		if(cluster){
			formattedPath=Domibus.formatPathSlashes(pathToDir)
			mainLogFolder=new File(formattedPath)
			mainLogFolder.eachDir {
				if (isLogFolder(pathToDir.toString() + it.name.toString() + "/logs",log)) {
					dirList << it.name
				}
			}		
		}

		// In case of a cluster deployment, store the number of lines to skip for each managed server in a map 
		dirList.each{
			val ->
			pathToLogFile=pathToDir+val+"/logs/" + logFileToCheck
			lineCount=returnNumberOfLinesToSkip(side, pathToLogFile, log, context, testRunner)	
			logLineNumbMap[val]=lineCount
		}

		// In case of a non cluster deployment, store the number of lines to skip in the soapUI testcase custom property "skipNumberOfLines"
		// In case of a cluster deployment, store the map of [directory_name:number_of_lines_to_skip] in the soapUI testcase custom property "skipNumberOfLines"
		if(dirList.size==0){			
			pathToLogFile=pathToDir + logFileToCheck
			lineCount=returnNumberOfLinesToSkip(side, pathToLogFile, log, context, testRunner)	
			testRunner.testCase.setPropertyValue( "skipNumberOfLines", lineCount)
		}else{
			testRunner.testCase.setPropertyValue( "skipNumberOfLines", new JsonBuilder(logLineNumbMap).toPrettyString())
		}
		debugLog("  ====  \"checkNumberOfLinesToSkipInLogFile\" DONE.", log)
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
// Check if a string/list os strings is present in the log file
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII 
    static  checkSingleLogFile(side, pathToLogFile, logValueList, linesToSkip="", log, context, testRunner){
        debugLog("  ====  Calling \"checkSingleLogFile\".", log)
		
        def testFile=null		
		def searchResultMap=[:]
        def skipNumberOfLines = linesToSkip
		
		// Get the line number from which the search must start
        if (skipNumberOfLines == "") {
            log.info "  checkSingleLogFile  [][]  skipNumberOfLines property not defined on the test case level would start to search on first line"
            skipNumberOfLines = 0
        } else{
            skipNumberOfLines = skipNumberOfLines.toInteger()
		}

        // Check file exists
		try{
			testFile = new File(pathToLogFile)
		}catch (Exception ex) {
            log.error "  checkSingleLogFile  [][]  Not possible to open file"+pathToLogFile
            assert 0,"Exception occurred: " + ex
        }
        if (!testFile.exists()) {
            testRunner.fail("File [${pathToLogFile}] does not exist. Can't check logs.")
            return
        }else{ 
			debugLog("  checkSingleLogFile  [][]  File [${pathToLogFile}] exists.",log)
		}
		
        def foundTotalNumber = 0
        def fileContent = testFile.text
        log.info " checkSingleLogFile  [][]  would skip ${skipNumberOfLines} lines"
		
		// Get the log file number of lines
        def logSizeInLines = fileContent.readLines().size()
		
		// Check the number of lines to skip against the total number of lines in the file 
        if (logSizeInLines < skipNumberOfLines) {
            log.error "Incorrect number of line to skip: it is higher than number of lines in log file (" + logSizeInLines + "). Fail the test."
            assert 0,"  checkSingleLogFile  [][]  Incorrect number of line to skip: it is higher than number of lines in log file (" + logSizeInLines + "). Failing the test."
        }

		// Check each log entry: in case found, about its status in searchResultMap
        for(logEntryToFind  in logValueList){
            testFile.eachLine{
                line, lineNumber ->
                    //lineNumber++
                    if (lineNumber > skipNumberOfLines) {
                        if(line =~ logEntryToFind) {
                            debugLog("  checkSingleLogFile  [][]  In log line $lineNumber searched entry was found. Line value is: $line",log)
							searchResultMap[hashValue(logEntryToFind)]=1
                        }
                    }
            }
		}
		return searchResultMap
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
// Check if a string/list os strings is present in all Domibus log files
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII 	
    static void  checkLogFile(side, logFileToCheck, logValueList, log, context, testRunner,checkPresent = true,domainValue = "default",String authUser = null, authPwd = null){
        debugLog("  ====  Calling \"checkLogFile\".", log)
        // Check that logs file contains specific entries specified in list logValueList
        // to set number of lines to skip configuration use method restoreDomibusPropertiesFromBackup(domibusPath,  log, context, testRunner)
		def pathToLogFile=null
		def pathToDir=null
		def logDirMap=[:]
		def resultMap=[:]
		def searchResultMap=[:]
		def clustered=false
		def skipNumberOfLines = context.expand('${#TestCase#skipNumberOfLines}')
		
		// Check if the domibus deployment is clustered or not
		clustered=Domibus.isClustered(side,context,log, domainValue, authUser, authPwd)

		// Get the path to the logs main directory
		pathToDir=pathToLogFiles(side, log, context)

		// Initialize all the strings to search for in the logs as not found (using hash value to avoid very long indexes ...)
		logValueList.each{
			val ->
			searchResultMap[hashValue(val)]=0
		}
		
		// In case clustered, search for logs in all the managed servers folders 
		if(clustered){
			logDirMap=new JsonSlurper().parseText(skipNumberOfLines)
			logDirMap.each{
				k,v ->
				pathToLogFile = pathToDir+ k + "/logs/" + logFileToCheck
				resultMap=checkSingleLogFile(side, pathToLogFile, logValueList, v, log, context, testRunner)
				resultMap.each{
					index,value ->
					if(value==1){
							searchResultMap[index]=1
					}					
				}
			}			
		}else{
			// Non clustered deployment: look in 1 log file in the main directory
			pathToLogFile=pathToDir + logFileToCheck
			resultMap=checkSingleLogFile(side, pathToLogFile, logValueList, skipNumberOfLines, log, context, testRunner)
			resultMap.each{
				index,value ->
				if(value==1){
					searchResultMap[index]=1
				}					
			}
		}
		
		debugLog("  checkLogFile  [][]  After searching: searchResultMap="+searchResultMap.inspect(),log)
		
		// Display the search results and fail the test if needed
		evaluateSearchOutcome(searchResultMap,logValueList,checkPresent,testRunner,log)
    }
}