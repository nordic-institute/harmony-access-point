

class ExtRestUtils {
	def context = null
	def log = null


    ExtRestUtils(context) {
        this.context = context
		this.log = log
    }

    // Class destructor
    void finalize() {
		LogUtils.debugLog("  ====  ExtRestUtils class not needed anymore.\".", log)
    }

//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Extract and log HTTP response code
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    /**
     * Extract and log HTTP response code
     * @param response
     * @return
     */	
    def static printHttpResponseCode(log,response){	
        LogUtils.debugLog("  ====  Calling \"printHttpResponseCode\".",log)		
		def responseCode=null
		try{
			responseCode=(response[1] =~ /HTTP\/1.1 (...)/)[0][0]
			log.info "  printHttpResponseCode  [][]  Rest request response code is: $responseCode"
		} catch (Exception ex) {
            log.error "  printHttpResponseCode  [][]  Could not extract the http response code" + ex
        }
		
		LogUtils.debugLog("  ====  \"printHttpResponseCode\" DONE.",log)	
    }
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Assert rest request result
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII	
    /**
     * Assert command result
     * @param response
     * @param expected
     * @return
     */	
    def static assertRestRequestOutcome(log,response, String expected, String message=null){	
        LogUtils.debugLog("  ====  Calling \"assertRestRequestOutcome\".",log)
		log.info "  assertRestRequestOutcome  [][]  expected response is \"$expected\"."
		LogUtils.debugLog("  assertRestRequestOutcome  [][]  ------------------------------------------", log)
		LogUtils.debugLog("  assertRestRequestOutcome  [][]  response[0] ="+response[0], log)
		LogUtils.debugLog("  assertRestRequestOutcome  [][]  ------------------------------------------", log)
		LogUtils.debugLog("  assertRestRequestOutcome  [][]  response[1] ="+response[1], log)
		LogUtils.debugLog("  assertRestRequestOutcome  [][]  ------------------------------------------", log)
		printHttpResponseCode(log,response)

        switch(expected.toLowerCase()){
			case "success":
                assert(response[1].contains("HTTP/1.1 200")||response[1].contains("HTTP/1.1 201")),"Rest request did not succeed: Response does not contain HTTP 200 or 201 codes"+" | "+response[1]+" | "+response[0]+" | "+response[2]
                break
            case "unauthorized":
                assert(response[1].contains("HTTP/1.1 403")||response[1].contains("HTTP/1.1 401")),"Rest request was not  unauthorized: Response does not contain HTTP 401 or 403 code"+" | "+response[1]
                break
            case "notfound":
                assert(response[1].contains("HTTP/1.1 404")),"Response does not contain HTTP 500 code"+" | "+response[1]
				if(message!=null){
					assert(response[0].contains(message)),"\"$message\" string not found in response "+response[0]
				}
                break
			case "formaterror":
                assert(response[1].contains("HTTP/1.1 500")),"Response does not contain HTTP 500 code"+" | "+response[1]
				if(message!=null){
					assert(response[0].contains(message)),"\"$message\" string not found in response "+response[0]
				}
                break
			case "internalerror":
                assert(response[1].contains("HTTP/1.1 500")),"Response does not contain HTTP 500 code"+" | "+response[1]
				if(message!=null){
					assert(response[0].contains(message)),"\"$message\" string not found in response "+response[0]
				}
                break	
			case "badrequest":
                assert(response[1].contains("HTTP/1.1 400")),"Response does not contain HTTP 400 code"+" | "+response[1]
				if(message!=null){
					assert(response[0].contains(message)),"\"$message\" string not found in response "+response[0]
				}
                break				
            default:
                assert(false), "Unknown expected status: $expected"+" | "+response[1]
		}
		
		LogUtils.debugLog("  ====  \"assertRestRequestOutcome\" DONE.",log)	
    }


//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Request mark message as deleted
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII	
    /**
     * Mark message, not in a final state, as deleted
     * @param side
     * @param messageId
     * @param pluginUser
     * @param pluginPassword
     * @return commandResult
     */	
    def static markMessageAsDeletedRequest(log,context, String side, String messageId, String pluginUser = null, String pluginPassword = null){	
        LogUtils.debugLog("  ====  Calling \"markMessageAsDeletedRequest\".",log)
		LogUtils.debugLog("  markMessageAsDeletedRequest  [][]  messageId = \"$messageId\".",log)

        def commandString = ["curl ",Domibus.urlToDomibus(side, log, context) + "/ext/monitoring/messages/delete/"+messageId,
								"-H", "accept: application/json",
								"-H", "Authorization: Basic ${"$pluginUser:$pluginPassword".bytes.encodeBase64().toString()}",
								"-X", "DELETE",
                                "-v"]	

		def commandResult = ShellUtils.runCommandInShell(commandString)
		
		LogUtils.debugLog("  ====  \"markMessageAsDeletedRequest\" DONE.",log)
		
		return commandResult		
    }
	
