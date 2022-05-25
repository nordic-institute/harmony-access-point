package eu.domibus.weblogic.security;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.DomibusUserDetails;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.security.Principal;
import java.util.*;

import static eu.domibus.api.multitenancy.DomainService.DEFAULT_DOMAIN;
import static org.junit.Assert.*;

/**
 * @author Catalin Enache
 * @since 4.1
 */
@RunWith(JMockit.class)
public class ECASUserDetailsServiceTest {

    @Tested
    private ECASUserDetailsService ecasUserDetailsService;

    @Injectable
    private DomainService domainService;

    @Injectable
    private DomibusConfigurationService domibusConfigurationService;

    @Injectable
    private DomainContextProvider domainContextProvider;

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    private final Map<String, AuthRole> userRoleMappings = new HashMap<>();

    private final Map<String, String> domainMappings = new HashMap<>();

    @Before
    public void setUp() {
        userRoleMappings.put("DIGIT_DOMRADM", AuthRole.ROLE_ADMIN);
        domainMappings.put("DIGIT_DOMDDOMN1", "domain1");
    }

    @Test
    public void loadUserDetails(@Mocked final PreAuthenticatedAuthenticationToken token, @Mocked final DomibusUserDetails domibusUserDetails) {
        final String username = "super";

        new Expectations(ecasUserDetailsService) {{
            token.getPrincipal();
            result = username;
            ecasUserDetailsService.loadUserByUsername(username);
            result = domibusUserDetails;
        }};

        // WHEN
        final UserDetails userDetails = ecasUserDetailsService.loadUserDetails(token);
        Assert.assertNotNull(userDetails);
    }

    @Test
    public void loadUserByUsername(@Mocked final DomibusUserDetails domibusUserDetails) throws Exception {
        final String username = "super";

        new Expectations(ecasUserDetailsService) {{
            ecasUserDetailsService.isWeblogicSecurity();
            result = true;

            ecasUserDetailsService.createUserDetails(username);
            result = domibusUserDetails;
        }};

        // WHEN
        ecasUserDetailsService.loadUserByUsername(username);

        new FullVerifications() {{
            String actualUsername;
            ecasUserDetailsService.createUserDetails(actualUsername = withCapture());
            times = 1;
            assertEquals(username, actualUsername);
        }};
    }

    @Test
    public void createUserDetails(@Mocked final Principal principal) throws Exception {
        final String username = "super";

        final Set<Principal> principals = new HashSet<>();
        principals.add(principal);

        new Expectations(ecasUserDetailsService) {{
            domibusPropertyProvider.getProperty(ECASUserDetailsService.ECAS_DOMIBUS_LDAP_GROUP_PREFIX_KEY);
            result = "DIGIT_DOM";

            domibusConfigurationService.isMultiTenantAware();
            result = true;

            ecasUserDetailsService.retrieveUserRoleMappings();
            result = userRoleMappings;

            ecasUserDetailsService.retrieveDomainMappings();
            result = domainMappings;

            ecasUserDetailsService.getPrincipals();
            result = principals;

            ecasUserDetailsService.isUserGroupPrincipal((Principal) any);
            result = true;

            principal.getName();
            result = "DIGIT_DOMRSADM";

            ecasUserDetailsService.chooseHighestUserGroup((ArrayList<AuthRole>) any);
            result = new SimpleGrantedAuthority(AuthRole.ROLE_AP_ADMIN.name());
        }};

        // WHEN
        ecasUserDetailsService.createUserDetails(username);

        new FullVerifications(ecasUserDetailsService) { /* no unexpected interactions */
        };
    }

    @Test
    public void validateAuthorities_NothingGrantedWhenInitialGrantedAuthorityNull() {
        // WHEN
        List<GrantedAuthority> grantedAuthorities = ecasUserDetailsService.validateHighestAuthority(null, DEFAULT_DOMAIN);

        // THEN
        assertTrue(grantedAuthorities.isEmpty());
        new FullVerifications() { /* no unexpected interactions */
        };
    }

    @Test
    public void validateAuthorities_NothingGrantedForNonSuperAdminUsersWhenDomainIsNull(@Injectable GrantedAuthority grantedAuthority) {
        // GIVEN
        new Expectations() {{
            grantedAuthority.getAuthority();
            result = AuthRole.ROLE_USER.name();
        }};

        // WHEN
        List<GrantedAuthority> grantedAuthorities = ecasUserDetailsService.validateHighestAuthority(grantedAuthority, null);

        // THEN
        assertTrue(grantedAuthorities.isEmpty());
        new FullVerifications() { /* no unexpected interactions */
        };
    }

