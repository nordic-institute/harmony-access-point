package ddsl.dcomponents;

import com.google.common.collect.Lists;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DObject;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.DFileUtils;

import java.io.File;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class DomibusPage extends DComponent {

	public DomibusPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	@FindBy(css = "page-header > h1")
	protected WebElement pageTitle;

	@FindBy(css = ".helpMenu")
	protected WebElement helpLnk;

	@FindBy(tagName = "md-dialog-container")
	protected WebElement dialogContainer;

	@FindBy(id = "saveascsvbutton_id")
	WebElement downloadCsvButton;

	public AlertArea getAlertArea() {
		return new AlertArea(driver);
	}

	public SideNavigation getSidebar() {
		return new SideNavigation(driver);
	}

	public SandwichMenu getSandwichMenu() {
		return new SandwichMenu(driver);
	}

	public void refreshPage() {
		driver.navigate().refresh();
		try {
			wait.forXMillis(300);
			waitForTitle();
		} catch (Exception e) {

		}
	}

	public DButton getDownloadCsvButton() {
		return new DButton(driver, downloadCsvButton);
	}

	public String getTitle() throws Exception {
		DObject pgTitleObj = new DObject(driver, pageTitle);
		String rawTitle = pgTitleObj.getText();

		if (rawTitle.contains(":")) {
//			removing listed domain from title
			return rawTitle.split(":")[1].trim();
		}
		return rawTitle;
	}

	public String getDomainFromTitle() throws Exception {
		DObject pgTitleObj = new DObject(driver, pageTitle);
		String rawTitle = pgTitleObj.getText();

		if (rawTitle.contains(":")) {
//			removing listed title
			return rawTitle.split(":")[0].trim();
		}
		return null;
	}

	public DomainSelector getDomainSelector() throws Exception {
		By domainSelectSelector = By.cssSelector("#sandwichMenuHolder > domain-selector > md-select");
		WebElement element = driver.findElement(domainSelectSelector);
		return new DomainSelector(driver, element);
	}

	public void waitForTitle() {
		wait.forElementToBe(pageTitle);
	}

	public boolean hasOpenDialog() {
		log.info("checking for any opened dialogs");
		try {
			wait.forElementToBeVisible(dialogContainer);
			if (weToDobject(dialogContainer).isVisible()) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	/*  This method will check if downloadCsv link is present on page or not   */
	public void clickDownloadCsvButton(WebElement element) throws Exception {
		DButton button = new DButton(driver, element);
		if (button.isVisible()) {
			button.click();
		} else {
			log.info("CSV Button is not available on this page ");
		}

	}

	public File downloadCsv() throws Exception {

		File containerDir = new File(DFileUtils.downloadFolderPath());

		int waited = 0;
		while ((FileUtils.listFiles(containerDir, FileFileFilter.FILE, null).size() > 0) && (waited < 10)) {
			FileUtils.cleanDirectory(containerDir);
			wait.forXMillis(1000);
			waited++;
		}


		DButton button = weToDButton(downloadCsvButton);
		if (button.isVisible()) {
			button.click();
		} else {
			log.info("CSV Button is not available on this page ");
		}

		waited = 0;
		while ((FileUtils.listFiles(containerDir, FileFileFilter.FILE, null).size() !=1 ) && (waited < 10)) {
			wait.forXMillis(1000);
			waited++;
		}

		if(FileUtils.listFiles(containerDir, FileFileFilter.FILE, null).size() !=1 ){
			log.error("Unexpected number of files i the download folder: " +
					FileUtils.listFiles(containerDir, FileFileFilter.FILE, null).size());
			throw new Exception("Unexpected number of files i the download folder");
		}

		File file = Lists.newArrayList(FileUtils.listFiles(containerDir, FileFileFilter.FILE, null)).get(0);
		return file;
	}


}