	def static testMarkMessageAsDeletedStatus(log,context,String side, String messageId,String status, String pluginUser = null, String pluginPassword = null, String message=null){
		LogUtils.debugLog("  ====  Calling \"testMarkMessageAsDeletedStatus\".",log)

		def outcome=markMessageAsDeletedRequest(log, context, side, messageId, pluginUser, pluginPassword)
		assertRestRequestOutcome(log, outcome,status,message)
		
		// Do more checks

		LogUtils.debugLog("  ====  \"testMarkMessageAsDeletedStatus\" DONE.",log)
	}
	
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Request mark messages as deleted
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    /**
     * Mark message, not in a final state, as deleted
     * @param side
     * @param fromDate
     * @param toDate
     * @param pluginUser
     * @param pluginPassword
     * @return commandResult
     */	
    def static markMessagesAsDeletedRequest(log,context, String side, String fromDate, String toDate, String pluginUser = null, String pluginPassword = null){	
        LogUtils.debugLog("  ====  Calling \"markMessagesAsDeletedRequest\".")
		LogUtils.debugLog("  markMessagesAsDeletedRequest  [][]  fromDate=\"$fromDate\"  toDate=\"$toDate\".",log)

		def json=Domibus.ifWindowsEscapeJsonString('{\"fromDate\":\"' + "${fromDate}" + '\",\"toDate\":\"' + "${toDate}" + '\"}')
        def commandString = ["curl ",Domibus.urlToDomibus(side, log, context) + "/ext/monitoring/messages/delete",
								"-H", "accept: */*",
								"-H", "Content-Type: application/json",
								"-H", "Authorization: Basic ${"$pluginUser:$pluginPassword".bytes.encodeBase64().toString()}",
								"-X", "DELETE",
								"-d", json,
                                "-v"]	

		def commandResult = ShellUtils.runCommandInShell(commandString)
		
		LogUtils.debugLog("  ====  \"markMessagesAsDeletedRequest\" DONE.",log)
		
		return commandResult		
    }
	
	def static testMarkMessagesAsDeletedStatus(log, context, String side, String fromDate, String toDate, String status, String pluginUser = null, String pluginPassword = null, String message=null){
		LogUtils.debugLog("  ====  Calling \"testMarkMessagesAsDeletedStatus\".",log)

		def outcome=markMessagesAsDeletedRequest(log, context, side, fromDate, toDate, pluginUser, pluginPassword,context)
		assertRestRequestOutcome(log, outcome,status,message)

		LogUtils.debugLog("  ====  \"testMarkMessagesAsDeletedStatus\" DONE.",log)
	}	
	
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Request clear all caches
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    /**
     * Clear all caches
     * @return 
     */	
    def static clearAllCachesRequest(log,context, String side, String pluginUser = null, String pluginPassword = null){	
        LogUtils.debugLog("  ====  Calling \"clearAllCachesRequest\".",log)

        def commandString = ["curl ",Domibus.urlToDomibus(side, log, context) + "/ext/cache",
								"-H", "accept: */*",
								"-H", "Authorization: Basic ${"$pluginUser:$pluginPassword".bytes.encodeBase64().toString()}",
								"-X", "DELETE",
                                "-v"]	

		def commandResult = ShellUtils.runCommandInShell(commandString)
		
		LogUtils.debugLog("  ====  \"clearAllCachesRequest\" DONE.",log)
		
		return commandResult		
    }
	
