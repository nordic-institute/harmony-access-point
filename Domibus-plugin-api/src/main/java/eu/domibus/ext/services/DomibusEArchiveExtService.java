package eu.domibus.ext.services;

import java.util.Date;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface DomibusEArchiveExtService {

    void updateStartDateContinuousArchive(Date startDate);

    void updateStartDateSanityArchive(Date startDate);

}
