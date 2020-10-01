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

import static eu.domibus.core.crypto.spi.dss.DssExtensionPropertyManager.*;

/**
 * @author Thomas Dussart
 * @since 4.1
 */
@RunWith(JMockit.class)
public class CustomTrustedListPropertyMapperTest {

    @Test
    public void map(
            @Mocked DomibusPropertyExtService domibusPropertyExtService, @Mocked KeyStoreCertificateSource keyStoreCertificateSource) throws IOException {
        String list1 = "list1";
        String list2 = "list2";
        List<String> customListSuffixes = new ArrayList<>(Arrays.asList(list1, list2));
        List<String> customTrustedListProperties = new ArrayList<>(Arrays.asList("url", "code"));
        String keystorePath = "C:\\pki\\test.jks";
        String keystoreType = "JKS";
        String keystorePasswd = "localdemo";
        String customList1Url = "firstUrl";
        String customList1Code = "CX";
        String customList2Url = "secondUrl";
        String customList2Code = "CUST";
        CustomTrustedListPropertyMapper customTrustedListPropertyMapper = new CustomTrustedListPropertyMapper(domibusPropertyExtService);
        new Expectations(customTrustedListPropertyMapper) {{

            domibusPropertyExtService.getNestedProperties(CUSTOM_TRUSTED_LISTS_PREFIX);
            result = customListSuffixes;

            domibusPropertyExtService.getNestedProperties(CUSTOM_TRUSTED_LISTS_PREFIX + "."+list1);
            result = customTrustedListProperties;

            domibusPropertyExtService.getNestedProperties(CUSTOM_TRUSTED_LISTS_PREFIX + "."+list2);
            result = customTrustedListProperties;

            domibusPropertyExtService.getProperty(DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_TYPE);
            result = keystoreType;

            domibusPropertyExtService.getProperty(DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_PATH);

            this.result = keystorePath;

            domibusPropertyExtService.getProperty(DSS_CUSTOM_TRUSTED_LIST_KEYSTORE_PASSWORD);
            this.result = keystorePasswd;

            domibusPropertyExtService.getProperty(DSS_CUSTOM_TRUSTED_LIST_1_URL);
            this.result = customList1Url;

            domibusPropertyExtService.getProperty(DSS_CUSTOM_TRUSTED_LIST_1_CODE);
            this.result = customList1Code;

            domibusPropertyExtService.getProperty(DSS_CUSTOM_TRUSTED_LIST_2_URL);
            this.result = customList2Url;

            domibusPropertyExtService.getProperty(DSS_CUSTOM_TRUSTED_LIST_2_CODE);
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