package domibus.ui.functional;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import ddsl.enums.PAGES;
import domibus.ui.SeleniumTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import pages.domains.DomainPage;

import static java.lang.String.format;

public class DomainsPgTest extends SeleniumTest {

    /*    EDELIVERY-8685 - DOM-1 - Domains page acess*/
    @Test(description = "DOM-1", groups = {"multiTenancy"})
    public void domainPageAccess() throws Exception {
        SoftAssert soft = new SoftAssert();

        DomibusPage page = new DomibusPage(driver);
        soft.assertTrue(page.getSidebar().getPageLnk(PAGES.DOMAINS).isPresent(), "Domains link is displayed for super");

        String username = rest.getUser(null, DRoles.ADMIN, true, false, true).getString("userName");
        login(username, data.defaultPass());

        soft.assertFalse(page.getSidebar().getPageLnk(PAGES.DOMAINS).isPresent(), "Domains link is NOT displayed for admin user");

        soft.assertAll();
    }

    /*     EDELIVERY-8686 - DOM-2 - Domains page has no domain selector */
    @Test(description = "DOM-2", groups = {"multiTenancy"})
    public void domainSelectorOnDomainsPage() throws Exception {
        SoftAssert soft = new SoftAssert();

        DomibusPage page = new DomibusPage(driver);
        page.getSidebar().goToPage(PAGES.DOMAINS);

        try {
            soft.assertFalse(page.getDomainSelector().isDisplayed(), "Domain selector is NOT displayed on Domains page");
        } catch (Exception e) {
        }

        soft.assertAll();
    }

    /* EDELIVERY-8689 - DOM-4 - Disable domain at runtime */
    @Test(description = "DOM-4", groups = {"multiTenancy"})
    public void disableEnableDomain() throws Exception {
        SoftAssert soft = new SoftAssert();

        DomainPage page = new DomainPage(driver);
        page.getSidebar().goToPage(PAGES.DOMAINS);

        String domainName = rest.getNonDefaultDomain();
        String code = rest.getDomainCodeForName(domainName);

        log.info("Disabling domain " + domainName);
        //make sure domain with code is enabled
        if (!page.grid().isActive(code)) {
            log.info("Enabling domain " + domainName);
            page.grid().toggleActive(code);
            soft.assertTrue(page.grid().isActive(code), "Domain is now enabled");
        }

        log.info("Getting users from domain " + domainName);
        String superU = rest.getUser(code, DRoles.SUPER, true, false, true).getString("userName");
        String adminU = rest.getUser(code, DRoles.ADMIN, true, false, true).getString("userName");
        String userU = rest.getUser(code, DRoles.USER, true, false, true).getString("userName");
        String pluserU = rest.getPluginUser(code, DRoles.ADMIN, true, true).getString("userName");

        rest.pmode().uploadPMode("pmodes/pmode-dataSetupBlue.xml", code);

        //disable domain and check that users cannot login and domain cannot receive messages
        log.info("Disabling domain " + domainName);
        page.grid().toggleActive(code);
        soft.assertFalse(page.getAlertArea().isError(), "Success message is displayed");

        soft.assertFalse(page.grid().isActive(code), "Domain status is disabled");

        log.info("Checking that users cannot login");
        soft.assertFalse(rest.login(superU, data.defaultPass()), "Login with super admin from disabled domain is not allowed");
        soft.assertFalse(rest.login(adminU, data.defaultPass()), "Login with domain admin from disabled domain is not allowed");
        soft.assertFalse(rest.login(userU, data.defaultPass()), "Login with user from disabled domain is not allowed");

        try {
            log.info("Checking that domain cannot receive messages");
            String messId = messageSender.sendMessage(pluserU, data.defaultPass(), null, null);
            soft.assertTrue(messId.isEmpty(), "Message is not sent to disabled domain");
        } catch (Exception e) {
        }

        log.info("Enabling domain " + domainName);
        page.grid().toggleActive(code);
        soft.assertTrue(page.grid().isActive(code), "Domain is now enabled");

        log.info("Checking that users can login");
        soft.assertTrue(rest.login(superU, data.defaultPass()), "Login with super admin ");
        soft.assertTrue(rest.login(adminU, data.defaultPass()), "Login with domain admin ");
        soft.assertTrue(rest.login(userU, data.defaultPass()), "Login with user");

        log.info("Checking that domain can receive messages");
        String messId = messageSender.sendMessage(pluserU, data.defaultPass(), null, null);
        soft.assertFalse(messId.isEmpty(), "Message is sent to enabled domain");

        soft.assertAll();
    }


