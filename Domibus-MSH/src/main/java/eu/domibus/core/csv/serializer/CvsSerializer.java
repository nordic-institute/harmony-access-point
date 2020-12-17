package eu.domibus.core.csv.serializer;

import com.opencsv.ICSVWriter;

/**
 * @author François Gautier
 * @since 4.2
 */
public interface CvsSerializer {
    static final String LIST_DELIMITER = String.valueOf(ICSVWriter.DEFAULT_SEPARATOR);

    boolean canHandle(Object fieldValue);
    String serialize(Object fieldValue);
}
