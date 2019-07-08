package eu.domibus.core.csv;

import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * @author Tiago Miguel
 * @since 4.0
 */
public interface CsvService {

    String APPLICATION_EXCEL_STR = "application/ms-excel";


    String exportToCSV(List<?> list, Class tClass,
                       final Map<String, String> customColumnNames, List<String> excludedColumns);

    int getMaxNumberRowsToExport();

    /**
     * It builds the {@code ResponseEntity} object used in Save As dialog
     *
     * @param resultText
     * @param moduleName
     * @return
     */
    ResponseEntity<String> getResponseEntity(final String resultText, final String moduleName);

}
