package eu.domibus.core.ebms3.mapper;

import eu.domibus.api.ebms3.model.Ebms3Messaging;
import eu.domibus.api.model.Messaging;
import eu.domibus.core.ebms3.mapper.signal.Ebms3SignalMapper;
import eu.domibus.core.ebms3.mapper.usermessage.Ebms3UserMessageMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = Ebms3CentralMapperConfig.class,
        uses = {
                Ebms3UserMessageMapper.class,
                Ebms3SignalMapper.class,
        })
public interface Ebms3MessagingMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "signalMessage", target = "signalMessage")
    @Mapping(source = "userMessage", target = "userMessage")
    @Mapping(target = "s12MustUnderstand", ignore = true)
    @Mapping(target = "otherAttributes", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"entityId", "creationTime", "modificationTime", "createdBy", "modifiedBy"})
    Ebms3Messaging messagingEntityToEbms3(Messaging messaging);

    @Mapping(target = "entityId", ignore = true)
    @Mapping(target = "creationTime", ignore = true)
    @Mapping(target = "modificationTime", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "modifiedBy", ignore = true)
    @BeanMapping(ignoreUnmappedSourceProperties = {"s12MustUnderstand", "otherAttributes"})
    @InheritInverseConfiguration
    Messaging messagingEbms3ToEntity(Ebms3Messaging ebms3Messaging);
}
