package eu.domibus.ebms3.sender;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Thomas Dussart
 * @since 4.0
 */

public class DomainPullFrequencyTest {

    @Test
    public void testDomainPulFrequencyNoRecoveingTimeInSecondTest(){
        final DomainPullFrequency domainPullFrequency = new DomainPullFrequency(30, 0, 10);
        assertEquals(30l,domainPullFrequency.getMaxRequestPerJobCycle().longValue());
        for(int i=0;i<30;i++){
            domainPullFrequency.increaseErrorCounter();
            assertEquals(30l,domainPullFrequency.getMaxRequestPerJobCycle().longValue());
        }

    }

    @Test
    public void testDomainPulFrequency10SecondsTimeTest() throws InterruptedException {
        final int recoveringTimeInHalfSeconds = 60;
        final DomainPullFrequency domainPullFrequency = new DomainPullFrequency(15, recoveringTimeInHalfSeconds, 10);
        //assertEquals(30l,domainPullFrequency.getMaxRequestPerJobCycle().longValue());
        int seconds=0;
        do {
            for (int i = 0; i < 30; i++) {
                domainPullFrequency.getMaxRequestPerJobCycle();
                //domainPullFrequency.increaseErrorCounter();
                //assertEquals(30l,domainPullFrequency.getMaxRequestPerJobCycle().longValue());
            }
            Thread.sleep(1000);
            seconds++;
            System.out.println("Second "+seconds);
        }while (seconds< recoveringTimeInHalfSeconds);
    }

}