	def static testClearAllCachesRequest(log, context, String side, String status, String pluginUser = null, String pluginPassword = null, String message=null){
		LogUtils.debugLog("  ====  Calling \"testClearAllCachesRequest\".",log)

		def outcome=clearAllCachesRequest(log, context, side, pluginUser, pluginPassword)
		assertRestRequestOutcome(log, outcome,status,message)

		LogUtils.debugLog("  ====  \"testClearAllCachesRequest\" DONE.",log)
	}

//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Request clear second level caches
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    /**
     * Clear second level caches
     * @return 
     */	
    def static clear2LCachesRequest(log,context, String side, String pluginUser = null, String pluginPassword = null){	
        LogUtils.debugLog("  ====  Calling \"clear2LCachesRequest\".",log)

        def commandString = ["curl ",Domibus.urlToDomibus(side, log, context) + "/ext/2LCache",
								"-H", "accept: */*",
								"-H", "Authorization: Basic ${"$pluginUser:$pluginPassword".bytes.encodeBase64().toString()}",
								"-X", "DELETE",
                                "-v"]	

		def commandResult = ShellUtils.runCommandInShell(commandString)
		
		LogUtils.debugLog("  ====  \"clear2LCachesRequest\" DONE.",log)
		
		return commandResult		
    }
	
	def static testClear2LCachesRequest(log, context, String side, String status, String pluginUser = null, String pluginPassword = null, String message=null){
		LogUtils.debugLog("  ====  Calling \"testClear2LCachesRequest\".",log)

		def outcome=clear2LCachesRequest(log, context, side, pluginUser, pluginPassword)
		assertRestRequestOutcome(log, outcome,status,message)

		LogUtils.debugLog("  ====  \"testClear2LCachesRequest\" DONE.",log)
	}
	
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Request create plugin user
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    /**
     * Create plugin user
     * @param side
     * @param fromDate
     * @param toDate
     * @param pluginUser
     * @param pluginPassword
     * @param newPluginUserDetails
     * @return commandResult
     */	
    def static createPluginUserRequest(log,context, String side, String pluginUser = null, String pluginPassword = null,newPluginUserDetails=[]){	
        LogUtils.debugLog("  ====  Calling \"createPluginUserRequest\".",log)

		def json=null
		if(newPluginUserDetails["active"]==null){
			newPluginUserDetails["active"]=true
		}
		if(newPluginUserDetails["defaultPassword"]==null){
			newPluginUserDetails["defaultPassword"]=true
		}
		if(newPluginUserDetails["originalUser"]==null){
			newPluginUserDetails["originalUser"]=" "
		}
		
		json=Domibus.ifWindowsEscapeJsonString('{\"userName\":\"' + "${newPluginUserDetails["userName"]}" + '\",\"password\":\"' + "${newPluginUserDetails["password"]}"+ '\", \"originalUser\":\"' + "${newPluginUserDetails["originalUser"]}"+ '\",\"authRoles\":\"' + "${newPluginUserDetails["authRoles"]}"+ '\",\"active\":\"' + "${newPluginUserDetails["active"]}"+ '\",\"defaultPassword\":\"' + "${newPluginUserDetails["defaultPassword"]}"+ '\"}')
        def commandString = ["curl ",Domibus.urlToDomibus(side, log, context) + "/ext/pluginUser",
								"-H", "content-type: application/json",
								"-H", "Authorization: Basic ${"$pluginUser:$pluginPassword".bytes.encodeBase64().toString()}",
								"-d", json,
                                "-v"]	

		def commandResult = ShellUtils.runCommandInShell(commandString)
		
		LogUtils.debugLog("  ====  \"createPluginUserRequest\" DONE.",log)
		
		return commandResult		
    }
	
