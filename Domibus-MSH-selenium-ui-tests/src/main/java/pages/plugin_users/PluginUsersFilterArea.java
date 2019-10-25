package pages.plugin_users;

import ddsl.dcomponents.DComponent;
import ddsl.dcomponents.FilterArea;
import ddsl.dobjects.Select;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class PluginUsersFilterArea extends FilterArea {

	public PluginUsersFilterArea(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	@FindBy(css = "#authType_id")
	public WebElement authTypeSelectContainer;

	@FindBy(css = "#endPoint_id")
	public WebElement userRoleSelectContainer;

	@FindBy(css = "#process_id")
	public WebElement originalUserInput;

	@FindBy(css = "#partyID_id")
	public WebElement usernameInput;

	public void search(String authType, String role, String origUser, String username) throws Exception {
		if (null != authType) getAuthTypeSelect().selectOptionByText(authType);
		if (null != role) getUserRoleSelect().selectOptionByText(role);
		if (null != origUser) getOriginalUserInput().fill(origUser);
		if (null != username) getUsernameInput().fill(username);
		clickSearch();
	}

	public Select getAuthTypeSelect() {
		return new Select(driver, authTypeSelectContainer);
	}

	public Select getUserRoleSelect() {
		return new Select(driver, userRoleSelectContainer);
	}

	public DInput getOriginalUserInput() {
		return new DInput(driver, originalUserInput);
	}

	public DInput getUsernameInput() {
		return new DInput(driver, usernameInput);
	}

}