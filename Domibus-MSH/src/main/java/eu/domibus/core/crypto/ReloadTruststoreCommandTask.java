package eu.domibus.core.crypto;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.clustering.CommandTask;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class ReloadTruststoreCommandTask implements CommandTask {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(ReloadTruststoreCommandTask.class);

    protected MultiDomainCryptoService multiDomainCryptoService;
    protected DomainContextProvider domainContextProvider;

    public ReloadTruststoreCommandTask(MultiDomainCryptoService multiDomainCryptoService, DomainContextProvider domainContextProvider) {
        this.multiDomainCryptoService = multiDomainCryptoService;
        this.domainContextProvider = domainContextProvider;
    }

    @Override
    public boolean canHandle(String command) {
        return StringUtils.equalsIgnoreCase(Command.RELOAD_TRUSTSTORE, command);
    }

    @Override
    public void execute(Map<String, String> properties) {
        LOGGER.debug("Reloading PMode command");

        Domain currentDomain = domainContextProvider.getCurrentDomain();
        multiDomainCryptoService.refreshTrustStore(currentDomain);
    }
}
