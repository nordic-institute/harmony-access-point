package eu.domibus.api.property;

import org.junit.Test;

import static org.junit.Assert.*;

public class DomibusPropertyMetadataTest {

    @Test
    public void isOnlyGlobal() {
        DomibusPropertyMetadata global = DomibusPropertyMetadata.getReadOnlyGlobalProperty("prop1");
        assertTrue(global.isOnlyGlobal());

        DomibusPropertyMetadata domain = new DomibusPropertyMetadata("porp2", DomibusPropertyMetadata.Usage.DOMAIN, true);
        assertFalse(domain.isOnlyGlobal());
    }

    @Test
    public void isGlobal() {
        DomibusPropertyMetadata global = DomibusPropertyMetadata.getReadOnlyGlobalProperty("prop1");

        assertTrue(global.isGlobal());
        assertTrue(global.isOnlyGlobal());

        DomibusPropertyMetadata domain = new DomibusPropertyMetadata("porp2", DomibusPropertyMetadata.Usage.DOMAIN, true);
        assertFalse(domain.isGlobal());

        DomibusPropertyMetadata global_and_domain = new DomibusPropertyMetadata("porp3", DomibusPropertyMetadata.Usage.GLOBAL_AND_DOMAIN, false);
        assertTrue(global_and_domain.isGlobal());
        assertTrue(global_and_domain.isDomain());
        assertFalse(global_and_domain.isSuper());
    }

    @Test
    public void isSuper() {
        DomibusPropertyMetadata domain_and_super = new DomibusPropertyMetadata("porp3", DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true);
        assertFalse(domain_and_super.isGlobal());
        assertTrue(domain_and_super.isDomain());
        assertTrue(domain_and_super.isSuper());
    }

    @Test
    public void isDomain() {
        DomibusPropertyMetadata domain = new DomibusPropertyMetadata("porp2", DomibusPropertyMetadata.Usage.DOMAIN, true);
        assertTrue(domain.isDomain());
        assertFalse(domain.isGlobal());
        assertFalse(domain.isSuper());

        DomibusPropertyMetadata domain_and_super = new DomibusPropertyMetadata("porp3", DomibusPropertyMetadata.Usage.DOMAIN_AND_SUPER, true);
        assertFalse(domain_and_super.isGlobal());
        assertTrue(domain_and_super.isDomain());
        assertTrue(domain_and_super.isSuper());
    }

    @Test
    public void getOnTheFlyProperty() {
        DomibusPropertyMetadata res = DomibusPropertyMetadata.getOnTheFlyProperty("propName");

        assertEquals("UNKNOWN", res.getModule());
        assertEquals(DomibusPropertyMetadata.Usage.ANY, res.getUsage());
    }
}