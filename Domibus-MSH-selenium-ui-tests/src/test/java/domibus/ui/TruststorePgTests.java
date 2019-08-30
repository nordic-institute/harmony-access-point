package domibus.ui;

import ddsl.enums.PAGES;
import domibus.BaseUXTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.truststore.TrustStorePage;
import pages.truststore.TruststoreModal;
import rest.RestServicePaths;

import java.util.Map;

/**
 * @author Catalin Comanici

 * @since 4.1
 */
public class TruststorePgTests extends BaseUXTest {

	private static String name = "Name";
	private static String subject = "Subject";
	private static String issuer = "Issuer";
	private static String validFrom = "Valid from";
	private static String validUntil = "Valid until";


	@Test(description = "TRST-1", groups = {"multiTenancy", "singleTenancy"})
	public void openTrustorePage() throws Exception {

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.TRUSTSTORE);

		TrustStorePage page = new TrustStorePage(driver);

		soft.assertTrue(page.grid().isPresent(), "Grid appears in the page");
		soft.assertTrue(page.grid().getRowsNo()>0, "Grid has at least one row");

		soft.assertAll();
	}

	@Test(description = "TRST-2", groups = {"multiTenancy", "singleTenancy"})
	public void doubleClickTruststoreEntry() throws Exception {

		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.TRUSTSTORE);

		TrustStorePage page = new TrustStorePage(driver);
		Map<String, String> entryInfo = page.grid().getRowInfo(0);

		page.grid().doubleClickRow(0);
		TruststoreModal modal = new TruststoreModal(driver);

		soft.assertEquals(entryInfo.get(name), modal.getNameInput().getText(), "Name is the same in grid and modal");
		soft.assertEquals(entryInfo.get(subject), modal.getSubjectInput().getText(), "Subject is the same in grid and modal");
		soft.assertEquals(entryInfo.get(issuer), modal.getIssuerInput().getText(), "Issuer is the same in grid and modal");
		soft.assertEquals(entryInfo.get(validFrom), modal.getValidFromInput().getText(), "Valid From date is the same in grid and modal");
		soft.assertEquals(entryInfo.get(validUntil), modal.getValidToInput().getText(), "Valid Until date is the same in grid and modal");

		soft.assertAll();
	}

	@Test(description = "TRST-3", groups = {"multiTenancy", "singleTenancy"})
	public void csvFileDownload() throws Exception{
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.TRUSTSTORE);
		TrustStorePage page = new TrustStorePage(driver);

		String fileName = rest.downloadGrid(RestServicePaths.TRUSTSTORE_CSV, null, null);

		page.grid().getGridCtrl().showCtrls();
		page.grid().getGridCtrl().getAllLnk().click();

		page.grid().getPagination().getPageSizeSelect().selectOptionByText("100");

		page.grid().checkCSVAgainstGridInfo(fileName, soft);

		soft.assertAll();
	}

	@Test(description = "TRST-4", groups = {"multiTenancy", "singleTenancy"})
	public void gridSelfAssert() throws Exception {
		SoftAssert soft = new SoftAssert();
		login(data.getAdminUser()).getSidebar().gGoToPage(PAGES.TRUSTSTORE);
		TrustStorePage page = new TrustStorePage(driver);

		page.grid().assertControls(soft);

		soft.assertAll();
	}

}
