class MockUtils {



    static def retrieveMockService(mockServiceName, testRunner, log) {
        LogUtils.debugLog("  ====  Start \"retrieveMockService\".", log)
        def mockService = null
        try{
            mockService = testRunner.testCase.testSuite.project.getMockServiceByName(mockServiceName)
        }
        catch (Exception ex) {
            log.error "  retrieveMockService  [][]  Can't find mock service called: " + mockServiceName
            assert 0,"Exception occurred: " + ex
        }
        LogUtils.debugLog("  ====  Ending \"retrieveMockService\".", log)
        return mockService
    }

    static def retrieveRestMockService(restMockServiceName, testRunner, log) {
        LogUtils.debugLog("  ====  Start \"retrieveRestMockService\".", log)
        def mockService = null
        try{
            mockService = testRunner.testCase.testSuite.project.getRestMockServiceByName(restMockServiceName)
        }
        catch (Exception ex) {
            log.error "  retrieveRestMockService  [][]  Can't find mock service called: " + restMockServiceName
            assert 0,"Exception occurred: " + ex
        }
        LogUtils.debugLog("  ====  Ending \"retrieveRestMockService\".", log)
        return mockService
    }

    static def retrieveMockRunner(mockServiceName, testRunner, log) {
        LogUtils.debugLog("  ====  Start \"retrieveMockRunner\".", log)
        def mockService=retrieveMockService(mockServiceName, testRunner, log)
        def mockRunner = mockService.getMockRunner()
        LogUtils.debugLog("  ====  Ending \"retrieveMockRunner\".", log)
        return mockRunner
    }

    static def retrieveRestMockRunner(restMockServiceName, testRunner, log) {
        LogUtils.debugLog("  ====  Start \"retrieveRestMockRunner\".", log)
        def mockService=retrieveRestMockService(restMockServiceName, testRunner, log)
        def mockRunner = mockService.getMockRunner()
        LogUtils.debugLog("  ====  Ending \"retrieveRestMockRunner\".", log)
        return mockRunner
    }

    static void  stopMockService(String mockServiceName,testRunner,log) {
        LogUtils.debugLog("  ====  Calling \"stopMockService\".",log)
        LogUtils.debugLog("  stopMockService  [][]  Mock service name: \"" + mockServiceName+"\"",log)
        def mockService = retrieveMockService(mockServiceName, testRunner, log)

        def mockRunner = mockService.getMockRunner()
        if(mockRunner!= null){
            mockRunner.stop()
			
			// Wait for mock service status update 
			sleep(DomibusConstants.MOCK_RUNNER_UPDATE_DELAY)
			
            assert(!mockRunner.isRunning()),"  stopMockService  [][]  Mock service is still running."
            mockRunner.release()
        }
        log.info ("  stopMockService  [][]  Mock service \"" + mockServiceName + "\" is stopped.")
    }

    static void  stopAllMockServices(testRunner,log) {
        LogUtils.debugLog("  ====  Calling \"stopAllMockServices\".",log)
        def project = testRunner.testCase.testSuite.project
        def mockServicesCount = project.getMockServiceCount()
        for (i in 0..(mockServicesCount-1)){
            // Stop each mock service
            def mockServiceName = project.getMockServiceAt(i).getName()
            LogUtils.debugLog("  stopAllMockServices  [][]  Stopping service: \"" + mockServiceName+"\"",log)
            stopMockService(mockServiceName,testRunner,log)
        }
        log.info ("  stopAllMockServices  [][]  All mock services are stopped.")
    }


    static void  startMockService(String mockServiceName,testRunner,log,stopAll=true) {
        LogUtils.debugLog("  ====  Calling \"startMockService\".",log)
        LogUtils.debugLog("  startMockService  [][]  Mock service name: \"" + mockServiceName+"\"",log)
        if(stopAll==true){
            stopAllMockServices(testRunner,log)
        }else{
            stopMockService(mockServiceName,testRunner,log)
        }
        def mockService=retrieveMockService(mockServiceName, testRunner, log)
        mockService.start()
		
		// Wait for mock service status update
		sleep(DomibusConstants.MOCK_RUNNER_UPDATE_DELAY)
        
		def mockRunner = mockService.getMockRunner()
        assert(mockRunner!= null),"  startMockService  [][]  Can't get mock runner: mock service did not start."
        assert(mockRunner.isRunning()),"  startMockService  [][]  Mock service did not start."
        log.info ("  startMockService  [][]  Mock service \"" + mockServiceName + "\" is running.")
    }

    static void  stopRestMockService(String restMockServiceName,testRunner,log) {
        LogUtils.debugLog("  ====  Calling \"stopRestMockService\".",log)
        LogUtils.debugLog("  stopRestMockService  [][]  Rest mock service name:" + restMockServiceName,log)
        def mockService=retrieveRestMockService(restMockServiceName, testRunner, log)

        def mockRunner = mockService.getMockRunner()
        if(mockRunner!= null){
            mockRunner.stop()
			
			// Wait for mock service status update
			sleep(DomibusConstants.MOCK_RUNNER_UPDATE_DELAY)
			
            assert(!mockRunner.isRunning()),"  startRestMockService  [][]  Mock service is still running."
            mockRunner.release()
        }
        log.info ("  stopRestMockService  [][]  Rest mock service " + restMockServiceName + " is stopped.")
    }

