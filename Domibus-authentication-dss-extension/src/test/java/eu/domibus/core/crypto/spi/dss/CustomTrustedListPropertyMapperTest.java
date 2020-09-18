package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.europa.esig.dss.spi.x509.KeyStoreCertificateSource;
import eu.europa.esig.dss.tsl.OtherTrustedList;
import mockit.Expectations;
import mockit.Mocked;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
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
            @Mocked DomibusPropertyExtService domibusPropertyExtService, @Mocked KeyStoreCertificateSource keyStoreCertificateSource) throws IOException {
        List<String> customTrustedListProperties = new ArrayList<>(Arrays.asList("url", "code"));
        String keystorePath = "C:\\pki\\test.jks";
        String keystoreType = "JKS";
        String keystorePasswd = "localdemo";
        String customList1Url = "https://s3.eu-central-1.amazonaws.com/custom-trustlist/trusted-list.xml";
        String customList1Code = "CX";
        String customList2Url = "https://s5.eu-central-1.amazonaws.com/custom-trustlist/trusted-list.xml";
        String customList2Code = "CUST";
        CustomTrustedListPropertyMapper customTrustedListPropertyMapper = new CustomTrustedListPropertyMapper(domibusPropertyExtService);
        new Expectations(customTrustedListPropertyMapper) {{

            domibusPropertyExtService.containsPropertyKey("domibus.authentication.dss.custom.trusted.list1");
            result = true;

            domibusPropertyExtService.containsPropertyKey("domibus.authentication.dss.custom.trusted.list2");
            result = true;

            domibusPropertyExtService.getNestedProperties("domibus.authentication.dss.custom.trusted.list1");
            result = customTrustedListProperties;

            domibusPropertyExtService.getNestedProperties("domibus.authentication.dss.custom.trusted.list2");
            result = customTrustedListProperties;

            domibusPropertyExtService.getProperty("domibus.authentication.dss.custom.trusted.list.keystore.type");
            result = keystoreType;

            domibusPropertyExtService.getProperty("domibus.authentication.dss.custom.trusted.list.keystore.path");

            this.result = keystorePath;

            domibusPropertyExtService.getProperty("domibus.authentication.dss.custom.trusted.list.keystore.password");
            this.result = keystorePasswd;

            domibusPropertyExtService.getProperty("domibus.authentication.dss.custom.trusted.list1.url");
            this.result = customList1Url;

            domibusPropertyExtService.getProperty("domibus.authentication.dss.custom.trusted.list1.code");
            this.result = customList1Code;

            domibusPropertyExtService.getProperty("domibus.authentication.dss.custom.trusted.list2.url");
            this.result = customList2Url;

            domibusPropertyExtService.getProperty("domibus.authentication.dss.custom.trusted.list2.code");
            this.result = customList2Code;

            customTrustedListPropertyMapper.initKeyStoreCertificateSource(keystorePath, keystoreType, keystorePasswd);
            this.result = keyStoreCertificateSource;
        }};
        List<OtherTrustedList> otherTrustedLists = customTrustedListPropertyMapper.map();
        Assert.assertEquals(2, otherTrustedLists.size());
        OtherTrustedList otherTrustedList = otherTrustedLists.get(0);
        Assert.assertEquals(customList1Url, otherTrustedList.getUrl());
        Assert.assertEquals(customList1Code, otherTrustedList.getCountryCode());

        otherTrustedList = otherTrustedLists.get(1);
        Assert.assertEquals(customList2Url, otherTrustedList.getUrl());
        Assert.assertEquals(customList2Code, otherTrustedList.getCountryCode());
    }
}