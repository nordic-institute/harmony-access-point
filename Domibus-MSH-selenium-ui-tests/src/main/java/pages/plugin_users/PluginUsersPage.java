package pages.plugin_users;

import ddsl.dcomponents.DomibusPage;
import ddsl.dobjects.DButton;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class PluginUsersPage extends DomibusPage {
	public PluginUsersFilterArea filters = new PluginUsersFilterArea(driver);
	@FindBy(css = "#saveButtonId")
	public WebElement saveBtn;
	@FindBy(css = "#pageGridId")
	private WebElement userGridContainer;

	@FindBy(css = "#cancelButtonId")
	private WebElement cancelBtn;
	@FindBy(css = "#addButtonId")
	private WebElement newBtn;
	@FindBy(css = "#editButtonId")
	private WebElement editBtn;
	@FindBy(css = "#deleteButtonId")
	private WebElement deleteBtn;

	public PluginUsersPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public PluginUsersFilterArea filters() {
		return new PluginUsersFilterArea(driver);
	}

	public DButton getCancelBtn() {
		return new DButton(driver, cancelBtn);
	}

	public DButton getSaveBtn() {
		return new DButton(driver, saveBtn);
	}

	public DButton getNewBtn() {
		return new DButton(driver, newBtn);
	}

	public DButton getEditBtn() {
		return new DButton(driver, editBtn);
	}

	public DButton getDeleteBtn() {
		return new DButton(driver, deleteBtn);
	}

	public PluginUserGrid grid() {
		return new PluginUserGrid(driver, userGridContainer);
	}

	public void newUser(String user, String role, String password, String confirmation) throws Exception {
		getNewBtn().click();

		PluginUserModal popup = new PluginUserModal(driver);
		popup.fillData(user, role, password, confirmation);
		popup.clickOK();
	}


	public boolean isLoaded() throws Exception {

		if (!getCancelBtn().isPresent()) {
			return false;
		}
		if (!getSaveBtn().isPresent()) {
			return false;
		}

		if (!getNewBtn().isEnabled()) {
			return false;
		}
		if (!getEditBtn().isPresent()) {
			return false;
		}
		if (!getDeleteBtn().isPresent()) {
			return false;
		}

		if (!userGridContainer.isDisplayed()) {
			return false;
		}

		return true;
	}
}
