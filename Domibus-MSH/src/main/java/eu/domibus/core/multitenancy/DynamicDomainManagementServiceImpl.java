package eu.domibus.core.multitenancy;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.DomibusCoreException;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainsAware;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.cache.DomibusCacheService;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.exception.ConfigurationException;
import eu.domibus.core.multitenancy.dao.DomainDao;
import eu.domibus.core.property.PropertyProviderDispatcher;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainsAwareExt;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMAIN_TITLE;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
@Service
public class DynamicDomainManagementServiceImpl implements DynamicDomainManagementService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDomainManagementServiceImpl.class);

    @Autowired
    private DomainService domainService;

    @Autowired
    DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected DomainDao domainDao;

    @Autowired
    PropertyProviderDispatcher propertyProviderDispatcher;

    @Autowired
    DomibusCacheService domibusCacheService;

    @Autowired
    SignalService signalService;

    @Autowired
    List<DomainsAware> domainsAwareList;

    @Autowired
    List<DomainsAwareExt> externalDomainsAwareList;

    @Autowired
    private DomibusCoreMapper coreMapper;

    @Override
    public void addDomain(String domainCode) {
        if (domainService.getDomains().stream().anyMatch(el -> el.getCode().equals(domainCode))) {
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, String.format("Cannot add domain [%s] since is is already added.", domainCode));
        }

        //check domain code is among folders
        if (!domainDao.findAll().stream().anyMatch(el -> el.getCode().equals(domainCode))) {
            throw new ConfigurationException(String.format("Cannot add domain [%s] since there is no corresponding folder or the folder is invalid.", domainCode));
        }
        // temporary  create like this
        Domain domain = new Domain(domainCode, domainCode);

        doAddDomain(domain);

        //notify other nodes in the cluster
        LOG.debug("Broadcasting adding domain [{}]", domainCode);
        try {
            signalService.signalDomainsAdded(domain);
        } catch (Exception ex) {
            LOG.warn("Exception signaling adding domain [{}].", domainCode, ex);
        }
    }

    private void doAddDomain(Domain domain) {
        domibusPropertyProvider.loadProperties(domain);

        //need this eviction since the load properties puts an empty value as domain title
        domibusCacheService.evict(DomibusCacheService.DOMIBUS_PROPERTY_CACHE, propertyProviderDispatcher.getCacheKeyValue(domain, DOMAIN_TITLE));
        domain.setName(domainDao.getDomainTitle(domain));

        //todo  add an add method to domains service ??
        domainService.getDomains().add(domain);

        try {
            List<DomainsAware> executed = new ArrayList<>();
            domainsAwareList.forEach(bean -> {
                try {
                    LOG.info("Adding domain [{}] in bean [{}]", domain, bean);
                    bean.onDomainAdded(domain);
                    executed.add(bean);
                } catch (Exception addExeption) {
                    executed.forEach(executedBean -> {
                        try {
                            executedBean.onDomainRemoved(domain);
                            LOG.info("Removed domain [{}] in bean [{}] due to error on adding [{}]", domain, executedBean, bean);
                        } catch (Exception removeException) {
                            LOG.warn("Error removing the domain [{}] from bean [{}] ", domain, executedBean, addExeption);
                        }
                    });
                    throw addExeption;
                }
            });
        } catch (Exception ex) {
            domainService.getDomains().remove(domain);
            throw new DomibusCoreException(DomibusCoreErrorCode.DOM_001, String.format("Error adding the domain [%s]. ", domain), ex);
        }

        //notify external modules
        DomainDTO domainDTO = coreMapper.domainToDomainDTO(domain);
        externalDomainsAwareList.forEach(el -> {
            LOG.info("Notifying external module [{}]", el);
            try {
                el.onDomainAdded(domainDTO);
            } catch (Exception ex) {
                LOG.warn("Error adding the domain [{}] to module [{}] ", domain, el, ex);
            }
        });

    }

    //todo delete??
//    @Override
//    public void checkAndHandleDomainsChanged() {
//        if (domibusConfigurationService.isSingleTenantAware()) {
//            return;
//        }
//
//        List<Domain> addedDomains = getAddedDomains();
//        if (addedDomains.isEmpty()) {
//            return;
//        }
//
//        addedDomains.forEach(domain -> {
//            try {
//                addDomain(domain);
//
//                LOG.debug("Broadcasting dynamically adding domain [{}]", domain);
//                signalService.signalDomainsAdded(domain);
//            } catch (Exception ex) {
//                //todo return a result type detailing the outcome for each domain
//                LOG.error("Could not add domain [[]]!");
//            }
//        });
//    }
//    private List<Domain> getAddedDomains() {
//        List<Domain> previousDomains = domainService.getDomains();
//        List<Domain> currentDomains = domainDao.findAll();
//        List<Domain> addedDomains = currentDomains.stream()
//                .filter(el -> !previousDomains.contains(el))
//                .collect(Collectors.toList());
//        return addedDomains;
//    }
}