    @Test
    public void validateAuthorities_GrantedForNonSuperAdminUsersWhenDomainIsNotNull(@Injectable GrantedAuthority grantedAuthority) {
        // GIVEN
        new Expectations() {{
            grantedAuthority.getAuthority();
            result = AuthRole.ROLE_ADMIN.name();
        }};

        // WHEN
        List<GrantedAuthority> grantedAuthorities = ecasUserDetailsService.validateHighestAuthority(grantedAuthority, DEFAULT_DOMAIN);

        // THEN
        assertEquals(Collections.singletonList(grantedAuthority), grantedAuthorities);
        new FullVerifications() { /* no unexpected interactions */
        };
    }

    @Test
    public void validateAuthorities_NothingGrantedForSuperAdminUsersInSingleTenancy(@Injectable GrantedAuthority grantedAuthority) {
        // GIVEN
        new Expectations() {{
            grantedAuthority.getAuthority();
            result = AuthRole.ROLE_AP_ADMIN.name();

            domibusConfigurationService.isMultiTenantAware();
            result = false;
        }};

        // WHEN
        List<GrantedAuthority> grantedAuthorities = ecasUserDetailsService.validateHighestAuthority(grantedAuthority, null);

        // THEN
        assertTrue(grantedAuthorities.isEmpty());
        new FullVerifications() { /* no unexpected interactions */
        };
    }

    @Test
    public void validateAuthorities_GrantedForSuperAdminUsersInMultitenancy(@Injectable GrantedAuthority grantedAuthority) {
        // GIVEN
        new Expectations() {{
            grantedAuthority.getAuthority();
            result = AuthRole.ROLE_AP_ADMIN.name();

            domibusConfigurationService.isMultiTenantAware();
            result = true;
        }};

        // WHEN
        List<GrantedAuthority> grantedAuthorities = ecasUserDetailsService.validateHighestAuthority(grantedAuthority, null);

        // THEN
        assertEquals(Collections.singletonList(grantedAuthority), grantedAuthorities);
        new FullVerifications() { /* no unexpected interactions */
        };
    }

    @Test
    public void getFirstDomain_ReturnsDefaultDomainInSingleTenancy() {
        // GIVEN
        Set<String> domainCodesFromLdap = new HashSet<>();
        domainCodesFromLdap.add("red");

        new Expectations() {{
            domibusConfigurationService.isSingleTenantAware();
            result = true;
        }};

        // WHEN
        Domain firstDomain = ecasUserDetailsService.getFirstDomain(domainCodesFromLdap);

        // THEN
        assertEquals(DEFAULT_DOMAIN, firstDomain);
    }

    @Test
    public void getFirstDomain_ReturnsNullWhenNoDomainLdapCodesMatchDomibusExistingDomains() {
        // GIVEN
        Set<String> domainCodesFromLdap = new HashSet<>();
        domainCodesFromLdap.add("red");
        domainCodesFromLdap.add("yellow");
        domainCodesFromLdap.add("blue");

        new Expectations() {{
            final List<Domain> existing = new ArrayList<>();
            existing.add(DEFAULT_DOMAIN);
            existing.add(new Domain("green", "Green"));

            domainService.getDomains();
            result = existing;
        }};

        // WHEN
        Domain firstDomain = ecasUserDetailsService.getFirstDomain(domainCodesFromLdap);

        // THEN
        assertNull(firstDomain);
    }

    @Test
    public void getFirstDomain_ReturnsFirstMatchWhenOneOrMoreDomainLdapCodesMatchDomibusExistingDomains() {
        // GIVEN
        Set<String> domainCodesFromLdap = new HashSet<>();
        domainCodesFromLdap.add("red");
        domainCodesFromLdap.add("yellow");
        domainCodesFromLdap.add("blue");

        new Expectations() {{
            final List<Domain> existing = new ArrayList<>();
            existing.add(new Domain("yellow", "Yellow"));

            domainService.getDomains();
            result = existing;
        }};

        // WHEN
        Domain firstDomain = ecasUserDetailsService.getFirstDomain(domainCodesFromLdap);

        // THEN
        assertEquals(new Domain("yellow", "Yellow"), firstDomain);
    }

