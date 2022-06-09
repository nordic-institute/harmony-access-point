package eu.domibus.core.crypto;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cxf.TLSReaderService;
import eu.domibus.core.clustering.CommandTask;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageConstants;
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

    public ReloadTLSTruststoreCommandTask(TLSReaderService tlsReaderService) {
        this.tlsReaderService = tlsReaderService;
    }

    @Override
    public boolean canHandle(String command) {
        return StringUtils.equalsIgnoreCase(Command.RELOAD_TLS_TRUSTSTORE, command);
    }

    @Override
    public void execute(Map<String, String> properties) {
        LOGGER.debug("Reloading tls truststore command.");

        String domainCode = properties.get(MessageConstants.DOMAIN);
        tlsReaderService.reset(domainCode);
    }
}
