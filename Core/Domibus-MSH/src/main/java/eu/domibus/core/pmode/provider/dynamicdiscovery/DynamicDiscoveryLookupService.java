package eu.domibus.core.pmode.provider.dynamicdiscovery;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.dynamicdyscovery.DynamicDiscoveryLookupEntity;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.MultiDomainCryptoService;
import eu.domibus.api.pmode.PModeEventListener;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Cosmin Baciu
 * @since 5.1.1
 */
@Service
public class DynamicDiscoveryLookupService implements PModeEventListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDiscoveryLookupService.class);

    private static final String PMODE_EVENT_LISTENER_NAME = DynamicDiscoveryLookupService.class.getName();

    private final Map<String, String> finalRecipientAccessPointUrls = new ConcurrentHashMap<>();

    protected DynamicDiscoveryLookupDao dynamicDiscoveryLookupDao;
    protected CertificateService certificateService;
    protected MultiDomainCryptoService multiDomainCryptoService;
    protected DomainContextProvider domainProvider;
    protected DynamicDiscoveryLookupHelper dynamicDiscoveryLookupHelper;
    protected PModeProvider pModeProvider;
    protected SignalService signalService;

    public DynamicDiscoveryLookupService(DynamicDiscoveryLookupDao dynamicDiscoveryLookupDao, CertificateService certificateService, MultiDomainCryptoService multiDomainCryptoService, DomainContextProvider domainProvider, DynamicDiscoveryLookupHelper dynamicDiscoveryLookupHelper, PModeProvider pModeProvider, SignalService signalService) {
        this.dynamicDiscoveryLookupDao = dynamicDiscoveryLookupDao;
        this.certificateService = certificateService;
        this.multiDomainCryptoService = multiDomainCryptoService;
        this.domainProvider = domainProvider;
        this.dynamicDiscoveryLookupHelper = dynamicDiscoveryLookupHelper;
        this.pModeProvider = pModeProvider;
        this.signalService = signalService;
    }

    @Override
    public String getName() {
        return PMODE_EVENT_LISTENER_NAME;
    }

    @Override
    public void onRefreshPMode() {
        LOG.info("Clearing the final recipient cache");

        //TODO do we need to clear this cache on PMode refresh?
        clearFinalRecipientAccessPointUrlsCache();
    }

    /**
     * Save the final recipient URL in the database and in the memory cache
     * Saving the time when the DDC lookup was performed last time
     * We use the discovered time as retention parameter to clean up using a job the dynamically discovered certificates and parties
     */
    @Override
    public void afterDynamicDiscoveryLookup(String finalRecipientValue, String finalRecipientUrl, String partyName, String partyType, List<String> partyProcessNames, String certificateCn, X509Certificate x509Certificate) {
        saveDynamicDiscoveryLookupTime(finalRecipientValue, finalRecipientUrl, partyName, partyType, partyProcessNames, certificateCn, x509Certificate);
    }

    public String getEndpointURL(String finalRecipient) {
        if (finalRecipient == null) {
            LOG.debug("No final recipient provided");
            return null;
        }
        String finalRecipientAPUrl = finalRecipientAccessPointUrls.get(finalRecipient);

        if (StringUtils.isNotBlank(finalRecipientAPUrl)) {
            LOG.debug("Getting from cache the endpoint URL for final recipient [{}]", finalRecipient);
            return finalRecipientAPUrl;
        }
        LOG.debug("Checking from database the endpoint URL for final recipient [{}]", finalRecipient);
        final DynamicDiscoveryLookupEntity lookupEntity = dynamicDiscoveryLookupDao.findByFinalRecipient(finalRecipient);
        if (lookupEntity == null) {
            LOG.debug("No endpoint URL found in the database for final recipient [{}]", finalRecipient);
            return null;
        }
        finalRecipientAPUrl = lookupEntity.getFinalRecipientUrl();
        LOG.debug("Updating the cache from database for final recipient [{}] with endpoint URL [{}]", finalRecipient, finalRecipientAPUrl);
        finalRecipientAccessPointUrls.put(finalRecipient, finalRecipientAPUrl);
        return finalRecipientAPUrl;
    }

    public void clearFinalRecipientAccessPointUrlsCache() {
        finalRecipientAccessPointUrls.clear();
    }

    public Map<String, String> getFinalRecipientAccessPointUrls() {
        return finalRecipientAccessPointUrls;
    }

    /**
     * Saves the time when the DDC lookup was performed last time from SMP
     */
    @Transactional
    public void saveDynamicDiscoveryLookupTime(String finalRecipientValue,
                                               String finalRecipientUrl,
                                               String partyName,
                                               String partyType,
                                               List<String> partyProcessNames,
                                               String certificateCn,
                                               final X509Certificate certificate) {
        LOG.debug("Saving the certificate discovery date for certificate with alias [{}]", finalRecipientValue);

        DynamicDiscoveryLookupEntity lookupEntity = dynamicDiscoveryLookupDao.findByFinalRecipient(finalRecipientValue);
        if (lookupEntity == null) {
            LOG.info("Creating DDC lookup entity for final recipient [{}]", finalRecipientValue);
            lookupEntity = new DynamicDiscoveryLookupEntity();
            lookupEntity.setFinalRecipientValue(finalRecipientValue);
        }

        lookupEntity.setFinalRecipientUrl(finalRecipientUrl);
        lookupEntity.setPartyName(partyName);
        lookupEntity.setPartyType(partyType);
        lookupEntity.setPartyProcesses(partyProcessNames);
        lookupEntity.setCn(certificateCn);

        if (certificate == null) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_004, "Could not save the dynamic discovery lookup entity: provided X509 certificate is null");
        }

        lookupEntity.setSubject(certificate.getSubjectDN().getName());
        lookupEntity.setSerial(certificate.getSerialNumber() + "");
        lookupEntity.setIssuerSubject(certificate.getIssuerDN().getName());
        final String fingerprint = certificateService.extractFingerprints(certificate);
        lookupEntity.setFingerprint(fingerprint);

        final Date dynamicDiscoveryUpdateTime = new Date();
        lookupEntity.setDynamicDiscoveryTime(dynamicDiscoveryUpdateTime);

        //update the final recipient URL cache
        finalRecipientAccessPointUrls.put(finalRecipientValue, finalRecipientUrl);

        LOG.debug("Saving/updating DDC lookup details [{}]", lookupEntity);
        try {
            dynamicDiscoveryLookupDao.createOrUpdate(lookupEntity);
        } catch (DataIntegrityViolationException e) {
            //in a cluster environment, a DDC lookup entity can be created/updated in parallel and a unique constraint is raised
            //in case a constraint violation occurs we don't do anything because the other node added the latest data in parallel
            LOG.warn("Could not create or update DDC lookup entity with entity id [{}]. It could be that another node updated the same entity in parallel", lookupEntity.getEntityId(), e);
        }
    }

    /**
     * Cleans the dynamically discovered entries which were not discovered more recently than the specified number of hours
     * It deletes the DCC entries from the truststore/PMode/database
     */
    public void deleteDDCLookupEntriesNotDiscoveredInTheLastPeriod(int retentionInHours) {
        Date dateLimit = DateUtils.addHours(new Date(), retentionInHours * -1);

        //clean expired DDC certificates from the truststore
        deleteFromTruststoreExpiredDdcCertificates(dateLimit);

        //clean expired DDC parties from the PMode
        deleteFromPmodeExpiredDdcParties(dateLimit);

        //clean expired DDC final recipients from the database and from the cache
        deleteExpiredDdcFinalRecipients(dateLimit);
    }

    protected void deleteFromTruststoreExpiredDdcCertificates(Date dateLimit) {
        //DDC certificates
        LOG.info("Getting the DDC certificates which were not discovered more recently than [{}]", dateLimit);
        final List<String> certificateCNs = dynamicDiscoveryLookupDao.findCertificatesNotDiscoveredInTheLastPeriod(dateLimit);
        LOG.info("Deleting [{}] from truststore the DDC certificates not discovered more recently than [{}] with the following CNs: [{}]", certificateCNs.size(), dateLimit, certificateCNs);
        deleteCertificatesFromTruststore(certificateCNs);
    }

    protected void deleteFromPmodeExpiredDdcParties(Date dateLimit) {
        LOG.info("Getting the DDC parties which were not discovered more recently than [{}]", dateLimit);
        List<String> partyNames = dynamicDiscoveryLookupDao.findPartiesNotDiscoveredInTheLastPeriod(dateLimit);
        LOG.info("Deleting [{}] from PMode the DDC parties not discovered more recently than [{}] with the following party names: [{}]", partyNames.size(), dateLimit, partyNames);
        deletePartiesFromPMode(partyNames);

        //signal to delete pmode parties also from the other members from the cluster
        signalService.signalDeletePmodeParties(partyNames);
    }

    public void deleteExpiredDdcFinalRecipients(Date dateLimit) {
        final List<DynamicDiscoveryLookupEntity> dynamicDiscoveryLookupEntities = dynamicDiscoveryLookupHelper.deleteFromDatabaseExpiredDdcFinalRecipients(dateLimit);
        if (CollectionUtils.isEmpty(dynamicDiscoveryLookupEntities)) {
            LOG.debug("There are no expired DDC final recipients entities to delete");
            return;
        }

        final List<String> finalRecipients = dynamicDiscoveryLookupEntities.stream().map(discoveryLookupEntity -> discoveryLookupEntity.getFinalRecipientValue()).collect(Collectors.toList());

        //removing from the URL cache the final recipients value and signal to delete it also from the other members from the cluster
        deleteFinalRecipientsFromCache(finalRecipients);
        signalService.signalDeleteFinalRecipientCache(finalRecipients);
    }

    public void deleteFinalRecipientsFromCache(List<String> finalRecipients) {
        for (String finalRecipient : finalRecipients) {
            finalRecipientAccessPointUrls.remove(finalRecipient);
        }
    }

    protected void deleteCertificatesFromTruststore(List<String> certificateCNs) {
        final Domain currentDomain = domainProvider.getCurrentDomain();
        multiDomainCryptoService.removeCertificate(currentDomain, certificateCNs);
    }

    /**
     * Delete the provided parties from the PMode party list businessProcesses->parties and from each process responder parties process->responderParties
     */
    public void deletePartiesFromPMode(List<String> partyNames) {
        for (String partyName : partyNames) {
            final Domain currentDomain = domainProvider.getCurrentDomain();
            LOG.info("Deleting from the PMode the DCC party with alias [{}] for domain [{}]", partyName, currentDomain);

            //Removes party from the list of available parties businessProcesses->parties
            pModeProvider.removeParty(partyName);

            //Removes party from the list of responderParties from all processes->responderParties
            pModeProvider.removeReceiverParty(partyName);
        }
    }
}
