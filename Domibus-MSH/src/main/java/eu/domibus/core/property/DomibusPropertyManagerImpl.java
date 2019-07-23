package eu.domibus.core.property;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyManager;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.property.DomibusPropertyChangeNotifier;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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
    private DomibusPropertyChangeNotifier domibusPropertyChangeNotifier;

    /**
     * Returns the properties that this PropertyProvider is able to handle.
     *
     * @return a map
     * @implNote This list will be moved in the database eventually.
     */
    @Override
    public Map<String, DomibusPropertyMetadata> getKnownProperties() {

        return Arrays.stream(new DomibusPropertyMetadata[]{
                new DomibusPropertyMetadata("domibus.UI.title.name", true, true),
                new DomibusPropertyMetadata("domibus.ui.replication.enabled", true, true),
                new DomibusPropertyMetadata("domibus.ui.support.team.name", true, true),
                new DomibusPropertyMetadata("domibus.ui.support.team.email", true, true),

                new DomibusPropertyMetadata("domibus.security.keystore.location", true, false),
                new DomibusPropertyMetadata("domibus.security.keystore.type", true, false),
                new DomibusPropertyMetadata("domibus.security.keystore.password", true, false),
                new DomibusPropertyMetadata("domibus.security.key.private.alias", true, false),

                new DomibusPropertyMetadata("domibus.security.truststore.location", true, false),
                new DomibusPropertyMetadata("domibus.security.truststore.type", true, false),
                new DomibusPropertyMetadata("domibus.security.truststore.password", true, false),

                new DomibusPropertyMetadata("domibus.auth.unsecureLoginAllowed", true, false),
                new DomibusPropertyMetadata("domibus.console.login.maximum.attempt", true, true),
                new DomibusPropertyMetadata("domibus.console.login.suspension.time", true, true),
                new DomibusPropertyMetadata("domibus.certificate.revocation.offset", true, false),
                new DomibusPropertyMetadata("domibus.certificate.crl.excludedProtocols", true, false),

                new DomibusPropertyMetadata("domibus.plugin.login.maximum.attempt", true, false),
                new DomibusPropertyMetadata("domibus.plugin.login.suspension.time", true, true),

                new DomibusPropertyMetadata("domibus.passwordPolicy.pattern", true, true),
                new DomibusPropertyMetadata("domibus.passwordPolicy.validationMessage", true, true),
                new DomibusPropertyMetadata("domibus.passwordPolicy.expiration", true, true),
                new DomibusPropertyMetadata("domibus.passwordPolicy.defaultPasswordExpiration", true, true),
                new DomibusPropertyMetadata("domibus.passwordPolicy.warning.beforeExpiration", true, true),
                new DomibusPropertyMetadata("domibus.passwordPolicy.dontReuseLast", true, true),
                new DomibusPropertyMetadata("domibus.passwordPolicy.checkDefaultPassword", false),

                new DomibusPropertyMetadata("domibus.plugin.passwordPolicy.pattern", true, true),
                new DomibusPropertyMetadata("domibus.plugin.passwordPolicy.validationMessage", true, true),
                new DomibusPropertyMetadata("domibus.passwordPolicy.plugin.expiration", true, true),
                new DomibusPropertyMetadata("domibus.passwordPolicy.plugin.defaultPasswordExpiration", true, true),
                new DomibusPropertyMetadata("domibus.passwordPolicy.plugin.dontReuseLast", true, true),

                new DomibusPropertyMetadata("domibus.attachment.storage.location", true, false),
                new DomibusPropertyMetadata("domibus.payload.encryption.active", true, true),

                new DomibusPropertyMetadata("domibus.msh.messageid.suffix", true, true),
                new DomibusPropertyMetadata("domibus.msh.retry.messageExpirationDelay", false),

                new DomibusPropertyMetadata("domibus.dynamicdiscovery.useDynamicDiscovery", true, true),
                new DomibusPropertyMetadata("domibus.smlzone", true, true),
                new DomibusPropertyMetadata("domibus.dynamicdiscovery.client.specification", true, true),
                new DomibusPropertyMetadata("domibus.dynamicdiscovery.peppolclient.mode", true, true),
                new DomibusPropertyMetadata("domibus.dynamicdiscovery.oasisclient.regexCertificateSubjectValidation", true, true),
                new DomibusPropertyMetadata("domibus.dynamicdiscovery.partyid.responder.role", true, true),
                new DomibusPropertyMetadata("domibus.dynamicdiscovery.partyid.type", true, true),
                new DomibusPropertyMetadata("domibus.dynamicdiscovery.transportprofileas4", true, true),

                new DomibusPropertyMetadata("domibus.listPendingMessages.maxCount", false),
                new DomibusPropertyMetadata("domibus.jms.queue.maxBrowseSize", false), //there is one place at init time that it is not refreshed
                new DomibusPropertyMetadata("domibus.jms.internalQueue.expression", false),

                new DomibusPropertyMetadata("domibus.receiver.certificate.validation.onsending", true, true),
                new DomibusPropertyMetadata("domibus.sender.certificate.validation.onsending", true, true),
                new DomibusPropertyMetadata("domibus.sender.certificate.validation.onreceiving", true, true),
                new DomibusPropertyMetadata("domibus.sender.trust.validation.onreceiving", true, true),
                new DomibusPropertyMetadata("domibus.sender.trust.validation.expression", true, false),
                new DomibusPropertyMetadata("domibus.sender.certificate.subject.check", true, true),
                new DomibusPropertyMetadata("domibus.sender.trust.validation.truststore_alias", true, true),
                new DomibusPropertyMetadata("domibus.sendMessage.messageIdPattern", true, false),

                new DomibusPropertyMetadata("domibus.dispatcher.connectionTimeout", true, true),
                new DomibusPropertyMetadata("domibus.dispatcher.receiveTimeout", true, true),
                new DomibusPropertyMetadata("domibus.dispatcher.allowChunking", true, true),
                new DomibusPropertyMetadata("domibus.dispatcher.chunkingThreshold", true, true),
                new DomibusPropertyMetadata("domibus.dispatcher.concurency", true, true),
                new DomibusPropertyMetadata("domibus.dispatcher.largeFiles.concurrency", true, true),
                new DomibusPropertyMetadata("domibus.dispatcher.cacheable", true, true),
                new DomibusPropertyMetadata("domibus.dispatcher.connection.keepAlive", true, true),
                new DomibusPropertyMetadata("domibus.retentionWorker.message.retention.downloaded.max.delete", true, true),
                new DomibusPropertyMetadata("domibus.retentionWorker.message.retention.not_downloaded.max.delete", true, true),
                new DomibusPropertyMetadata("domibus.retention.jms.concurrency", true, true),
                new DomibusPropertyMetadata("domibus.dispatch.ebms.error.unrecoverable.retry", true, true),

                new DomibusPropertyMetadata("domibus.proxy.enabled", false),
                new DomibusPropertyMetadata("domibus.proxy.http.host", false),
                new DomibusPropertyMetadata("domibus.proxy.http.port", false),
                new DomibusPropertyMetadata("domibus.proxy.user", false),
                new DomibusPropertyMetadata("domibus.proxy.password", false),
                new DomibusPropertyMetadata("domibus.proxy.nonProxyHosts", false),

                new DomibusPropertyMetadata("domibus.ui.replication.sync.cron.max.rows", true, true),
                new DomibusPropertyMetadata("domibus.plugin.notification.active", false),
                new DomibusPropertyMetadata("domibus.nonrepudiation.audit.active", false),
                new DomibusPropertyMetadata("domibus.sendMessage.failure.delete.payload", true, true),
                new DomibusPropertyMetadata("domibus.sendMessage.attempt.audit.active", false),
                new DomibusPropertyMetadata("domibus.fourcornermodel.enabled", false),
                new DomibusPropertyMetadata("domibus.logging.payload.print", false),

                new DomibusPropertyMetadata("domibus.attachment.temp.storage.location", false),
                new DomibusPropertyMetadata("domibus.dispatcher.splitAndJoin.concurrency", true, true),
                new DomibusPropertyMetadata("domibus.dispatcher.splitAndJoin.payloads.schedule.threshold", true, true),

                new DomibusPropertyMetadata("domain.title", true, false),
                new DomibusPropertyMetadata("domibus.userInput.blackList", false),
                new DomibusPropertyMetadata("domibus.userInput.whiteList", false),

                DomibusPropertyMetadata.getGlobalProperty("domibus.account.unlock.cron"),
                DomibusPropertyMetadata.getGlobalProperty("domibus.certificate.check.cron"),
                DomibusPropertyMetadata.getGlobalProperty("domibus.plugin.account.unlock.cron"),
                DomibusPropertyMetadata.getGlobalProperty("domibus.passwordPolicies.check.cron"),
                DomibusPropertyMetadata.getGlobalProperty("domibus.plugin_passwordPolicies.check.cron"),
                DomibusPropertyMetadata.getGlobalProperty("domibus.payload.temp.job.retention.cron"),
                new DomibusPropertyMetadata("domibus.msh.retry.cron", true, true),
                new DomibusPropertyMetadata("domibus.retentionWorker.cronExpression", true, true),
                new DomibusPropertyMetadata("domibus.msh.pull.cron", true, true),
                new DomibusPropertyMetadata("domibus.pull.retry.cron", true, true),
                new DomibusPropertyMetadata("domibus.alert.cleaner.cron", true, true),
                new DomibusPropertyMetadata("domibus.alert.retry.cron", true, true),
                DomibusPropertyMetadata.getGlobalProperty("domibus.alert.super.cleaner.cron"),
                DomibusPropertyMetadata.getGlobalProperty("domibus.alert.super.retry.cron"),
                DomibusPropertyMetadata.getGlobalProperty("domibus.ui.replication.sync.cron"),
                new DomibusPropertyMetadata("domibus.splitAndJoin.receive.expiration.cron", true, true),

        }).collect(Collectors.toMap(x -> x.getName(), x -> x));
    }

    @Override
    public String getKnownPropertyValue(String domainCode, String propertyName) {
        DomibusPropertyMetadata meta = this.getKnownProperties().get(propertyName);
        if (meta == null) {
            throw new IllegalArgumentException(propertyName);
        }

        Domain domain = domainCode == null ? null : this.domainService.getDomain(domainCode);

        if (!meta.isDomainSpecific()) {
            return domibusPropertyProvider.getProperty(meta.getName());
        } else {
            if (meta.isWithFallback()) {
                return domibusPropertyProvider.getDomainProperty(domain, meta.getName());
            } else if (!meta.isWithFallback()) {
                return domibusPropertyProvider.getProperty(domain, meta.getName());
            }
        }

        throw new NotImplementedException("Get value for : " + propertyName);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue, boolean broadcast) {
        DomibusPropertyMetadata propMeta = this.getKnownProperties().get(propertyName);
        if (propMeta == null) {
            throw new IllegalArgumentException(propertyName);
        }

        Domain propertyDomain = null;
        if (domibusConfigurationService.isMultiTenantAware()) {
            propertyDomain = domainCode == null ? null : domainService.getDomain(domainCode);
            propertyDomain = propMeta.isDomainSpecific() ? propertyDomain : null;
        }
        this.domibusPropertyProvider.setPropertyValue(propertyDomain, propertyName, propertyValue);

        boolean shouldBroadcast = broadcast && propMeta.isClusterAware();
        domibusPropertyChangeNotifier.signalPropertyValueChanged(domainCode, propertyName, propertyValue, shouldBroadcast);
    }

    @Override
    public void setKnownPropertyValue(String domainCode, String propertyName, String propertyValue) {
        setKnownPropertyValue(domainCode, propertyName, propertyValue, true);
    }


    @Override
    public boolean hasKnownProperty(String name) {
        return this.getKnownProperties().containsKey(name);
    }
}
