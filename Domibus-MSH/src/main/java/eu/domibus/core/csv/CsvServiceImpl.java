package eu.domibus.core.csv;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.opencsv.CSVWriter;
import eu.domibus.api.csv.CsvException;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.exceptions.RequestValidationException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.util.DomibusStringUtil;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_UI_CSV_MAX_ROWS;

/**
 * @author Tiago Miguel
 * @author Ion Perpegel
 * @since 4.0
 */

@Service
public class CsvServiceImpl implements CsvService {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CsvServiceImpl.class);

    @Autowired
    private DomibusPropertyProvider domibusPropertyProvider;

    @Override
    public String exportToCSV(List<?> list, Class theClass,
                              Map<String, String> customColumnNames, List<String> excludedColumns) {
        StringWriter result = new StringWriter();
        CSVWriter csvBuilder = new CSVWriter(result);

        List<Field> activeFields = getExportedFields(list, theClass, excludedColumns);

        createCSVColumnHeader(csvBuilder, activeFields, customColumnNames);
        createCSVContents(list, csvBuilder, activeFields);

        return result.toString();
    }

    @Override
    public int getMaxNumberRowsToExport() {
        return domibusPropertyProvider.getIntegerProperty(DOMIBUS_UI_CSV_MAX_ROWS);
    }

    @Override
    public String getCsvFilename(String module) {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        return module + "_datatable_" + dateFormat.format(date) + ".csv";
    }

    @Override
    public int getPageSizeForExport() {
        return getMaxNumberRowsToExport() + 1;
    }

    @Override
    public void validateMaxRows(long count) throws RequestValidationException {
        validateMaxRows(count, null);
    }

    @Override
    public void validateMaxRows(long count, Supplier<Long> countMethod) throws RequestValidationException {
        if (count > getMaxNumberRowsToExport()) {
            Long all = countMethod == null ? count : countMethod.get();
            String message = String.format("The number of elements to export [%s] exceeds the maximum allowed [%s]."
                    , all, getMaxNumberRowsToExport());
            throw new RequestValidationException(message);
        }
    }

    protected List<Field> getExportedFields(List<?> list, Class theClass, List<String> excludedColumns) {
        Class<?> clazz;
        if (CollectionUtils.isNotEmpty(list)) {
            clazz = list.get(0).getClass();
        } else if (theClass != null) {
            clazz = theClass;
        } else {
            return new ArrayList<>();
        }

        final List<String> excludedCols = excludedColumns == null ? new ArrayList<>() : excludedColumns;
        List<Field> fields = getAllFields(clazz);
        List<Field> activeFields = fields.stream()
                .filter(field -> !excludedCols.contains(field.getName()))
                .collect(Collectors.toList());

        return activeFields;
    }

    public List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }

        return fields;
    }

    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        return getAllFields(fields, clazz);
    }

    protected void writeCSVRow(CSVWriter csvBuilder, List<String> values) {
        csvBuilder.writeNext(values.toArray(new String[0]), false);
    }

    protected void createCSVColumnHeader(CSVWriter csvBuilder, List<Field> fields,
                                         Map<String, String> customColumnNames) {
        if (customColumnNames == null) {
            customColumnNames = new HashMap<String, String>();
        }
        List<String> fieldValues = new ArrayList<>();
        for (Field field : fields) {
            String varName = field.getName();
            if (customColumnNames.get(varName.toUpperCase()) != null) {
                varName = customColumnNames.get(varName.toUpperCase());
            }
            fieldValues.add(DomibusStringUtil.uncamelcase(varName));
        }
        writeCSVRow(csvBuilder, fieldValues);
    }

    protected void createCSVContents(List<?> list, CSVWriter csvBuilder, List<Field> fields) {
        if (list == null) {
            return;
        }
        for (Object elem : list) {
            List<String> fieldValues = new ArrayList<>();
            for (Field field : fields) {
                // set that field to be accessible
                field.setAccessible(true);
                try {
                    String value = serializeFieldValue(field, elem);
                    fieldValues.add(value);
                } catch (IllegalAccessException e) {
                    LOG.error("Exception while writing on CSV ", e);
                    throw new CsvException(DomibusCoreErrorCode.DOM_001, "Exception while writing on CSV", e);
                }
            }
            writeCSVRow(csvBuilder, fieldValues);
        }
    }

    protected String serializeFieldValue(Field field, Object elem) throws IllegalAccessException {
        Object fieldValue = field.get(elem);
        if (fieldValue == null) {
            return StringUtils.EMPTY;
        }
        if (fieldValue instanceof Map) {
            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
            return gson.toJson(fieldValue);
        }
        if (fieldValue instanceof Date) {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss'GMT'Z");
            ZonedDateTime d = ZonedDateTime.ofInstant(((Date) fieldValue).toInstant(), ZoneId.systemDefault());
            return d.format(f);
        }
        return Objects.toString(fieldValue, StringUtils.EMPTY);
    }

}
