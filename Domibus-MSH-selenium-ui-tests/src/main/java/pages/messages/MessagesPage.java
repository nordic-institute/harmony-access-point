package pages.messages;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DButton;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class MessagesPage extends DomibusPage {
	
	
	@FindBy(id = "pageGridId")
	public WebElement gridContainer;
	@FindBy(id = "downloadbutton_id")
	public WebElement downloadBtn;
	@FindBy(id = "resendbutton_id")
	public WebElement resendBtn;
	
	public MessagesPage(WebDriver driver) {
		super(driver);
		log.debug("Messages page init");
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
		
	}
	
	public DGrid grid() {
		return new DGrid(driver, gridContainer);
	}
	
	public DButton getDownloadButton() {
		return new DButton(driver, downloadBtn);
	}
	
	public DButton getResendButton() {
		return new DButton(driver, resendBtn);
	}
	
	public MessageFilterArea getFilters() {
		return new MessageFilterArea(driver);
	}
	
	public boolean isLoaded() {
		return (getDownloadButton().isPresent()
				&& getResendButton().isPresent()
				&& null != grid()
				&& null != getFilters());
	}
	
	public String getCssofRowSpecificActionIcon(int rowNumber, String iconName) {
		if (iconName.equals("Download")) {
			return "#downloadButtonRow" + rowNumber + "_id";
		}
		if (iconName.equals("Resend")) {
			return "#resendButtonRow" + rowNumber + "_id";
		}
		return "";
	}
	
	public Boolean getActionIconStatus(int rowNumber, String iconName) {
		WebElement iconElement = driver.findElement(By.cssSelector(getCssofRowSpecificActionIcon(rowNumber, iconName)));
		wait.forElementToBeVisible(iconElement);
		return iconElement.isEnabled();
	}
	
	
}
