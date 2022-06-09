package eu.domibus.plugin.ws.backend;

import eu.domibus.plugin.ws.AbstractBackendWSIT;
import eu.domibus.plugin.ws.exception.WSPluginException;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;

import static org.hamcrest.core.Is.is;

/**
 * @author François Gautier
 * @since 5.0
 */
@Transactional
public class WSBackendMessageLogServiceIT extends AbstractBackendWSIT {

    @Autowired
    private WSBackendMessageLogService wsBackendMessageLogService;

    private WSBackendMessageLogEntity entityFailed2021;
    private WSBackendMessageLogEntity entityFailed2022;


    @Before
    public void setUp() {
        entityFailed2021 = create(WSBackendMessageStatus.SEND_FAILURE);
        entityFailed2022 = create(WSBackendMessageStatus.SEND_FAILURE);
        entityFailed2022.setFailed(new Date());
        entityFailed2021.setFailed(new Date());
        createEntityAndFlush(Arrays.asList(entityFailed2021,
                entityFailed2022));
    }

    @Test
    public void updateForRetry() {
        wsBackendMessageLogService.updateForRetry(Arrays.asList(entityFailed2021.getMessageId(),
                entityFailed2022.getMessageId()));

        em.refresh(entityFailed2022);
        em.refresh(entityFailed2021);
        MatcherAssert.assertThat(entityFailed2022.getSendAttempts(), is(0));
        MatcherAssert.assertThat(entityFailed2021.getSendAttempts(), is(0));
        MatcherAssert.assertThat(entityFailed2021.getMessageStatus().name(), is(WSBackendMessageStatus.WAITING_FOR_RETRY.name()));
        MatcherAssert.assertThat(entityFailed2022.getMessageStatus().name(), is(WSBackendMessageStatus.WAITING_FOR_RETRY.name()));
    }

    @Test(expected = WSPluginException.class)
    public void updateForRetry_error() {
        wsBackendMessageLogService.updateForRetry(Arrays.asList("notFound", entityFailed2021.getMessageId(),
                entityFailed2022.getMessageId(), "notFound2"));
    }
}