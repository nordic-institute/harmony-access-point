package eu.domibus.plugin.fs;

import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Cosmin Baciu
 * @since 4.1.2
 */
public interface FSXMLHelper {

    <T> T parseXML(InputStream inputStream, Class<T> clazz) throws JAXBException;

    <T> void writeXML(OutputStream outputStream, Class<T> clazz, T objectToWrite) throws JAXBException;
}
