package eu.domibus.core.pmode;

import eu.domibus.api.cache.CacheConstants;
import eu.domibus.api.cluster.Command;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.clustering.CommandTask;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.core.pmode.provider.PModeProvider;
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
public class ReloadPModeCommandTask implements CommandTask {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(ReloadPModeCommandTask.class);

    protected PModeProvider pModeProvider;
    protected MultiDomainCryptoService multiDomainCryptoService;
    protected DomainContextProvider domainContextProvider;
    protected DomibusCacheService domibusCacheService;

    public ReloadPModeCommandTask(PModeProvider pModeProvider, MultiDomainCryptoService multiDomainCryptoService, DomainContextProvider domainContextProvider, DomibusCacheService domibusCacheService) {
        this.pModeProvider = pModeProvider;
        this.multiDomainCryptoService = multiDomainCryptoService;
        this.domainContextProvider = domainContextProvider;
        this.domibusCacheService = domibusCacheService;
    }

    @Override
    public boolean canHandle(String command) {
        return StringUtils.equalsIgnoreCase(Command.RELOAD_PMODE, command);
    }

    @Override
    public void execute(Map<String, String> properties) {
        LOGGER.debug("Reloading PMode command");

        domibusCacheService.clearCache(CacheConstants.DICTIONARY_QUERIES);
        Domain currentDomain = domainContextProvider.getCurrentDomain();
        pModeProvider.refresh();
        multiDomainCryptoService.resetTrustStore(currentDomain);
    }
}
