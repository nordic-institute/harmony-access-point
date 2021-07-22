package eu.domibus.core.message;

import eu.domibus.api.model.*;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.usermessage.domain.PartProperties;
import eu.domibus.api.usermessage.domain.PayloadInfo;
import eu.domibus.common.ErrorCode;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.payload.persistence.PayloadPersistenceHelper;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.fail;

/**
 * @author François Gautier
 * @since 5.0
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class PartInfoServiceImplTest {

    private static final String STRING_TYPE = "string";

    @Tested
    private PartInfoServiceImpl partInfoService;

    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    private PartInfoDao partInfoDao;

    @Injectable
    private PayloadPersistenceHelper payloadPersistenceHelper;

    @Test
    public void clearPayloadData_empty(@Injectable final UserMessage userMessage) {
        new Expectations(partInfoService) {{
            partInfoService.findPartInfo(userMessage);
            result = new ArrayList<>();
            times = 1;
        }};

        partInfoService.clearPayloadData(userMessage);

        new FullVerifications() {
        };
    }

    @Test
    public void clearPayloadData_find2(@Injectable final UserMessage userMessage,
                                       @Injectable final PartInfo partInfo1,
                                       @Injectable final PartInfo partInfo2) {
        List<PartInfo> partInfos = asList(partInfo1, partInfo2);
        new Expectations(partInfoService) {{
            partInfoService.findPartInfo(userMessage);
            this.result = partInfos;
            times = 1;

            partInfoService.clearDatabasePayloads(partInfos);
            times = 1;
            partInfoService.clearFileSystemPayloads(partInfos);
            times = 1;
        }};

        partInfoService.clearPayloadData(userMessage);

        new FullVerifications() {};
    }

    @Test
    public void testScheduleSourceMessagePayloads(@Injectable final UserMessage userMessage,
                                                  @Injectable final Domain domain,
                                                  @Injectable final PartInfo partInfo) {


        List<PartInfo> partInfos = new ArrayList<>();
        partInfos.add(partInfo);

        new Expectations() {{
            partInfo.getLength();
            result = 20 * PartInfoServiceImpl.BYTES_IN_MB;

            domibusPropertyProvider.getLongProperty(PartInfoServiceImpl.PROPERTY_PAYLOADS_SCHEDULE_THRESHOLD);
            result = 15;
        }};

        final boolean scheduleSourceMessagePayloads = partInfoService.scheduleSourceMessagePayloads(partInfos);
        Assert.assertTrue(scheduleSourceMessagePayloads);

        new FullVerifications() {{
        }};
    }

    @Test
    public void testScheduleSourceMessagePayloads_noParts() {


        final boolean scheduleSourceMessagePayloads = partInfoService.scheduleSourceMessagePayloads(null);
        Assert.assertFalse(scheduleSourceMessagePayloads);

        new FullVerifications() {{
        }};
    }

    @Test
    public void testCheckCharset_HappyFlow() throws EbMS3Exception {
        UserMessage userMessage = new UserMessage();
        PayloadInfo payloadInfo = new PayloadInfo();
        PartInfo partInfo = getPartInfo2("MimeType", "text/xml", null);
        payloadInfo.setPartInfo(new HashSet<>());

        partInfoService.checkPartInfoCharset(userMessage, asList(partInfo));
    }

    @Test
    public void testCheckCharset_InvalidCharset() {
        UserMessage userMessage = new UserMessage();
        PartInfo partInfo = getPartInfo("CharacterSet", "!#InvalidCharSet");
        try {
            partInfoService.checkPartInfoCharset(userMessage, asList(partInfo));
            fail("EBMS3Exception was expected!!");
        } catch (EbMS3Exception e) {
            Assert.assertEquals(ErrorCode.EbMS3ErrorCode.EBMS_0003, e.getErrorCode());
        }

        new FullVerifications() {
        };
    }

    private PartInfo getPartInfo(String name, String value) {
        return getPartInfo2(name, value, null);
    }

    public static PartInfo getPartInfo2(String name, String value, String href) {
        PartInfo partInfo = new PartInfo();

        Set<PartProperty> partProperties1 = new HashSet<>();
        PartProperty partProperty = new PartProperty();
        partProperty.setName(name);
        partProperty.setValue(value);
        partProperties1.add(partProperty);
        partInfo.setPartProperties(partProperties1);
        partInfo.setHref(href);
        return partInfo;
    }
    public static eu.domibus.api.usermessage.domain.PartInfo getPartInfo(String name, String value, String href) {
        eu.domibus.api.usermessage.domain.PartInfo partInfo = new eu.domibus.api.usermessage.domain.PartInfo();
        PartProperties partProperties = new PartProperties();
        partProperties.setProperty(new HashSet<>());
        partProperties.getProperty().add(createProperty(name, value));
        partInfo.setPartProperties(partProperties);
        partInfo.setHref(href);
        return partInfo;
    }

    protected static eu.domibus.api.usermessage.domain.Property createProperty(String name, String value) {
        eu.domibus.api.usermessage.domain.Property aProperty = new eu.domibus.api.usermessage.domain.Property();
        aProperty.setValue(value);
        aProperty.setName(name);
        aProperty.setType(STRING_TYPE);
        return aProperty;
    }
}