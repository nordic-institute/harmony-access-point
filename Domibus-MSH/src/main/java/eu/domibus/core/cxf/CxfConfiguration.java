package eu.domibus.core.cxf;

import eu.domibus.core.ebms3.ws.attachment.AttachmentCleanupInterceptor;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.BusExtensionPostProcessor;
import org.apache.cxf.bus.spring.BusWiringBeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.Collections.singletonList;

/**
 * @author François Gautier
 * @since 4.2
 */
@Configuration
public class CxfConfiguration {

    @Bean(name = Bus.DEFAULT_BUS_ID, destroyMethod = "shutdown")
    public DomibusBus busCore(AttachmentCleanupInterceptor attachmentCleanupInterceptor) {
        DomibusBus domibusBus = new DomibusBus();
        domibusBus.setOutInterceptors(singletonList(attachmentCleanupInterceptor));
        domibusBus.setOutFaultInterceptors(singletonList(attachmentCleanupInterceptor));
        return domibusBus;
    }

    //the following beans were migrated from classpath:META-INF/cxf/cxf.xml
    @Bean("attachmentCleanupInterceptor")
    public AttachmentCleanupInterceptor attachmentCleanupInterceptor() {
        return new AttachmentCleanupInterceptor();
    }

    @Bean("org.apache.cxf.bus.spring.BusWiringBeanFactoryPostProcessor")
    public BusWiringBeanFactoryPostProcessor busWiringBeanFactoryPostProcessor() {
        return new BusWiringBeanFactoryPostProcessor();
    }

    @Bean("org.apache.cxf.bus.spring.BusExtensionPostProcessor")
    public BusExtensionPostProcessor jsr250BeanPostProcessor() {
        return new BusExtensionPostProcessor();
    }
}
