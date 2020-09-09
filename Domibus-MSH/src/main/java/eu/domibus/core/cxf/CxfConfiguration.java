package eu.domibus.core.cxf;

import eu.domibus.core.ebms3.ws.attachment.AttachmentCleanupInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static java.util.Collections.singletonList;

/**
 * @author François Gautier
 * @since 4.2
 */
@Configuration
public class CxfConfiguration {

    @Bean
    public DomibusBus busCore(AttachmentCleanupInterceptor attachmentCleanupInterceptor) {
        DomibusBus domibusBus = new DomibusBus();
        domibusBus.setOutInterceptors(singletonList(attachmentCleanupInterceptor));
        domibusBus.setOutFaultInterceptors(singletonList(attachmentCleanupInterceptor));
        return domibusBus;
    }

    @Bean("attachmentCleanupInterceptor")
    public AttachmentCleanupInterceptor attachmentCleanupInterceptor(){
        return new AttachmentCleanupInterceptor();
    }
}
