package eu.domibus.plugin.webService.custom;

import eu.domibus.plugin.Submission;
import eu.domibus.plugin.validation.SubmissionValidationException;
import eu.domibus.plugin.validation.SubmissionValidator;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Set;

/**
 * @author Cosmin Baciu
 */
public class CustomSubmissionValidator implements SubmissionValidator {

    public static final String MIME_TYPE = "MimeType";
    public static final String TEXT_XML = "text/xml";

    @Override
    public void validate(Submission submission) throws SubmissionValidationException {
        Set<Submission.Payload> payloads = submission.getPayloads();
        if (payloads == null) {
            return;
        }
        for (Submission.Payload payload : payloads) {
            validatePayload(payload);
        }
    }

    private void validatePayload(Submission.Payload payload) {
        //validate payload if the mime type is text/xml for instance
        //any custom condition can be implemented based on the business needs
        Submission.TypedProperty mimeTypeProperty = getMimeTypeProperty(payload);
        if (mimeTypeProperty == null) {
            throw new SubmissionValidationException(MIME_TYPE + " property is mandatory");
        }
        String mimeTypeValue = mimeTypeProperty.getValue();
        if (!StringUtils.equals(TEXT_XML, mimeTypeValue)) {
            throw new SubmissionValidationException(String.format("Only payloads having [%s]=[%s] are accepted", MIME_TYPE, TEXT_XML));
        }
    }

    private Submission.TypedProperty getMimeTypeProperty(Submission.Payload payload) {
        Collection<Submission.TypedProperty> payloadProperties = payload.getPayloadProperties();
        if (payloadProperties == null) {
            return null;
        }

        for (Submission.TypedProperty payloadProperty : payloadProperties) {
            if (StringUtils.equals(MIME_TYPE, payloadProperty.getKey())) {
                return payloadProperty;
            }
        }
        return null;
    }

}
