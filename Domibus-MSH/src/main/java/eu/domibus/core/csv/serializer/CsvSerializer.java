package eu.domibus.core.csv.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.opencsv.ICSVWriter;

/**
 * @author François Gautier
 * @since 4.2
 */
public interface CsvSerializer {
    String LIST_DELIMITER = String.valueOf(ICSVWriter.DEFAULT_SEPARATOR);

    boolean canHandle(Object fieldValue);

    String serialize(Object fieldValue) throws JsonProcessingException;
}
