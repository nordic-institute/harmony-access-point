class PushUtils {

    /**
     * Check the push plugin notification content
     * @param requestContent
     * @param notifType
     * @param messageId
     * @param testRunner
     * @param log
     * @param messageStatus
     * @return
     */
    static def checkPushPluginNotification(requestContent,notifType,messageId,testRunner, log,messageStatus=null) {
		LogUtils.debugLog("  ====  Calling \"checkPushPluginNotification\".", log)
		
		def retrievedNotifType=null
		def retrievedMessId=null
		def retrievedMesStatus=null
		
		def requestXml = new XmlSlurper().parseText(requestContent)
			
		requestXml.depthFirst().each {
            if (it.name() == "messageID") {
                retrievedMessId=it.text().toLowerCase().trim()
				retrievedNotifType=it.parent().name().toLowerCase().trim()
            }
            if (it.name() == "messageStatus") {
                retrievedMesStatus=it.text().toLowerCase().trim()
            }
        }
		assert ((retrievedNotifType==notifType.toLowerCase().trim())&&(retrievedMessId==messageId.toLowerCase().trim())),"Error: checkPushPluginNotification: Notification not found. Expected $notifType for message $messageId.\n Retrieved $retrievedNotifType for message $retrievedMessId."
		if(messageStatus!=null){
			assert (retrievedMesStatus==messageStatus.toLowerCase().trim()),"Error: checkPushPluginNotification: Wrong message status inside the notification. Expected status \"$messageStatus\". Retrieved status $retrievedMesStatus."
		}
        log.info "---- Received Notification: $retrievedNotifType"
		log.info "---- Notification for message ID: $retrievedMessId"
		if(messageStatus!=null){
			log.info "---- Message status inside the notification: $messageStatus"
		}
		LogUtils.debugLog("  ====  End \"checkPushPluginNotification\".", log)
    }
}
