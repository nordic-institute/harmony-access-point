package eu.domibus.core.cxf;

import eu.domibus.core.ebms3.ws.attachment.AttachmentCleanupInterceptor;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.Collections.singletonList;

/**
 * @author François Gautier
 * @since 4.2
 */
@Configuration
public class CxfConfiguration {

    @Bean("busCore")
    public Bus busCore(AttachmentCleanupInterceptor attachmentCleanupInterceptor) {
        SpringBus springBus = new SpringBus();
        springBus.setOutInterceptors(singletonList(attachmentCleanupInterceptor));
        springBus.setOutFaultInterceptors(singletonList(attachmentCleanupInterceptor));
        return springBus;
    }

    @Bean(Bus.DEFAULT_BUS_ID)
    public Bus bus(AttachmentCleanupInterceptor attachmentCleanupInterceptor) {
        SpringBus springBus = new SpringBus();
        springBus.setOutInterceptors(singletonList(attachmentCleanupInterceptor));
        springBus.setOutFaultInterceptors(singletonList(attachmentCleanupInterceptor));
        return springBus;
    }

    @Bean("attachmentCleanupInterceptor")
    public AttachmentCleanupInterceptor attachmentCleanupInterceptor(){
        return new AttachmentCleanupInterceptor();
    }
}
