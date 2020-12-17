package eu.domibus.core.multitenancy;

import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

/**
 * @author Ion Perpegel(nperpion)
 * @since 4.0
 */
public class UserDomainServiceSingleDomainImpl implements UserDomainService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserDomainServiceSingleDomainImpl.class);

    /**
     * Get the domain associated to the provided user. <br>
     * In single domain mode, this is always the DEFAULT domain.
     *
     * @return the code of the default domain
     */
    @Override
    public String getDomainForUser(String user) {
        LOG.debug("Using default domain for user [{}]", user);
        return DomainService.DEFAULT_DOMAIN.getCode();
    }

    /**
     * Get the preferred domain associated to a super user. <br>
     * In single domain mode, this is always the DEFAULT domain.
     *
     * @return the code of the default domain
     */
    @Override
    public String getPreferredDomainForUser(String user) {
        return this.getDomainForUser(user);
    }

    @Override
    public void setDomainForUser(String user, String domainCode) {
        LOG.trace("set domain for user [{}] in single tenancy mode", user);
    }

    @Override
    public void setPreferredDomainForUser(String user, String domainCode) {
        LOG.trace("set preferred domain for user [{}] in single tenancy mode", user);
    }

    @Override
    public void deleteDomainForUser(String user) {
        LOG.trace("delete domain for user [{}] in single tenancy mode", user);
    }

}
