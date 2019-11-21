package ddsl.dcomponents;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class SandwichMenu extends DComponent {


	private WebDriverWait localWait = wait.defaultWait;

	public SandwichMenu(WebDriver driver) {
		super(driver);
		log.debug("sandwich menu init");
	}

	By expandButton = By.id("settingsmenu_id");

	By menuContainer = By.id("settingsmenu_expanded_id");

	By currentUserID = By.cssSelector("button[role=\"menuitem\"]:nth-of-type(1) span");

	By logoutLnk = By.id("logout_id");

	By changePassLnk =By.cssSelector(".mat-menu-item#changePassword_id");
	By changePassId =By.id("changePassword_id");



	public String getCurrentUserID() throws Exception{
		expandMenu();
		localWait.until(ExpectedConditions.presenceOfElementLocated(currentUserID));
		return driver.findElement(currentUserID).getText().trim();
	}

	private boolean isMenuExpanded() {
		try {
			driver.findElement(menuContainer);
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	private void expandMenu() throws Exception {
		clickVoidSpace();

		if (isMenuExpanded()) return;
		driver.findElement(expandButton).click();
		try {
			wait.defaultWait.until(ExpectedConditions.visibilityOfElementLocated(menuContainer));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void contractMenu() throws Exception {
		if (!isMenuExpanded()) return;
		clickVoidSpace();
	}

	public boolean isLoggedIn() throws Exception {
		expandMenu();
		String userIDStr = driver.findElement(currentUserID).getText();
		boolean toReturn = !StringUtils.equalsIgnoreCase(userIDStr, "Not logged in");
		log.debug("User login status is: " + toReturn);

		contractMenu();
		return toReturn;
	}

	public void logout() throws Exception {

		clickVoidSpace();
		wait.defaultWait.until(ExpectedConditions.invisibilityOfElementLocated(By.className("cdk-overlay-container")));

		expandMenu();
		log.debug("Logging out...");
		driver.findElement(logoutLnk).click();
		contractMenu();
		wait.defaultWait.until(ExpectedConditions.visibilityOfElementLocated(expandButton));
	}

	/**This method is implemented to open Change Password page from Sandwich menu*/

	public void openchangePassword() throws Exception {

		clickVoidSpace();
		log.debug("Expand Sandwich menu");
		expandMenu();
		log.debug("click on Change password link");
		driver.findElement(changePassId).click();
		wait.defaultWait.until(ExpectedConditions.visibilityOfElementLocated(expandButton));
	}
	/**This method is implemented to check presence of link in Sandwich menu*/
	public boolean isChangePassLnkPresent() throws Exception {
		expandMenu();
		String changePasswordLnk = driver.findElement(changePassLnk).getText();
		boolean toReturn = !StringUtils.equalsIgnoreCase(changePasswordLnk, "Change Password");
		log.debug("Availability of Change Password link is : " + toReturn);

		return toReturn;
	}

}
