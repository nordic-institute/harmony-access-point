package eu.domibus.core.csv.serializer;

import com.opencsv.ICSVWriter;
import eu.domibus.api.exceptions.DomibusCoreException;

/**
 * @author François Gautier
 * @since 4.2
 */
public interface CsvSerializer {
    String LIST_DELIMITER = String.valueOf(ICSVWriter.DEFAULT_SEPARATOR);

    boolean canHandle(Object fieldValue);

    String serialize(Object fieldValue) throws DomibusCoreException;
}
