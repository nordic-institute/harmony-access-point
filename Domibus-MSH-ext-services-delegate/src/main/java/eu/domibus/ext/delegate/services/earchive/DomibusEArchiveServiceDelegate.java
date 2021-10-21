package eu.domibus.ext.delegate.services.earchive;

import eu.domibus.api.earchive.DomibusEArchiveService;
import eu.domibus.ext.services.DomibusEArchiveExtService;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class DomibusEArchiveServiceDelegate implements DomibusEArchiveExtService {

    private final DomibusEArchiveService domibusEArchiveService;

    public DomibusEArchiveServiceDelegate(DomibusEArchiveService domibusEArchiveService) {
        this.domibusEArchiveService = domibusEArchiveService;
    }

    @Override
    public void updateStartDateContinuousArchive(Date startDate) {
        domibusEArchiveService.updateStartDateContinuousArchive(startDate);
    }
    @Override
    public void updateStartDateSanityArchive(Date startDate) {
        domibusEArchiveService.updateStartDateSanityArchive(startDate);
    }
}