	def static testCreatePluginUserRequest(log, context, String side, String status, String pluginUser = null, String pluginPassword = null, newPluginUserDetails=[], String message=null){
		LogUtils.debugLog("  ====  Calling \"testCreatePluginUserRequest\".",log)

		def outcome=createPluginUserRequest(log, context, side, pluginUser, pluginPassword, newPluginUserDetails)
		assertRestRequestOutcome(log, outcome,status,message)

		LogUtils.debugLog("  ====  \"testCreatePluginUserRequest\" DONE.",log)
	}
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Request download the user message payload: selelct with message ID
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    /**
     * Create plugin user
     * @param side
     * @param message ID
     * @param message CID
     * @param pluginUser
     * @param pluginPassword
     * @param newPluginUserDetails
     * @return commandResult
     */	
    def static downloadPayloadMessageIDRequest(log,context, String side, String messageId, String messageCID, String pluginUser = null, String pluginPassword = null){	
        LogUtils.debugLog("  ====  Calling \"downloadPayloadMessageIDRequest\".",log)
		
        def commandString = ["curl ",Domibus.urlToDomibus(side, log, context) + "/ext/messages/"+messageId+"/payloads/"+messageCID,
								"-H", "accept: */*",
								"-H", "Authorization: Basic ${"$pluginUser:$pluginPassword".bytes.encodeBase64().toString()}",
                                "-v"]	

		def commandResult = ShellUtils.runCommandInShell(commandString)
		
		LogUtils.debugLog("  ====  \"downloadPayloadMessageIDRequest\" DONE.",log)
		
		return commandResult		
    }
	
	def static testDownloadPayloadMessageIDRequest(log,context, String side, String status, String messageId, String messageCID, String pluginUser = null, String pluginPassword = null, String message=null){
		LogUtils.debugLog("  ====  Calling \"testDownloadPayloadMessageIDRequest\".",log)

		def outcome=downloadPayloadMessageIDRequest(log, context, side, messageId, messageCID, pluginUser, pluginPassword)
		assertRestRequestOutcome(log, outcome,status,message)

		LogUtils.debugLog("  ====  \"testDownloadPayloadMessageIDRequest\" DONE.",log)
	}
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
//  Request download the user message payload: selelct with entity ID
//IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
    /**
     * Create plugin user
     * @param side
     * @param entity ID
     * @param message CID
     * @param pluginUser
     * @param pluginPassword
     * @param newPluginUserDetails
     * @return commandResult
     */	
    def static downloadPayloadEntityIDRequest(log,context, String side, String entitytId, String messageCID, String pluginUser = null, String pluginPassword = null){	
        LogUtils.debugLog("  ====  Calling \"downloadPayloadEntityIDRequest\".",log)
		
		log.info "  downloadPayloadEntityIDRequest  [][]  Sending request to download payload with entitytId=\"$entitytId\" and CID=\"$messageCID\"."
        def commandString = ["curl ",Domibus.urlToDomibus(side, log, context) + "/ext/messages/ids/"+entitytId+"/payloads/"+messageCID,
								"-H", "accept: */*",
								"-H", "Authorization: Basic ${"$pluginUser:$pluginPassword".bytes.encodeBase64().toString()}",
                                "-v"]	

		def commandResult = ShellUtils.runCommandInShell(commandString)
		
		LogUtils.debugLog("  ====  \"downloadPayloadEntityIDRequest\" DONE.",log)
		
		return commandResult		
    }
	
	def static testDownloadPayloadEntityIDRequest(log,context, String side, String status, String entitytId, String messageCID, String pluginUser = null, String pluginPassword = null, String message=null){
		LogUtils.debugLog("  ====  Calling \"testDownloadPayloadEntityIDRequest\".",log)

		log.info "  testDownloadPayloadEntityIDRequest  [][]  Test that status=\"$status\" for request to download payload with entitytId=\"$entitytId\" and CID=\"$messageCID\"."
		def outcome=downloadPayloadEntityIDRequest(log, context, side, entitytId, messageCID, pluginUser, pluginPassword)
		assertRestRequestOutcome(log, outcome,status,message)

		LogUtils.debugLog("  ====  \"testDownloadPayloadEntityIDRequest\" DONE.",log)
	}

