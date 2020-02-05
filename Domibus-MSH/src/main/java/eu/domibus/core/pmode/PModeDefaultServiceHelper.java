package eu.domibus.core.pmode;

import eu.domibus.api.pmode.PModeIssue;
import eu.domibus.api.pmode.PModeServiceHelper;
import eu.domibus.api.pmode.PModeValidationException;
import eu.domibus.api.pmode.domain.LegConfiguration;
import eu.domibus.messaging.XmlProcessingException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
@Service
public class PModeDefaultServiceHelper implements PModeServiceHelper {

    @Override
    public Integer getMaxAttempts(LegConfiguration legConfiguration) {
        return legConfiguration.getReceptionAwareness() == null ? 1 : legConfiguration.getReceptionAwareness().getRetryCount();
    }
}
