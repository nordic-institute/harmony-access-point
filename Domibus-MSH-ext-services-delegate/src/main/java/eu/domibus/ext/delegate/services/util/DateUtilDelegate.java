package eu.domibus.ext.delegate.services.util;

import eu.domibus.api.exceptions.DomibusDateTimeException;
import eu.domibus.api.util.DateUtil;
import eu.domibus.ext.exceptions.DomibusDateTimeExtException;
import eu.domibus.ext.services.DateExtService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Delegate service exposing date utility operations.
 *
 * @author Sebastian-Ion TINCU
 * @since 5.0
 */
@Service
public class DateUtilDelegate implements DateExtService {

    private final DateUtil dateUtil;

    public DateUtilDelegate(DateUtil dateUtil) {
        this.dateUtil = dateUtil;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getUtcDate() {
        return dateUtil.getUtcDate();
    }

    @Override
    public Long getIdPkDateHour(String date) {
        try {
            return dateUtil.getIdPkDateHour(date);
        } catch (DomibusDateTimeException e) {
            throw new DomibusDateTimeExtException("Could not get IdPK from date [" + date + "]", e);
        }
    }

}
