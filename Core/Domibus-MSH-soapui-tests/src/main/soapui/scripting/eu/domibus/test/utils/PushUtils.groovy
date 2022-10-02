package eu.domibus.test.utils

class PushUtils {
    static def pushBackendMockServicePropertyName = "pushBackendMockServicePropertyName"
    /**
     * Retrieve SoapUI Backed Webservice WSDL mock service
     * @param testRunner
     * @param log
     * @return
     */
    static def retrievePushMock(testRunner) {
        def pushServiceName = testRunner.testCase.testSuite.project.getPropertyValue(pushBackendMockServicePropertyName)
        def pushMock = testRunner.testCase.testSuite.project.getMockServiceByName(pushServiceName)
        assert pushMock != null: "Mock service has been renamed or removed"
        return pushMock
    }

    /**
     * Retrieve SoapUI Backed Webservice runner
     * @param testRunner
     * @param log
     * @return mockRunner
     */
    static def retrievePushMockRunner(testRunner, log) {
        LogUtils.debugLog("  ====  Start \"storeLatestMessagesId\".", log)
        def pushMock = retrievePushMock(testRunner)
        assert pushMock.mockRunner != null, "Mock runner should be turn on"
        LogUtils.debugLog("  ====  Ending \"storeLatestMessagesId\".", log)
        return pushMock.mockRunner
    }
    /**
     * Start SoapUI Backed Webservice for Push notification
     * @param testRunner
     * @param log
     * @return mockRunner
     */
    static def startPushMockRunner(testRunner, log) {
        LogUtils.debugLog("  ====  Start \"startPushMockRunner\".", log)
        def pushMock = retrievePushMock(testRunner)

        if (pushMock.mockRunner == null) {
            log.info "Starting the BackendService PUSH mock service"
            pushMock.start()
        } else
            log.info "Mock service is running - nothing to do"

        LogUtils.debugLog("  ====  Ending \"startPushMockRunner\".", log)
        return pushMock.mockRunner
    }
    /**
     * Stop SoapUI Backed Webservice for Push notification
     * @param testRunner
     * @param log
     */
    static def stopPushMockRunner(testRunner, log) {
        LogUtils.debugLog("  ====  Start \"stopPushMockRunner\".", log)
        def pushMock = retrievePushMock(testRunner)

        if (pushMock.mockRunner != null) {
            log.info "Mock service is running would try to stop it"
            pushMock.mockRunner.stop()
        } else
            log.info "Mock service is not running - nothing to do"

        LogUtils.debugLog("  ====  Ending \"stopPushMockRunner\".", log)
    }
    /**
     * Retrieve Nth request received by Backend PUSH mock service, count from latest as 1
     * @param testRunner
     * @param log
     */
    static def retrieveNthFromEndRequestForPushMock(testRunner, log, int indexFromEnd=1) {
        LogUtils.debugLog("  ====  Start \"retrieveLastRequestFromPushMock\".", log)
        def pushMockRunner = retrievePushMockRunner(testRunner)

        def numberOfMessages = pushMockRunner.getMockResultCount()
        LogUtils.debugLog("  ====  \"numberOfMessages=\"" + numberOfMessages, log)
        def mockResult = pushMockRunner.getMockResultAt(numberOfMessages-indexFromEnd)
        def request = mockResult.getMockRequest()

        LogUtils.debugLog("Retrieved request send to mock content: " + request.getRequestContent(), log)
        LogUtils.debugLog("  ====  Ending \"retrieveLastRequestFromPushMock\".", log)
        return request
    }
}
