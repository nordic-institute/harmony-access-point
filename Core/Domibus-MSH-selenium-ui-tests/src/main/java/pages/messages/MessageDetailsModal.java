package pages.messages;

import ddsl.dcomponents.popups.InfoModal;
import ddsl.dobjects.DInput;
import org.apache.commons.lang3.StringUtils;
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
public class MessageDetailsModal extends InfoModal {
	@FindBy(css = "app-messagelog-details > mat-dialog-content input")
	List<WebElement> inputs;

	public MessageDetailsModal(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public String getValue(String fieldName) {
		for (WebElement input : inputs) {
			String curentFieldName = input.getAttribute("placeholder").trim();
			if (StringUtils.equalsIgnoreCase(curentFieldName, fieldName)) {
				return new DInput(driver, input).getText();
			}
		}
		return null;
	}

}
