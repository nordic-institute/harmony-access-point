package pages.pmode.parties.modal;

import ddsl.dcomponents.popups.EditModal;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

public class PPartyModal extends EditModal {
	public PPartyModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);

		wait.forElementToBeEnabled(nameInput);
	}

	@FindBy(css = "#name_id_detail")
	protected WebElement nameInput;

	@FindBy(css = "#endPoint_id_detail")
	protected WebElement endpointInput;

	public CertSection certSection = new CertSection(driver);
	public IdentifiersSection identifiersSection = new IdentifiersSection(driver);
	public ProcessSection processSection = new ProcessSection(driver);


}
