package eu.domibus.core.ebms3.mapper.signal;

import eu.domibus.api.ebms3.model.Ebms3SignalMessage;
import eu.domibus.api.model.SignalMessage;
import eu.domibus.core.ebms3.mapper.Ebms3CentralMapperConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Mapper(config = Ebms3CentralMapperConfig.class,
        uses = {
                Ebms3MessageInfoMapper.class,
                Ebms3PullRequestMapper.class,
                Ebms3ReceiptMapper.class,
                Ebms3ErrorMapper.class,
                Ebms3DescriptionMapper.class,
        })
public interface Ebms3SignalMapper {

    @Mapping(source = "messageInfo", target = "messageInfo")
    @Mapping(source = "pullRequest", target = "pullRequest")
    @Mapping(source = "receipt", target = "receipt")
    @Mapping(source = "error", target = "error")
    @Mapping(target = "any", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"rawEnvelopeLog","entityId", "creationTime", "modificationTime", "createdBy", "modifiedBy"})
    Ebms3SignalMessage signalMessageEntityToEbms3(SignalMessage signalMessage);

    @Mapping(target = "entityId", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "modificationTime", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"any"})
    @InheritInverseConfiguration
    SignalMessage signalMessageEbms3ToEntity(Ebms3SignalMessage ebms3SignalMessage);
}
