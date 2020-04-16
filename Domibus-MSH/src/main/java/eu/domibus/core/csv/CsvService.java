package eu.domibus.core.csv;

import eu.domibus.api.exceptions.RequestValidationException;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Tiago Miguel
 * @since 4.0
 */
public interface CsvService {

    String APPLICATION_EXCEL_STR = "application/ms-excel";

    String exportToCSV(List<?> list, Class tClass, final Map<String, String> customColumnNames, List<String> excludedColumns);

    int getMaxNumberRowsToExport();

    String getCsvFilename(String module);

    int getPageSizeForExport();

    /**
     * Validates that the number of items to export is less than the maximum allowed
     * @param count number of items to export, it is the size of the items list
     * @throws RequestValidationException
     */
    void validateMaxRows(long count) throws RequestValidationException;

    /**
     * Validates that the number of items to export is less than the maximum allowed
     * @param count the number of items retrieved, where the page size was set to max allowed + 1 to see if there are more elements
     * @param countMethod in case there are more elements than the max allowed, count the elements to show to the user
     * @throws RequestValidationException in case there are more elements, the total count is calculated and error thrown
     */
    void validateMaxRows(long count, Supplier<Long> countMethod) throws RequestValidationException;
}
