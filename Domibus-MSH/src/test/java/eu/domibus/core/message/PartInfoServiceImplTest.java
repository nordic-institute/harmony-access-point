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
    public void clearPayloadData_empty() {
        new Expectations(partInfoService) {{
            partInfoDao.findPartInfoByUserMessageEntityId(1L);
            result = new ArrayList<>();
            times = 1;
        }};

        partInfoService.clearPayloadData(1L);

        new FullVerifications() {
        };
    }

    @Test
    public void clearPayloadData_find2(@Injectable final PartInfo partInfo1,
                                       @Injectable final PartInfo partInfo2) {
        List<PartInfo> partInfos = asList(partInfo1, partInfo2);
        new Expectations(partInfoService) {{
            partInfoDao.findPartInfoByUserMessageEntityId(1L);
            result = partInfos;
            times = 1;

            partInfoService.clearDatabasePayloads(partInfos);
            times = 1;
            partInfoService.clearFileSystemPayloads(partInfos);
            times = 1;
        }};

        partInfoService.clearPayloadData(1L);

        new FullVerifications() {
        };
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
}