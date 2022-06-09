package eu.domibus.plugin.validation;

import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.Submission;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.Resource;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;
import java.util.Set;

/**
 * Created by Cosmin Baciu on 04-Aug-16.
 */
public class SchemaPayloadSubmissionValidator implements SubmissionValidator {

    protected static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(SchemaPayloadSubmissionValidator.class);

    private static final ThreadLocal<XMLInputFactory> xmlInputFactoryThreadLocal =
            ThreadLocal.withInitial(() -> {
                XMLInputFactory inputFactory = XMLInputFactory.newInstance();
                inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
                inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
                inputFactory.setProperty(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                return inputFactory;
            });

    protected JAXBContext jaxbContext;
    protected Resource schema;

    @Override
    public void validate(Submission submission) throws SubmissionValidationException {
        LOG.debug("Validating submission");

        Set<Submission.Payload> payloads = submission.getPayloads();
        if (payloads == null) {
            LOG.debug("There are no payloads to validate");
            return;
        }
        for (Submission.Payload payload : payloads) {
            validatePayload(payload);
        }
    }

    protected void validatePayload(Submission.Payload payload) {
        XmlValidationEventHandler jaxbValidationEventHandler = new XmlValidationEventHandler();
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            sf.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, StringUtils.EMPTY);
            sf.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, StringUtils.EMPTY);

            StreamSource xsdSource = new StreamSource(schema.getInputStream());
            Schema schema = sf.newSchema(xsdSource);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setSchema(schema);
            unmarshaller.setEventHandler(jaxbValidationEventHandler);
            InputStream payloadStream = payload.getPayloadDatahandler().getInputStream();
            XMLStreamReader streamReader = getXmlInputFactory().createXMLStreamReader(payloadStream);
            unmarshaller.unmarshal(streamReader);
            if (jaxbValidationEventHandler.hasErrors()) {
                throw new SubmissionValidationException("Error validating payload [" + payload.getContentId() + "]:" + jaxbValidationEventHandler.getErrorMessage());
            }
        } catch (SubmissionValidationException e) {
            throw e;
        } catch (Exception e) {
            String message = "Error validating the payload [" + payload.getContentId() + "]";
            if (jaxbValidationEventHandler.hasErrors()) {
                message += ":" + jaxbValidationEventHandler.getErrorMessage();
            }
            throw new SubmissionValidationException(message, e);
        }
    }

    public void setJaxbContext(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    public void setSchema(Resource schema) {
        this.schema = schema;
    }

    private XMLInputFactory getXmlInputFactory() {
        return xmlInputFactoryThreadLocal.get();
    }
}
