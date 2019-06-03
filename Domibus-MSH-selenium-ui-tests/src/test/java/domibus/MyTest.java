package domibus;

import ddsl.enums.DOMIBUS_PAGES;
import domibus.ui.BaseTest;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.jms.JMSMonitoringPage;
import pages.jms.JMSMoveMessageModal;
import rest.DomibusRestClient;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * @author Catalin Comanici
 * @description:
 * @since 4.1
 */
public class MyTest { //extends BaseTest {

	@Test
	public void testProp() throws Exception {
		DomibusRestClient rest = new DomibusRestClient();
		Reader reader = Files.newBufferedReader(Paths.get("C:\\Users\\User\\Downloads\\pluginusers_datatable_2019-06-03 13-17-05.csv"));
		CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

		for (CSVRecord record : csvParser.getRecords()) {
			System.out.println("record.get(0) = " + record.get(0));
			rest.deletePluginUser(record.get(0), null);
		}


//

////		SoftAssert soft = new SoftAssert();
//		login(data.getAdminUser()).getSidebar().gGoToPage(DOMIBUS_PAGES.JMS_MONITORING);
//		JMSMonitoringPage page = new JMSMonitoringPage(driver);
//
//		int noOfMessages = page.filters().selectQueueWithMessages();
//		if(noOfMessages>0) {
//			HashMap<String, String> rowInfo = page.grid().getRowInfo(0);
//			page.grid().selectRow(0);
//			page.getMoveButton().click();
//
//			JMSMoveMessageModal modal = new JMSMoveMessageModal(driver);
//			modal.getQueueSelect().selectOptionByText("");
//			modal.clickCancel();
//		}
//
////		soft.assertAll();
	}




}
