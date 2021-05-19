package eu.domibus.ext.rest;

import eu.domibus.ext.services.CacheExtService;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class CacheExtResourceIT {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Autowired
    private CacheExtResource cacheExtResource;

    @Autowired
    private CacheExtService cacheExtService;

    private MockMvc mockMvc;

    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    static class ContextConfiguration {
        
        @Bean
        public CacheExtResource cacheExtResource(CacheExtService cacheExtService) {
            return new CacheExtResource(cacheExtService, null);
        }
        
        @Bean
        public CacheExtService cacheExtService() {
            return Mockito.mock(CacheExtService.class);
        }
    }

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cacheExtResource).build();
    }

    @Test
    public void deleteCache_noUser() throws Exception {
        expectedException.expectCause(CoreMatchers.isA(AuthenticationCredentialsNotFoundException.class));

        mockMvc.perform(delete("/ext/cache"));

        Mockito.verify(cacheExtService, Mockito.times(0)).evictCaches();
    }

    @Test
    @WithMockUser
    public void deleteCache_notAdmin() throws Exception {
        expectedException.expectCause(CoreMatchers.isA(AccessDeniedException.class));

        mockMvc.perform(delete("/ext/cache"));

        Mockito.verify(cacheExtService, Mockito.times(0)).evictCaches();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void deleteCache_admin() throws Exception {
        mockMvc.perform(delete("/ext/cache"));

        Mockito.verify(cacheExtService, Mockito.times(1)).evictCaches();
    }

    @Test
    public void delete2LCache_noUser() throws Exception {
        expectedException.expectCause(CoreMatchers.isA(AuthenticationCredentialsNotFoundException.class));

        mockMvc.perform(delete("/ext/2LCache"));

        Mockito.verify(cacheExtService, Mockito.times(0)).evict2LCaches();

    }

    @Test
    @WithMockUser
    public void delete2LCache_notAdmin() throws Exception {
        expectedException.expectCause(CoreMatchers.isA(AccessDeniedException.class));

        mockMvc.perform(delete("/ext/2LCache"));

        Mockito.verify(cacheExtService, Mockito.times(0)).evict2LCaches();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    public void delete2LCache_admin() throws Exception {

        mockMvc.perform(delete("/ext/2LCache"));

        Mockito.verify(cacheExtService, Mockito.times(1)).evict2LCaches();
    }

}