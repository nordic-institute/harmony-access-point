package eu.domibus.api.multitenancy;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
public interface UserDomainService {

    String getDomainForUser(String user);

    void setDomainForUser(String user, String domainCode);

    String getPreferredDomainForUser(String user);

    void setPreferredDomainForUser(String user, String domainCode);

    void deleteDomainForUser(String user);
}
