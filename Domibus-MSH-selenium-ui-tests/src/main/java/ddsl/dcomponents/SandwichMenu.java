package ddsl.dcomponents;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class SandwichMenu extends DComponent {

	public SandwichMenu(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
		log.debug("sandwich menu init");
	}

	@FindBy(id = "settingsmenu_id")
	WebElement expandButton;

	@FindBy(id = "settingsmenu_expanded_id")
	WebElement menuContainer;

	@FindBy(id = "currentuser_id")
	WebElement currentuser;

	@FindBy(id = "changePassword_id")
	WebElement changePassLnk;

	@FindBy(id = "logout_id")
	WebElement logoutLnk;


	private boolean isMenuExpanded() {
		try {
			if (weToDobject(menuContainer).isVisible()) {
				return true;
			}
		} catch (Exception e) {	}
		return false;
	}

	private void expandMenu() throws Exception {
		clickVoidSpace();

		weToDButton(expandButton).click();
		wait.forElementToBeVisible(menuContainer);
	}


	public String getCurrentUserID() throws Exception {
		expandMenu();

		String currentUserId = "";
		try {
			currentUserId = weToDButton(currentuser).getText();
		} catch (Exception e) {	}

		return "";
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
		} catch (Exception e) {	}

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
