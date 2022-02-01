package eu.domibus.web.security;

import com.google.common.collect.Lists;
import eu.domibus.core.user.ui.UserRole;
import eu.domibus.core.user.ui.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/**
 * @author Thomas Dussart, Catalin Enache
 * @since 3.3
 */
public class DomibusUserDetails implements UserDetails {
    private final UserDetails springUser;

    private boolean defaultPasswordUsed;

    private Integer daysTillExpiration;

    private boolean externalAuthProvider;

    protected String domain;

    /**
     * The codes of all available domains this user is able to access or control.
     */
    private Set<String> availableDomainCodes = new HashSet<>();

    public DomibusUserDetails(final User user) {
        this.defaultPasswordUsed = user.hasDefaultPassword();
        springUser = org.springframework.security.core.userdetails.User
                .withUsername(user.getUserName())
                .password(user.getPassword())
                .authorities(getGrantedAuthorities(user.getRoles()))
                .build();
    }

    /**
     * Build the user detail object from Spring principal only
     *
     * @param username
     * @param password
     * @param authorities
     */
    public DomibusUserDetails(String username, String password,
                              Collection<? extends GrantedAuthority> authorities) {

        this.springUser = org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password(password)
                .authorities(authorities)
                .build();
    }

    private List<GrantedAuthority> getGrantedAuthorities(Collection<UserRole> roles) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        for (UserRole role : roles) {
            authorities.add(new SimpleGrantedAuthority(role.getName()));
        }
        return Lists.newArrayList(authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return springUser.getAuthorities();
    }

    @Override
    public String getPassword() {
        return springUser.getPassword();
    }

    @Override
    public String getUsername() {
        return springUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return springUser.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return springUser.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return springUser.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return springUser.isEnabled();
    }

    public boolean isDefaultPasswordUsed() { return defaultPasswordUsed; }

    public void setDefaultPasswordUsed(boolean defaultPasswordUsed) {
        this.defaultPasswordUsed = defaultPasswordUsed;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Set<String> getAvailableDomainCodes() {
        return Collections.unmodifiableSet(availableDomainCodes);
    }

    public void setAvailableDomainCodes(Set<String> availableDomainCodes) {
        this.availableDomainCodes = new HashSet<>(availableDomainCodes);
    }

    public Integer getDaysTillExpiration() {
        return daysTillExpiration;
    }

    public void setDaysTillExpiration(Integer daysTillExpiration) {
        this.daysTillExpiration = daysTillExpiration;
    }

    public boolean isExternalAuthProvider() {
        return externalAuthProvider;
    }

    public void setExternalAuthProvider(boolean externalAuthProvider) {
        this.externalAuthProvider = externalAuthProvider;
    }
}
