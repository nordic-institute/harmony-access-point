package eu.domibus.ext.delegate.services.message;

import eu.domibus.api.util.DomibusStringUtil;
import eu.domibus.ext.services.MessageExtService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * @author Sebastian-Ion TINCU
 * @since 4.1
 */
@Service
public class MessageServiceImpl implements MessageExtService {

    private final DomibusStringUtil domibusStringUtil;

    public MessageServiceImpl(DomibusStringUtil domibusStringUtil) {
        this.domibusStringUtil = domibusStringUtil;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String cleanMessageIdentifier(String messageId) {
        return StringUtils.trimToEmpty(messageId);
    }

    @Override
    public boolean isTrimmedStringLengthLongerThanDefaultMaxLength(String messageId){
        return domibusStringUtil.isTrimmedStringLengthLongerThanDefaultMaxLength(messageId);
    }

    @Override
    public String sanitizeFileName(String fileName){
        return domibusStringUtil.sanitizeFileName(fileName);
    }
}