    @Test
    public void getAvailableDomainCodes_ReturnsValidLdapDomainCodesForNonSuperAdminUsersInMultitenancy(@Injectable GrantedAuthority highestAuthority) {
        // GIVEN
        final Set<String> domainCodesFromLdap = new HashSet<>();
        domainCodesFromLdap.add("red");
        domainCodesFromLdap.add("yellow");
        domainCodesFromLdap.add("blue");

        final List<Domain> availableDomains = Collections.singletonList(new Domain("blue", "Blue"));

        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;

            highestAuthority.getAuthority();
            result = AuthRole.ROLE_USER.name();

            domainService.getDomains();
            result = availableDomains;
        }};


        // WHEN
        Set<String> availableDomainCodes = ecasUserDetailsService.getAvailableDomainCodes(domainCodesFromLdap, highestAuthority);

        // THEN
        assertEquals(Collections.singleton("blue"), availableDomainCodes);
    }

    @Test
    public void getAvailableDomainCodes_ReturnsValidLdapDomainCodesInSingleTenancy(@Injectable GrantedAuthority highestAuthority) {
        // GIVEN
        final Set<String> domainCodesFromLdap = new HashSet<>();
        domainCodesFromLdap.add("red");
        domainCodesFromLdap.add("yellow");
        domainCodesFromLdap.add("blue");

        final List<Domain> availableDomains = Collections.singletonList(new Domain("yellow", "Yellow"));

        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = false;

            domainService.getDomains();
            result = availableDomains;
        }};

        // WHEN
        Set<String> availableDomainCodes = ecasUserDetailsService.getAvailableDomainCodes(domainCodesFromLdap, highestAuthority);

        // THEN
        assertEquals(Collections.singleton("yellow"), availableDomainCodes);
    }


    @Test
    public void getAvailableDomainCodes_ReturnsAllValidLdapDomainCodesForSuperAdminUsersInMultitenancy(@Injectable GrantedAuthority highestAuthority) {
        // GIVEN
        final Set<String> domainCodesFromLdap = new HashSet<>();
        domainCodesFromLdap.add("red");
        domainCodesFromLdap.add("yellow");
        domainCodesFromLdap.add("blue");

        final List<Domain> availableDomains = Arrays.asList(
                new Domain("red", "Red"),
                new Domain("yellow", "Yellow"));

        new Expectations() {{
            domibusConfigurationService.isMultiTenantAware();
            result = true;

            highestAuthority.getAuthority();
            result = AuthRole.ROLE_AP_ADMIN.name();

            domainService.getDomains();
            result = availableDomains;
        }};


        // WHEN
        Set<String> availableDomainCodes = ecasUserDetailsService.getAvailableDomainCodes(domainCodesFromLdap, highestAuthority);

        // THEN
        assertEquals(new HashSet<>(Arrays.asList("red", "yellow")), availableDomainCodes);
    }

    @Test
    public void retrieveDomainMappings() {
        // GIVEN
        new Expectations() {{
            domibusPropertyProvider.getProperty(ECASUserDetailsService.ECAS_DOMIBUS_DOMAIN_MAPPINGS_KEY);
            result = "DIGIT_DOMDDOMN1=domain1;";
        }};

        // WHEN
        Map<String, String> domainMappings = ecasUserDetailsService.retrieveDomainMappings();

        // THEN
        assertTrue(domainMappings.containsKey("DIGIT_DOMDDOMN1"));
        assertEquals("domain1", domainMappings.get("DIGIT_DOMDDOMN1"));
    }

    @Test
    public void retrieveUserRoleMappings() {
        new Expectations() {{
            domibusPropertyProvider.getProperty(ECASUserDetailsService.ECAS_DOMIBUS_USER_ROLE_MAPPINGS_KEY);
            result = "DIGIT_DOMRUSR=ROLE_USER;DIGIT_DOMRADM=ROLE_ADMIN;DIGIT_DOMRSADM=ROLE_AP_ADMIN;";
        }};

        // WHEN
        Map<String, AuthRole> userRoleMappings = ecasUserDetailsService.retrieveUserRoleMappings();

        assertEquals(3, userRoleMappings.size());
        assertEquals(AuthRole.ROLE_USER, userRoleMappings.get("DIGIT_DOMRUSR"));
        assertEquals(AuthRole.ROLE_ADMIN, userRoleMappings.get("DIGIT_DOMRADM"));
        assertEquals(AuthRole.ROLE_AP_ADMIN, userRoleMappings.get("DIGIT_DOMRSADM"));
    }

    @Test
    public void chooseHighestUserGroup_AdminRolesHaveHigherPrecedenceThanUserRoles() {
        final List<AuthRole> roles = new ArrayList<>();
        roles.add(AuthRole.ROLE_ADMIN);
        roles.add(AuthRole.ROLE_USER);

        assertEquals(new SimpleGrantedAuthority(AuthRole.ROLE_ADMIN.name()),
                ecasUserDetailsService.chooseHighestUserGroup(roles));
    }

    @Test
    public void chooseHighestUserGroup_SuperAdminRolesHaveHigherPrecedenceThanAdminOrUserRoles() {
        final List<AuthRole> roles = new ArrayList<>();
        roles.add(AuthRole.ROLE_ADMIN);
        roles.add(AuthRole.ROLE_USER);
        roles.add(AuthRole.ROLE_AP_ADMIN);

        assertEquals(new SimpleGrantedAuthority(AuthRole.ROLE_AP_ADMIN.name()),
                ecasUserDetailsService.chooseHighestUserGroup(roles));
    }

}
