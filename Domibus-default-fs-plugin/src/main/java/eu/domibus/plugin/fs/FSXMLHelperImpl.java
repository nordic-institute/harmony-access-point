package eu.domibus.plugin.fs;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.ebms3.ObjectFactory;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 * @author Cosmin Baciu
 */
public class FSXMLHelperImpl implements FSXMLHelper {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(FSXMLHelperImpl.class);

    private static final String XSD_FILES_LOCATION = "xsd";
    private static final String DEFAULT_SCHEMA = "fs-plugin.xsd";

    protected JAXBContext jaxbContext;

    public FSXMLHelperImpl(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    @Override
    public <T> T parseXML(InputStream inputStream, Class<T> clazz) throws JAXBException {
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        try {
            Schema schema = loadSchema(DEFAULT_SCHEMA);
            unmarshaller.setSchema(schema);
        } catch (SAXException ex) {
            LOG.warn("Error loading default schema", ex);
        }

        StreamSource streamSource = new StreamSource(inputStream);
        JAXBElement<T> jaxbElement = unmarshaller.unmarshal(streamSource, clazz);

        return jaxbElement.getValue();
    }

    /**
     * Writes an object to XML file
     *
     * @param outputStream  outputStream to write into
     * @param clazz         class of the object
     * @param objectToWrite object to write
     * @throws JAXBException JAXB exception
     */
    @Override
    public void writeXML(OutputStream outputStream, Class clazz, Object objectToWrite) throws JAXBException {
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        QName qName = new QName("http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/", clazz.getSimpleName());

        marshaller.marshal(new JAXBElement(qName, clazz, null, objectToWrite), outputStream);
    }

    protected Schema loadSchema(String schemaName) throws SAXException {
        return loadSchema(FSXMLHelperImpl.class.getClassLoader().getResourceAsStream(XSD_FILES_LOCATION + "/" + schemaName));
    }

    protected Schema loadSchema(InputStream inputStream) throws SAXException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        StreamSource schemaSource = new StreamSource(inputStream);
        return schemaFactory.newSchema(schemaSource);
    }
}
