package domibus.ui.functional;

import org.testng.Reporter;
import com.bluecatcode.junit.shaded.org.apache.commons.lang3.StringUtils;
import ddsl.dcomponents.DomibusPage;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.pmode.current.PModeCurrentPage;
import utils.Gen;
import utils.PModeXMLUtils;

public class PModeValidationsTest extends SeleniumTest {


	/* EDELIVERY-7292 - PMC-14 - PMode validations -  are considered invalid in any value */
	@Test(description = "PMC-14", groups = {"multiTenancy", "singleTenancy"})
	public void invalidCharacters() throws Exception {
		SoftAssert soft = new SoftAssert();

		if (!rest.pmode().isPmodeUploaded(null)) {
			Reporter.log("uploading pmode to modify");
			log.info("uploading pmode to modify");
			rest.pmode().uploadPMode("pmodes/pmode-blue.xml", null);
		}

		String currentPmode = rest.pmode().getCurrentPmode(null);

		PModeXMLUtils xmlutils = new PModeXMLUtils(currentPmode);
		String currentParty = xmlutils.getCurrentPartyName();

		String newPmode = currentPmode.replaceAll(currentParty, currentParty + "<>");
		PModeCurrentPage page = new PModeCurrentPage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_CURRENT);

		page.modifyListedPmode(newPmode);

		soft.assertTrue(page.getAlertArea().isError(), "Page shows error");

		newPmode = currentPmode.replaceAll("name=\"", "name=\"<>");
		page.modifyListedPmode(newPmode);

		soft.assertTrue(page.getAlertArea().isError(), "Page shows error");

		soft.assertAll();
	}


	/* EDELIVERY-7293 - PMC-15 - PMode validations - all listed URLs are valid */
	@Test(description = "PMC-15", groups = {"multiTenancy", "singleTenancy"})
	public void invalidURLs() throws Exception {
		SoftAssert soft = new SoftAssert();

		if (!rest.pmode().isPmodeUploaded(null)) {
			Reporter.log("uploading pmode to modify");
			log.info("uploading pmode to modify");
			rest.pmode().uploadPMode("pmodes/pmode-blue.xml", null);
		}

		String currentPmode = rest.pmode().getCurrentPmode(null);

		PModeCurrentPage page = new PModeCurrentPage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_CURRENT);

		String newPmode = currentPmode.replaceAll("http://", Gen.rndStr(5));
		page.modifyListedPmode(newPmode);

		soft.assertTrue(page.getAlertArea().isError(), "Page shows error");

		soft.assertAll();
	}

	/* EDELIVERY-7294 - PMC-16 - PMode validations - attributes with integer values are validated as integers */
	@Test(description = "PMC-16", groups = {"multiTenancy", "singleTenancy"})
	public void integerAttributesValidations() throws Exception {
		SoftAssert soft = new SoftAssert();

		if (!rest.pmode().isPmodeUploaded(null)) {
			Reporter.log("uploading pmode to modify");
			log.info("uploading pmode to modify");
			rest.pmode().uploadPMode("pmodes/pmode-blue.xml", null);
		}

		String currentPmode = rest.pmode().getCurrentPmode(null);

		String newPmode = currentPmode.replaceAll("\\d+", Gen.rndStr(5));

		PModeCurrentPage page = new PModeCurrentPage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_CURRENT);

		page.modifyListedPmode(newPmode);

		soft.assertTrue(page.getAlertArea().isError(), "Page shows error");

		soft.assertAll();
	}

	/* EDELIVERY-7288 - PMC-12 - PMode validations - boolean attributes dont accept other values */
	@Test(description = "PMC-12", groups = {"multiTenancy", "singleTenancy"})
	public void validationsBooleans() throws Exception {
		SoftAssert soft = new SoftAssert();

		String currentPmode = rest.pmode().getCurrentPmode(null);
		String newPmode = currentPmode.replaceAll("true", Gen.rndStr(5)).replaceAll("false", Gen.rndStr(5));

		PModeCurrentPage page = new PModeCurrentPage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_CURRENT);

		page.modifyListedPmode(newPmode);

		soft.assertTrue(page.getAlertArea().isError(), "Error message shown");
		soft.assertAll();
	}

	/* EDELIVERY-7291 - PMC-13 - PMode validations - party describing current system must be present */
	@Test(description = "PMC-13", groups = {"multiTenancy", "singleTenancy"})
	public void currentPmodeNoCurrentParty() throws Exception {
		SoftAssert soft = new SoftAssert();

		if (!rest.pmode().isPmodeUploaded(null)) {
			Reporter.log("uploading pmode to modify");
			log.info("uploading pmode to modify");
			rest.pmode().uploadPMode("pmodes/pmode-blue.xml", null);
		}

		String currentPmode = rest.pmode().getCurrentPmode(null);

		PModeXMLUtils xmlutils = new PModeXMLUtils(currentPmode);
		String currentParty = xmlutils.getCurrentPartyName();
		xmlutils.removeParty(currentParty);

		String newPmode = xmlutils.printDoc();

		PModeCurrentPage page = new PModeCurrentPage(driver);
		page.getSidebar().goToPage(PAGES.PMODE_CURRENT);

		page.modifyListedPmode(newPmode);

		soft.assertTrue(page.getAlertArea().isError(), "Error message shown");
		soft.assertAll();
	}


}
