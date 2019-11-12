package eu.domibus.plugin.fs;

import javax.xml.bind.JAXBException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Cosmin Baciu
 */
public interface FSXMLHelper {

    <T> T parseXML(InputStream inputStream, Class<T> clazz) throws JAXBException;

    void writeXML(OutputStream outputStream, Class clazz, Object objectToWrite) throws JAXBException;
}
