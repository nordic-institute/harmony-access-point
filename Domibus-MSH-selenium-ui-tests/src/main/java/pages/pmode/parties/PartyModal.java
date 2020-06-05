package pages.pmode.parties;

import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.popups.EditModal;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.List;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class PartyModal extends EditModal {
	@FindBy(css = "#name_id_detail")
	protected WebElement nameInput;
	@FindBy(css = "#endPoint_id_detail")
	protected WebElement endpointInput;
	@FindBy(css = "#subjectName_id")
	protected WebElement certSubjectNameInput;
	@FindBy(css = "#validityFrom_id")
	protected WebElement certValidFromInput;
	@FindBy(css = "#validityTo_id")
	protected WebElement certValidToInput;
	@FindBy(css = "#issuer_id")
	protected WebElement certIssuerInput;
	@FindBy(css = "#fingerPrint_id")
	protected WebElement certFingerPrintInput;
	@FindBy(css = "mat-dialog-container div:nth-child(2) > mat-card > mat-card-content > div > label")
	protected WebElement importButton;
	@FindBy(css = "#identifierTable")
	protected WebElement identifierTable;
	@FindBy(css = "mat-dialog-content div:nth-child(3) button:nth-child(1)")
	protected WebElement newIdentifierButton;
	@FindBy(css = "mat-dialog-content div:nth-child(3) button:nth-child(2)")
	protected WebElement editIdentifierButton;
	@FindBy(css = "mat-dialog-content div:nth-child(3) button:nth-child(3)")
	protected WebElement delIdentifierButton;
	@FindBy(css = "#processTable")
	protected WebElement processTable;
	@FindBy(css = "input[type='checkbox']")
	protected List<WebElement> inputCheckboxes;
	@FindBy(xpath = ".//*[@class='mat-dialog-title'][starts-with(@id,\"mat-dialog-title-\")]")
	protected WebElement partyHeader;

	public PartyModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);

		wait.forElementToBeEnabled(nameInput);
	}

	public DInput getNameInput() {
		return new DInput(driver, nameInput);
	}

	public DInput getEndpointInput() {
		return new DInput(driver, endpointInput);
	}

	public DInput getCertSubjectNameInput() {
		return new DInput(driver, certSubjectNameInput);
	}

	public DInput getCertValidFromInput() {
		return new DInput(driver, certValidFromInput);
	}

	public DInput getCertValidToInput() {
		return new DInput(driver, certValidToInput);
	}

	public DInput getCertIssuerInput() {
		return new DInput(driver, certIssuerInput);
	}

	public DInput getCertFingerPrintInput() {
		return new DInput(driver, certFingerPrintInput);
	}

	public DButton getImportButton() {
		return new DButton(driver, importButton);
	}

	public DGrid getIdentifierTable() {
		return new DGrid(driver, identifierTable);
	}

	public DButton getNewIdentifierButton() {
		return new DButton(driver, newIdentifierButton);
	}

	public DButton getEditIdentifierButton() {
		return new DButton(driver, editIdentifierButton);
	}

	public DButton getDelIdentifierButton() {
		return new DButton(driver, delIdentifierButton);
	}

	public DGrid getProcessTable() {
		return new DGrid(driver, processTable);
	}

	public void fillNewPartyForm(String name, String endpoint, String partyId) throws Exception {
		getNameInput().fill(name);
		getEndpointInput().fill(endpoint);
		getNewIdentifierButton().click();

		PartyIdentifierModal pimodal = new PartyIdentifierModal(driver);
		pimodal.fillFields(partyId);
		pimodal.clickOK();
	}

	public void clickIRCheckboxes() throws Exception {
		wait.forElementToBeVisible(partyHeader);
		JavascriptExecutor js = (JavascriptExecutor) driver;
		log.info("Scroll horizontally");
		js.executeScript("window.scrollBy(0,1000)");
		List<WebElement> checkboxes = driver.findElements(By.cssSelector("datatable-body-cell >div >mat-checkbox"));
		WebElement headerElement = driver.findElement(By.xpath(getXpathOfFieldHeader("Processes")));
		wait.forElementToBeVisible(headerElement);

		if (checkboxes.size() > 0) {
			log.info("Click on initiator checkbox");
			checkboxes.get(0).click();
			WebElement responderCheckbox = checkboxes.get(checkboxes.size() - 1);
			log.info("Click on Responder checkbox");
			responderCheckbox.click();
		}
		log.info("Click on Ok button");
		getOkBtn().click();
	}

	public Boolean getCheckboxStatus(String fieldName) {
		wait.forElementToBeVisible(partyHeader);
		JavascriptExecutor js = (JavascriptExecutor) driver;
		log.info("Scroll horizontally");
		js.executeScript("window.scrollBy(0,1000)");
		List<WebElement> checkboxes = driver.findElements(By.cssSelector("datatable-body-cell >div >mat-checkbox"));
		if (fieldName.equals("Initiator")) {
			log.info("Initiator checkbox selection status : " + checkboxes.get(0).isSelected());
			return false;
		} else if (fieldName.equals("Responder")) {
			log.info("Responder checkbox selection status : " + checkboxes.get(checkboxes.size() - 1).isSelected());
			return false;
		} else {
			return true;
		}
	}

	public String getXpathOfFieldHeader(String fieldName) {
		return ".//mat-card-title[contains(text()," + fieldName + ")]";
	}
}


