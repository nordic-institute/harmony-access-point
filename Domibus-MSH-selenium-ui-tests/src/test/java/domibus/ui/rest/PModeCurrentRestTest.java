package domibus.ui.rest;

import com.sun.jersey.api.client.ClientResponse;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PModeCurrentRestTest extends RestTest {
	
	@Test
	public void uploadValidFileAndComment() throws Exception {
		SoftAssert soft = new SoftAssert();
		String uploadPath = "rest_pmodes/pmode-blue.xml";
		
		
		for (String domain : domains) {
			rest.pmode().uploadPMode(uploadPath, domain);
			int pmodeId = rest.pmode().getLatestPModeID(domain);
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
	public void downloadInvalidStrings(String evilStr) throws Exception {
		SoftAssert soft = new SoftAssert();
		ClientResponse response = rest.pmode().downloadPmode(null, evilStr);
		validateInvalidResponse(response, soft);
		soft.assertAll();
	}
	
	@Test(dataProvider = "readInvalidStrings")
	public void uploadFileInvalidComments(String evilStr) throws Exception {
		SoftAssert soft = new SoftAssert();
		ClientResponse response = rest.pmode().uploadPMode("rest_pmodes/pmode-blue.xml", evilStr, null);
		validateInvalidResponse(response, soft);
		soft.assertAll();
	}
	
	
}