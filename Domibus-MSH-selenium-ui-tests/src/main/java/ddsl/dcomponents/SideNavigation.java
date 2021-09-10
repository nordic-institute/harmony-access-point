package ddsl.dcomponents;

import ddsl.dobjects.DButton;
import ddsl.dobjects.DLink;
import ddsl.enums.PAGES;
import metricss.MyMetrics;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class SideNavigation extends DComponent {

	@FindBy(tagName = "mat-sidenav")
	public WebElement sideBar;

	private WebElement topLogo;
	private WebElement topLogoText;
	@FindBy(id = "messages_id")
	private WebElement messagesLnk;
	@FindBy(id = "messagefilter_id")
	private WebElement messagefilterLnk;
	@FindBy(id = "errorlog_id")
	private WebElement errorlogLnk;
	@FindBy(css = "mat-sidenav button.sideNavButton[tabindex=\"0\"]")
	private List<WebElement> matSidebarButtons;
	//	--------------------PMODE-------------------------

	@FindBy(id = "current_pmode_id")
	private WebElement pmodeCurrentLnk;

	@FindBy(id = "pmode_archive_id")
	private WebElement pmodeArchiveLnk;

	@FindBy(id = "pmode_party_id")
	private WebElement pmodePartiesLnk;

	@FindBy(css = "#mat-expansion-panel-header-0")
	private WebElement pmodeExpandLnk;
	//	----------------------------------------------------

	//	--------------TRUSTSTORES---------------------------
	@FindBy(id = "truststore_id")
	private WebElement domibusTruststoreLnk;

	@FindBy(id = "tls_truststore_id")
	private WebElement tlsTruststoreLnk;

	@FindBy(id = "mat-expansion-panel-header-1")
	private WebElement truststoreExpand;
	//	----------------------------------------------------

	@FindBy(id = "jmsmonitoring_id")
	private WebElement jmsmonitoringLnk;

	@FindBy(id = "user_id")
	private WebElement userLnk;
	@FindBy(css = "#plugin_user_id")
	private WebElement pluginUsersLnk;
	@FindBy(css = "#audit_id")
	private WebElement auditLnk;
	@FindBy(css = "#alerts_id")
	private WebElement alertsLnk;
	@FindBy(css = "#logging_id")
	private WebElement loggingLnk;
	@FindBy(css = "#connectionmonitoring_id")
	private WebElement connectionMonitoring_Lnk;
	@FindBy(css = "#properties_id")
	private WebElement properties_Lnk;

	public SideNavigation(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, 1), this);
	}


	private boolean isSectionExpanded(WebElement sectionHead) {
		try {
			wait.forAttributeToContain(sectionHead, "class", "mat-expanded");
			return new DButton(driver, sectionHead).getAttribute("class").contains("mat-expanded");
		} catch (Exception e) {
		}
		return false;
	}

	private void expandSection(WebElement sectionHead) {
		if (isSectionExpanded(sectionHead)) return;
		try {
			weToDButton(sectionHead).click();
			wait.forAttributeToContain(sectionHead, "class", "mat-expanded");
		} catch (Exception e) {
			log.warn("Could not expand pmode: ", e);
		}
		wait.forXMillis(200);
	}


	public DLink getPageLnk(PAGES page) {

		wait.forElementToHaveText(sideBar);

		log.debug("Get link to " + page.name());
		switch (page) {
			case MESSAGES:
				return new DLink(driver, messagesLnk);
			case MESSAGE_FILTER:
				return new DLink(driver, messagefilterLnk);
			case ERROR_LOG:
				return new DLink(driver, errorlogLnk);
			case PMODE_CURRENT:
				expandSection(pmodeExpandLnk);
				return new DLink(driver, pmodeCurrentLnk);
			case PMODE_ARCHIVE:
				expandSection(pmodeExpandLnk);
				return new DLink(driver, pmodeArchiveLnk);
			case PMODE_PARTIES:
				expandSection(pmodeExpandLnk);
				return new DLink(driver, pmodePartiesLnk);
			case JMS_MONITORING:
				return new DLink(driver, jmsmonitoringLnk);
			case TRUSTSTORES_DOMIBUS:
				expandSection(truststoreExpand);
				return new DLink(driver, domibusTruststoreLnk);
			case TRUSTSTORES_TLS:
				expandSection(truststoreExpand);
				return new DLink(driver, tlsTruststoreLnk);
			case USERS:
				return new DLink(driver, userLnk);
			case PLUGIN_USERS:
				return new DLink(driver, pluginUsersLnk);
			case AUDIT:
				return new DLink(driver, auditLnk);
			case ALERTS:
				return new DLink(driver, alertsLnk);
			case LOGGING:
				return new DLink(driver, loggingLnk);
			case CONNECTION_MONITORING:
				return new DLink(driver, connectionMonitoring_Lnk);
			case PROPERTIES:
				return new DLink(driver, properties_Lnk);
			default:
				return null;
		}
	}


	public void goToPage(PAGES page) throws Exception {
		
		DomibusPage pg = new DomibusPage(driver);

		log.info("Navigating to " + page.name());
		DLink link = getPageLnk(page);
		log.debug("got link with text " + link.getLinkText());
		String text = link.element.findElement(By.cssSelector("span span")).getText().trim();

		String pgTitle = null;

		try {
			pgTitle = pg.getTitle();
		} catch (Exception e) {
		}

		if (StringUtils.containsIgnoreCase(pgTitle, text)) {
			log.info("already here, refreshing page");
			pg.refreshPage();
		} else {
			link.click();
		}
		wait.forElementToContainText(pg.pageTitle, text);
		log.debug("Navigated to " + page.name());
		
	}

	public boolean isUserState() throws Exception {
		return (matSidebarButtons.size() == 2
				&& weToDButton(matSidebarButtons.get(0)).getText().equalsIgnoreCase("Messages")
				&& weToDButton(matSidebarButtons.get(1)).getText().equalsIgnoreCase("Error Log")
		);
	}

	public boolean isAdminState() throws Exception {

		// for Admin if is Domibus NOT multidomain loggin page should be visible
		// for Admin if is Domibus IS multidomain loggin page should NOT be visible
		boolean loggingVisibility = (!data.isMultiDomain() == getPageLnk(PAGES.LOGGING).isPresent());

		return loggingVisibility
				&&(getPageLnk(PAGES.MESSAGES).isPresent()
				&& getPageLnk(PAGES.ERROR_LOG).isPresent()
				&& getPageLnk(PAGES.MESSAGE_FILTER).isPresent()
				&& getPageLnk(PAGES.PMODE_CURRENT).isPresent()
				&& getPageLnk(PAGES.PMODE_ARCHIVE).isPresent()
				&& getPageLnk(PAGES.PMODE_PARTIES).isPresent()
				&& getPageLnk(PAGES.JMS_MONITORING).isPresent()
				&& getPageLnk(PAGES.TRUSTSTORES_DOMIBUS).isPresent()
				&& getPageLnk(PAGES.USERS).isPresent()
				&& getPageLnk(PAGES.PLUGIN_USERS).isPresent()
				&& getPageLnk(PAGES.AUDIT).isPresent()
				&& getPageLnk(PAGES.ALERTS).isPresent()
				&& getPageLnk(PAGES.CONNECTION_MONITORING).isPresent()
				&& getPageLnk(PAGES.PROPERTIES).isPresent()
		);
	}

	public boolean isSuperState() throws Exception {
		return (getPageLnk(PAGES.LOGGING).isPresent()
				&& getPageLnk(PAGES.MESSAGES).isPresent()
				&& getPageLnk(PAGES.ERROR_LOG).isPresent()
				&& getPageLnk(PAGES.MESSAGE_FILTER).isPresent()
				&& getPageLnk(PAGES.PMODE_CURRENT).isPresent()
				&& getPageLnk(PAGES.PMODE_ARCHIVE).isPresent()
				&& getPageLnk(PAGES.PMODE_PARTIES).isPresent()
				&& getPageLnk(PAGES.JMS_MONITORING).isPresent()
				&& getPageLnk(PAGES.TRUSTSTORES_DOMIBUS).isPresent()
				&& getPageLnk(PAGES.USERS).isPresent()
				&& getPageLnk(PAGES.PLUGIN_USERS).isPresent()
				&& getPageLnk(PAGES.AUDIT).isPresent()
				&& getPageLnk(PAGES.ALERTS).isPresent()
				&& getPageLnk(PAGES.CONNECTION_MONITORING).isPresent()
				&& getPageLnk(PAGES.PROPERTIES).isPresent()
		);
	}

	public boolean isLinkPresent(PAGES page) throws Exception {

		boolean isLinkPresent = true;
		try {
			getPageLnk(page).getLinkText();
		} catch (Exception e) {
			isLinkPresent = false;
		}
		return isLinkPresent;
	}


}
