package eu.domibus.core.jpa;

import mockit.Expectations;
import mockit.Injectable;
import mockit.integration.junit4.JMockit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.io.Resource;

import java.io.IOException;

import static eu.domibus.core.jpa.DomibusJPAConfiguration.CONFIG_DOMIBUS_ORM;
import static org.junit.Assert.*;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(JMockit.class)
public class DomibusJPAConfigurationTest {

    private DomibusJPAConfiguration domibusJPAConfiguration;

    @Before
    public void setUp() throws Exception {
        domibusJPAConfiguration = new DomibusJPAConfiguration();
    }

    @Test
    public void getRelativePath_null() {
        String relativePath = domibusJPAConfiguration.getRelativePath(null);
        assertNull(relativePath);
    }

    @Test
    public void getRelativePath_PathNull(@Injectable Resource resource) throws IOException {
        new Expectations() {{
            resource.getURL().getPath();
            result = null;
        }};
        String relativePath = domibusJPAConfiguration.getRelativePath(resource);
        assertNull(relativePath);
    }

    @Test
    public void getRelativePath_Exception(@Injectable Resource resource) throws IOException {
        new Expectations() {{
            resource.getURL().getPath();
            result = new IOException();
        }};
        String relativePath = domibusJPAConfiguration.getRelativePath(resource);
        assertNull(relativePath);
    }

    @Test
    public void getRelativePath_noJar(@Injectable Resource resource) throws IOException {
        new Expectations() {{
            resource.getURL().getPath();
            result = "test";
        }};
        String relativePath = domibusJPAConfiguration.getRelativePath(resource);
        assertEquals("", relativePath);
    }

    @Test
    public void getRelativePath_jar(@Injectable Resource resource) throws IOException {
        String expected = CONFIG_DOMIBUS_ORM + "ehcache-default.xml";

        new Expectations() {{
            resource.getURL().getPath();
            result = "WEB-INF/lib/domibus-MSH-5.0-SNAPSHOT.jar!/" + expected;
        }};
        String relativePath = domibusJPAConfiguration.getRelativePath(resource);
        assertEquals(expected, relativePath);
    }
}