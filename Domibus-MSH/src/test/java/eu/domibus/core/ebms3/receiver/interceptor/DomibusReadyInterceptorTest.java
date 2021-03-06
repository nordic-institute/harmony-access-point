package eu.domibus.core.ebms3.receiver.interceptor;

import eu.domibus.core.ebms3.receiver.interceptor.DomibusReadyInterceptor;
import eu.domibus.core.status.DomibusStatusService;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JMockit.class)
public class DomibusReadyInterceptorTest {

    @Injectable
    private DomibusStatusService domibusStatusService;

    @Tested
    private DomibusReadyInterceptor domibusReadyInterceptor;

    @Test(expected = Fault.class)
    public void isReady(@Mocked final Message message){
        new Expectations(){{
            domibusStatusService.isNotReady();
            result=true;
        }};
        domibusReadyInterceptor.handleMessage(message);
    }

}