    /*EDELIVERY-9627 - DOM-6 - Super user tries to disable it's own preferred domain and recieves error*/
    @Test(description = "DOM-6", groups = {"multiTenancy"})
    public void superDisablesPreferredDomain() throws Exception {
        SoftAssert soft = new SoftAssert();

        DomainPage page = new DomainPage(driver);

        String currentUser = page.getCurrentLoggedInUser();
        String userDomain = rest.users().getUser(currentUser).getString("domain");

        log.info("Trying to disable domain " + userDomain);

        log.info("Making sure domain is not selected");
        page.getDomainSelector().selectOptionByText(userDomain);
        page.getDomainSelector().selectAnotherDomain();

        log.info("Going to domains page");
        page.wait.forXMillis(1000);
        page.getSidebar().goToPage(PAGES.DOMAINS);

        log.info("Disabling domain " + userDomain);
        page.grid().toggleActive(userDomain);

        log.info("Checking that error message is displayed");
        soft.assertTrue(page.getAlertArea().isError(), "Error message is displayed");
        soft.assertEquals(page.getAlertArea().getAlertMessage(), format(DMessages.DOMAINS.DISABLE_CURRENT_USER_DOMAIN, userDomain), "Error message is correct");

        soft.assertAll();
    }


    /*     EDELIVERY-9628 - DOM-7 - Login using super user that has it's prefered domain disabled fails with error */
    @Test(description = "DOM-7", groups = {"multiTenancy"})
    public void superPreferredDisabledDomainLogin() throws Exception {
        SoftAssert soft = new SoftAssert();

        DomainPage page = new DomainPage(driver);
        page.getSidebar().goToPage(PAGES.DOMAINS);

        String domainName = rest.getNonDefaultDomain();
        String code = rest.getDomainCodeForName(domainName);

        log.info("Disabling domain " + domainName);
        //make sure domain with code is enabled
        if (!page.grid().isActive(code)) {
            log.info("Enabling domain " + domainName);
            page.grid().toggleActive(code);
            soft.assertTrue(page.grid().isActive(code), "Domain is now enabled");
        }

        log.info("Getting super user from domain " + domainName);
        String superU = rest.getUser(code, DRoles.SUPER, true, false, true).getString("userName");

        log.info("Disabling domain " + domainName);
        page.grid().toggleActive(code);
        soft.assertFalse(page.grid().isActive(code), "Domain is now disabled");

        log.info("Trying to login with super user from disabled domain");
        soft.assertFalse(rest.login(superU, data.defaultPass()), "Login with super admin from disabled domain is not allowed");

        soft.assertAll();
    }

    /* EDELIVERY-9629 - DOM-8 - Currently selected domain cannot be disabled (domain listed in page title) */
    @Test(description = "DOM-8", groups = {"multiTenancy"})
    public void disableCurrentDomain() throws Exception {
        SoftAssert soft = new SoftAssert();

        DomainPage page = new DomainPage(driver);
        page.getSidebar().goToPage(PAGES.DOMAINS);

        String domainName = page.getDomainFromTitle();

        page.grid().toggleActive(domainName);

        soft.assertTrue(page.getAlertArea().isError(), "Error message is displayed");
        soft.assertEquals(page.getAlertArea().getAlertMessage(), format(DMessages.DOMAINS.DISABLE_CURRENT_DOMAIN, domainName), "Error message is correct");

        soft.assertAll();
    }

