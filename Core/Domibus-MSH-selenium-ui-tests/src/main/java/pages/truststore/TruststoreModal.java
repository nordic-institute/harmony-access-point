package pages.truststore;

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
 * @since 4.1
 */
public class TruststoreModal extends InfoModal {
	@FindBy(css = "input[placeholder=Name]")
	WebElement nameInput;
	@FindBy(css = "input[placeholder=Subject]")
	WebElement subjectInput;
	@FindBy(css = "input[placeholder=Issuer]")
	WebElement issuerInput;
	@FindBy(css = "input[placeholder=\"Valid from\"]")
	WebElement validFromInput;
	@FindBy(css = "input[placeholder=\"Valid until\"]")
	WebElement validToInput;
	public TruststoreModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public DInput getNameInput() {
		return new DInput(driver, nameInput);
	}

	public DInput getSubjectInput() {
		return new DInput(driver, subjectInput);
	}

	public DInput getIssuerInput() {
		return new DInput(driver, issuerInput);
	}

	public DInput getValidFromInput() {
		return new DInput(driver, validFromInput);
	}

	public DInput getValidToInput() {
		return new DInput(driver, validToInput);
	}

	public HashMap<String, String> getInfo() throws Exception {
		List<WebElement> inputs = driver.findElements(By.cssSelector("mat-dialog-container input"));
		HashMap<String, String> info = new HashMap<>();

		for (WebElement input : inputs) {
			DInput x = weToDInput(input);
			info.put(x.getAttribute("placeholder"), x.getText());
		}

		return info;
	}


}
