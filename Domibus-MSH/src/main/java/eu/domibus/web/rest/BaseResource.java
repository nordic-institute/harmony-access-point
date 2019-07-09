package eu.domibus.web.rest;

import eu.domibus.api.csv.CsvException;
import eu.domibus.core.csv.CsvService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * Groups all common REST Resource code
 *
 * @author Catalin Enache
 * @since 4.1.1
 */
public abstract class BaseResource {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BaseResource.class);


    /**
     * exports to CSV
     * @param list
     * @param tClass
     * @param customColumnNames
     * @param excludedColumns
     * @param moduleName
     * @return
     */
    protected ResponseEntity<String> exportToCSV(List<?> list, Class tClass,
                                                 final Map<String, String> customColumnNames,
                                                 List<String> excludedColumns,
                                                 final String moduleName) {
        String resultText;
        try {
            resultText = getCsvService().exportToCSV(list, tClass,
                    customColumnNames, excludedColumns);
        } catch (CsvException e) {
            LOG.error("Exception caught during export to CSV", e);
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(CsvService.APPLICATION_EXCEL_STR))
                .header("Content-Disposition", "attachment; filename=\"" + getCsvService().getCsvFilename(moduleName)+"\"")
                .body(resultText);
    }

    public abstract CsvService getCsvService();
}
