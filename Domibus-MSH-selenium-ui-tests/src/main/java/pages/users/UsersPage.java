package pages.users;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.popups.Dialog;
import ddsl.dobjects.Checkbox;
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


public class UsersPage extends DomibusPage {
	@FindBy(id = "deleted_id")
	WebElement deletedChk;
	@FindBy(id = "pageGridId")
	private WebElement userTableContainer;

	@FindBy(id = "cancelButtonId")
	private WebElement cancelBtn;

	@FindBy(id = "saveButtonId")
	private WebElement saveBtn;

	@FindBy(id = "addButtonId")
	private WebElement newBtn;

	@FindBy(id = "editButtonId")
	private WebElement editBtn;

	@FindBy(id = "deleteButtonId")
	private WebElement deleteBtn;

	@FindBy(id = "searchbutton_id")
	private WebElement searchBtn;

	public UsersPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public DGrid grid() {
		return new DGrid(driver, userTableContainer);
	}

	public UsersGrid getUsersGrid() {
		return new UsersGrid(driver, userTableContainer);
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

	public DButton getSearchBtn() {
		return new DButton(driver, searchBtn);
	}

	public Checkbox getDeletedChk() {
		return new Checkbox(driver, deletedChk);
	}

	public boolean isLoaded() throws Exception {
		return (getCancelBtn().isPresent()
				&& getSaveBtn().isPresent()
				&& getNewBtn().isEnabled()
				&& getEditBtn().isPresent()
				&& getDeleteBtn().isPresent()
				&& grid().isPresent());
	}

	public void saveAndConfirm() throws Exception {
		log.info("saving");
		getSaveBtn().click();
		new Dialog(driver).confirm();
	}

	public void cancelAndConfirm() throws Exception {
		log.info("canceling");
		getCancelBtn().click();
		new Dialog(driver).confirm();
	}

	public void newUser(String user, String email, String role, String password, String confirmation) throws Exception {
		getNewBtn().click();
		UserModal modal = new UserModal(driver);
		modal.fillData(user, email, role, password, confirmation);
		modal.getOkBtn().click();
	}

	public void editUser(String user, String email, String role, String password, String confirmation) throws Exception {
		getEditBtn().click();

		UserModal modal = new UserModal(driver);
		modal.fillData("", email, role, password, confirmation);
		modal.getOkBtn().click();
	}

	public void includeDeletedUsers() throws Exception {
		log.info("including deleted users in search");
		if (!getDeletedChk().isChecked()) {
			getDeletedChk().click();
			getDeletedChk().click();
		}
		getSearchBtn().click();
	}

}
