package pages.pmode.parties.modal;

import ddsl.dcomponents.DComponent;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

public class CertSection extends DComponent {
	public CertSection(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

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

	public DInput getCertSubjectNameInput() {
		return weToDInput(certSubjectNameInput);
	}

	public DInput getCertValidFromInput() {
		return weToDInput(certValidFromInput);
	}

	public DInput getCertValidToInput() {
		return weToDInput(certValidToInput);
	}

	public DInput getCertIssuerInput() {
		return weToDInput(certIssuerInput);
	}

	public DInput getCertFingerPrintInput() {
		return weToDInput(certFingerPrintInput);
	}

	public DButton getImportButton() {
		return weToDButton(importButton);
	}
}
