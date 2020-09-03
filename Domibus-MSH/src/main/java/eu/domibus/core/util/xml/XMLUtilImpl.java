package eu.domibus.core.util.xml;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.xml.DefaultUnmarshallerResult;
import eu.domibus.api.util.xml.UnmarshallerResult;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.validation.XmlValidationEventHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SCHEMAFACTORY;

/**
 * StAX marshaller and unmarshaller utility class.
 *
 * @author Cosmin BACIU
 * @author Sebastian-Ion TINCU
 * @since 3.2
 */
@Component
public class XMLUtilImpl implements XMLUtil {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(XMLUtilImpl.class);

    protected DomibusPropertyProvider domibusPropertyProvider;

    public XMLUtilImpl(DomibusPropertyProvider domibusPropertyProvider) {
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    private static final ThreadLocal<DocumentBuilderFactory> documentBuilderFactoryThreadLocal = ThreadLocal.withInitial(() -> {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        return documentBuilderFactory;
    });

    private static final ThreadLocal<DocumentBuilderFactory> documentBuilderFactoryNamespaceAwareThreadLocal = ThreadLocal.withInitial(() -> {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        return documentBuilderFactory;
    });

    private static final ThreadLocal<TransformerFactory> transformerFactoryThreadLocal = ThreadLocal.withInitial(() -> {
        return createTransformerFactory();
    });

    private static final ThreadLocal<MessageFactory> messageFactoryThreadLocal = ThreadLocal.withInitial(() -> {
        try {
            return MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        } catch (SOAPException e) {
            throw new DomibusXMLException("Error initializing MessageFactory", e);
        }
    });

    public static DocumentBuilderFactory getDocumentBuilderFactory() {
        return documentBuilderFactoryThreadLocal.get();
    }

    public static DocumentBuilderFactory getDocumentBuilderFactoryNamespaceAware() {
        return documentBuilderFactoryNamespaceAwareThreadLocal.get();
    }

    public static TransformerFactory createTransformerFactory() {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        try {
            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        } catch (TransformerConfigurationException e) {
            throw new DomibusXMLException("Error initializing TransformerFactory", e);
        }
        return transformerFactory;
    }

    public static MessageFactory getMessageFactory() {
        return messageFactoryThreadLocal.get();
    }

    public static TransformerFactory getTransformerFactory() {
        return transformerFactoryThreadLocal.get();
    }

    @Override
    public UnmarshallerResult unmarshal(boolean ignoreWhitespaces, JAXBContext jaxbContext, InputStream xmlStream, InputStream xsdStream) throws SAXException, JAXBException, XMLStreamException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        if (xsdStream != null) {
            Schema schema = getSchema(xsdStream);
            unmarshaller.setSchema(schema);
        }

        XmlValidationEventHandler jaxbValidationEventHandler = new XmlValidationEventHandler();
        unmarshaller.setEventHandler(jaxbValidationEventHandler);

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);

        XMLEventReader eventReader = inputFactory.createXMLEventReader(xmlStream);
        if (ignoreWhitespaces) {
            eventReader = inputFactory.createFilteredReader(eventReader, new WhitespaceFilter());
        }

        DefaultUnmarshallerResult result = new DefaultUnmarshallerResult();
        result.setResult(unmarshaller.unmarshal(eventReader));
        result.setValid(!jaxbValidationEventHandler.hasErrors());
        result.setErrors(jaxbValidationEventHandler.getErrors());
        return result;
    }

    @Override
    public byte[] marshal(JAXBContext jaxbContext, Object input, InputStream xsdStream) throws SAXException, JAXBException {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        if (xsdStream != null) {
            Schema schema = getSchema(xsdStream);
            marshaller.setSchema(schema);
        }

        ByteArrayOutputStream xmlStream = new ByteArrayOutputStream();
        marshaller.marshal(input, xmlStream);
        return xmlStream.toByteArray();
    }

    private Schema getSchema(InputStream xsdStream) throws SAXException {
        SchemaFactory schemaFactory = schemaFactoryInstance();
        schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, StringUtils.EMPTY);
        schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, StringUtils.EMPTY);
        return schemaFactory.newSchema(new StreamSource(xsdStream));
    }

    private SchemaFactory schemaFactoryInstance() {
        String schemaFactoryClassName = domibusPropertyProvider.getProperty(DOMIBUS_SCHEMAFACTORY);
        try {
            LOG.trace("Found [{}] class name for [{}]", schemaFactoryClassName, DOMIBUS_SCHEMAFACTORY);
            return (SchemaFactory) Class.forName(schemaFactoryClassName).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NullPointerException e) {
            LOG.error("Could not instantiate [{}]", schemaFactoryClassName, e);
        }
        LOG.trace("Using default XML Schema factory");
        return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    }

}
