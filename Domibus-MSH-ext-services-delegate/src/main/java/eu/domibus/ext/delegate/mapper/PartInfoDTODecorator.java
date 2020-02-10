package eu.domibus.ext.delegate.mapper;

import eu.domibus.api.converter.ConverterException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.usermessage.domain.PartInfo;
import eu.domibus.ext.domain.PartInfoDTO;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;


/**
 * @since 4.2
 */
public abstract class PartInfoDTODecorator implements PartInfoMapper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartInfoDTODecorator.class);

    @Autowired
    @Qualifier("delegate")
    protected PartInfoMapper delegate;

    public PartInfoDTODecorator() {
    }

    @Override
    public PartInfoDTO partInfoToPartInfoDTO(PartInfo partInfo) {
        PartInfoDTO partInfoDTO = delegate.partInfoToPartInfoDTO(partInfo);

        InputStream inputStream = null;
        try {
            inputStream = partInfo.getPayloadDatahandler().getInputStream();
            byte[] payloadBytes = IOUtils.toByteArray(inputStream);
            String resultBase64Encoded = Base64.getEncoder().encodeToString(payloadBytes);
            partInfoDTO.setPayload(resultBase64Encoded);
        } catch (IOException e) {
            throw new ConverterException(DomibusCoreErrorCode.DOM_008, "Could not convert payload", e);
        }
        return partInfoDTO;
    }


}