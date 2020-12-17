package utils;

import ddsl.dcomponents.grid.DGrid;
import ddsl.enums.PAGES;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.asserts.SoftAssert;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class TestUtils {
	
	protected static Logger log = LoggerFactory.getLogger("TestUtils");
	
	/* Checks if List provided is sorted*/
	public static boolean isStringListSorted(List<String> strings, Order order) {
		Comparator c = (a, b) -> StringUtils.compareIgnoreCase((String) a, (String) b);
		if (order.equals(Order.DESC)) {
			c = c.reversed();
		}
		List<String> sorted = (List<String>) strings.stream().sorted(c).collect(Collectors.toList());
		return ListUtils.isEqualList(strings, sorted);
	}
	
	/* Checks if List provided is sorted asc*/
	public static boolean isIntegerListSorted(List<Integer> integers, Order order) {
		List<Integer> sorted = new ArrayList<>();
		if (order.equals(Order.DESC)) {
			Comparator c = Collections.reverseOrder();
			sorted = (List<Integer>) integers.stream().sorted(c).collect(Collectors.toList());
		} else {
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
		} else {
			sortedDates = (List<Date>) dates.stream().sorted().collect(Collectors.toList());
		}
		return ListUtils.isEqualList(dates, sortedDates);
	}
	
	public static <T extends DGrid> void testSortingForColumn(SoftAssert soft, T grid, JSONObject colDesc) throws Exception {
		log.info("test sorting for " + colDesc.getString("name"));
		
		String columnName = colDesc.getString("name");
		List<String> columns = grid.getColumnNames();
		if (!columns.contains(columnName)) {
			log.debug(String.format("Column %s is not visible, sort testing for it is skipped", columnName));
			return;
		}
		if (!colDesc.getBoolean("sortable")) {
			log.debug(String.format("Column %s is not sortable, sort testing for it is skipped", columnName));
			return;
		}
		if (!StringUtils.equalsIgnoreCase(grid.getSortedColumnName(), columnName)) {
			grid.sortBy(columnName);
			Order order = grid.getSortOrder();
			checkSortOrder(soft, columnName, colDesc.getString("type"), order, grid.getListedValuesOnColumn(columnName));
		}
	}
	
	public static void checkSortOrder(SoftAssert soft, String columnName, String type, Order order, List<String> values) throws Exception {
		log.debug("Checking sort for " + columnName);
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
			if (StringUtils.isNotEmpty(list.get(i).trim())) {
				toReturn.add(Integer.valueOf(list.get(i).trim()));
			}
		}
		return toReturn;
	}
	
	private static List<Date> listStringToDate(List<String> list) throws ParseException {
		List<Date> toReturn = new ArrayList<>();
		
		for (int i = 0; i < list.size(); i++) {
			if (StringUtils.isNotEmpty(list.get(i))) {
				toReturn.add(TestRunData.UI_DATE_FORMAT.parse(list.get(i)));
			}
		}
		return toReturn;
	}
	
	public static boolean areMapsEqual(HashMap<String, String> map1, HashMap<String, String> map2) {
		if (!ListUtils.isEqualList(map1.keySet(), map2.keySet())) {
			return false;
		}
		if (!ListUtils.isEqualList(map1.values(), map2.values())) {
			return false;
		}
		
		for (String key : map1.keySet()) {
			if (!StringUtils.equalsIgnoreCase(map1.get(key), map2.get(key))) {
				return false;
			}
		}
		
		return true;
	}
	
	public static JSONObject getPageDescriptorObject(PAGES page) {
		String suffix = "PageDescriptor.json";
		String prefix = "pageDescriptors/";
		String pageName = page.toString().replaceAll("_", "").toLowerCase();
		String fileName = prefix + pageName + suffix;
		
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
	
	public static String getNonDefaultColumn(JSONArray columns) throws JSONException {
		for (int i = 0; i < columns.length(); i++) {
			JSONObject col = columns.getJSONObject(i);
			if (!col.getBoolean("visibleByDefault")) {
				return col.getString("name");
			}
		}
		return StringUtils.EMPTY;
	}
	
	public static boolean isEqualListContent(List<String> l1, List<String> l2) {
		Collections.sort(l1);
		Collections.sort(l2);
		return ListUtils.isEqualList(l1, l2);
	}
	
	
	public static HashMap<String, String> unzip(String zipFilePath) throws Exception {
		
		String destDir = zipFilePath.replaceAll(".zip", "");
		
		HashMap<String, String> zipContent = new HashMap<>();
		
		File dir = new File(destDir);
		// create output directory if it doesn't exist
		if (!dir.exists()) dir.mkdirs();
		
		
		FileInputStream fis;
		//buffer for read and write data to file
		byte[] buffer = new byte[1024];
		
		fis = new FileInputStream(zipFilePath);
		ZipInputStream zis = new ZipInputStream(fis);
		ZipEntry ze = zis.getNextEntry();
		while (ze != null) {
			
			String fileName = ze.getName();
			File newFile = new File(destDir + File.separator + fileName);
			
			log.info("Unzipping to " + newFile.getAbsolutePath());
			//create directories for sub directories in zip
			new File(newFile.getParent()).mkdirs();
			FileOutputStream fos = new FileOutputStream(newFile);
			int len;
			while ((len = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
			fos.close();
			//close this ZipEntry
			zis.closeEntry();
			
			String fileContent = new String(Files.readAllBytes(Paths.get(newFile.getAbsolutePath())));
			zipContent.put(fileName, fileContent);
			
			ze = zis.getNextEntry();
		}
		
		//close last ZipEntry
		zis.closeEntry();
		zis.close();
		fis.close();
		
		return zipContent;
	}
	
//	public static String getValueFromXMLString(String xmlString, String key) {
//		String start = key + ">";
//		String end = "<\\/eb:" + key;
//
//		Pattern p = Pattern.compile(start + "(.*?)" + end);
//		Matcher m = p.matcher(xmlString);
//		m.find();
//		return m.group(1);
//	}
	
	public static String getValueFromXMLString(String xmlString, String key) throws Exception {
		log.debug("Extracting " + key + " from " + xmlString);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(new InputSource(new StringReader(xmlString)));
		
		String value = StringUtils.EMPTY;
		try {
			value = doc.getElementsByTagName("eb:" + key).item(0).getTextContent();
		} catch (Exception e) {}
		log.info("Extracted value " + value);
		return value;
	}
	
	public static String jmsDateStrFromTimestamp(Long timestamp) {
		Date date = new Date();
		date.setTime(timestamp);
		
		TestRunData.REST_JMS_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
		String dateStr = TestRunData.REST_JMS_DATE_FORMAT.format(date);
		return dateStr;
	}
	
}
