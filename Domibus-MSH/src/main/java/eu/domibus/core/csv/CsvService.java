package eu.domibus.core.csv;

import eu.domibus.api.exceptions.RequestValidationException;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Tiago Miguel
 * @since 4.0
 */
public interface CsvService {

    String APPLICATION_EXCEL_STR = "application/ms-excel";


    String exportToCSV(List<?> list, Class tClass,
                       final Map<String, String> customColumnNames, List<String> excludedColumns);

    int getMaxNumberRowsToExport();

    String getCsvFilename(String module);

    int getPageSizeForExport();

    void validateMaxRows(Integer count) throws RequestValidationException;

    <R> void validateMaxRows(Integer count, Supplier<R> countMethod) throws RequestValidationException;
}