    static void stopAllRestMockServices(testRunner, log) {
        LogUtils.debugLog("  ====  Calling \"stopAllRestMockServices\".",log)
        def project = testRunner.testCase.testSuite.project
        def restMockServicesCount = project.getRestMockServiceCount()
        for (i in 0..(restMockServicesCount-1)){
            // Stop each rest mock service
            def restMockServiceName = project.getRestMockServiceAt(i).getName()
            LogUtils.debugLog("  stopAllRestMockServices  [][]  Stopping Rest service: " + restMockServiceName,log)
            stopRestMockService(restMockServiceName,testRunner,log)
        }
        log.info ("  stopAllRestMockServices  [][]  All rest mock services are stopped.")
    }

    static void  startRestMockService(String restMockServiceName,testRunner,log,stopAll=true) {
        LogUtils.debugLog("  ====  Calling \"startRestMockService\".",log)
        LogUtils.debugLog("  startRestMockService  [][]  Rest mock service name:" + restMockServiceName,log)
        if(stopAll){
            stopAllRestMockServices(testRunner, log)
        }else{
            stopRestMockService(restMockServiceName,testRunner,log)
        }
        def mockService=retrieveRestMockService(restMockServiceName, testRunner, log)
        mockService.start()
		
		// Wait for mock service status update
		sleep(DomibusConstants.MOCK_RUNNER_UPDATE_DELAY)
		
        def mockRunner = mockService.getMockRunner()
        assert(mockRunner!= null),"  startRestMockService  [][]  Can't get mock runner: mock service did not start."
        assert(mockRunner.isRunning()),"  startRestMockService  [][]  Mock service did not start."
        log.info("  startRestMockService  [][]  Rest mock service " + restMockServiceName + " is running.")
    }
	
    /**
     * Save in test case properties current number of requests received in backend mock
     * @param testRunner
     * @param log
     * @return
     */
    static def saveInitialNumberOfRequestsInMock(mockServiceName,testRunner, log) {
		LogUtils.debugLog("  ====  Calling \"saveInitialNumberOfRequestsInMock\".",log)
        def currentNumberOfRequestsInMock = retrieveMockRunner(mockServiceName,testRunner, log).getMockResultCount()
		Domibus.setTestCaseCustProp(DomibusConstants.TC_CP_NBR_REQ_RECEIVED_BY_MOCK,currentNumberOfRequestsInMock.toString(),log,testRunner)
        LogUtils.debugLog("Set test case property \"${DomibusConstants.TC_CP_NBR_REQ_RECEIVED_BY_MOCK}\" to value ${currentNumberOfRequestsInMock}.", log)
		LogUtils.debugLog("  ====  Ending \"saveInitialNumberOfRequestsInMock\".", log)
    }
	
    /**
     * Retrieve Nth request received by Backend PUSH mock service, count from latest as 1
     * @param testRunner
     * @param log
     */
    static def retrieveRequestReceivedByMockAtIndex(mockServiceName,testRunner, log, int indexFromEnd=1) {
        LogUtils.debugLog("  ====  Start \"retrieveRequestReceivedByMockAtIndex\".", log)
        def mockRunner = retrieveMockRunner(mockServiceName, testRunner, log)

        def numberOfRequests = mockRunner.getMockResultCount()
        LogUtils.debugLog("number of request in mock service=" + numberOfRequests, log)
        def mockResult = mockRunner.getMockResultAt(numberOfRequests-indexFromEnd)
        def request = mockResult.getMockRequest()

        LogUtils.debugLog("Retrieved request number: ${numberOfRequests-indexFromEnd} send to mock content: " + request.getRequestContent(), log)
        LogUtils.debugLog("  ====  Ending \"retrieveRequestReceivedByMockAtIndex\".", log)
        return request
    }	
	
    /**
     * Check that number of requests increased as expected - comparing to previously stored in test case properties  value
     * @param testRunner
     * @param log
     * @param expectedIncrease
     * @return
     */
    static def checkNumberOfNewRequestsReceivedByMock(mockServiceName,testRunner, log, int expectedIncrease) {
        def currentNumberOfRequestsInMock=retrieveMockRunner(mockServiceName,testRunner, log).getMockResultCount()
        def previousNumberOfRequestsReceivedByMock = Domibus.getTestCaseCustProp(DomibusConstants.TC_CP_NBR_REQ_RECEIVED_BY_MOCK,log, testRunner)  as Integer
        assert currentNumberOfRequestsInMock == previousNumberOfRequestsReceivedByMock + expectedIncrease,
                "Expecting ${expectedIncrease} new requests. However, number of requests before was: " +
                        "${previousNumberOfRequestsReceivedByMock} and number of request currently is: ${currentNumberOfRequestsInMock}"
        log.info "The number of new requests received by the mock service is equal to ${expectedIncrease} requests."
    }
	

}