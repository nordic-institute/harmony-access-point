package ddsl.dcomponents;

import ddsl.dobjects.DButton;
import ddsl.dobjects.DObject;
import org.apache.commons.io.FileUtils;
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
	
	@FindBy(css = "page-header > h1")
	protected WebElement pageTitle;
	@FindBy(css = ".helpMenu")
	protected WebElement helpLnk;
	@FindBy(tagName = "mat-dialog-container")
	protected WebElement dialogContainer;
	
	@FindBy(id = "saveascsvbutton_id")
	protected WebElement saveCSV;
	
	By domainSelectSelector = By.cssSelector("#sandwichMenuHolder > domain-selector > mat-select");
	
	public DomibusPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}
	
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
			waitForPageToLoad();
			wait.forXMillis(300);
		} catch (Exception e) {
			log.warn(e.getMessage());
		}
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
		WebElement element = driver.findElement(domainSelectSelector);
		return new DomainSelector(driver, element);
	}
	
	public void waitForPageToLoad() throws Exception {
		wait.forElementToBeVisible(getSandwichMenu().expandButton);
	}
	
	public void waitForPageTitle() throws Exception {
		wait.forElementToBeVisible(pageTitle);
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
	
	public DButton getSaveCSVButton() {
		return weToDButton(saveCSV);
	}
	
	public String pressSaveCsvAndSaveFile() throws Exception {
		
		log.info("Customized location for download");
		String filePath = data.downloadFolderPath();
		
		log.info("Clean given directory");
		FileUtils.cleanDirectory(new File(filePath));
		
		log.info("Click on download csv button");
		getSaveCSVButton().click();
		
		log.info("Wait for download to complete");
		
		wait.forXMillis(3000);
		
		log.info("Check if file is downloaded at given location");
		if(!DFileUtils.isFileDownloaded(filePath)){
			throw new Exception("Could not find file");
		}
		
		return DFileUtils.getCompleteFileName(data.downloadFolderPath());
	}
	
	
	
	
}
