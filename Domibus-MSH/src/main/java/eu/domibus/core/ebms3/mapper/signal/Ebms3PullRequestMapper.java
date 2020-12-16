package eu.domibus.core.ebms3.mapper.signal;

import eu.domibus.api.ebms3.model.Ebms3PullRequest;
import eu.domibus.api.model.PullRequest;
import eu.domibus.core.ebms3.mapper.Ebms3CentralMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = Ebms3CentralMapperConfig.class)
public interface Ebms3PullRequestMapper {

    @Mapping(source = "mpc", target = "mpc")
    @Mapping(target = "otherAttributes", ignore = true)
    Ebms3PullRequest pullRequestEntityToEbms3(PullRequest pullRequest);

    @BeanMapping(ignoreUnmappedSourceProperties = {"otherAttributes"})
    @InheritInverseConfiguration
    PullRequest pullRequestEbms3ToEntity(Ebms3PullRequest ebms3PullRequest);
}
