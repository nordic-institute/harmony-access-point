package pages.errorLog;

import ddsl.dcomponents.popups.InfoModal;
import ddsl.dobjects.DInput;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.HashMap;
import java.util.List;

/**
 * @author Catalin Comanici
 * @since 4.1.1
 */
public class ErrorModal extends InfoModal {
	@FindBy(css = "mat-dialog-container [placeholder=\"Signal Message Id\"]")
	WebElement signalMessIdInput;
	@FindBy(css = "mat-dialog-container [placeholder=\"AP Role\"]")
	WebElement apRoleInput;
	@FindBy(css = "mat-dialog-container [placeholder=\"Message Id\"]")
	WebElement messIdInput;
	@FindBy(css = "mat-dialog-container [placeholder=\"Error Code\"]")
	WebElement errCodeInput;
	@FindBy(css = "mat-dialog-container [placeholder=\"Error Detail\"]")
	WebElement errDetailInput;
	@FindBy(css = "mat-dialog-container [placeholder=\"Timestamp\"]")
	WebElement timestampInput;
	@FindBy(css = "mat-dialog-container [placeholder=\"Notified\"]")
	WebElement notifiedInput;

	public ErrorModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public HashMap<String, String> getListedInfo() throws Exception {
		List<WebElement> inputs = driver.findElements(By.cssSelector("mat-dialog-container input"));
		HashMap<String, String> info = new HashMap<>();

		for (WebElement input : inputs) {
			DInput x = weToDInput(input);
			info.put(x.getAttribute("placeholder"), x.getText());
		}

		return info;
	}


}
