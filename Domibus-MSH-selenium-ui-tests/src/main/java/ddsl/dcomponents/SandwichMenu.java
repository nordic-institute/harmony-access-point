package ddsl.dcomponents;

import org.apache.commons.lang3.StringUtils;
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


public class SandwichMenu extends DComponent {

	@FindBy(id = "settingsmenu_id")
	WebElement expandButton;
	@FindBy(id = "settingsmenu_expanded_id")
	WebElement menuContainer;
	//	@FindBy(css = "#currentuser_id span")
//	WebElement currentuser;
	By currentuser = By.cssSelector("#currentuser_id span");
	@FindBy(id = "changePassword_id")
	WebElement changePassLnk;
	@FindBy(id = "logout_id")
	WebElement logoutLnk;

	public SandwichMenu(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
		log.debug("sandwich menu init");
	}

	private boolean isMenuExpanded() {
		try {
			if (weToDobject(menuContainer).isVisible()) {
				return true;
			}
		} catch (Exception e) {
		}
		return false;
	}

	private void expandMenu() throws Exception {
		clickVoidSpace();

		weToDButton(expandButton).click();
		wait.forElementToBeVisible(menuContainer);
		
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}


	public String getCurrentUserID() throws Exception {
		expandMenu();
		wait.forXMillis(100);
		String currentUserId = "";
		try {
			currentUserId = wait.forElementToBeVisible(driver.findElement(currentuser)).getText().trim();
		} catch (Exception e) {
		}

		return currentUserId;
	}

	private void contractMenu() throws Exception {
		clickVoidSpace();
	}

	public boolean isLoggedIn() throws Exception {
		expandMenu();
		boolean toReturn = false;

		try {
//			String userIDStr = getCurrentUserID();
			toReturn = !StringUtils.containsIgnoreCase(weToDobject(menuContainer).getText(), "Not logged in");
			log.debug("User login status is: " + toReturn);
		} catch (Exception e) {
		}

		contractMenu();
		return toReturn;
	}

	public void logout() throws Exception {
		expandMenu();
		weToDButton(logoutLnk).click();
	}

	/**
	 * This method is implemented to open Change Password page from Sandwich menu
	 */

	public void openchangePassword() throws Exception {
		expandMenu();
		weToDButton(changePassLnk).click();
	}

	/**
	 * This method is implemented to check presence of link in Sandwich menu
	 */
	public boolean isChangePassLnkPresent() throws Exception {
		expandMenu();
		return weToDButton(changePassLnk).isVisible();
	}

}
