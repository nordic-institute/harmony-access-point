package utils;

import ddsl.dcomponents.DComponent;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DObject;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.asserts.SoftAssert;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class TestUtils {

	protected static Logger log = LoggerFactory.getLogger("thename");

	/* Checks if List provided is sorted*/
	public static boolean isStringListSorted(List<String> strings, Order order) {
		if (order.equals(Order.DESC)) {
			Comparator c = Collections.reverseOrder();
			return strings.stream().sorted(c).collect(Collectors.toList()).equals(strings);
		}
		return strings.stream().sorted().collect(Collectors.toList()).equals(strings);
	}

	/* Checks if List provided is sorted asc*/
	public static boolean isIntegerListSorted(List<Integer> strings, Order order) {
		if (order.equals(Order.DESC)) {
			Comparator c = Collections.reverseOrder();
			return strings.stream().sorted(c).collect(Collectors.toList()).equals(strings);
		}
		return strings.stream().sorted().collect(Collectors.toList()).equals(strings);
	}

	/* Checks if List provided is sorted desc*/
	public static boolean isDateListSorted(List<Date> dates, Order order) {
		if (order.equals(Order.DESC)) {
			Comparator c = Collections.reverseOrder();
			return dates.stream().sorted(c).collect(Collectors.toList()).equals(dates);
		}
		return dates.stream().sorted().collect(Collectors.toList()).equals(dates);
	}

	public static boolean areListsEqual(List<Object> flist, List<Object> slist) {
		if (flist.size() != slist.size()) {
			return false;
		}
		flist.removeAll(slist);
		return flist.size() == 0;
	}

	public static <T extends DComponent> void basicFilterPresence(SoftAssert soft, T filtersArea, JSONArray filtersDescription) throws Exception {

		Field[] fields = filtersArea.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (!field.getType().toString().contains("WebElement")) {
				continue;
			}

			for (int i = 0; i < filtersDescription.length(); i++) {
				JSONObject currentNode = filtersDescription.getJSONObject(i);
				if (StringUtils.equalsIgnoreCase(currentNode.getString("name"), field.getName())) {

					WebElement element = (WebElement) field.get(filtersArea);
					DObject object = new DObject(filtersArea.getDriver(), element);

					soft.assertEquals(object.isPresent(), currentNode.getBoolean("isDefault"),
							String.format("Filter %s isPresent = %s as expected", field.getName(), currentNode.getBoolean("isDefault")));
					if (currentNode.getBoolean("isDefault")) {
						log.info(object.getAttribute("placeholder"));
						soft.assertEquals(object.getAttribute("placeholder"), currentNode.getString("placeholder"), "Placeholder text is correct - " + currentNode.getString("placeholder"));
					}
					continue;
				}
			}

		}
	}

	public static <T extends DComponent> void advancedFilterPresence(SoftAssert soft, T filtersArea, JSONArray filtersDescription) throws Exception {

		Field[] fields = filtersArea.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (!field.getType().toString().contains("WebElement")) {
				continue;
			}

			for (int i = 0; i < filtersDescription.length(); i++) {
				JSONObject currentNode = filtersDescription.getJSONObject(i);
				if (StringUtils.equalsIgnoreCase(currentNode.getString("name"), field.getName())) {

					WebElement element = (WebElement) field.get(filtersArea);
					DObject object = new DObject(filtersArea.getDriver(), element);

					soft.assertEquals(object.isPresent(), true,
							String.format("Filter %s isPresent as expected", field.getName()));

					log.info(object.getAttribute("placeholder"));
					soft.assertEquals(object.getAttribute("placeholder"), currentNode.getString("placeholder"), "Placeholder text is correct - " + currentNode.getString("placeholder"));

					continue;
				}
			}
		}
	}

	public static <T extends DGrid> void testSortingForColumn(SoftAssert soft, T grid, JSONObject colDesc) throws Exception {
		String columnName = colDesc.getString("name");
		List<String> columns = grid.getColumnNames();
		if (!columns.contains(columnName)) {
			log.info(String.format("Column %s is not visible, sort testing for it is skipped", columnName));
			return;
		}
		if (!colDesc.getBoolean("sortable")) {
			log.info(String.format("Column %s is not sortable, sort testing for it is skipped", columnName));
			return;
		}
		if (!StringUtils.equalsIgnoreCase(grid.getSortedColumnName(), columnName)) {
			grid.sortBy(columnName);
			Order order = grid.getSortOrder();
			checkSortOrder(soft, columnName, colDesc.getString("type"), order, grid.getValuesOnColumn(columnName));
		}
	}

	private static void checkSortOrder(SoftAssert soft, String columnName, String type, Order order, List<String> values) throws Exception {
		log.info("Checking sort for " + columnName);
		if (StringUtils.equalsIgnoreCase(type, "text")) {
			soft.assertTrue(isStringListSorted(values, order), "Text sorting for column " + columnName);
		} else if (StringUtils.equalsIgnoreCase(type, "datetime")) {
			soft.assertTrue(isDateListSorted(listStringToDate(values), order), "Date sorting for column " + columnName);
		} else if (StringUtils.equalsIgnoreCase(type, "integer")) {
			soft.assertTrue(isIntegerListSorted(listStringToInt(values), order), "Integer sorting for column " + columnName);
		} else {
			throw new Exception("Unknown data type, cannot check sort for " + columnName);
		}
	}


	private static List<Integer> listStringToInt(List<String> list) {
		List<Integer> toReturn = new ArrayList<>();

		for (int i = 0; i < list.size(); i++) {
			toReturn.add(Integer.valueOf(list.get(i).trim()));
		}
		return toReturn;
	}

	private static List<Date> listStringToDate(List<String> list) throws ParseException {
		List<Date> toReturn = new ArrayList<>();

		for (int i = 0; i < list.size(); i++) {
			toReturn.add(TestRunData.UI_DATE_FORMAT.parse(list.get(i)));
		}
		return toReturn;
	}


}
