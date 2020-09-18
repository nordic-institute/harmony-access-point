package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.europa.esig.dss.tsl.OtherTrustedList;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
@RunWith(JMockit.class)
public class CustomTrustedListPropertyMapperTest {

    @Test
    public void map(
            @Mocked DomibusPropertyExtService domibusPropertyExtService) {
        List<String> customTrustedListProperties = new ArrayList<>(Arrays.asList("url", "code"));
        new Expectations() {{

            domibusPropertyExtService.getNestedProperties("domibus.authentication.dss.custom.trusted.list1");
            result = customTrustedListProperties;

            domibusPropertyExtService.containsPropertyKey("domibus.authentication.dss.custom.trusted.list1");
            result = true;

            domibusPropertyExtService.containsPropertyKey("domibus.authentication.dss.custom.trusted.list2");
            result = false;

            domibusPropertyExtService.getProperty("domibus.authentication.dss.custom.trusted.list1.url");
            result = "https://s3.eu-central-1.amazonaws.com/custom-trustlist/trusted-list.xml";

            domibusPropertyExtService.getProperty("domibus.authentication.dss.custom.trusted.list.keystore.type");
            result = "JKS";

            domibusPropertyExtService.getProperty("domibus.authentication.dss.custom.trusted.list.keystore.path");
            result = "C:\\pki\\test.jks";

            domibusPropertyExtService.getProperty("domibus.authentication.dss.custom.trusted.list.keystore.password");
            result = "localdemo";

            domibusPropertyExtService.getProperty("domibus.authentication.dss.custom.trusted.list1.code");
            result = "CX";
        }};
        List<OtherTrustedList> otherTrustedLists = new CustomTrustedListPropertyMapper(domibusPropertyExtService).map();
        Assert.assertEquals(0, otherTrustedLists.size());
        new Verifications() {
            {
                domibusPropertyExtService.getProperty((DomainDTO) any, (String) any);
                times = 0;
            }
        };
    }

    ;
}