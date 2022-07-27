package pages.pmode.current;

import ddsl.dcomponents.DomibusPage;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.Gen;




public class PModeCurrentPage extends DomibusPage {
	@FindBy(id = "pmodetextarea_id")
	WebElement textArea;
	@FindBy(id = "cancelButtonId")
	WebElement cancelBtn;
	@FindBy(id = "saveButtonId")
	WebElement saveBtn;
	@FindBy(id = "uploadButtonId")
	WebElement uploadBtn;
	@FindBy(id = "downloadButtonId")
	WebElement downloadBtn;
	@FindBy(css = ".pModeInfo >span")
	WebElement infoTxt;
	@FindBy(id = "pmodetextarea_id")
	WebElement CurrentPmodeXml;

	public PModeCurrentPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public DInput getTextArea() {
		return new DInput(driver, textArea);
	}


	public DButton getCancelBtn() {
		return new DButton(driver, cancelBtn);
	}

	public DButton getSaveBtn() {
		return new DButton(driver, saveBtn);
	}

	public DButton getUploadBtn() {
		return new DButton(driver, uploadBtn);
	}

	public DButton getDownloadBtn() {
		return new DButton(driver, downloadBtn);
	}

	public void getPmodeInfoText() {
		log.debug("Check if Pmode is uploaded or not ");
		wait.forElementToBeVisible(infoTxt);
		if (!infoTxt.isDisplayed()) {
			log.debug("Pmode is already uploaded");
		}
		log.debug("Pmode status on pmode current page : " + infoTxt.getText().trim());
	}

	public void saveAndConfirm(String description) throws Exception {

		getSaveBtn().click();

		PModeCofirmationModal modal = new PModeCofirmationModal(driver);

		if (StringUtils.isEmpty(description)) {
			description = Gen.rndStr(10);
		}

		modal.getDescriptionTextArea().isEnabled();
		modal.descriptionTextArea.sendKeys(description);

		modal.clickOK();
	}

	public void modifyListedPmode(String newPmode) throws Exception {
		getTextArea().fill(newPmode);

		log.info("saving");
		saveAndConfirm(Gen.randomAlphaNumeric(20));
	}


}


