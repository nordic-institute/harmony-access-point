package eu.domibus.core.property;

import eu.domibus.api.configuration.DomibusConfigurationService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusProperty;
import eu.domibus.api.property.DomibusPropertyMetadata;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.services.DomibusPropertyManagerExt;
import mockit.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManager.*;

public class DomibusPropertyServiceImplTest {

    @Injectable
    protected DomainExtConverter domainConverter;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Injectable
    protected DomibusConfigurationService domibusConfigurationService;

    @Injectable
    private List<DomibusPropertyManagerExt> propertyManagers;

    @Injectable
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Mocked
    @Spy
    private DomibusPropertyManagerExt propertyManager1;

    @Mocked
    private DomibusPropertyManagerExt propertyManager2;

    @Tested
    DomibusPropertyServiceImpl domibusPropertyService;

    Map<String, DomibusPropertyMetadataDTO> props1, props2;
    String domainCode = "domain1";
    Domain domain = new Domain(domainCode, "DomainName1");

    @Before
    public void setUp() {
        props1 = Arrays.stream(new DomibusPropertyMetadataDTO[]{
                new DomibusPropertyMetadataDTO(DOMIBUS_UI_TITLE_NAME, true, true),
                new DomibusPropertyMetadataDTO(DOMIBUS_UI_REPLICATION_ENABLED, true, true),
                new DomibusPropertyMetadataDTO(DOMIBUS_SEND_MESSAGE_MESSAGE_ID_PATTERN, true, false),
                new DomibusPropertyMetadataDTO(DOMIBUS_PLUGIN_PASSWORD_POLICY_PATTERN, true, true),
        }).collect(Collectors.toMap(x -> x.getName(), x -> x));

        props2 = Arrays.stream(new DomibusPropertyMetadataDTO[]{
                new DomibusPropertyMetadataDTO(DOMIBUS_UI_SUPPORT_TEAM_NAME, true, true),
                new DomibusPropertyMetadataDTO(DOMIBUS_PLUGIN_PASSWORD_POLICY_VALIDATION_MESSAGE, true, true),
                new DomibusPropertyMetadataDTO(DOMIBUS_PASSWORD_POLICY_PLUGIN_EXPIRATION, true, true),
                new DomibusPropertyMetadataDTO(DOMIBUS_PASSWORD_POLICY_PLUGIN_DEFAULT_PASSWORD_EXPIRATION, true, true),
                new DomibusPropertyMetadataDTO(DOMIBUS_PASSWORD_POLICY_PLUGIN_DONT_REUSE_LAST, true, true),
        }).collect(Collectors.toMap(x -> x.getName(), x -> x));

        propertyManagers = Arrays.asList(propertyManager1, propertyManager2);
    }

    DomibusPropertyMetadata convert(DomibusPropertyMetadataDTO meta) {
        return new DomibusPropertyMetadata(meta.getName(), meta.getModule(), meta.isDomainSpecific(), meta.isWithFallback(), meta.isClusterAware());
    }

    @Test
    public void getProperties() {
        new Expectations() {{
            domainContextProvider.getCurrentDomainSafely();
            result = domain;

            propertyManager1.getKnownProperties();
            result = props1;

            propertyManager2.getKnownProperties();
            result = props2;

            domibusConfigurationService.isMultiTenantAware();
            result = true;

            propertyManager1.getKnownPropertyValue(domainCode, DOMIBUS_UI_TITLE_NAME);
            result = "val1";

            domainConverter.convert(props1.get(DOMIBUS_UI_TITLE_NAME), DomibusPropertyMetadata.class);
            result = convert(props1.get(DOMIBUS_UI_TITLE_NAME));

            propertyManager1.getKnownPropertyValue(domainCode, DOMIBUS_UI_REPLICATION_ENABLED);
            result = "val2";

            domainConverter.convert(props1.get(DOMIBUS_UI_REPLICATION_ENABLED), DomibusPropertyMetadata.class);
            result = convert(props1.get(DOMIBUS_UI_REPLICATION_ENABLED));

            propertyManager2.getKnownPropertyValue(domainCode, DOMIBUS_UI_SUPPORT_TEAM_NAME);
            result = "val3";

            domainConverter.convert(props2.get(DOMIBUS_UI_SUPPORT_TEAM_NAME), DomibusPropertyMetadata.class);
            result = convert(props2.get(DOMIBUS_UI_SUPPORT_TEAM_NAME));
        }};

        List<DomibusProperty> actual = domibusPropertyService.getProperties("domibus.UI");

        Assert.assertEquals(3, actual.size());
        Assert.assertEquals(true, actual.stream().anyMatch(el -> el.getMetadata().getName().equals(DOMIBUS_UI_TITLE_NAME)));
        Assert.assertEquals(true, actual.stream().anyMatch(el -> el.getMetadata().getName().equals(DOMIBUS_UI_REPLICATION_ENABLED)));
        Assert.assertEquals(true, actual.stream().anyMatch(el -> el.getMetadata().getName().equals(DOMIBUS_UI_SUPPORT_TEAM_NAME)));
        Assert.assertEquals("val1", actual.stream().filter(el -> el.getMetadata().getName().equals(DOMIBUS_UI_TITLE_NAME)).findFirst().get().getValue());
    }

    @Test
    public void setPropertyValue() {
        new Expectations() {{
            domainContextProvider.getCurrentDomainSafely();
            result = domain;

            propertyManager2.hasKnownProperty(DOMIBUS_UI_TITLE_NAME);
            result = false;

            propertyManager1.hasKnownProperty(DOMIBUS_UI_TITLE_NAME);
            result = true;
        }};

        domibusPropertyService.setPropertyValue(DOMIBUS_UI_TITLE_NAME, "val11");

        new Verifications() {{
            propertyManager1.setKnownPropertyValue(domainCode, DOMIBUS_UI_TITLE_NAME, "val11");
        }};
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPropertyValue_notMatch() {
        new Expectations() {{
            domainContextProvider.getCurrentDomainSafely();
            result = domain;

            propertyManager1.hasKnownProperty("non_existing_prop");
            result = false;

            propertyManager2.hasKnownProperty("non_existing_prop");
            result = false;
        }};

        domibusPropertyService.setPropertyValue("non_existing_prop", "val11");
    }

    @Test(expected = IllegalArgumentException.class)
    public void setPropertyValue_tooLong() {
        int limit = 100;
        String propertyToTest = DOMIBUS_UI_TITLE_NAME;
        String longValue = StringUtils.repeat("A", limit + 1);
        new Expectations() {{
            domibusPropertyProvider.getIntegerProperty(DOMIBUS_PROPERTY_LENGTH_MAX);
            result = limit;
        }};

        domibusPropertyService.setPropertyValue(propertyToTest, longValue);
    }
}
