package domibus.ui.functional;

import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.TestServicePage;

import java.util.List;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class TestServicePgTest extends SeleniumTest {
	
	/* TS-1 - Login as super admin and open Connections Monitoring page */
	@Test(description = "TS-1", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void openWindow() throws Exception {
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.CONNECTION_MONITORING);
		TestServicePage page = new TestServicePage(driver);
		
		log.info("checking page ..");
		soft.assertTrue(page.isLoaded(), "Page shows all desired elements");
		
		if (!rest.pmode().isPmodeUploaded(null)) {
			log.info("checking error message when no pmode is uploaded");
			soft.assertTrue(page.invalidConfigurationState(), "Page shows invalid configuration state");
		}
		soft.assertAll();
	}
	
	/*	TS-2 - User checks available parties in the Party ID	*/
	@Test(description = "TS-2", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void availableParties() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("uploading PMode");
		rest.pmode().uploadPMode("pmodes/pmode-blue.xml", null);
		
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.CONNECTION_MONITORING);
		TestServicePage page = new TestServicePage(driver);
		
		soft.assertTrue(page.isLoaded(), "Page shows all desired elements");
		
		List<String> options = page.getPartySelector().getOptionsTexts();
		log.info("checking available parties");
		soft.assertTrue(options.contains("domibus-blue") && options.contains("domibus-red"), "Party selector shows the correct parties");
		
		soft.assertAll();
	}
	
	/*	TS-3 - User picks a party and pushes Test button	*/
	@Test(description = "TS-3", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void testBlueParty() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("uploading pmode");
		rest.pmode().uploadPMode("pmodes/pmode-blue.xml", null);
		
		login(data.getAdminUser()).getSidebar().goToPage(PAGES.CONNECTION_MONITORING);
		TestServicePage page = new TestServicePage(driver);
		
		log.info("selecting blue party");
		page.getPartySelector().selectOptionByText("domibus-blue");
		
		soft.assertTrue(page.getTestBtn().isEnabled(), "Test button is enabled after picking a party to test");
		log.info("clicking test");
		page.getTestBtn().click();
		page.waitForEchoRequestData();
		soft.assertTrue(page.getUpdateBtn().isEnabled(), "Update button is enabled after test button is clicked");
		
		log.info("checking echo message properties");
		soft.assertTrue(StringUtils.equalsIgnoreCase(page.getToParty().getText(), "domibus-blue"), "Correct party is listed");
		soft.assertTrue(!page.getToAccessPoint().getText().isEmpty(), "To access point contains data");
		soft.assertTrue(!page.getTimeSent().getText().isEmpty(), "Time sent contains data");
		soft.assertTrue(!page.getToMessage().getText().isEmpty(), "To Message id contains data");
		
		soft.assertAll();
	}
	
	
}
