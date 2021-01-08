package eu.domibus.core.ebms3.mapper.usermessage;

import eu.domibus.api.ebms3.model.Ebms3MessageProperties;
import eu.domibus.api.model.MessageProperties;
import eu.domibus.core.ebms3.mapper.Ebms3CentralMapperConfig;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Mapper(config = Ebms3CentralMapperConfig.class,
        uses = {
                Ebms3PropertyMapper.class,
        })
public interface Ebms3MessagePropertiesMapper {

    @Mapping(source = "property", target = "property")
    Ebms3MessageProperties descriptionEntityToEbms3(MessageProperties messageProperties);

    @InheritInverseConfiguration
    MessageProperties descriptionEbms3ToEntity(Ebms3MessageProperties ebms3MessageProperties);
}
