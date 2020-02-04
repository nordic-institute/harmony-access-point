package eu.domibus.ext.delegate.services.pmode;

import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.api.pmode.PModeService;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.PModeArchiveInfoDTO;
import eu.domibus.ext.domain.PModeIssueDTO;
import eu.domibus.ext.services.PModeExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Catalin Enache
 * @since 4.1.1
 */
@Service
public class PModeServiceDelegate implements PModeExtService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PModeServiceDelegate.class);

    @Autowired
    private PModeService pModeService;

    @Autowired
    private DomainExtConverter domainConverter;

    @Override
    public byte[] getPModeFile(int id) {
        return pModeService.getPModeFile(id);
    }

    @Override
    public PModeArchiveInfoDTO getCurrentPmode() {
        final PModeArchiveInfo pModeArchiveInfo = pModeService.getCurrentPMode();
        return domainConverter.convert(pModeArchiveInfo, PModeArchiveInfoDTO.class);
    }

    @Override
    public List<PModeIssueDTO> updatePModeFile(byte[] bytes, String description) {
        List<PModeIssue> issues = pModeService.updatePModeFile(bytes, description);
        return domainConverter.convert(issues, PModeIssueDTO.class);
    }
}