    def static updateBatchStatusRequest(log,context, String side, String batchId, String batchStatus="ARCHIVED", String pluginUser = null, String pluginPassword = null){	
        LogUtils.debugLog("  ====  Calling \"updateBatchStatusRequest\".",log)
		
		log.info "  updateBatchStatusRequest  [][]  Sending request to update the status of batch \"$batchId\" to \"$batchStatus\"."
        def commandString = ["curl ",Domibus.urlToDomibus(side, log, context) + "/ext/archive/batches/exported/$batchId/close?status=$batchStatus",
								"-H", "accept: application/json",
								"-H", "Authorization: Basic ${"$pluginUser:$pluginPassword".bytes.encodeBase64().toString()}",
								"-X", "PUT",
                                "-v"]	

		def commandResult = ShellUtils.runCommandInShell(commandString)
		
		LogUtils.debugLog("  ====  \"updateBatchStatusRequest\" DONE.",log)
		
		return commandResult		
    }
	
	def static testUpdateBatchStatusRequest(log,context, String side, String status, String batchId, String batchStatus="ARCHIVED", String pluginUser = null, String pluginPassword = null, String message=null){
		LogUtils.debugLog("  ====  Calling \"testUpdateBatchStatusRequest\".",log)

		log.info "  testUpdateBatchStatusRequest  [][]  Test that status=\"$status\" for request to update the status of batch \"$batchId\" to \"$batchStatus\"."
		def outcome=updateBatchStatusRequest(log, context, side, batchId, batchStatus, pluginUser, pluginPassword)
		assertRestRequestOutcome(log, outcome,status,message)

		LogUtils.debugLog("  ====  \"testUpdateBatchStatusRequest\" DONE.",log)
	}

    def static getContinuousStartDateRequest(log,context,side,String pluginUser = null, String pluginPassword = null, returnSet=false){	
        LogUtils.debugLog("  ====  Calling \"getContinuousStartDateRequest\".",log)
		
		log.info "  getContinuousStartDateRequest  [][]  Sending request to get the continuous start date."
        def commandString = ["curl ",Domibus.urlToDomibus(side, log, context) + "/ext/archive/continuous-mechanism/start-date",
								"-H", "accept: application/json",
								"-H", "Authorization: Basic ${"$pluginUser:$pluginPassword".bytes.encodeBase64().toString()}",
								"-X", "GET",
                                "-v"]	

		def commandResult = ShellUtils.runCommandInShell(commandString)
		
		LogUtils.debugLog("  ====  \"getContinuousStartDateRequest\" DONE.",log)
		if(returnSet){
			return commandResult	
		}else{
			return commandResult[0]
		}		
    }
	
	def static testGetContinuousStartDateRequest(log,context, String side, String status, String pluginUser = null, String pluginPassword = null, String message=null){
		LogUtils.debugLog("  ====  Calling \"testGetContinuousStartDateRequest\".",log)

		log.info "  testGetContinuousStartDateRequest  [][]  Test that status=\"$status\" for request to get the continuous start date."
		def outcome=getContinuousStartDateRequest(log, context, side, pluginUser, pluginPassword,true)
		assertRestRequestOutcome(log, outcome,status,message)

		LogUtils.debugLog("  ====  \"testGetContinuousStartDateRequest\" DONE.",log)
	}

    def static putContinuousStartDateRequest(log,context, side, String dateValue,String pluginUser = null, String pluginPassword = null){	
        LogUtils.debugLog("  ====  Calling \"putContinuousStartDateRequest\".",log)
		
		log.info "  putContinuousStartDateRequest  [][]  Sending request to update the continuous start date."
        def commandString = ["curl ",Domibus.urlToDomibus(side, log, context) + "/ext/archive/continuous-mechanism/start-date?messageStartDate=$dateValue",
								"-H", "accept: */*",
								"-H", "Authorization: Basic ${"$pluginUser:$pluginPassword".bytes.encodeBase64().toString()}",
								"-X", "PUT",
                                "-v"]	

		def commandResult = ShellUtils.runCommandInShell(commandString)
		
		LogUtils.debugLog("  ====  \"putContinuousStartDateRequest\" DONE.",log)
		
		return commandResult		
    }
	
