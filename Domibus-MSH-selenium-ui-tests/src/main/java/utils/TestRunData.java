package utils;


import ddsl.enums.DRoles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class TestRunData {
	public static SimpleDateFormat UI_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
	public static SimpleDateFormat CSV_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss'GMT'Z");
	public static SimpleDateFormat CSV_DATE_FORMAT2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	public static SimpleDateFormat REST_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	public static SimpleDateFormat REST_JMS_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	public static SimpleDateFormat TESTSERVICE_DATE_FORMAT = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss");
	public static SimpleDateFormat DATEWIDGET_DATE_FORMAT = new SimpleDateFormat(" dd/MM/yyyy HH:mm");
	static Properties prop = new Properties();
	Logger log = LoggerFactory.getLogger("TestRunData");
	
	
	public TestRunData() {
		if (prop.isEmpty()) {
			loadTestData();
		}
	}
	
	private void loadTestData() {
		try {
//			String filename = System.getenv("propertiesFile");
			String filename = System.getProperty("propertiesFile");
			FileInputStream stream = new FileInputStream(new File(filename));
			prop.load(stream);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public HashMap<String, String> getUser(String role) {
		
		HashMap<String, String> toReturn = new HashMap<>();
		
		toReturn.put("username", prop.getProperty(role + ".username"));
		toReturn.put("pass", prop.getProperty(role + ".password"));
		
		return toReturn;
	}
	
	public String defaultPass() {
		return prop.getProperty("default.password");
	}
	
	public String getNewTestPass() {
		return prop.getProperty("new.password");
	}
	
	public HashMap<String, String> getAdminUser() {
		if (isMultiDomain()) {
			return getUser(DRoles.SUPER);
		}
		return getUser(DRoles.ADMIN);
	}
	
	public String getUiBaseUrl() {
		String url = prop.getProperty("UI_BASE_URL");
		log.debug(url);
		return url;
	}
	
	public Integer getTIMEOUT() {
		return Integer.valueOf(prop.getProperty("SHORT_TIMEOUT_SECONDS"));
	}
	
	public Integer getLongWait() {
		return Integer.valueOf(prop.getProperty("LONG_TIMEOUT_SECONDS"));
	}
	
	public String getReportsFolder() {
		return prop.getProperty("reports.folder");
	}
	
	public boolean isMultiDomain() {
		return Boolean.valueOf(prop.getProperty("isMultiDomain"));
	}
	
	public boolean isHeadless() {
		try {
			return Boolean.valueOf(prop.getProperty("headless"));
		} catch (Exception e) {
			log.debug("e = " + e);
			return false;
		}
	}
	
	public boolean useProxy() {
		try {
			return Boolean.valueOf(prop.getProperty("useProxy"));
		} catch (Exception e) {
			log.debug("e = " + e);
			return false;
		}
	}
	
	public String getProxyAddress() {
		return prop.getProperty("proxyAddress");
	}
	
	public String getChromeDriverPath() {
		return prop.getProperty("webdriver.chrome.driver");
	}
	
	public String getFirefoxDriverPath() {
		return prop.getProperty("webdriver.gecko.driver");
	}
	
	public String getEdgeDriverPath() {
		return prop.getProperty("webdriver.ie.driver");
	}
	
	public String getRunBrowser() {
//		return System.getenv("runBrowser");
		return System.getProperty("runBrowser");
	}
	
	public Integer getUiReplicationcronTime() {
		return Integer.valueOf(prop.getProperty("UIReplication_CronTime"));
	}
	
	public String fromUIToWidgetFormat(String uiDate) throws ParseException {
		Date date = UI_DATE_FORMAT.parse(uiDate);
		return DATEWIDGET_DATE_FORMAT.format(date);
	}
	
	public String downloadFolderPath() {
		return System.getProperty("user.dir") + File.separator + "downloadFiles";
	}
}
