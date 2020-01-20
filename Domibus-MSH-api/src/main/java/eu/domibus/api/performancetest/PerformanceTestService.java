package eu.domibus.api.performancetest;

public interface PerformanceTestService {

    void testDBSaveAndJMS();

    void testDBJMSCommit();

    void testJMS();

    void testSave(String type, int count);
}
