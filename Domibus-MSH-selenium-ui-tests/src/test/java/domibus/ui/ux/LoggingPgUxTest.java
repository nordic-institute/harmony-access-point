package domibus.ui.ux;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.PAGES;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.Logging.LoggingPage;
import utils.BaseUXTest;

/**
 * @author Rupam
**/

public class LoggingPgUxTest extends BaseUXTest {

    @Test(description = "LOG-2", groups = {"multiTenancy", "singleTenancy"})
    public void verifyPageElement() throws Exception {
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        LoggingPage lPage = new LoggingPage(driver);
        log.info("Navigate to logging page");
        page.getSidebar().goToPage(PAGES.LOGGING);
        log.info("Verify presence of all required elements");
        soft.assertTrue(lPage.isLoaded(),"page is loaded successfully along with all required fields");
        soft.assertAll();

    }
    @Test(description = "LOG-3", groups = {"multiTenancy", "singleTenancy"})
    public void verifyPackageName() throws Exception {
        SoftAssert soft = new SoftAssert();
        DomibusPage page = new DomibusPage(driver);
        LoggingPage lPage = new LoggingPage(driver);
        log.info("Navigate to logging page");
        page.getSidebar().goToPage(PAGES.LOGGING);
        log.info("Verify presence of eu.domibus in package/class input field");
        soft.assertTrue(lPage.getPackageClassInputField().getText().equals("eu.domibus"),"default package name is correct");
        soft.assertAll();
    }

}
