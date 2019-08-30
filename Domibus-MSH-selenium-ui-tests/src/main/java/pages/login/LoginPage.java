package pages.login;

import ddsl.dobjects.DInput;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import ddsl.dcomponents.DomibusPage;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.HashMap;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class LoginPage extends DomibusPage {

	public LoginPage(WebDriver driver) {
		super(driver);

		log.debug(".... init");

		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	@FindBy(id = "username_id")
	private WebElement username;

	@FindBy(id = "password_id")
	private WebElement password;

	@FindBy(id = "loginbutton_id")
	private WebElement loginBtn;

	public boolean isLoaded() {

		log.debug("check if is loaded");
		wait.forElementToBeVisible(username);
		if (!username.isEnabled()) {
			log.error("Could not find username input");
			return false;
		}
		if (!password.isEnabled()) {
			log.error("Could not find password input");
			return false;
		}
		if (!loginBtn.isDisplayed()) {
			log.error("Could not find login button");
			return false;
		}
		log.debug("Login page controls loaded");
		return true;
	}

	public <T extends DomibusPage> T login(String user, String pass, Class<T> expect) {
		log.debug("Login started");
		username.clear();
		username.sendKeys(user);
		password.clear();
		password.sendKeys(pass);
		loginBtn.click();
		wait.forElementToBeVisible(helpLnk);
		log.debug("Login action done");

		return PageFactory.initElements(driver, expect);
	}

	public void login(String user, String pass) {
		log.debug("Login started");
		username.clear();
		username.sendKeys(user);
		password.clear();
		password.sendKeys(pass);
		loginBtn.click();

		wait.defaultWait.until(ExpectedConditions.or(
				ExpectedConditions.visibilityOf(pageTitle),
				ExpectedConditions.visibilityOf(getAlertArea().alertMessage)
		));

		log.debug("Login action done");
	}

	public void login(String userRole) throws Exception {
		HashMap<String, String> user = data.getUser(userRole);
		log.debug("Login started");
		new DInput(driver, username).fill(user.get("username"));
		new DInput(driver, password).fill(user.get("pass"));
		loginBtn.click();
		wait.forElementToBeVisible(helpLnk);
		log.debug("Login action done");
	}

	public void login(HashMap<String, String> user) throws Exception {

		wait.longWaitforElementToBe(loginBtn);

		log.debug("Login started");
		new DInput(driver, username).fill(user.get("username"));
		new DInput(driver, password).fill(user.get("pass"));
		loginBtn.click();
		wait.forElementToBeVisible(helpLnk);
		log.debug("Login action done");
	}


}
