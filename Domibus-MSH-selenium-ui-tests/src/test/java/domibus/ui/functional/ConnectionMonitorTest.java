package domibus.ui.functional;

import ddsl.enums.DMessages;
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
	@Test(description = "CM-1", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
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
	/* CM-3 - Open details view for party that has never been tested or monitored */
	@Test(description = "CM-3", groups = {"multiTenancy", "singleTenancy"})
	public void partyNotTested() throws Exception {
		SoftAssert soft = new SoftAssert();

		ConnectionMonitoringPage page = new ConnectionMonitoringPage(driver);
		page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);

		int size = page.connectionStatusIcons.size();
		for (int i=0;i<size;i++){
			if(page.connectionStatusIcons.get(i).getText().equals("indeterminate_check_box")){
				String partyName =page.grid().getRowSpecificColumnVal(i,"Party");
				soft.assertTrue(page.grid().getLastReceived(partyName).equals("never"),"Never is shown as Last received");
				soft.assertTrue(page.grid().getLastSent(partyName).equals("never"),"Never is sown as last sent");
			    page.grid().getActionButton("Details").get(i).click();
				TestMessDetailsModal modalTest = new TestMessDetailsModal(driver);
				soft.assertTrue(modalTest.getTestbutton().isEnabled(),"Test button is enabled");
				soft.assertFalse(modalTest.getUpdateBtn().isEnabled(),"Update button is disabled");
				soft.assertTrue(page.getAlertArea().isError(),"Error message is shown");
				String expectedError =
				String.format("Error retrieving Last Sent Test Message for %s [DOM_001]:No User message found for party [%s]",partyName,partyName);
				soft.assertEquals(expectedError,page.getAlertArea().getAlertMessage(),"Correct error message is shown");
				soft.assertTrue(modalTest.getSentMessInfo().get("time").isEmpty(),"ToPartyId field is blank");
				soft.assertTrue(modalTest.getSentMessInfo().get("id").isEmpty(),"ToAccessPoint field is blank");
				soft.assertTrue(modalTest.getSentMessInfo().get("party").isEmpty(),"Time Sent field is blank");
				soft.assertTrue(modalTest.getSentMessInfo().get("url").isEmpty(),"Message Id field is blank");

				soft.assertTrue(modalTest.getRespMessInfo().get("time").isEmpty(),"FromPartyId field is blank");
				soft.assertTrue(modalTest.getRespMessInfo().get("id").isEmpty(),"FromAccessPoint field is blank");
				soft.assertTrue(modalTest.getRespMessInfo().get("party").isEmpty(),"Time Received field is blank");
				soft.assertTrue(modalTest.getRespMessInfo().get("url").isEmpty(),"Message Id field is blank");

			}
		}
		soft.assertAll();
	}



	
}