package eu.domibus.api.earchive;

import java.util.Date;

/**
 * @author François Gautier
 * @since 5.0
 */
public interface DomibusEArchiveService {

    void updateStartDateContinuousArchive(Date startDate);
    void updateStartDateSanityArchive(Date startDate);

}
