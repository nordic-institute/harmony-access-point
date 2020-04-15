package eu.domibus.web.rest;

import eu.domibus.core.csv.CsvService;
import eu.domibus.core.csv.CsvServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Groups all common REST Resource code
 *
 * @author Catalin Enache
 * @since 4.1.1
 */
public abstract class BaseResource {

    @Autowired
    CsvServiceImpl csvServiceImpl;

    /**
     * Get the Csv service
     *
     * @return
     */
    protected CsvService getCsvService() {
        return csvServiceImpl;
    }

    /**
     * exports to CSV
     *
     * @param list              the list of objects to export
     * @param itemClass         the class of the object instances, used to determine the columns
     * @param customColumnNames needed in case different column titles than the attribute name
     * @param excludedColumns   the list of excluded columns from the final export
     * @param moduleName        the seed of the name of the generated file
     * @return The comma-separated list of records
     */
    protected ResponseEntity<String> exportToCSV(List<?> list, Class itemClass, final Map<String, String> customColumnNames,
                                                 List<String> excludedColumns, final String moduleName) {

        String result = getCsvService().exportToCSV(list, itemClass, customColumnNames, excludedColumns);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(CsvService.APPLICATION_EXCEL_STR))
                .header("Content-Disposition", "attachment; filename=\"" + getCsvService().getCsvFilename(moduleName) + "\"")
                .body(result);
    }

    /**
     * Overloaded method to export as CSV
     *
     * @param list       the list of objects to export
     * @param itemClass  the class of the object instances, used to determine the columns
     * @param moduleName the seed of the name of the generated file
     * @return The comma-separated list of records
     */
    protected ResponseEntity<String> exportToCSV(List<?> list, Class itemClass, final String moduleName) {
        return exportToCSV(list, itemClass, new HashMap<>(), new ArrayList<>(), moduleName);
    }

    /**
     * Overloaded method to export as CSV
     *
     * @param list              the list of objects to export
     * @param itemClass         the class of the object instances, used to determine the columns
     * @param customColumnNames needed in case different column titles than the attribute name
     * @param moduleName        the seed of the name of the generated file
     * @return The comma-separated list of records
     */
    protected ResponseEntity<String> exportToCSV(List<?> list, Class itemClass, final Map<String, String> customColumnNames,
                                                 final String moduleName) {
        return exportToCSV(list, itemClass, customColumnNames, new ArrayList<>(), moduleName);
    }

    /**
     * Overloaded method to export as CSV
     *
     * @param list            the list of objects to export
     * @param itemClass       the class of the object instances, used to determine the columns
     * @param excludedColumns the list of excluded columns from the final export
     * @param moduleName      the seed of the name of the generated file
     * @return The comma-separated list of records
     */
    protected ResponseEntity<String> exportToCSV(List<?> list, Class itemClass, List<String> excludedColumns,
                                                 final String moduleName) {
        return exportToCSV(list, itemClass, new HashMap<>(), excludedColumns, moduleName);
    }

}
