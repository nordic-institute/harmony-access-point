package utils;

import ddsl.dcomponents.DComponent;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DObject;
import ddsl.enums.PAGES;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.asserts.SoftAssert;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class TestUtils {

	protected static Logger log = LoggerFactory.getLogger("TestUtils");

	/* Checks if List provided is sorted*/
	public static boolean isStringListSorted(List<String> strings, Order order) {
		List<String> sorted = new ArrayList<>();
		if (order.equals(Order.DESC)) {
			Comparator c = Collections.reverseOrder();
			sorted = (List<String>) strings.stream().sorted(c).collect(Collectors.toList());
		}else {
			sorted = (List<String>) strings.stream().sorted().collect(Collectors.toList());
		}
		return ListUtils.isEqualList(strings, sorted);
	}

	/* Checks if List provided is sorted asc*/
	public static boolean isIntegerListSorted(List<Integer> integers, Order order) {
		List<Integer> sorted = new ArrayList<>();
		if (order.equals(Order.DESC)) {
			Comparator c = Collections.reverseOrder();
			sorted = (List<Integer>) integers.stream().sorted(c).collect(Collectors.toList());
		}else {
			sorted = (List<Integer>) integers.stream().sorted().collect(Collectors.toList());
		}
		return ListUtils.isEqualList(integers, sorted);
	}

	/* Checks if List provided is sorted desc*/
	public static boolean isDateListSorted(List<Date> dates, Order order) {
		List<Date> sortedDates = new ArrayList<>();
		if (order.equals(Order.DESC)) {
			Comparator c = Collections.reverseOrder();
			sortedDates = (List<Date>) dates.stream().sorted(c).collect(Collectors.toList());
		}else {
			sortedDates = (List<Date>) dates.stream().sorted().collect(Collectors.toList());
		}
		return ListUtils.isEqualList(dates, sortedDates);
	}

//	public static boolean areListsEqual(List<Object> flist, List<Object> slist) {
//		if (flist.size() != slist.size()) {
//			return false;
//		}
//		flist.removeAll(slist);
//		return flist.size() == 0;
//	}

	public static <T extends DGrid> void testSortingForColumn(SoftAssert soft, T grid, JSONObject colDesc) throws Exception {
		System.out.println("testing " + colDesc.getString("name"));

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

	public static JSONObject getPageDescriptorObject(PAGES page) {
		String suffix = "PageDescriptor.json";
		String prefix =  "pageDescriptors/";
		String pageName = StringUtils.removeAll(page.toString(), "_").toLowerCase();
		String fileName = prefix+pageName+suffix;

		JSONObject jsonObject;
		try {
			InputStream fin = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName);
			String content = new Scanner(fin).useDelimiter("\\Z").next();
			jsonObject = new JSONObject(content);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return jsonObject;
	}


}
