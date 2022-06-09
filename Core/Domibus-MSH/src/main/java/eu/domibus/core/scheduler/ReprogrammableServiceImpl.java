package eu.domibus.core.scheduler;

import eu.domibus.api.model.TimezoneOffset;
import eu.domibus.api.scheduler.Reprogrammable;
import eu.domibus.core.time.TimezoneOffsetService;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 *  Service that allows rescheduling and resetting of future events.
 *
 * @author Sebastian-Ion TINCU
 * @since 5.0
 */
@Service
public class ReprogrammableServiceImpl implements ReprogrammableService {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(ReprogrammableServiceImpl.class);

    private final ZoneId defaultZone;

    private final TimezoneOffsetService timezoneOffsetService;

    public ReprogrammableServiceImpl(TimezoneOffsetService timezoneOffsetService) {
        this.timezoneOffsetService = timezoneOffsetService;

        this.defaultZone = ZoneId.systemDefault();
        LOG.info("Default zone for rescheduling future attempts [{}]", defaultZone);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeRescheduleInfo(Reprogrammable reprogrammable) {
        reprogrammable.setNextAttempt(null);
        reprogrammable.setTimezoneOffset(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setRescheduleInfo(Reprogrammable reprogrammable, Date nextAttempt) {
        int zoneOffsetSeconds = defaultZone.getRules().getOffset(LocalDateTime.now()).getTotalSeconds();
        LOG.debug("Next attempt (UTC) [{}] will have zone offset total number of seconds [{}]", nextAttempt, zoneOffsetSeconds);

        TimezoneOffset timezoneOffset = timezoneOffsetService.getTimezoneOffset(defaultZone.getId(), zoneOffsetSeconds);

        reprogrammable.setNextAttempt(nextAttempt);
        reprogrammable.setTimezoneOffset(timezoneOffset);
    }
}
