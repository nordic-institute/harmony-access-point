package eu.domibus.core.message;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.core.Is;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author François Gautier
 * @since 5.0
 */
public class UserMessageLogDaoTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessageLogDao.class);

    @Test
    public void updateByBatch() {
        List<Pair<Long, Long>> result = new ArrayList<>();

        new UserMessageLogDao(null, null, null, null, null)
                .update(getResultList(),
                        longs -> {
                            result.add(Pair.of(longs.get(0), longs.get(longs.size() - 1)));
                            LOG.info("Batch starts [{}] and ends [{}]", longs.get(0), longs.get(longs.size() - 1));
                        });

        assertThat(result.size(), Is.is(2));
        assertThat(result.get(0).getLeft(), Is.is(0L));
        assertThat(result.get(0).getRight(), Is.is(999L));
        assertThat(result.get(1).getLeft(), Is.is(1000L));
        assertThat(result.get(1).getRight(), Is.is(1499L));
    }

    private List<Long> getResultList() {
        List<Long> result = new ArrayList<>();
        for (int i = 0; i < 1500; i++) {
            result.add((long) i);
        }
        return result;
    }
}