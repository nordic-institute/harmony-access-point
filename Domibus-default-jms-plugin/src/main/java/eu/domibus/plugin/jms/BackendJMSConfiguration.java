package eu.domibus.plugin.jms;

import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.handler.MessageRetriever;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Configuration
public class BackendJMSConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendJMSConfiguration.class);

    @Bean
    public BackendJMSQueueService backendJMSQueueService(DomibusPropertyExtService domibusPropertyExtService,
                                                         DomainContextExtService domainContextExtService,
                                                         MessageRetriever messageRetriever) {
        return new BackendJMSQueueService(domibusPropertyExtService, domainContextExtService, messageRetriever);
    }
}
