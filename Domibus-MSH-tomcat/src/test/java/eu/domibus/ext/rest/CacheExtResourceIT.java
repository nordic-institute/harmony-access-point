package eu.domibus.ext.rest;

import eu.domibus.AbstractIT;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

/**
 * @author François Gautier
 * @since 5.0
 */
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class CacheExtResourceIT extends AbstractIT {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    private CacheExtResource cacheExtResource;

    @Autowired
    private CacheServiceWithCounters cacheExtService;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cacheExtResource).build();

        cacheExtService.resetEvictCachesCounter();
        cacheExtService.resetEvict2LCachesCounter();
    }

    @Test
    public void deleteCache_noUser() throws Exception {
        expectedException.expectCause(CoreMatchers.isA(AuthenticationCredentialsNotFoundException.class));

        mockMvc.perform(delete("/ext/cache"));

        Assert.assertEquals(0, cacheExtService.getEvictCachesCounter());
    }

    @Test
    @WithMockUser
    public void deleteCache_notAdmin() throws Exception {
        expectedException.expectCause(CoreMatchers.isA(AccessDeniedException.class));

        mockMvc.perform(delete("/ext/cache"));

        Assert.assertEquals(0, cacheExtService.getEvictCachesCounter());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteCache_admin() throws Exception {
        mockMvc.perform(delete("/ext/cache"));

        Assert.assertEquals(1, cacheExtService.getEvictCachesCounter());
    }

    @Test
    public void delete2LCache_noUser() throws Exception {
        expectedException.expectCause(CoreMatchers.isA(AuthenticationCredentialsNotFoundException.class));

        mockMvc.perform(delete("/ext/2LCache"));

        Assert.assertEquals(0, cacheExtService.getEvict2LCachesCounter());
    }

    @Test
    @WithMockUser
    public void delete2LCache_notAdmin() throws Exception {
        expectedException.expectCause(CoreMatchers.isA(AccessDeniedException.class));

        mockMvc.perform(delete("/ext/2LCache"));

        Assert.assertEquals(0, cacheExtService.getEvict2LCachesCounter());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void delete2LCache_admin() throws Exception {
        mockMvc.perform(delete("/ext/2LCache"));

        Assert.assertEquals(1, cacheExtService.getEvict2LCachesCounter());
    }

}