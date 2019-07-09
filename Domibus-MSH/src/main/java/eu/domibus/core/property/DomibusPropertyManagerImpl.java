package eu.domibus.core.property;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.property.DomibusPropertyChangeListener;
import eu.domibus.property.DomibusPropertyManager;
import eu.domibus.property.DomibusPropertyMetadata;
import eu.domibus.property.PropertyUsageType;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DomibusPropertyManagerImpl implements DomibusPropertyManager {

    private static final DomibusLogger LOGGER = DomibusLoggerFactory.getLogger(DomibusPropertyManagerImpl.class);

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    private DomainService domainService;

    @Autowired
    private DomibusConfigurationService domibusConfigurationService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private List<DomibusPropertyChangeListener> domibusPropertyChangeListeners;


    /**
     * Returns the properties that this PropertyProvider is able to handle.
     *
     * @return a map
     * @implNote This list will be moved in the database eventually.
     */
    @Override
    public Map<String, DomibusPropertyMetadata> getKnownProperties() {

        return Arrays.stream(new DomibusPropertyMetadata[]{
                new DomibusPropertyMetadata("domibus.UI.title.name"),
                new DomibusPropertyMetadata("domibus.ui.replication.enabled"),
                new DomibusPropertyMetadata("domibus.ui.support.team.name"),
                new DomibusPropertyMetadata("domibus.ui.support.team.email"),

                new DomibusPropertyMetadata("domibus.security.keystore.location", PropertyUsageType.DOMAIN_PROPERTY_RESOLVED),
                new DomibusPropertyMetadata("domibus.security.keystore.type", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),
                new DomibusPropertyMetadata("domibus.security.keystore.password", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),
                new DomibusPropertyMetadata("domibus.security.key.private.alias", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),

                new DomibusPropertyMetadata("domibus.security.truststore.location", PropertyUsageType.DOMAIN_PROPERTY_RESOLVED),
                new DomibusPropertyMetadata("domibus.security.truststore.type", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),
                new DomibusPropertyMetadata("domibus.security.truststore.password", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),

                new DomibusPropertyMetadata("domibus.auth.unsecureLoginAllowed", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),
                new DomibusPropertyMetadata("domibus.console.login.maximum.attempt", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.console.login.suspension.time", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.certificate.revocation.offset", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),
                new DomibusPropertyMetadata("domibus.certificate.crl.excludedProtocols", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),

                new DomibusPropertyMetadata("domibus.plugin.login.maximum.attempt", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),
                new DomibusPropertyMetadata("domibus.plugin.login.suspension.time", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),

                new DomibusPropertyMetadata("domibus.passwordPolicy.pattern", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.passwordPolicy.validationMessage", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.passwordPolicy.expiration", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.passwordPolicy.defaultPasswordExpiration", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.passwordPolicy.warning.beforeExpiration", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.passwordPolicy.dontReuseLast", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.passwordPolicy.checkDefaultPassword", PropertyUsageType.GLOBAL_PROPERTY),

                new DomibusPropertyMetadata("domibus.plugin.passwordPolicy.pattern", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.plugin.passwordPolicy.validationMessage", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.passwordPolicy.plugin.expiration", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.passwordPolicy.plugin.defaultPasswordExpiration", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.passwordPolicy.plugin.dontReuseLast", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),

                new DomibusPropertyMetadata("domibus.attachment.storage.location", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),
                new DomibusPropertyMetadata("domibus.payload.encryption.active", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),

                new DomibusPropertyMetadata("domibus.msh.messageid.suffix", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.msh.retry.messageExpirationDelay", PropertyUsageType.GLOBAL_PROPERTY),

                new DomibusPropertyMetadata("domibus.dynamicdiscovery.useDynamicDiscovery", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.smlzone", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dynamicdiscovery.client.specification", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dynamicdiscovery.peppolclient.mode", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dynamicdiscovery.oasisclient.regexCertificateSubjectValidation", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dynamicdiscovery.partyid.responder.role", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dynamicdiscovery.partyid.type", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dynamicdiscovery.transportprofileas4", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),

                new DomibusPropertyMetadata("domibus.listPendingMessages.maxCount", PropertyUsageType.GLOBAL_PROPERTY),
                new DomibusPropertyMetadata("domibus.jms.queue.maxBrowseSize", PropertyUsageType.GLOBAL_PROPERTY), //there is one place at init time that it is not refreshed
                new DomibusPropertyMetadata("domibus.jms.internalQueue.expression", PropertyUsageType.GLOBAL_PROPERTY),

                new DomibusPropertyMetadata("domibus.receiver.certificate.validation.onsending", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.sender.certificate.validation.onsending", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.sender.certificate.validation.onreceiving", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.sender.trust.validation.onreceiving", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.sender.trust.validation.expression", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),
                new DomibusPropertyMetadata("domibus.sender.certificate.subject.check", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.sender.trust.validation.truststore_alias", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.sendMessage.messageIdPattern", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),

                new DomibusPropertyMetadata("domibus.dispatcher.connectionTimeout", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dispatcher.receiveTimeout", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dispatcher.allowChunking", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dispatcher.chunkingThreshold", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dispatcher.concurency", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dispatcher.largeFiles.concurrency", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dispatcher.cacheable", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dispatcher.connection.keepAlive", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.retentionWorker.message.retention.downloaded.max.delete", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.retentionWorker.message.retention.not_downloaded.max.delete", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.retention.jms.concurrency", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dispatch.ebms.error.unrecoverable.retry", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),

                new DomibusPropertyMetadata("domibus.proxy.enabled", PropertyUsageType.GLOBAL_PROPERTY),
                new DomibusPropertyMetadata("domibus.proxy.http.host", PropertyUsageType.GLOBAL_PROPERTY),
                new DomibusPropertyMetadata("domibus.proxy.http.port", PropertyUsageType.GLOBAL_PROPERTY),
                new DomibusPropertyMetadata("domibus.proxy.user", PropertyUsageType.GLOBAL_PROPERTY),
                new DomibusPropertyMetadata("domibus.proxy.password", PropertyUsageType.GLOBAL_PROPERTY),
                new DomibusPropertyMetadata("domibus.proxy.nonProxyHosts", PropertyUsageType.GLOBAL_PROPERTY),

                new DomibusPropertyMetadata("domibus.ui.replication.sync.cron.max.rows", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.plugin.notification.active", PropertyUsageType.GLOBAL_PROPERTY),
                new DomibusPropertyMetadata("domibus.nonrepudiation.audit.active", PropertyUsageType.GLOBAL_PROPERTY),
                new DomibusPropertyMetadata("domibus.sendMessage.failure.delete.payload", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.sendMessage.attempt.audit.active", PropertyUsageType.GLOBAL_PROPERTY),
                new DomibusPropertyMetadata("domibus.fourcornermodel.enabled", PropertyUsageType.GLOBAL_PROPERTY),
                new DomibusPropertyMetadata("domibus.logging.payload.print", PropertyUsageType.GLOBAL_PROPERTY),

                new DomibusPropertyMetadata("domibus.attachment.temp.storage.location", PropertyUsageType.GLOBAL_PROPERTY),
                new DomibusPropertyMetadata("domibus.dispatcher.splitAndJoin.concurrency", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),
                new DomibusPropertyMetadata("domibus.dispatcher.splitAndJoin.payloads.schedule.threshold", PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK),

                new DomibusPropertyMetadata("domain.title", PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK),
                new DomibusPropertyMetadata("domibus.userInput.blackList", PropertyUsageType.GLOBAL_PROPERTY),
                new DomibusPropertyMetadata("domibus.userInput.whiteList", PropertyUsageType.GLOBAL_PROPERTY),
        }).collect(Collectors.toMap(x -> x.getName(), x -> x));
    }

    @Override
    public String getKnownPropertyValue(String domainCode, String propertyName) {
        DomibusPropertyMetadata meta = this.getKnownProperties().get(propertyName);
        if (meta == null) {
            throw new IllegalArgumentException(propertyName);
        }

        Domain domain = domainCode == null ? null : this.domainService.getDomain(domainCode);

        if (meta.getUsage() == PropertyUsageType.DOMAIN_PROPERTY_WITH_FALLBACK) {
            return domibusPropertyProvider.getDomainProperty(domain, meta.getName());
        } else if (meta.getUsage() == PropertyUsageType.DOMAIN_PROPERTY_NO_FALLBACK) {
            return domibusPropertyProvider.getProperty(domain, meta.getName());
        } else if (meta.getUsage() == PropertyUsageType.DOMAIN_PROPERTY_RESOLVED) {
            return domibusPropertyProvider.getResolvedProperty(domain, meta.getName());
        } else if (meta.getUsage() == PropertyUsageType.GLOBAL_PROPERTY) {
            return domibusPropertyProvider.getProperty(meta.getName());
        }

        throw new NotImplementedException("Get value for : " + propertyName);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast) {
        DomibusPropertyMetadata meta = this.getKnownProperties().get(propertyName);
        if (meta == null) {
            throw new IllegalArgumentException(propertyName);
        }

        final Domain domain = domainCode == null
                || !domibusConfigurationService.isMultiTenantAware()
                || meta.getUsage() == PropertyUsageType.GLOBAL_PROPERTY
                ? null : domainService.getDomain(domainCode);
        this.domibusPropertyProvider.setPropertyValue(domain, propertyName, propertyValue);

        handlePropertyChange(domainCode, propertyName, propertyValue);

        if (broadcast) { //signal for other nodes
            SignalService signalService = applicationContext.getBean(SignalService.class);
            signalService.signalDomibusPropertyChange(domainCode, propertyName, propertyValue);
        }
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue) {
        setKnownPropertyValue(domainCode, propertyName, propertyValue, true);
    }

    private void handlePropertyChange(String domainCode, String propertyName, String propertyValue) {
        // notify interested listeners that the property value changed
        List<DomibusPropertyChangeListener> listeners = domibusPropertyChangeListeners.stream()
                .filter(listener -> listener.handlesProperty(propertyName))
                .collect(Collectors.toList());
        listeners.forEach(listener -> {
            try {
                listener.propertyValueChanged(domainCode, propertyName, propertyValue);
            } catch (Throwable ex) {
                LOGGER.error("An error occurred while setting property [{}] to [{}] ", propertyName, propertyValue, ex);
            }
        });
    }

    @Override
    public boolean hasKnownProperty(String name) {
        return this.getKnownProperties().containsKey(name);
    }
}
