package eu.domibus.plugin.fs.worker;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomainExtService;
import eu.domibus.ext.services.DomibusConfigurationExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.FSMessage;
import eu.domibus.plugin.fs.ebms3.CollaborationInfo;
import eu.domibus.plugin.fs.exception.FSSetUpException;
import eu.domibus.plugin.fs.property.FSPluginProperties;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @author Tiago Miguel
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class FSDomainService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSDomainService.class);

    @Autowired
    protected DomibusConfigurationExtService domibusConfigurationExtService;

    @Autowired
    protected DomainExtService domainExtService;

    @Autowired
    protected FSPluginProperties fsPluginProperties;

    @Autowired
    protected DomainContextExtService domainContextExtService;

    private final Map<String, Pattern> domainPatternCache = new HashMap<>();

    /**
     * Verifies if the provided domain exists. For multitenancy mode it checks if the FS Plugin domain is configured in Domibus core. For non multitenancy mode is always return true.
     *
     * @param domain FS Plugin domain
     * @return true if we are in Multi Tenancy configuration and domain is configured. Otherwise it returns true.
     */
    public boolean verifyDomainExists(String domain) {
        if (domibusConfigurationExtService.isMultiTenantAware()) {
            LOG.debug("Checking if the domain [{}] is configured in Domibus core", domain);

            if (domainExtService.getDomain(domain) == null) {
                throw new FSSetUpException("Domain " + domain + " not configured in Domibus");
            }
            LOG.debug("Domain [{}] is configured in Domibus core", domain);
            return true;
        }
        LOG.trace("Provided domain [{}] is configured in non multitenancy mode", domain);
        return true;
    }

    /**
     * Returns all domains that need to be processed by various operations (sending, purging etc)
     * In multi-tenancy, this means only the current domain.
     * In single-tenancy, all configured fsplugin domains are returned.
     *
     * @return a list of domain codes
     */
    public List<String> getDomainsToProcess() {
        if (domibusConfigurationExtService.isMultiTenantAware()) {
            // in multi-tenancy, process only the current domain
            LOG.trace("Multi-tenancy mode, process current domain");
            return Arrays.asList(domainContextExtService.getCurrentDomain().getCode());
        }
        
        // in single-tenancy, process all fsplugin-defined domains
        LOG.trace("Single-tenancy mode, process known domains");
        return fsPluginProperties.getDomains();
    }

    /**
     * Resolves FS Plugin domain to Domibus core domain
     *
     * @param fsPluginDomain The FS Plugin domain to resolved
     * @return the Domibus core domain
     */
    public DomainDTO fsDomainToDomibusDomain(String fsPluginDomain) {
        if (!domibusConfigurationExtService.isMultiTenantAware()) {
            fsPluginDomain = FSSendMessagesService.DEFAULT_DOMAIN;
            LOG.debug("Domibus is running in non multitenancy mode, using the Domibus default domain");
        }

        final DomainDTO domainDTO = domainExtService.getDomain(fsPluginDomain);
        LOG.debug("Resolving FSPlugin domain [{}] to Domibus domain [{}]", fsPluginDomain, domainDTO);
        return domainDTO;
    }

    public String getFSPluginDomain(FSMessage fsMessage) {
        CollaborationInfo collaborationInfo = fsMessage.getMetadata().getCollaborationInfo();
        String service = collaborationInfo.getService().getValue();
        String action = collaborationInfo.getAction();
        return getFSPluginDomain(service, action);
    }

    /**
     * Gets the FS Plugin domain associated to the service and action. For multitenancy mode it always gets the domain from the Domibus core. In non multitenancy mode
     * it resolves the domain using the FS Plugin configuration
     *
     * @param service The UserMessage service value
     * @param action  The UserMessage action value
     * @return the FS Plugin domain
     */
    public String getFSPluginDomain(String service, String action) {
        LOG.trace("Getting FSPluginDomain");

        // get multiTenantAware
        boolean multiTenantAware = domibusConfigurationExtService.isMultiTenantAware();
        LOG.trace("Is Domibus running in multitenancy mode? [{}]", multiTenantAware);

        // get domain info
        String domain;
        if (multiTenantAware) {
            domain = domainContextExtService.getCurrentDomain().getCode();
            LOG.trace("Getting current domain [{}]", domain);
        } else {
            domain = resolveFSPluginDomain(service, action);
            if (StringUtils.isEmpty(domain)) {
                LOG.debug("Using default domain: no configured domain found");
                domain = FSSendMessagesService.DEFAULT_DOMAIN;
            }
        }
        LOG.trace("FSPluginDomain is [{}]", domain);

        return domain;
    }

    /**
     * Resolves the FS Plugin domain using the FS Plugin configuration
     *
     * @param service The UserMessage service value
     * @param action  The UserMessage action value
     * @return the FS Plugin domain
     */
    protected String resolveFSPluginDomain(String service, String action) {
        LOG.debug("Resolving domain for service [{}] and action [{}]", service, action);

        String serviceAction = service + "#" + action;
        List<String> domains = fsPluginProperties.getDomains();
        for (String domain : domains) {
            Pattern domainExpressionPattern = getFSPluginDomainPattern(domain);
            if (domainExpressionPattern != null) {
                boolean domainMatches = domainExpressionPattern.matcher(serviceAction).matches();
                if (domainMatches) {
                    LOG.debug("Resolved domain [{}] for service [{}] and action [{}]", domain, service, action);
                    return domain;
                }
            }
        }
        LOG.debug("No domain configured for service [{}] and action [{}]", service, action);
        return null;
    }

    protected Pattern getFSPluginDomainPattern(String domain) {
        synchronized (domainPatternCache) {
            if (domainPatternCache.containsKey(domain)) {
                return domainPatternCache.get(domain);
            }
        }

        String domainExpression = fsPluginProperties.getExpression(domain);
        LOG.debug("Getting domain pattern for domain [{}] using domain expression", domain, domainExpression);

        Pattern domainExpressionPattern = null;
        if (StringUtils.isNotEmpty(domainExpression)) {
            try {
                domainExpressionPattern = Pattern.compile(domainExpression);
            } catch (PatternSyntaxException e) {
                LOG.warn("Invalid domain expression for " + domain, e);
            }
        }

        // domainExpressionPattern may be null, we should still cache null and return it
        domainPatternCache.put(domain, domainExpressionPattern);
        return domainExpressionPattern;
    }

    public void resetPatterns() {
        LOG.debug("Clearing domain pattern cache.");
        synchronized (domainPatternCache) {
            domainPatternCache.clear();
        }
    }

}
