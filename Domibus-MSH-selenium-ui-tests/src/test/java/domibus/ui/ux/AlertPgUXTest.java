package domibus.ui.ux;

import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.Alert.AlertPage;
import utils.TestUtils;

public class AlertPgUXTest extends SeleniumTest {

	JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.ALERTS);


	// EDELIVERY-7154 - ALRT-42 - Modify no of visible rows
    /* EDELIVERY-7154 - ALRT-42 - Modify no of visible rows */
	@Test(description = "ALRT-42", groups = {"multiTenancy", "singleTenancy"})
	public void modifyVisibleColumns() throws Exception {
		SoftAssert soft = new SoftAssert();

		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);

		page.grid().waitForRowsToLoad();
		page.grid().checkModifyVisibleColumns(soft);


		soft.assertAll();

	}

	// EDELIVERY-7154 - ALRT-42 - Modify no of visible rows
    /* EDELIVERY-7154 - ALRT-42 - Modify no of visible rows */
	@Test(description = "ALRT-42", groups = {"multiTenancy", "singleTenancy"})
	public void modifyVisibleRows() throws Exception {
		SoftAssert soft = new SoftAssert();

		AlertPage page = new AlertPage(driver);
		page.getSidebar().goToPage(PAGES.ALERTS);

		page.grid().waitForRowsToLoad();
		page.grid().checkChangeNumberOfRows(soft);


		soft.assertAll();

	}


}
