package eu.domibus.core.earchive.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.NoArgGenerator;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.model.configuration.LegConfiguration;
import eu.domibus.common.model.configuration.ReceptionAwareness;
import eu.domibus.core.earchive.EArchiveBatchDao;
import eu.domibus.core.earchive.EArchiveBatchStartDao;
import eu.domibus.core.earchive.EArchiveBatchUserMessageDao;
import eu.domibus.core.pmode.provider.LegConfigurationPerMpc;
import eu.domibus.core.pmode.provider.PModeProvider;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_EARCHIVE_BATCH_MPCS;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author François Gautier
 * @since 5.0
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
@RunWith(JMockit.class)
public class EArchivingJobServiceTest {

    @Tested
    private EArchivingJobService eArchivingJobService;

    @Injectable
    private EArchiveBatchUserMessageDao eArchiveBatchUserMessageDao;
    @Injectable
    private DomibusPropertyProvider domibusPropertyProvider;
    @Injectable
    private PModeProvider pModeProvider;
    @Injectable
    private EArchiveBatchDao eArchiveBatchDao;
    @Injectable
    private EArchiveBatchStartDao eArchiveBatchStartDao;
    @Injectable
    private NoArgGenerator uuidGenerator;
    @Injectable
    private ObjectMapper domibusJsonMapper;

    @Test
    public void getMpcs() {

        new Expectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_EARCHIVE_BATCH_MPCS);
            result = "test1, test2,test3";
        }};
        List<String> mpcs = eArchivingJobService.getMpcs();
        Assert.assertEquals("test1", mpcs.get(0));
        Assert.assertEquals("test2", mpcs.get(1));
        Assert.assertEquals("test3", mpcs.get(2));

    }

    @Test
    public void getMpcs_empty() {

        new Expectations() {{
            domibusPropertyProvider.getProperty(DOMIBUS_EARCHIVE_BATCH_MPCS);
            result = null;
        }};
        List<String> mpcs = eArchivingJobService.getMpcs();
        assertTrue(mpcs.isEmpty());

    }

    @Test
    public void getMaxRetryTimeOutFiltered(
            @Injectable LegConfiguration legConfiguration11,
            @Injectable LegConfiguration legConfiguration12,
            @Injectable LegConfiguration legConfiguration13,
            @Injectable ReceptionAwareness receptionAwareness11,
            @Injectable ReceptionAwareness receptionAwareness12,
            @Injectable ReceptionAwareness receptionAwareness13) {
        HashMap<String, List<LegConfiguration>> map = new HashMap<>();
        map.put("mpc1", asList(legConfiguration11,
                legConfiguration12,
                legConfiguration13));
        map.put("mpc2", Collections.singletonList(null));
        new Expectations() {{
            legConfiguration11.getReceptionAwareness();
            result = receptionAwareness11;
            legConfiguration12.getReceptionAwareness();
            result = receptionAwareness12;
            legConfiguration13.getReceptionAwareness();
            result = receptionAwareness13;

            receptionAwareness11.getRetryTimeout();
            result = 1;
            receptionAwareness12.getRetryTimeout();
            result = 2;
            receptionAwareness13.getRetryTimeout();
            result = 3;
        }};

        int mpc1 = eArchivingJobService.getMaxRetryTimeOutFiltered(Collections.singletonList("mpc1"), new LegConfigurationPerMpc(map));

        assertEquals(3, mpc1);
        new FullVerifications() {
        };
    }
}