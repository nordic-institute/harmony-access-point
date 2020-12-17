package pages.connectionMon;

import ddsl.dcomponents.DomibusPage;
import ddsl.enums.DMessages;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class ConnectionMonitoringPage extends DomibusPage {
	
	
	public ConnectionMonitoringPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}
	
	@FindBy(id = "pageGridId")
	private WebElement gridContainer;
	
	public ConMonGrid grid(){
		return new ConMonGrid(driver, gridContainer);
	}
	
	
	public Boolean invalidConfigurationState() {
		try {
			return getAlertArea().getAlertMessage().equalsIgnoreCase(DMessages.CONN_MON_PMODE_CONFIG_ERR);
		} catch (Exception e) {
		
		}
		return false;
	}
	
	public Boolean isLoaded() {
		return grid().isPresent();
	}
}
