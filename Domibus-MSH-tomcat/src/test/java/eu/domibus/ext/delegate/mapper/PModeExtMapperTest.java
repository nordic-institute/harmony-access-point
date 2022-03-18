package eu.domibus.ext.delegate.mapper;

import eu.domibus.AbstractIT;
import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.ext.domain.PModeArchiveInfoDTO;
import eu.domibus.ext.domain.ValidationIssueDTO;
import eu.europa.ec.digit.commons.test.api.ObjectService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * @author François Gautier
 * @since 5.0
 */
@Transactional
public class PModeExtMapperTest extends AbstractIT {

    @Autowired
    private PModeExtMapper pModeExtMapper;

    @Autowired
    private ObjectService objectService;

    @Test
    public void PModeArchiveInfoToPModeArchiveInfoDTO() {
        PModeArchiveInfoDTO toConvert = (PModeArchiveInfoDTO) objectService.createInstance(PModeArchiveInfoDTO.class);
        final PModeArchiveInfo converted = pModeExtMapper.pModeArchiveInfoDTOToPModeArchiveInfo(toConvert);
        final PModeArchiveInfoDTO convertedBack = pModeExtMapper.pModeArchiveInfoToPModeArchiveInfoDTO(converted);

        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void PModeArchiveInfoDTOToPModeArchiveInfo() {
        PModeArchiveInfo toConvert = (PModeArchiveInfo) objectService.createInstance(PModeArchiveInfo.class);
        final PModeArchiveInfoDTO converted = pModeExtMapper.pModeArchiveInfoToPModeArchiveInfoDTO(toConvert);
        final PModeArchiveInfo convertedBack = pModeExtMapper.pModeArchiveInfoDTOToPModeArchiveInfo(converted);

        objectService.assertObjects(convertedBack, toConvert);
    }

    @Test
    public void ValidationIssueToValidationIssueDTO() {
        ValidationIssueDTO toConvert = (ValidationIssueDTO) objectService.createInstance(ValidationIssueDTO.class);
        final List<ValidationIssue> converted = pModeExtMapper.validationIssueDTOToValidationIssue(Collections.singletonList(toConvert));
        final List<ValidationIssueDTO> convertedBack = pModeExtMapper.validationIssueToValidationIssueDTO(converted);

        objectService.assertObjects(convertedBack.get(0), toConvert);
    }

    @Test
    public void ValidationIssueDTOToValidationIssue() {
        ValidationIssue toConvert = (ValidationIssue) objectService.createInstance(ValidationIssue.class);
        final List<ValidationIssueDTO> converted = pModeExtMapper.validationIssueToValidationIssueDTO(Collections.singletonList(toConvert));
        final List<ValidationIssue> convertedBack = pModeExtMapper.validationIssueDTOToValidationIssue(converted);

        objectService.assertObjects(convertedBack.get(0), toConvert);
    }
}