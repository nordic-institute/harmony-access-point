package eu.domibus.ext.delegate.services.pmode;

import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.api.pmode.PModeService;
import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.ext.delegate.converter.DomainExtConverter;
import eu.domibus.ext.domain.PModeArchiveInfoDTO;
import eu.domibus.ext.domain.ValidationIssueDTO;
import eu.domibus.ext.services.PModeExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
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

    @Autowired
    MultiPartFileUtil multiPartFileUtil;

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
    public List<ValidationIssueDTO> updatePModeFile(byte[] bytes, String description) {
        List<ValidationIssue> issues = pModeService.updatePModeFile(bytes, description);
        return domainConverter.convert(issues, ValidationIssueDTO.class);
    }

    @Override
    public List<ValidationIssueDTO> updatePModeFile(MultipartFile file, String description) {
        byte[] bytes = multiPartFileUtil.validateAndGetFileContent(file, Arrays.asList(MimeTypeUtils.APPLICATION_XML, MimeTypeUtils.TEXT_XML));
        List<ValidationIssue> issues = pModeService.updatePModeFile(bytes, description);
        return domainConverter.convert(issues, ValidationIssueDTO.class);
    }
}
