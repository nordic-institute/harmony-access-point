package ddsl.dcomponents;

import ddsl.dobjects.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;


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

	@FindBy(tagName = "mat-dialog-container")
	protected WebElement dialogContainer;


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
		By domainSelectSelector = By.cssSelector("#sandwichMenuHolder > domain-selector > mat-select");
		WebElement element = driver.findElement(domainSelectSelector);
		return new DomainSelector(driver, element);
	}

	public void waitForPageToLoad() throws Exception {
		wait.forElementToBeVisible(getSandwichMenu().expandButton);
	}

	public boolean hasOpenDialog(){
		log.info("checking for any opened dialogs");
		try{
			wait.forElementToBeVisible(dialogContainer);
			if(weToDobject(dialogContainer).isVisible()){
				return true;
			}
		}
		catch (Exception e){}
		return false;
	}

}
