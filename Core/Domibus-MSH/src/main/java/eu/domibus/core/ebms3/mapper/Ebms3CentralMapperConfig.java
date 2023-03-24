package eu.domibus.core.ebms3.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@MapperConfig(
        componentModel = "spring",
        unmappedSourcePolicy = ReportingPolicy.ERROR,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface Ebms3CentralMapperConfig {
}
