package eu.domibus.ext.delegate.services.pmode;

import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.api.pmode.PModeService;
import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.api.util.MultiPartFileUtil;
import eu.domibus.ext.delegate.mapper.PModeExtMapper;
import eu.domibus.ext.domain.PModeArchiveInfoDTO;
import eu.domibus.ext.domain.ValidationIssueDTO;
import eu.domibus.ext.services.PModeExtService;
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

    protected PModeService pModeService;

    protected final PModeExtMapper domibusExtMapper;

    final MultiPartFileUtil multiPartFileUtil;

    public PModeServiceDelegate(PModeService pModeService, PModeExtMapper pModeExtMapper, MultiPartFileUtil multiPartFileUtil) {
        this.pModeService = pModeService;
        this.domibusExtMapper = pModeExtMapper;
        this.multiPartFileUtil = multiPartFileUtil;
    }

    @Override
    public byte[] getPModeFile(int id) {
        return pModeService.getPModeFile(id);
    }

    @Override
    public PModeArchiveInfoDTO getCurrentPmode() {
        final PModeArchiveInfo pModeArchiveInfo = pModeService.getCurrentPMode();
        return domibusExtMapper.pModeArchiveInfoToPModeArchiveInfoDTO(pModeArchiveInfo);
    }

    @Override
    public List<ValidationIssueDTO> updatePModeFile(byte[] bytes, String description) {
        List<ValidationIssue> issues = pModeService.updatePModeFile(bytes, description);
        return domibusExtMapper.validationIssueToValidationIssueDTO(issues);
    }

    @Override
    public List<ValidationIssueDTO> updatePModeFile(MultipartFile file, String description) {
        byte[] bytes = multiPartFileUtil.validateAndGetFileContent(file, Arrays.asList(MimeTypeUtils.APPLICATION_XML, MimeTypeUtils.TEXT_XML));
        List<ValidationIssue> issues = pModeService.updatePModeFile(bytes, description);
        return domibusExtMapper.validationIssueToValidationIssueDTO(issues);
    }
}
