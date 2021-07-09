package domibus.ui.functional;

import io.qameta.allure.*;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import com.sun.jersey.api.client.ClientResponse;
import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.popups.Dialog;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import jdk.nashorn.internal.objects.Global;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.ChangePassword.ChangePasswordPage;
import pages.properties.PropGrid;
import pages.properties.PropertiesPage;
import pages.users.UserModal;
import pages.users.UsersGrid;
import pages.users.UsersPage;
import utils.Gen;
import utils.TestUtils;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.*;

@Epic("")
@Feature("")
public class PropertiesPgTest extends SeleniumTest {

JSONObject descriptorObj = TestUtils.getPageDescriptorObject(PAGES.PROPERTIES);

String passExpirationDatePattern = "yyyy-MM-dd'T'HH:mm:ss.SSS";

private String modifyProperty(String propertyName, Boolean isDomain, String newPropValue) throws Exception {

Allure.step("going to properties page");
log.info("going to properties page");
PropertiesPage page = new PropertiesPage(driver);
page.getSidebar().goToPage(PAGES.PROPERTIES);

Allure.step("waiting for grid to load");
log.info("waiting for grid to load");
page.propGrid().waitForRowsToLoad();

Allure.step("filtering for property");
log.info("filtering for property");
page.filters().filterBy(propertyName, null, null, null, isDomain);

PropGrid grid = page.propGrid();
grid.waitForRowsToLoad();

Allure.step("setting property");
log.info("setting property");
String oldVal = (grid.getPropertyValue(propertyName));
grid.setPropertyValue(propertyName, newPropValue);
page.getAlertArea().waitForAlert();

return oldVal;
}

/*EDELIVERY-7302 - PROP-1 - Verify presence of Domibus Properties page*/
/*  PROP-1 - Verify presence of Domibus Properties page  */
@Description("PROP-1 - Verify presence of Domibus Properties page")
@Link(name = "EDELIVERY-7302", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7302")
@AllureId("PROP-1")
@Test(description = "PROP-1", groups = {"multiTenancy", "singleTenancy"})
public void pageAvailability() throws Exception {
SoftAssert soft = new SoftAssert();

DomibusPage page = new DomibusPage(driver);
Allure.step("checking if option is available for system admin");
log.info("checking if option is available for system admin");
soft.assertTrue(page.getSidebar().isLinkPresent(PAGES.PROPERTIES), data.getAdminUser().get("username") + "has the option to access properties");

if (data.isMultiDomain()) {
String username = rest.getUsername(null, DRoles.ADMIN, true, false, true);
login(username, data.defaultPass());
Allure.step("checking if option is available for role ADMIN");
log.info("checking if option is available for role ADMIN");
soft.assertTrue(page.getSidebar().isLinkPresent(PAGES.PROPERTIES), username + "has the option to access properties");
}

String userUsername = rest.getUsername(null, DRoles.USER, true, false, true);
login(userUsername, data.defaultPass());

Allure.step("checking if option is available for role USER");
log.info("checking if option is available for role USER");
soft.assertFalse(page.getSidebar().isLinkPresent(PAGES.PROPERTIES), userUsername + "has the option to access properties");

soft.assertAll();
}


/*EDELIVERY-7303 - PROP-2 - Open Properties page as Super admin */
/*  PROP-2 - Open Properties page as Super admin  */
@Description("PROP-2 - Open Properties page as Super admin")
@Link(name = "EDELIVERY-7303", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7303")
@AllureId("PROP-2")
@Test(description = "PROP-2", groups = {"multiTenancy"})
public void openPageSuper() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("going to properties page");
log.info("going to properties page");
PropertiesPage page = new PropertiesPage(driver);
page.getSidebar().goToPage(PAGES.PROPERTIES);

Allure.step("waiting for grid to load");
log.info("waiting for grid to load");
page.propGrid().waitForRowsToLoad();

basicFilterPresence(soft, page.filters(), descriptorObj.getJSONArray("filters"));
page.filters().expandArea();
advancedFilterPresence(soft, page.filters(), descriptorObj.getJSONArray("filters"));

soft.assertTrue(page.filters().getShowDomainChk().isVisible(), "Show domain checkbox is displayed");
soft.assertTrue(page.filters().getShowDomainChk().isChecked(), "Show domain checkbox is checked");

soft.assertTrue(page.grid().isPresent(), "Grid displayed");

Allure.step("check at least one domain property id displayed");
log.info("check at least one domain property id displayed");
List<String> values = page.grid().getListedValuesOnColumn("Usage");
soft.assertTrue(values.contains("Domain"), "at least one domain prop shown");

soft.assertAll();
}


/*  EDELIVERY-7305 - PROP-3 - Open Properties page as Admin  */
/*  PROP-3 - Open Properties page as Admin  */
@Description("PROP-3 - Open Properties page as Admin")
@Link(name = "EDELIVERY-7305", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7305")
@AllureId("PROP-3")
@Test(description = "PROP-3", groups = {"multiTenancy", "singleTenancy"})
public void openPageAdmin() throws Exception {
SoftAssert soft = new SoftAssert();

String username = rest.getUsername(null, DRoles.ADMIN, true, false, true);
login(username, data.defaultPass());

Allure.step("going to properties page");
log.info("going to properties page");
PropertiesPage page = new PropertiesPage(driver);
page.getSidebar().goToPage(PAGES.PROPERTIES);

Allure.step("waiting for grid to load");
log.info("waiting for grid to load");
page.propGrid().waitForRowsToLoad();

basicFilterPresence(soft, page.filters(), descriptorObj.getJSONArray("filters"));
page.filters().expandArea();
advancedFilterPresence(soft, page.filters(), descriptorObj.getJSONArray("filters"));

soft.assertTrue(page.grid().isPresent(), "Grid displayed");

if (data.isMultiDomain()) {
Allure.step(" checking if a global property can be viewed by admin");
log.info(" checking if a global property can be viewed by admin");
page.filters().filterBy("wsplugin.mtom.enabled", null, null, null, null);
page.grid().waitForRowsToLoad();

soft.assertEquals(page.grid().getRowsNo(), 0, "No rows displayed");
}
soft.assertAll();
}


/*  EDELIVERY-7306 - PROP-4 - Filter properties using available filters  */
/*  PROP-4 - Filter properties using available filters  */
@Description("PROP-4 - Filter properties using available filters")
@Link(name = "EDELIVERY-7306", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7306")
@AllureId("PROP-4")
@Test(description = "PROP-4", groups = {"multiTenancy", "singleTenancy"})
public void filterProperties() throws Exception {
SoftAssert soft = new SoftAssert();

String propName = "domibus.alert.cert.expired.active";

Allure.step("going to properties page");
log.info("going to properties page");
PropertiesPage page = new PropertiesPage(driver);
page.getSidebar().goToPage(PAGES.PROPERTIES);

Allure.step("waiting for grid to load");
log.info("waiting for grid to load");
page.propGrid().waitForRowsToLoad();

Allure.step(" checking if a global property can be viewed by admin");
log.info(" checking if a global property can be viewed by admin");
page.filters().filterBy(propName, null, null, null, null);
page.grid().waitForRowsToLoad();

soft.assertEquals(page.grid().getRowsNo(), 1, "1 rows displayed");

HashMap<String, String> info = page.grid().getRowInfo(0);

soft.assertEquals(info.get("Property Name"), propName, "correct property name is displayed");

soft.assertAll();
}

/*  EDELIVERY-7307 - PROP-5 - Change number of rows visible  */
/*  PROP-5 - Change number of rows visible  */
@Description("PROP-5 - Change number of rows visible")
@Link(name = "EDELIVERY-7307", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7307")
@AllureId("PROP-5")
@Test(description = "PROP-5", groups = {"multiTenancy", "singleTenancy"})
public void changeNumberOfRows() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("going to properties page");
log.info("going to properties page");
PropertiesPage page = new PropertiesPage(driver);
page.getSidebar().goToPage(PAGES.PROPERTIES);

Allure.step("waiting for grid to load");
log.info("waiting for grid to load");
page.propGrid().waitForRowsToLoad();

Allure.step("check changing number of rows visible");
log.info("check changing number of rows visible");
page.grid().checkChangeNumberOfRows(soft);

soft.assertAll();
}

/*  EDELIVERY-7308 - PROP-6 - Change visible columns  */
/*  PROP-6 - Change visible columns  */
@Description("PROP-6 - Change visible columns")
@Link(name = "EDELIVERY-7308", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7308")
@AllureId("PROP-6")
@Test(description = "PROP-6", groups = {"multiTenancy", "singleTenancy"})
public void changeVisibleColumns() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("going to properties page");
log.info("going to properties page");
PropertiesPage page = new PropertiesPage(driver);
page.getSidebar().goToPage(PAGES.PROPERTIES);

Allure.step("waiting for grid to load");
log.info("waiting for grid to load");
page.propGrid().waitForRowsToLoad();

Allure.step("checking changing visible columns");
log.info("checking changing visible columns");
page.propGrid().checkModifyVisibleColumns(soft);

soft.assertAll();
}

/* EDELIVERY-7309 - PROP-7 - Sort grid  */
/*  PROP-7 - Sort grid  */
@Description("PROP-7 - Sort grid")
@Link(name = "EDELIVERY-7309", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7309")
@AllureId("PROP-7")
@Test(description = "PROP-7", groups = {"multiTenancy", "singleTenancy"})
public void checkSorting() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("going to properties page");
log.info("going to properties page");
PropertiesPage page = new PropertiesPage(driver);
page.getSidebar().goToPage(PAGES.PROPERTIES);

Allure.step("waiting for grid to load");
log.info("waiting for grid to load");
page.propGrid().waitForRowsToLoad();

Allure.step("checking sorting");
log.info("checking sorting");

JSONArray colDescs = descriptorObj.getJSONObject("grid").getJSONArray("columns");
DGrid grid = page.propGrid();

for (int i = 0; i < 3; i++) {
JSONObject colDesc = colDescs.getJSONObject(i);
if (grid.getColumnNames().contains(colDesc.getString("name"))) {
TestUtils.testSortingForColumn(soft, page.propGrid(), colDesc);
}
}

soft.assertAll();
}

/* EDELIVERY-7310 - PROP-8 - Change active domain  */
/*  PROP-8 - Change active domain  */
@Description("PROP-8 - Change active domain")
@Link(name = "EDELIVERY-7310", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7310")
@AllureId("PROP-8")
@Test(description = "PROP-8", groups = {"multiTenancy"})
public void changeDomain() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("setting domaint title to empty string for default domain");
log.info("setting domaint title to empty string for default domain");
rest.properties().updateDomibusProperty("domain.title", "", null);

Allure.step("going to properties page");
log.info("going to properties page");
PropertiesPage page = new PropertiesPage(driver);
page.getSidebar().goToPage(PAGES.PROPERTIES);

Allure.step("waiting for grid to load");
log.info("waiting for grid to load");
page.propGrid().waitForRowsToLoad();

Allure.step("filter for property domain.title");
log.info("filter for property domain.title");
page.filters().filterBy("domain.title", null, null, null, true);
page.propGrid().waitForRowsToLoad();

page.propGrid().setPropertyValue("domain.title", page.getDomainFromTitle());

String firstValue = page.propGrid().getPropertyValue("domain.title");
Allure.step("got property value " + firstValue);
log.info("got property value " + firstValue);

Allure.step("changing domain");
log.info("changing domain");
page.getDomainSelector().selectAnotherDomain();
page.propGrid().waitForRowsToLoad();


String newDomainValue = page.propGrid().getPropertyValue("domain.title");
Allure.step("got value for new domain: " + newDomainValue);
log.info("got value for new domain: " + newDomainValue);

soft.assertNotEquals(firstValue, newDomainValue, "Values from the different domains are not equal");

Allure.step("resetting value");
log.info("resetting value");
rest.properties().updateDomibusProperty("domain.title", "", null);


soft.assertAll();
}

/* EDELIVERY-7311 - PROP-9 - Update property value to valid value and press save  */
/*  PROP-9 - Update property value to valid value and press save  */
@Description("PROP-9 - Update property value to valid value and press save")
@Link(name = "EDELIVERY-7311", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7311")
@AllureId("PROP-9")
@Test(description = "PROP-9", groups = {"multiTenancy", "singleTenancy"})
public void updateAndSave() throws Exception {
SoftAssert soft = new SoftAssert();

String domainTitleVal = Gen.randomAlphaNumeric(15);

Allure.step("going to properties page");
log.info("going to properties page");
PropertiesPage page = new PropertiesPage(driver);
page.getSidebar().goToPage(PAGES.PROPERTIES);

Allure.step("waiting for grid to load");
log.info("waiting for grid to load");
page.propGrid().waitForRowsToLoad();

Allure.step("filter for property domain.title");
log.info("filter for property domain.title");
page.filters().filterBy("domain.title", null, null, null, true);
page.propGrid().waitForRowsToLoad();

page.propGrid().setPropertyValue("domain.title", domainTitleVal);

page.getAlertArea().isShown();

page.refreshPage();

String pageValue = page.getDomainFromTitle();

String value = rest.properties().getPropertyValue("domain.title", true, null);
Allure.step("got property value " + value);
log.info("got property value " + value);

soft.assertEquals(value, domainTitleVal, "Set value is saved properly");
soft.assertEquals(pageValue, domainTitleVal, "Set value is shown in page title");

Allure.step("resetting value");
log.info("resetting value");
rest.properties().updateDomibusProperty("domain.title", "", null);


soft.assertAll();
}


/* EDELIVERY-7312 - PROP-10 - Update property value to invalid value and press save  */
/*  PROP-10 - Update property value to invalid value and press save  */
@Description("PROP-10 - Update property value to invalid value and press save")
@Link(name = "EDELIVERY-7312", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7312")
@AllureId("PROP-10")
@Test(description = "PROP-10", groups = {"multiTenancy", "singleTenancy"})
public void updateInvalidValue() throws Exception {
SoftAssert soft = new SoftAssert();

rest.properties().updateDomibusProperty("domibus.property.validation.enabled", "true");

Allure.step("going to properties page");
log.info("going to properties page");
PropertiesPage page = new PropertiesPage(driver);
page.getSidebar().goToPage(PAGES.PROPERTIES);

Allure.step("waiting for grid to load");
log.info("waiting for grid to load");
page.propGrid().waitForRowsToLoad();

Allure.step("filter for boolean properties");
log.info("filter for boolean properties");
page.filters().filterBy("", "BOOLEAN", null, null, true);
page.propGrid().waitForRowsToLoad();

Allure.step("getting info on row 0");
log.info("getting info on row 0");
HashMap<String, String> info = page.propGrid().getRowInfo(0);

String toSetValue = Gen.randomAlphaNumeric(5);
Allure.step("setting invalid value " + toSetValue);
log.info("setting invalid value " + toSetValue);
page.propGrid().setPropRowValueAndSave(0, toSetValue);

Allure.step("checking for error message");
log.info("checking for error message");
soft.assertTrue(page.getAlertArea().isError(), "Error message is shown");

Allure.step("check correct message is shown");
log.info("check correct message is shown");
soft.assertEquals(page.getAlertArea().getAlertMessage(),
String.format(DMessages.PROPERTIES_UPDATE_ERROR_TYPE, toSetValue, info.get("Property Name"), "BOOLEAN"),
"Correct error message is shown");

page.refreshPage();
page.propGrid().waitForRowsToLoad();

String value = page.propGrid().getPropertyValue(info.get("Property Name"));
Allure.step("getting value after refresh: " + value);
log.info("getting value after refresh: " + value);

soft.assertEquals(value, info.get("Property Value"), "Set value was not saved");

soft.assertAll();
}


/* EDELIVERY-7313 - PROP-11 - Update property value and press revert  */
/*  PROP-11 - Update property value and press revert  */
@Description("PROP-11 - Update property value and press revert")
@Link(name = "EDELIVERY-7313", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7313")
@AllureId("PROP-11")
@Test(description = "PROP-11", groups = {"multiTenancy", "singleTenancy"})
public void updateAndRevert() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("going to properties page");
log.info("going to properties page");
PropertiesPage page = new PropertiesPage(driver);
page.getSidebar().goToPage(PAGES.PROPERTIES);

Allure.step("waiting for grid to load");
log.info("waiting for grid to load");
page.propGrid().waitForRowsToLoad();

Allure.step("getting info on row 0");
log.info("getting info on row 0");
HashMap<String, String> info = page.propGrid().getRowInfo(0);

String toSetValue = Gen.randomAlphaNumeric(5);
Allure.step("setting invalid value " + toSetValue);
log.info("setting invalid value " + toSetValue);
page.propGrid().setPropRowValueAndRevert(0, toSetValue);

String value = page.propGrid().getPropertyValue(info.get("Property Name"));
Allure.step("getting value after refresh: " + value);
log.info("getting value after refresh: " + value);

soft.assertEquals(value, info.get("Property Value"), "Set value was not saved");

soft.assertAll();
}


/* EDELIVERY-7314 - PROP-12 - Update property value don't press save and move focus on another field  */
/*  PROP-12 - Update property value dont press save and move focus on another field  */
@Description("PROP-12 - Update property value dont press save and move focus on another field")
@Link(name = "EDELIVERY-7314", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7314")
@AllureId("PROP-12")
@Test(description = "PROP-12", groups = {"multiTenancy", "singleTenancy"})
public void fillAndDontSave() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("going to properties page");
log.info("going to properties page");
PropertiesPage page = new PropertiesPage(driver);
page.getSidebar().goToPage(PAGES.PROPERTIES);

Allure.step("waiting for grid to load");
log.info("waiting for grid to load");
page.propGrid().waitForRowsToLoad();

Allure.step("getting info on row 0");
log.info("getting info on row 0");
HashMap<String, String> info = page.propGrid().getRowInfo(0);

String toSetValue = Gen.randomAlphaNumeric(5);
Allure.step("setting invalid value " + toSetValue);
log.info("setting invalid value " + toSetValue);
page.propGrid().setPropRowValue(0, toSetValue);


page.grid().getGridCtrl().showCtrls();
page.wait.forXMillis(3000);

String value = page.propGrid().getPropertyValue(info.get("Property Name"));
Allure.step("getting value after refresh: " + value);
log.info("getting value after refresh: " + value);

soft.assertEquals(value, info.get("Property Value"), "Set value was not saved");

soft.assertAll();
}


/* EDELIVERY-7315 - PROP-13 - Update property value don't press save and go to another page   */
/*  PROP-13 - Update property value dont press save and go to another page   */
@Description("PROP-13 - Update property value dont press save and go to another page ")
@Link(name = "EDELIVERY-7315", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7315")
@AllureId("PROP-13")
@Test(description = "PROP-13", groups = {"multiTenancy", "singleTenancy"})
public void fillAndGoPage2() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("going to properties page");
log.info("going to properties page");
PropertiesPage page = new PropertiesPage(driver);
page.getSidebar().goToPage(PAGES.PROPERTIES);

Allure.step("waiting for grid to load");
log.info("waiting for grid to load");
page.propGrid().waitForRowsToLoad();

Allure.step("getting info on row 0");
log.info("getting info on row 0");
HashMap<String, String> info = page.propGrid().getRowInfo(0);

String toSetValue = Gen.randomAlphaNumeric(5);
Allure.step("setting invalid value " + toSetValue);
log.info("setting invalid value " + toSetValue);
page.propGrid().setPropRowValue(0, toSetValue);


page.wait.forXMillis(1000);
page.grid().getPagination().goToNextPage();

String value = rest.properties().getDomibusPropertyDetail(info.get("Property Name"), null).getString("value");
Allure.step("getting value after refresh: " + value);
log.info("getting value after refresh: " + value);

soft.assertEquals(value, info.get("Property Value"), "Set value was not saved");

soft.assertAll();
}

/* EDELIVERY-7316 - PROP-14 - Export to CSV   */
/*  PROP-14 - Export to CSV  */
@Description("PROP-14 - Export to CSV")
@Link(name = "EDELIVERY-7316", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7316")
@AllureId("PROP-14")
@Test(description = "PROP-14", groups = {"multiTenancy", "singleTenancy"})
public void exportCSV() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("going to properties page");
log.info("going to properties page");
PropertiesPage page = new PropertiesPage(driver);
page.getSidebar().goToPage(PAGES.PROPERTIES);

Allure.step("waiting for grid to load");
log.info("waiting for grid to load");
page.propGrid().waitForRowsToLoad();

PropGrid grid = page.propGrid();
grid.getGridCtrl().showAllColumns();
grid.getPagination().getPageSizeSelect().selectOptionByText("100");

String filename = page.pressSaveCsvAndSaveFile();

page.propGrid().relaxCheckCSVvsGridInfo(filename, soft, "text");

soft.assertAll();
}


/* EDELIVERY-7318 - PROP-16 - Update property domibus.console.login.maximum.attempt   */
/*  PROP-16 - Update property domibusconsoleloginmaximumattempt  */
@Description("PROP-16 - Update property domibusconsoleloginmaximumattempt")
@Link(name = "EDELIVERY-7318", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7318")
@AllureId("PROP-16")
@Test(description = "PROP-16", groups = {"multiTenancy", "singleTenancy"})
public void updateMaxLoginAttempts() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("going to properties page");
log.info("going to properties page");
PropertiesPage page = new PropertiesPage(driver);
page.getSidebar().goToPage(PAGES.PROPERTIES);

Allure.step("waiting for grid to load");
log.info("waiting for grid to load");
page.propGrid().waitForRowsToLoad();

page.filters().filterBy("domibus.console.login.maximum.attempt", null, null, null, null);
PropGrid grid = page.propGrid();
grid.waitForRowsToLoad();


grid.setPropertyValue("domibus.console.login.maximum.attempt", "1");

String username = rest.getUsername(null, DRoles.USER, true, false, false);

boolean userBlocked = false;
int attempts = 0;

while (!userBlocked && attempts < 10) {
Allure.step("attempting login with wrong pass and user " + username);
log.info("attempting login with wrong pass and user " + username);
ClientResponse response = rest.callLogin(username, "wrong password");
attempts++;

Allure.step("checking error message for account suspended message");
log.info("checking error message for account suspended message");
String errMessage = response.getEntity(String.class);
userBlocked = errMessage.contains("Suspended");
}

Allure.step("verifying number of attempts");
log.info("verifying number of attempts");
soft.assertEquals(attempts, 2, "User is blocked on the second attempt to login");

soft.assertAll();
}

/* EDELIVERY-7319 - PROP-17 - Update property domibus.console.login.suspension.time  */
/*  PROP-17 - Update property domibusconsoleloginsuspensiontime  */
@Description("PROP-17 - Update property domibusconsoleloginsuspensiontime")
@Link(name = "EDELIVERY-7319", url = "https://ec.europa.eu/cefdigital/tracker/browse/EDELIVERY-7319")
@AllureId("PROP-17")
@Test(description = "PROP-17", groups = {"multiTenancy", "singleTenancy"})
public void updateSuspensionTime() throws Exception {
SoftAssert soft = new SoftAssert();

Allure.step("going to properties page");
log.info("going to properties page");
PropertiesPage page = new PropertiesPage(driver);
page.getSidebar().goToPage(PAGES.PROPERTIES);

Allure.step("waiting for grid to load");
log.info("waiting for grid to load");
page.propGrid().waitForRowsToLoad();

PropGrid grid = page.propGrid();
grid.waitForRowsToLoad();


grid.setPropertyValue("domibus.console.login.suspension.time", "10");
grid.setPropertyValue("domibus.account.unlock.cron", "0 * * ? * *");

String username = rest.getUsername(null, DRoles.USER, true, false, true);

boolean userBlocked = false;
int attempts = 0;

while (!userBlocked && attempts < 10) {
Allure.step("attempting login with wrong pass and user " + username);
log.info("attempting login with wrong pass and user " + username);
ClientResponse response = rest.callLogin(username, "wrong password");
attempts++;

Allure.step("checking error message for account suspended message");
log.info("checking error message for account suspended message");
String errMessage = response.getEntity(String.class);
userBlocked = errMessage.contains("Suspended");
}

page.wait.forXMillis(60000);
ClientResponse response = rest.callLogin(username, data.defaultPass());
soft.assertEquals(response.getStatus(), 200, "Login response is success");

soft.assertAll();
}



}
