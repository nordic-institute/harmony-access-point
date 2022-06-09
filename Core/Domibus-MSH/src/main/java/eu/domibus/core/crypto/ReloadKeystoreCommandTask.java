package eu.domibus.core.crypto;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.core.clustering.CommandTask;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class ReloadKeystoreCommandTask implements CommandTask {

    private static final IDomibusLogger LOGGER = DomibusLoggerFactory.getLogger(ReloadKeystoreCommandTask.class);

    protected MultiDomainCryptoService multiDomainCryptoService;
    protected DomainContextProvider domainContextProvider;

    public ReloadKeystoreCommandTask(MultiDomainCryptoService multiDomainCryptoService, DomainContextProvider domainContextProvider) {
        this.multiDomainCryptoService = multiDomainCryptoService;
        this.domainContextProvider = domainContextProvider;
    }

    @Override
    public boolean canHandle(String command) {
        return StringUtils.equalsIgnoreCase(Command.RELOAD_KEYSTORE, command);
    }

    @Override
    public void execute(Map<String, String> properties) {
        LOGGER.debug("Reload keystore command executing");

        Domain currentDomain = domainContextProvider.getCurrentDomain();
        multiDomainCryptoService.refreshKeyStore(currentDomain);
    }
}
