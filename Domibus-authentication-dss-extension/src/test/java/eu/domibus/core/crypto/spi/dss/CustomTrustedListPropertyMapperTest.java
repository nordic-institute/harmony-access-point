package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.europa.esig.dss.tsl.OtherTrustedList;
import mockit.Expectations;
import mockit.Mocked;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.env.Environment;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
@RunWith(JMockit.class)
public class CustomTrustedListPropertyMapperTest {

    @Test
    public void map(
            @Mocked DomibusPropertyExtService domibusPropertyExtService,
            @Mocked DomainContextExtService domainContextExtService,
            @Mocked Environment environment) {
        new Expectations() {{
            domainContextExtService.getCurrentDomainSafely();result=null;
            environment.containsProperty("domibus.authentication.dss.custom.trusted.list.url[0]");result=true;
            environment.getProperty("domibus.authentication.dss.custom.trusted.list.url[0]");
            result = "https://s3.eu-central-1.amazonaws.com/custom-trustlist/trusted-list.xml";

            environment.containsProperty("domibus.authentication.dss.custom.trusted.list.keystore.type[0]");result=true;
            environment.getProperty("domibus.authentication.dss.custom.trusted.list.keystore.type[0]");
            result = "JKS";

            environment.containsProperty("domibus.authentication.dss.custom.trusted.list.keystore.path[0]");result=true;
            environment.getProperty("domibus.authentication.dss.custom.trusted.list.keystore.path[0]");
            result = "C:\\pki\\test.jks";

            environment.containsProperty("domibus.authentication.dss.custom.trusted.list.keystore.password[0]");result=true;
            environment.getProperty("domibus.authentication.dss.custom.trusted.list.keystore.password[0]");
            result = "localdemo";

            environment.containsProperty("domibus.authentication.dss.custom.trusted.list.country.code[0]");result=true;
            environment.getProperty("domibus.authentication.dss.custom.trusted.list.country.code[0]");
            result = "CX";
        }};
        List<OtherTrustedList> otherTrustedLists = new CustomTrustedListPropertyMapper(domibusPropertyExtService, domainContextExtService, environment).map();
        Assert.assertEquals(0,otherTrustedLists.size());
        new Verifications() {
            {
                domibusPropertyExtService.getDomainProperty((DomainDTO) any, (String) any);
                times = 0;

                domibusPropertyExtService.containsPropertyKey("domibus.authentication.dss.custom.trusted.list.url[0]");times=1;
            }
        };
    }

    ;
}