package eu.domibus.core.util.xml;

import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.xml.DefaultUnmarshallerResult;
import eu.domibus.api.util.xml.UnmarshallerResult;
import eu.domibus.api.util.xml.XMLUtil;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.validation.XmlValidationEventHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.ByteArrayInputStream;
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
@Component(XMLUtil.BEAN_NAME)
public class XMLUtilImpl implements XMLUtil {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(XMLUtilImpl.class);

    protected DomibusPropertyProvider domibusPropertyProvider;

    public XMLUtilImpl(DomibusPropertyProvider domibusPropertyProvider) {
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    private static final ThreadLocal<DocumentBuilderFactory> documentBuilderFactoryThreadLocal =
            ThreadLocal.withInitial(() -> {
                DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                makeSafe(documentBuilderFactory);
                return documentBuilderFactory;
            });

    private static final ThreadLocal<DocumentBuilderFactory> documentBuilderFactoryNamespaceAwareThreadLocal = ThreadLocal.withInitial(() -> {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        makeSafe(documentBuilderFactory);
        return documentBuilderFactory;
    });

    private static final ThreadLocal<TransformerFactory> transformerFactoryThreadLocal =
            ThreadLocal.withInitial(XMLUtilImpl::createTransformerFactory);

    private static final ThreadLocal<MessageFactory> messageFactoryThreadLocal = ThreadLocal.withInitial(() -> {
        try {
            return MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
        } catch (SOAPException e) {
            throw new DomibusXMLException("Error initializing MessageFactory", e);
        }
    });

    private static final ThreadLocal<XMLInputFactory> xmlInputFactoryThreadLocal =
            ThreadLocal.withInitial(() -> {
                XMLInputFactory inputFactory = XMLInputFactory.newInstance();
                inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
                inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
                inputFactory.setProperty(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                return inputFactory;
            });

    public DocumentBuilderFactory getDocumentBuilderFactory() {
        return documentBuilderFactoryThreadLocal.get();
    }

    public DocumentBuilderFactory getDocumentBuilderFactoryNamespaceAware() {
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


    @Override
    public MessageFactory getMessageFactorySoap12() {
        return messageFactoryThreadLocal.get();
    }

    @Override
    public TransformerFactory getTransformerFactory() {
        return transformerFactoryThreadLocal.get();
    }

    @Override
    public UnmarshallerResult unmarshal(boolean ignoreWhitespaces, JAXBContext jaxbContext, InputStream xmlStream, InputStream xsdStream)
            throws SAXException, JAXBException, XMLStreamException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        if (xsdStream != null) {
            Schema schema = getSchema(xsdStream);
            unmarshaller.setSchema(schema);
        }

        XmlValidationEventHandler jaxbValidationEventHandler = new XmlValidationEventHandler();
        unmarshaller.setEventHandler(jaxbValidationEventHandler);

        XMLInputFactory inputFactory = getXmlInputFactory();

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
    public XMLInputFactory getXmlInputFactory() {
        return xmlInputFactoryThreadLocal.get();
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

    @Override
    public XMLStreamReader getXmlStreamReaderFromNode(Node messagingXml) throws TransformerException, XMLStreamException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Source xmlSource = new DOMSource(messagingXml);
        Result outputTarget = new StreamResult(outputStream);
        final Transformer transformer = getTransformerFactory().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.transform(xmlSource, outputTarget);
        InputStream is = new ByteArrayInputStream(outputStream.toByteArray());

        XMLInputFactory inputFactory = getXmlInputFactory();
        XMLStreamReader reader = inputFactory.createXMLStreamReader(is);
        return reader;
    }

    private Schema getSchema(InputStream xsdStream) throws SAXException {
        SchemaFactory schemaFactory = createSchemaFactoryInstance();
        schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, StringUtils.EMPTY);
        schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, StringUtils.EMPTY);
        return schemaFactory.newSchema(new StreamSource(xsdStream));
    }

    private SchemaFactory createSchemaFactoryInstance() {
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

    private static void makeSafe(DocumentBuilderFactory dbf) {
        String feature = null;
        try {
            feature = "http://apache.org/xml/features/disallow-doctype-decl";
            dbf.setFeature(feature, true);

            feature = "http://xml.org/sax/features/external-general-entities";
            dbf.setFeature(feature, false);

            feature = "http://xml.org/sax/features/external-parameter-entities";
            dbf.setFeature(feature, false);

            feature = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
            dbf.setFeature(feature, false);
        } catch (ParserConfigurationException e) {
            throw new DomibusXMLException(String.format("The feature [%s] is probably not supported by your XML processor", feature), e);
        }

        try {
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);

            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, StringUtils.EMPTY);
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, StringUtils.EMPTY);
        } catch (Exception ex) {
            throw new DomibusXMLException("Could not secure the XML processor", ex);
        }
    }
}
