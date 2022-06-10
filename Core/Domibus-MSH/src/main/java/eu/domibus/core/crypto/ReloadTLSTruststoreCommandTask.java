package eu.domibus.core.crypto;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cxf.TLSReaderService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.clustering.CommandTask;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class ReloadTLSTruststoreCommandTask implements CommandTask {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(ReloadTLSTruststoreCommandTask.class);

    private final TLSReaderService tlsReaderService;
    private final DomainContextProvider domainContextProvider;

    public ReloadTLSTruststoreCommandTask(TLSReaderService tlsReaderService, DomainContextProvider domainContextProvider) {
        this.tlsReaderService = tlsReaderService;
        this.domainContextProvider = domainContextProvider;
    }

    @Override
    public boolean canHandle(String command) {
        return StringUtils.equalsIgnoreCase(Command.RELOAD_TLS_TRUSTSTORE, command);
    }

    @Override
    public void execute(Map<String, String> properties) {
        LOGGER.debug("Reloading tls truststore command.");

        Domain currentDomain = domainContextProvider.getCurrentDomain();
        tlsReaderService.reset(currentDomain.getCode());
    }
}
