package pages.pmode.current;

import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import ddsl.dcomponents.DomibusPage;
import utils.TestRunData;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class PModeCurrentPage extends DomibusPage {
	public PModeCurrentPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	@FindBy(id = "pmodetextarea_id")
	WebElement textArea;
	@FindBy(css = "table.group-action-button td > button:nth-child(1)")
	WebElement cancelBtn;
	@FindBy(css = "table.group-action-button td > button:nth-child(2)")
	WebElement saveBtn;
	@FindBy(css = "table.group-action-button td > button:nth-child(3)")
	WebElement uploadBtn;
	@FindBy(css = "table.group-action-button td > button:nth-child(4)")
	WebElement downloadBtn;
	@FindBy(css = ".pModeInfo >span")
	WebElement infoTxt;
	@FindBy(id="pmodetextarea_id")
	WebElement CurrentPmodeXml;



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


	}


