package eu.domibus.core.ebms3.mapper.usermessage;

import eu.domibus.api.ebms3.model.Ebms3AgreementRef;
import eu.domibus.api.model.AgreementRef;
import eu.domibus.core.ebms3.mapper.Ebms3CentralMapperConfig;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(config = Ebms3CentralMapperConfig.class)
public interface Ebms3AgreementRefMapper {

    @Mapping(source = "value", target = "value")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "pmode", target = "pmode")
    Ebms3AgreementRef agreementRefEntityToEbms3(AgreementRef agreementRef);

    @InheritInverseConfiguration
    AgreementRef agreementRefEbms3ToEntity(Ebms3AgreementRef ebms3AgreementRef);
}
