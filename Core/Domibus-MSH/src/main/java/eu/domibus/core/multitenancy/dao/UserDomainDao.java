package eu.domibus.core.multitenancy.dao;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface UserDomainDao {

    String findDomain(String userName);

    String findPreferredDomain(String userName);

    void updateOrCreateDomain(String userName, String domainCode);

    void updateOrCreatePreferredDomain(String userName, String preferredDomainCode);

    void deleteUserDomain(String userName);

    int deleteByDomain(String domain);

    List<UserDomainEntity> listPreferredDomains();

}
