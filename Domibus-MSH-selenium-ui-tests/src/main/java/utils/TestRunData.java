package utils;


import ddsl.enums.DRoles;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class TestRunData {
	static Properties prop = new Properties();


	public TestRunData() {
		if (prop.isEmpty()) {
			loadTestData();
		}
	}

	private void loadTestData() {
		try {
			String filename = System.getenv("propertiesFile");
			FileInputStream stream = new FileInputStream(new File( filename));
			prop.load(stream);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public HashMap<String, String> getUser(String role) {

		HashMap<String, String> toReturn = new HashMap<>();

		toReturn.put("username", prop.getProperty(role+".username"));
		toReturn.put("pass", prop.getProperty(role+".password"));

		return toReturn;
	}

	public String getDefaultTestPass() {
		return prop.getProperty("default.password");
	}


	public HashMap<String, String> getAdminUser() {
		if (isIsMultiDomain()) {
			return getUser(DRoles.SUPER);
		}
		return getUser(DRoles.ADMIN);
	}


	public String getUiBaseUrl() {
		return prop.getProperty("UI_BASE_URL");
	}

	public Integer getTIMEOUT() {
		return Integer.valueOf(prop.getProperty("SHORT_TIMEOUT"));
	}

	public Integer getLongWait() {
		return Integer.valueOf(prop.getProperty("LONG_TIMEOUT"));
	}

	public String getReportsFolder() {
		return prop.getProperty("reports.folder");
	}

	public boolean isIsMultiDomain() {
		return Boolean.valueOf(prop.getProperty("isMultiDomain"));
	}

	public String getChromeDriverPath() {
		return prop.getProperty("webdriver.chrome.driver");
	}

	public String getFirefoxDriverPath() {
		return prop.getProperty("webdriver.gecko.driver");
	}

	public String getIEDriverPath() {
		return prop.getProperty("webdriver.ie.driver");
	}

	public String getRunBrowser() {
		return System.getenv("runBrowser");
	}
}
