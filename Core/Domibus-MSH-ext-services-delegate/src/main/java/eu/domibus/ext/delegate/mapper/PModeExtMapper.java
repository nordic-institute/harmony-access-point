package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.pmode.PModeArchiveInfo;
import eu.domibus.api.pmode.ValidationIssue;
import eu.domibus.ext.domain.PModeArchiveInfoDTO;
import eu.domibus.ext.domain.ValidationIssueDTO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper to generate PMode abstract class conversion methods
 * @author François Gautier
 * @since 5.0
 */
@Mapper(componentModel = "spring")
public interface PModeExtMapper {

    PModeArchiveInfoDTO pModeArchiveInfoToPModeArchiveInfoDTO(PModeArchiveInfo pModeArchiveInfo);

    PModeArchiveInfo pModeArchiveInfoDTOToPModeArchiveInfo(PModeArchiveInfoDTO pModeArchiveInfoDto);

    List<ValidationIssueDTO> validationIssueToValidationIssueDTO(List<ValidationIssue> issues);

    List<ValidationIssue> validationIssueDTOToValidationIssue(List<ValidationIssueDTO> issues);
}
