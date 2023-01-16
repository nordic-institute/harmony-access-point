package eu.domibus.core.multitenancy;

import eu.domibus.api.cache.DomibusLocalCacheService;
import eu.domibus.api.multitenancy.DomainTaskExecutor;
import eu.domibus.api.multitenancy.UserDomainService;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.DomibusUserDetails;
import eu.domibus.common.DomibusCacheConstants;
import eu.domibus.core.multitenancy.dao.UserDomainDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.web.security.DomibusUserDetailsImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;

import java.util.ArrayList;

/**
 * @author Ion Perpegel
 * @since 4.0
 */
    public class UserDomainServiceMultiDomainImpl implements UserDomainService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserDomainServiceMultiDomainImpl.class);

    @Autowired
    protected DomainTaskExecutor domainTaskExecutor;

    @Autowired
    protected UserDomainDao userDomainDao;

    @Autowired
    protected DomibusLocalCacheService domibusLocalCacheService;

    @Autowired
    protected AuthUtils authUtils;

    /**
     * Get the domain associated to the provided user name from the general schema. <br>
     * This is done in a separate thread as the DB connection is cached per thread and cannot be changed anymore to the schema of the associated domain
     *
     * @return the domain code of the user
     */
    @Cacheable(cacheManager = DomibusCacheConstants.CACHE_MANAGER, value = DomibusLocalCacheService.USER_DOMAIN_CACHE, key = "#userName")
    @Override
    public String getDomainForUser(String userName) {
        LOG.debug("Searching domain for user named [{}]", userName);
        String domain = domainTaskExecutor.submit(() -> userDomainDao.findDomain(userName));
        LOG.debug("Found domain [{}] for user named [{}]", domain, userName);
        return domain;
    }

    /**
     * Get the preferred domain associated to the super user from the general schema. <br>
     * This is done in a separate thread as the DB connection is cached per thread and cannot be changed anymore to the schema of the associated domain
     *
     * @return the code of the preferred domain of a super user
     */
    @Cacheable(cacheManager = DomibusCacheConstants.CACHE_MANAGER, value = DomibusLocalCacheService.PREFERRED_USER_DOMAIN_CACHE, key = "#user", unless="#result == null")
    @Override
    public String getPreferredDomainForUser(String user) {
        LOG.debug("Searching preferred domain for user [{}]", user);
        String domain = domainTaskExecutor.submit(() -> userDomainDao.findPreferredDomain(user));
        LOG.debug("Found preferred domain [{}] for user [{}]", domain, user);
        return domain;
    }

    @Override
    public void setDomainForUser(String user, String domainCode) {
        LOG.debug("Setting domain [{}] for user [{}]", domainCode, user);

        executeInContext(() -> setDomainByUser(user, domainCode));
    }

    @Override
    public void setPreferredDomainForUser(String user, String domainCode) {
        LOG.debug("Setting preferred domain [{}] for user [{}]", domainCode, user);

        executeInContext(() -> setPreferredDomainByUser(user, domainCode));
    }

    @Override
    public void deleteDomainForUser(String user) {
        LOG.debug("Deleting domain for user [{}]", user);

        executeInContext(() -> deleteDomainByUser(user));
    }

    private void deleteDomainByUser(String user) {
        userDomainDao.deleteUserDomain(user);
        domibusLocalCacheService.clearCache(DomibusLocalCacheService.USER_DOMAIN_CACHE);
    }

    private void setDomainByUser(String user, String domainCode) {
        userDomainDao.updateOrCreateUserDomain(user, domainCode);
        domibusLocalCacheService.clearCache(DomibusLocalCacheService.USER_DOMAIN_CACHE);
    }

    private void setPreferredDomainByUser(String user, String domainCode) {
        userDomainDao.updateOrCreateUserPreferredDomain(user, domainCode);
        domibusLocalCacheService.clearCache(DomibusLocalCacheService.PREFERRED_USER_DOMAIN_CACHE);
    }

    protected void executeInContext(Runnable method) {
        DomibusUserDetails ud = authUtils.getUserDetails() != null
                ? authUtils.getUserDetails()
                : new DomibusUserDetailsImpl("domibus", StringUtils.EMPTY, new ArrayList<>());

        domainTaskExecutor.submit(() -> authUtils.runWithSecurityContext(() -> {
            LOG.putMDC(DomibusLogger.MDC_USER, ud.getUsername());
            method.run();
        }, ud.getUsername(), ud.getPassword()));
    }
}
