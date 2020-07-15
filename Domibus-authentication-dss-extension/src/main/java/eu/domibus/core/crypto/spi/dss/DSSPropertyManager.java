package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.domain.DomibusPropertyMetadataDTO;
import eu.domibus.ext.domain.Module;
import eu.domibus.ext.services.DomibusPropertyExtServiceDelegateAbstract;
import org.springframework.stereotype.Service;
import eu.domibus.ext.domain.DomibusPropertyMetadataDTO.Usage;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Ion Perpegel
 * @since 4.2
 * <p>
 * Property manager for the DSS properties.
 */
@Service
public class DSSPropertyManager extends DomibusPropertyExtServiceDelegateAbstract {

    public static final String DOMIBUS_AUTHENTICATION_DSS_CONSTRAINT_NAME = "domibus.authentication.dss.constraint.name";
    public static final String DOMIBUS_AUTHENTICATION_DSS_CONSTRAINT_STATUS = "domibus.authentication.dss.constraint.status";

    private Map<String, DomibusPropertyMetadataDTO> knownProperties = Arrays.stream(new String[]{
            DOMIBUS_AUTHENTICATION_DSS_CONSTRAINT_NAME,
            DOMIBUS_AUTHENTICATION_DSS_CONSTRAINT_STATUS
    })
            .map(name -> new DomibusPropertyMetadataDTO(name, Module.DSS, false, Usage.DOMAIN, true, true, false, true))
            .collect(Collectors.toMap(x -> x.getName(), x -> x));

    @Override
    public Map<String, DomibusPropertyMetadataDTO> getKnownProperties() {
        return knownProperties;
    }

}
