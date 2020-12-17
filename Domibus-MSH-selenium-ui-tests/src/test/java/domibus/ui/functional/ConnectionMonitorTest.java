package domibus.ui.functional;

import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.lang3.StringUtils;
import org.testng.SkipException;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.connectionMon.ConnectionMonitoringPage;
import pages.connectionMon.TestMessDetailsModal;

import java.util.HashMap;
import java.util.List;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class ConnectionMonitorTest extends SeleniumTest {
	
	/* CM-2 - Login as super admin and open Connections Monitoring page */
	@Test(description = "CM-2", groups = {"multiTenancy", "singleTenancy"})
	public void openWindow() throws Exception {
		SoftAssert soft = new SoftAssert();

		ConnectionMonitoringPage page = new ConnectionMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);
		
		if (!rest.pmode().isPmodeUploaded(null)) {
			log.info("checking error message when no pmode is uploaded");
			soft.assertTrue(page.invalidConfigurationState(), "Page shows invalid configuration state");
		}
		
		log.info("checking page ..");
		soft.assertTrue(page.isLoaded(), "Page shows all desired elements");
		soft.assertAll();
	}

	/* CM-1 - Login as system admin and open Connection Monitoring page without proper Pmode */
	@Test(description = "CM-1", groups = {"multiTenancy", "singleTenancy"})
	public void openWindowNoPmode() throws Exception {
		SoftAssert soft = new SoftAssert();

		ConnectionMonitoringPage page = new ConnectionMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);

		if (!rest.pmode().isPmodeUploaded(null)) {
			log.info("checking error message when no pmode is uploaded");
			soft.assertTrue(page.invalidConfigurationState(), "Page shows invalid configuration state");
		}else {
			throw new SkipException("Pmode already uploaded, test could not be executed");
		}
		soft.assertAll();
	}

	/*	TS-2 - User checks available parties in the Party ID	*/
	@Test(description = "TS-2", groups = {"multiTenancy", "singleTenancy"})
	public void availableParties() throws Exception {
		SoftAssert soft = new SoftAssert();
		log.info("uploading PMode");
		rest.pmode().uploadPMode("pmodes/pmode-blue.xml", null);
		
		ConnectionMonitoringPage page = new ConnectionMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);

		page.refreshPage();
		
		soft.assertTrue(page.isLoaded(), "Page shows all desired elements");

		List<String> options = page.grid().getValuesOnColumn("Party");
		log.info("checking available parties");
		soft.assertTrue(options.contains("domibus-blue") && options.contains("domibus-red"), "Parties are present");

		soft.assertAll();
	}

	/*	TS-3 - User picks a party and pushes Test button	*/
	@Test(description = "TS-3", groups = {"multiTenancy", "singleTenancy"})
	public void testBlueParty() throws Exception {
		String blueParty = "domibus-blue";
		SoftAssert soft = new SoftAssert();
		log.info("uploading pmode");
		rest.pmode().uploadPMode("pmodes/pmode-blue.xml", null);

		ConnectionMonitoringPage page = new ConnectionMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);
		
		page.grid().sendTestMessage(blueParty).click();
		page.wait.forXMillis(500);
		String lastSent = page.grid().getLastSent(blueParty);
		
		soft.assertEquals(lastSent, "a few seconds ago", "correct message shown when test button is pressed");
		
		page.grid().openDetails(blueParty).click();
		TestMessDetailsModal modal = new TestMessDetailsModal(driver);
		
		HashMap<String, String> info = modal.getSentMessInfo();
		modal.getCloseBtn().click();
		
		soft.assertEquals(info.get("party"), blueParty, "correct party is pinged");
		soft.assertNotNull(info.get("id"), "message id is displayed");
		
		soft.assertAll();
	}
	
	
}