    /* EDELIVERY-9630 - DOM-9 - When domain is disabled no users associated with that domain can login (not even super users) */
    @Test(description = "DOM-9", groups = {"multiTenancy"})
    public void usersDisabledDomainNoLogin() throws Exception {
        SoftAssert soft = new SoftAssert();

        DomainPage page = new DomainPage(driver);
        page.getSidebar().goToPage(PAGES.DOMAINS);

        String domainName = rest.getNonDefaultDomain();
        String code = rest.getDomainCodeForName(domainName);

        log.info("Disabling domain " + domainName);
        //make sure domain with code is enabled
        if (!page.grid().isActive(code)) {
            log.info("Enabling domain " + domainName);
            page.grid().toggleActive(code);
            soft.assertTrue(page.grid().isActive(code), "Domain is now enabled");
        }

        log.info("Getting users from domain " + domainName);
        String superU = rest.getUser(code, DRoles.SUPER, true, false, true).getString("userName");
        String adminU = rest.getUser(code, DRoles.ADMIN, true, false, true).getString("userName");
        String userU = rest.getUser(code, DRoles.USER, true, false, true).getString("userName");

        //disable domain and check that users cannot login and domain cannot receive messages
        log.info("Disabling domain " + domainName);
        page.grid().toggleActive(code);
        soft.assertFalse(page.getAlertArea().isError(), "Success message is displayed");

        soft.assertFalse(page.grid().isActive(code), "Domain status is disabled");

        log.info("Checking that users cannot login");
        soft.assertFalse(rest.login(superU, data.defaultPass()), "Login with super admin from disabled domain is not allowed");
        soft.assertFalse(rest.login(adminU, data.defaultPass()), "Login with domain admin from disabled domain is not allowed");
        soft.assertFalse(rest.login(userU, data.defaultPass()), "Login with user from disabled domain is not allowed");


        log.info("Enabling domain " + domainName);
        page.grid().toggleActive(code);
        soft.assertTrue(page.grid().isActive(code), "Domain is now enabled");

        log.info("Checking that users can login");
        soft.assertTrue(rest.login(superU, data.defaultPass()), "Login with super admin ");
        soft.assertTrue(rest.login(adminU, data.defaultPass()), "Login with domain admin ");
        soft.assertTrue(rest.login(userU, data.defaultPass()), "Login with user");

        soft.assertAll();
    }

    /* EDELIVERY-9631 - DOM-10 - Disabled domain doesn't receive messages */
    @Test(description = "DOM-10", groups = {"multiTenancy"})
    public void disabledDomainNoMess() throws Exception {
        SoftAssert soft = new SoftAssert();

        DomainPage page = new DomainPage(driver);
        page.getSidebar().goToPage(PAGES.DOMAINS);

        String domainName = rest.getNonDefaultDomain();
        String code = rest.getDomainCodeForName(domainName);

        log.info("Disabling domain " + domainName);
        //make sure domain with code is enabled
        if (!page.grid().isActive(code)) {
            log.info("Enabling domain " + domainName);
            page.grid().toggleActive(code);
            soft.assertTrue(page.grid().isActive(code), "Domain is now enabled");
        }

        log.info("Setting up domain " + domainName);
        String pluserU = rest.getPluginUser(code, DRoles.ADMIN, true, true).getString("userName");
        rest.pmode().uploadPMode("pmodes/pmode-dataSetupBlue.xml", code);

        //disable domain and check that users cannot login and domain cannot receive messages
        log.info("Disabling domain " + domainName);
        page.grid().toggleActive(code);
        soft.assertFalse(page.getAlertArea().isError(), "Success message is displayed");

        soft.assertFalse(page.grid().isActive(code), "Domain status is disabled");

        try {
            log.info("Checking that domain cannot receive messages");
            String messId = messageSender.sendMessage(pluserU, data.defaultPass(), null, null);
            soft.assertTrue(messId.isEmpty(), "Message is not sent to disabled domain");
        } catch (Exception e) {
        }

        log.info("Enabling domain " + domainName);
        page.grid().toggleActive(code);
        soft.assertTrue(page.grid().isActive(code), "Domain is now enabled");

        log.info("Checking that domain can receive messages");
        String messId = messageSender.sendMessage(pluserU, data.defaultPass(), null, null);
        soft.assertFalse(messId.isEmpty(), "Message is sent to enabled domain");

        soft.assertAll();
    }


}

