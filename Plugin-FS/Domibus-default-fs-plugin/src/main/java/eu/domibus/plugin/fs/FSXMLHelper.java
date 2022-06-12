package eu.domibus.plugin.fs;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Cosmin Baciu
 * @since 4.1.2
 */
public interface FSXMLHelper {

    <T> T parseXML(InputStream inputStream, Class<T> clazz) throws JAXBException, XMLStreamException;

    <T> void writeXML(OutputStream outputStream, Class<T> clazz, T objectToWrite) throws JAXBException;
}
