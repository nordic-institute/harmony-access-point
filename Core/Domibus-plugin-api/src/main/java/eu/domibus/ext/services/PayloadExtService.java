package eu.domibus.ext.services;

import eu.domibus.ext.domain.PartInfoDTO;
import eu.domibus.ext.exceptions.PayloadExtException;

import java.io.InputStream;

/**
 * Responsible for operations related with user message payloads.
 *
 * @author Cosmin Baciu
 * @since 5.0
 */
public interface PayloadExtService {

    /**
     * Validates the payload eg using an antivirus service if configured
     *
     * @param inputStream The payload's inputstream
     * @param mimeType    The payloads's mime type
     */
    void validatePayload(InputStream inputStream, String mimeType) throws PayloadExtException;

    PartInfoDTO getPayload(Long messageEntityId, String cid) throws PayloadExtException;

    PartInfoDTO getPayload(String messageId, String cid) throws PayloadExtException;
}
