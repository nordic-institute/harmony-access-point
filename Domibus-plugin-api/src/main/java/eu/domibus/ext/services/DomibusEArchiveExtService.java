package eu.domibus.ext.services;

import java.util.Date;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface DomibusEArchiveExtService {

    void updateStartDateContinuousArchive(Date startDate);

    Date getStartDateContinuousArchive();

    void updateStartDateSanityArchive(Date startDate);

    Date getStartDateSanityArchive();

}
