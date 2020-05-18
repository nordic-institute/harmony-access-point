package domibus.ui.rest;

import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.testng.collections.Lists;
import rest.RestServicePaths;
import utils.Generator;
import utils.TestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class PModeCurrentRestTest extends RestTest {

	@Test
	public void uploadValidFileAndComment() throws Exception{
		SoftAssert soft = new SoftAssert();
		String uploadPath = "rest_pmodes/pmode-blue.xml";


		for (String domain : domains) {
			rest.pmode().uploadPMode(uploadPath,domain);
			int pmodeId= rest.pmode().getLatestPModeID(domain);
			String dPmodePath = rest.pmode().downloadPmode(domain, pmodeId);

			String downloadedContent = new String(Files.readAllBytes(Paths.get(dPmodePath)));

			File file = new File(this.getClass().getClassLoader().getResource(uploadPath).getFile());
			String uploadedContent = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));

			soft.assertEquals(downloadedContent.toLowerCase(), uploadedContent.toLowerCase(),
					"Pmode was updated properly");
		}

		soft.assertAll();
	}

	@Test(dataProvider = "readInvalidStrings")
	public void downloadInvalidStrings(String evilStr) throws Exception{
		SoftAssert soft = new SoftAssert();
		ClientResponse response = rest.pmode().downloadPmode(null, evilStr);
		validateInvalidResponse(response, soft, 400);
		soft.assertAll();
	}

	@Test(dataProvider = "readInvalidStrings")
	public void uploadFileInvalidComments(String evilStr) throws Exception{
		SoftAssert soft = new SoftAssert();
		ClientResponse response = rest.pmode().uploadPMode("rest_pmodes/pmode-blue.xml", evilStr, null);
		validateInvalidResponse(response, soft, 400);
		soft.assertAll();
	}




}