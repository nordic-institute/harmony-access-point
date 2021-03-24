package domibus.ui.functional;

import ddsl.dobjects.DWait;
import ddsl.enums.DMessages;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
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
		rest.pmode().uploadPMode("pmodes/selfSending8080.xml", page.getDomainFromTitle());

		page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);

		int size = page.grid().getPagination().getTotalItems();
		for( int i=0;i<size;i++){

			log.info("name"+page.connectionStatusIcons.get(i).getText());
			if(page.connectionStatusIcons.get(i).getText().equals("indeterminate_check_box")){
				String partyName = page.grid().getRowSpecificColumnVal(i, "Party");
				log.info("party"+partyName);

				page.grid().getActionButton("Details").get(i).click();
				TestMessDetailsModal modalTest = new TestMessDetailsModal(driver);

				soft.assertTrue(modalTest.getTestbutton().isEnabled(), "Test button is enabled");
				soft.assertFalse(modalTest.getUpdateBtn().isEnabled(), "Update button is disabled");
				String expectedError =
						String.format("Error retrieving Last Sent Test Message for %s [DOM_001]:No User message found for party [%s]", partyName, partyName);
				soft.assertEquals(expectedError, page.getAlertArea().getAlertMessage(), "Correct error message is shown");
				log.info("test" + modalTest.getSentMessInfo().get("party"));

			    soft.assertTrue(modalTest.isMessInfoPresent("send"),"Sent Message info field have no data");

				soft.assertTrue(modalTest.isMessInfoPresent("receive"),"Received message info fields have no data");

				soft.assertTrue(modalTest.getCloseBtn().isEnabled(), "Enabled close button is shown");
				modalTest.getCloseBtn().click();
				page.grid().waitForRowsToLoad();
			}


		}

		soft.assertAll();
	}
	/* CM-4 - Open details view for party that has never been tested or monitored and push Test button */
	@Test(description = "CM-4", groups = {"multiTenancy", "singleTenancy"})
	public void sendTestMsg() throws Exception {
		SoftAssert soft = new SoftAssert();
		ConnectionMonitoringPage page = new ConnectionMonitoringPage(driver);

		page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);

		int size = page.grid().getPagination().getTotalItems();

		for( int i=0;i<size;i++){
			if(page.connectionStatusIcons.get(i).getText().equals("indeterminate_check_box")
			|| page.connectionStatusIcons.get(i).getText().equals("error")) {
				String partyName = page.grid().getRowSpecificColumnVal(i, "Party");
				log.info("party"+partyName);
				TestMessDetailsModal modalTest = new TestMessDetailsModal(driver);
				page.grid().getActionButton("Details").get(i).click();
				page.getAlertArea().closeButton.click();
				modalTest.getTestbutton().click();
				new DWait(driver).forXMillis(100);

				soft.assertFalse(modalTest.isMessInfoPresent("send"),"Sent message Info fields have data present");

				if(page.getAlertArea().isError()) {
					soft.assertTrue(modalTest.isMessInfoPresent("receive"), "Received info fields are blank");
				}
				else{
					soft.assertFalse(modalTest.isMessInfoPresent("receive"), "Received info fields are not blank");
				}
				modalTest.getCloseBtn().click();
				break;

			}
			else
			{
				throw new SkipException("No party available for error scenarios");
			}
		}
		soft.assertAll();
	}
	/* CM-5 - Open details view for party and push Update button */
	@Test(description = "CM-5", groups = {"multiTenancy", "singleTenancy"})
	public void checkUpdateFeature() throws Exception {
		SoftAssert soft = new SoftAssert();
		ConnectionMonitoringPage page = new ConnectionMonitoringPage(driver);

		page.getSidebar().goToPage(PAGES.CONNECTION_MONITORING);

		int noOfParties = rest.connMonitor().getConnectionMonitoringParties(page.getDomainFromTitle()).length();

		if(noOfParties>0) {

			TestMessDetailsModal modalTest = new TestMessDetailsModal(driver);
			page.grid().getActionButton("Details").get(0).click();
			modalTest.getTestbutton().click();
			modalTest.getUpdateBtn().click();
			if(page.getAlertArea().isShown()){
				soft.assertTrue(modalTest.isMessInfoPresent("receive"), "Response is not received for test message");
			}
			else {
				soft.assertFalse(modalTest.isMessInfoPresent("receive"), "Test Message is sent successfully");
			}
			modalTest.getCloseBtn().click();
		}

	}



	
}