	def static testPutContinuousStartDateRequest(log,context, String side, String dateValue, String status, String pluginUser = null, String pluginPassword = null, String message=null){
		LogUtils.debugLog("  ====  Calling \"testPutContinuousStartDateRequest\".",log)

		log.info "  testPutContinuousStartDateRequest  [][]  Test that status=\"$status\" for request to update the continuous start date."
		def outcome=putContinuousStartDateRequest(log, context, side, dateValue, pluginUser, pluginPassword)
		assertRestRequestOutcome(log, outcome,status,message)

		LogUtils.debugLog("  ====  \"testPutContinuousStartDateRequest\" DONE.",log)
	}
	
// ----------------------------------------------------------------------------------------------------------------------	
    def static getSanityStartDateRequest(log,context, side,String pluginUser = null, String pluginPassword = null, returnSet=false){	
        LogUtils.debugLog("  ====  Calling \"getSanityStartDateRequest\".",log)
		
		log.info "  getSanityStartDateRequest  [][]  Sending request to get the sanity mecanism start date."
        def commandString = ["curl ",Domibus.urlToDomibus(side, log, context) + "/ext/archive/sanity-mechanism/start-date",
								"-H", "accept: application/json",
								"-H", "Authorization: Basic ${"$pluginUser:$pluginPassword".bytes.encodeBase64().toString()}",
								"-X", "GET",
                                "-v"]	

		def commandResult = ShellUtils.runCommandInShell(commandString)
		
		LogUtils.debugLog("  ====  \"getSanityStartDateRequest\" DONE.",log)
		
		if(returnSet){
			return commandResult	
		}else{
			return commandResult[0]
		}		
    }
	
	def static testGetSanityStartDateRequest(log,context, String side, String status, String pluginUser = null, String pluginPassword = null, String message=null){
		LogUtils.debugLog("  ====  Calling \"testGetSanityStartDateRequest\".",log)

		log.info "  testGetSanityStartDateRequest  [][]  Test that status=\"$status\" for request to get the sanity mecanism start date."
		def outcome=getSanityStartDateRequest(log, context, side, pluginUser, pluginPassword,true)
		assertRestRequestOutcome(log, outcome,status,message)

		LogUtils.debugLog("  ====  \"testGetSanityStartDateRequest\" DONE.",log)
	}

    def static putSanityStartDateRequest(log,context, side, String dateValue,String pluginUser = null, String pluginPassword = null){	
        LogUtils.debugLog("  ====  Calling \"putSanityStartDateRequest\".",log)
		
		log.info "  putSanityStartDateRequest  [][]  Sending request to update the sanity mecanism start date."
        def commandString = ["curl ",Domibus.urlToDomibus(side, log, context) + "/ext/archive/sanity-mechanism/start-date?messageStartDate=$dateValue",
								"-H", "accept: */*",
								"-H", "Authorization: Basic ${"$pluginUser:$pluginPassword".bytes.encodeBase64().toString()}",
								"-X", "PUT",
                                "-v"]	

		def commandResult = ShellUtils.runCommandInShell(commandString)
		
		LogUtils.debugLog("  ====  \"putSanityStartDateRequest\" DONE.",log)
		
		return commandResult		
    }
	
	def static testPutSanityStartDateRequest(log,context, String side, String dateValue, String status, String pluginUser = null, String pluginPassword = null, String message=null){
		LogUtils.debugLog("  ====  Calling \"testPutSanityStartDateRequest\".",log)

		log.info "  testPutSanityStartDateRequest  [][]  Test that status=\"$status\" for request to update the sanity mecanism start date."
		def outcome=putSanityStartDateRequest(log, context, side, dateValue, pluginUser, pluginPassword)
		assertRestRequestOutcome(log, outcome,status,message)

		LogUtils.debugLog("  ====  \"testPutSanityStartDateRequest\" DONE.",log)
	}
	

}