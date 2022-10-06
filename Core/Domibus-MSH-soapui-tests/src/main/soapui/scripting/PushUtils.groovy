class PushUtils {
    static def pushBackendMockServicePropertyName = "pushBackendMockServicePropertyName"
    static def numberOfRequestsReceivedByMock = "numberOfRequestsReceivedByMock"
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
        LogUtils.debugLog("  ====  Start \"retrievePushMockRunner\".", log)
        def pushMock = retrievePushMock(testRunner)
        assert pushMock.mockRunner != null, "Mock runner should be turn on"
        LogUtils.debugLog("  ====  Ending \"retrievePushMockRunner\".", log)
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
        LogUtils.debugLog("  ====  Start \"retrieveNthFromEndRequestForPushMock\".", log)
        def pushMockRunner = retrievePushMockRunner(testRunner, log)

        def numberOfRequests = pushMockRunner.getMockResultCount()
        LogUtils.debugLog("number of request in mock service=" + numberOfRequests, log)
        def mockResult = pushMockRunner.getMockResultAt(numberOfRequests-indexFromEnd)
        def request = mockResult.getMockRequest()

        LogUtils.debugLog("Retrieved request number: ${numberOfRequests-indexFromEnd} send to mock content: " + request.getRequestContent(), log)
        LogUtils.debugLog("  ====  Ending \"retrieveNthFromEndRequestForPushMock\".", log)
        return request
    }

    /**
     * Save in test case properties current number of requests received in backend mock
     * @param testRunner
     * @param log
     * @return
     */
    static def saveInitialNumberOfRequestsInMock(testRunner, log) {
        def currentNumberOfRequestsInMock = retrievePushMockRunner(testRunner, log).getMockResultCount()
        testRunner.testCase.setPropertyValue(numberOfRequestsReceivedByMock,currentNumberOfRequestsInMock.toString())
        LogUtils.debugLog("Set test case property \"${numberOfRequestsReceivedByMock}\" to value ${currentNumberOfRequestsInMock}.", log)
    }

    /**
     * Check that number of requests increased as expected - comparing to previously stored in test case properties  value
     * @param testRunner
     * @param log
     * @param expectedIncrease
     * @return
     */
    static def checkNumberOfRequestsIncreasedExactlyBy(testRunner, log, int expectedIncrease) {
        def currentNumberOfRequestsInMock = retrievePushMockRunner(testRunner, log).getMockResultCount()
        def previousNumberOfRequestsReceivedByMock = testRunner.testCase.getPropertyValue(numberOfRequestsReceivedByMock)  as Integer
        assert currentNumberOfRequestsInMock == previousNumberOfRequestsReceivedByMock + expectedIncrease,
                "Expecting number of requests to grow by ${expectedIncrease} but number of requests before was: " +
                        "${previousNumberOfRequestsReceivedByMock} and number of request currently is: ${currentNumberOfRequestsInMock}"
        log.info "Number of requests received by mock service grown as expected by ${expectedIncrease} requests"
    }
}
