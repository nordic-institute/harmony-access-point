package eu.domibus.api.security;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public interface IDomibusUserDetails extends UserDetails {

    boolean isDefaultPasswordUsed();

    void setDefaultPasswordUsed(boolean defaultPasswordUsed);

    String getDomain();

    void setDomain(String domain);

    Set<String> getAvailableDomainCodes();

    void setAvailableDomainCodes(Set<String> availableDomainCodes);

    void addDomainCode(String domainCode);

    void removeDomainCode(String domainCode);

    Integer getDaysTillExpiration();

    void setDaysTillExpiration(Integer daysTillExpiration);

    boolean isExternalAuthProvider();

    void setExternalAuthProvider(boolean externalAuthProvider);
}
