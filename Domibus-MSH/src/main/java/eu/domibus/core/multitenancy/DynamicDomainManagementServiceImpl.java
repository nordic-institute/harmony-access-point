package eu.domibus.core.multitenancy;

import eu.domibus.api.cluster.SignalService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.DomainsAware;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.converter.DomibusCoreMapper;
import eu.domibus.core.multitenancy.dao.DomainDao;
import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainsAwareExt;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 5.0
 * <p>
 * Service responsible with adding domains at runtime
 */
@Service
public class DynamicDomainManagementServiceImpl implements DynamicDomainManagementService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DynamicDomainManagementServiceImpl.class);

    private final DomainService domainService;

    private final DomibusPropertyProvider domibusPropertyProvider;

    private final DomainDao domainDao;

    private final SignalService signalService;

    private final List<DomainsAware> domainsAwareList;

    private final List<DomainsAwareExt> externalDomainsAwareList;

    private final DomibusCoreMapper coreMapper;

    private final DomibusConfigurationService domibusConfigurationService;

    public DynamicDomainManagementServiceImpl(DomainService domainService,
                                              DomibusPropertyProvider domibusPropertyProvider,
                                              DomainDao domainDao,
                                              SignalService signalService,
                                              List<DomainsAware> domainsAwareList,
                                              List<DomainsAwareExt> externalDomainsAwareList,
                                              DomibusCoreMapper coreMapper, DomibusConfigurationService domibusConfigurationService) {

        this.domainService = domainService;
        this.domibusPropertyProvider = domibusPropertyProvider;
        this.domainDao = domainDao;
        this.signalService = signalService;
        this.domainsAwareList = domainsAwareList;
        this.externalDomainsAwareList = externalDomainsAwareList;
        this.coreMapper = coreMapper;
        this.domibusConfigurationService = domibusConfigurationService;
    }

    @Override
    public void addDomain(String domainCode) {
        validateAddition(domainCode);

        Domain domain = new Domain(domainCode, domainCode);

        internalAddDomain(domain);

        notifyExternalModulesofAddition(domain);

        notifyClusterNodesOfAddition(domainCode);
    }

    @Override
    public void removeDomain(String domainCode) {
        validateRemoval(domainCode);

        Domain domain = new Domain(domainCode, domainCode);

        internalRemoveDomain(domain);

        notifyExternalModulesOfRemoval(domain);

    }

    protected void validateRemoval(String domainCode) {
        if (domibusConfigurationService.isSingleTenantAware()) {
            throw new DomibusDomainException(String.format("Cannot remove a domain in single tenancy mode.", domainCode));
        }

        if (!domainService.getDomains().stream().anyMatch(el -> StringUtils.equals(el.getCode(), domainCode))) {
            throw new DomibusDomainException(String.format("Cannot remove domain [%s] since is is not present.", domainCode));
        }
    }

    protected void internalRemoveDomain(Domain domain) {
        domibusPropertyProvider.removeProperties(domain);

        domainService.removeDomain(domain.getCode());

        try {
            notifyInternalBeansOfRemoval(domain);
        } catch (Exception ex) {
            domainService.getDomains().add(domain);
            throw new DomibusDomainException(String.format("Error adding the domain [%s]. ", domain), ex);
        }
    }

    protected void notifyInternalBeansOfRemoval(Domain domain) throws Exception {
        List<DomainsAware> executedSuccessfully = new ArrayList<>();
        for (DomainsAware bean : domainsAwareList) {
            try {
                LOG.debug("Removing domain [{}] in bean [{}]", domain, bean);
                bean.onDomainRemoved(domain);
                executedSuccessfully.add(bean);
            } catch (Exception addException) {
                handleRemoveDomainException(domain, executedSuccessfully, bean, addException);
            }
        }
    }

    protected void handleRemoveDomainException(Domain domain, List<DomainsAware> executedSuccessfully, DomainsAware bean, Exception addException) throws Exception {
        executedSuccessfully.forEach(executedBean -> {
            try {
                executedBean.onDomainRemoved(domain);
                LOG.info("Removed domain [{}] in bean [{}] due to error on adding [{}]", domain, executedBean, bean);
            } catch (Exception removeException) {
                LOG.warn("Error removing the domain [{}] from bean [{}] ", domain, executedBean, addException);
            }
        });
        throw addException;
    }

    protected void notifyExternalModulesOfRemoval(Domain domain) {
        DomainDTO domainDTO = coreMapper.domainToDomainDTO(domain);
        externalDomainsAwareList.forEach(el -> {
            LOG.debug("Notifying external module [{}]", el);
            try {
                el.onDomainRemoved(domainDTO);
            } catch (Exception ex) {
                LOG.warn("Error removing domain [{}] to module [{}] ", domain, el, ex);
            }
        });
    }

    protected void notifyClusterNodesOfRemoval(String domainCode) {
        //notify other nodes in the cluster
        LOG.debug("Broadcasting removing domain [{}]", domainCode);
        try {
            signalService.signalDomainsRemoved(domainCode);
        } catch (Exception ex) {
            LOG.warn("Exception signaling removing domain [{}].", domainCode, ex);
        }
    }


    protected void validateAddition(String domainCode) {
        if (domibusConfigurationService.isSingleTenantAware()) {
            throw new DomibusDomainException(String.format("Cannot add a domain in single tenancy mode.", domainCode));
        }

        if (domainService.getDomains().stream().anyMatch(el -> StringUtils.equals(el.getCode(), domainCode))) {
            throw new DomibusDomainException(String.format("Cannot add domain [%s] since is is already added.", domainCode));
        }

        if (!domainDao.findAll().stream().anyMatch(el -> StringUtils.equals(el.getCode(), domainCode))) {
            throw new DomibusDomainException(String.format("Cannot add domain [%s] since there is no corresponding folder or the folder is invalid.", domainCode));
        }
    }

    protected void internalAddDomain(Domain domain) {
        domibusPropertyProvider.loadProperties(domain);

        domainService.add(domain);

        try {
            notifyInternalBeansOfAddition(domain);
        } catch (Exception ex) {
            domainService.getDomains().remove(domain);
            throw new DomibusDomainException(String.format("Error adding the domain [%s]. ", domain), ex);
        }
    }

    protected void notifyInternalBeansOfAddition(Domain domain) throws Exception {
        List<DomainsAware> executedSuccessfully = new ArrayList<>();
        for (DomainsAware bean : domainsAwareList) {
            try {
                LOG.debug("Adding domain [{}] in bean [{}]", domain, bean);
                bean.onDomainAdded(domain);
                executedSuccessfully.add(bean);
            } catch (Exception addException) {
                handleAddDomainException(domain, executedSuccessfully, bean, addException);
            }
        }
    }

    protected void handleAddDomainException(Domain domain, List<DomainsAware> executedSuccessfully, DomainsAware bean, Exception addException) throws Exception {
        executedSuccessfully.forEach(executedBean -> {
            try {
                executedBean.onDomainRemoved(domain);
                LOG.info("Removed domain [{}] in bean [{}] due to error on adding [{}]", domain, executedBean, bean);
            } catch (Exception removeException) {
                LOG.warn("Error removing the domain [{}] from bean [{}] ", domain, executedBean, addException);
            }
        });
        throw addException;
    }

    protected void notifyExternalModulesofAddition(Domain domain) {
        DomainDTO domainDTO = coreMapper.domainToDomainDTO(domain);
        externalDomainsAwareList.forEach(el -> {
            LOG.debug("Notifying external module [{}]", el);
            try {
                el.onDomainAdded(domainDTO);
            } catch (Exception ex) {
                LOG.warn("Error adding the domain [{}] to module [{}] ", domain, el, ex);
            }
        });
    }

    protected void notifyClusterNodesOfAddition(String domainCode) {
        //notify other nodes in the cluster
        LOG.debug("Broadcasting adding domain [{}]", domainCode);
        try {
            signalService.signalDomainsAdded(domainCode);
        } catch (Exception ex) {
            LOG.warn("Exception signaling adding domain [{}].", domainCode, ex);
        }
    }

}
