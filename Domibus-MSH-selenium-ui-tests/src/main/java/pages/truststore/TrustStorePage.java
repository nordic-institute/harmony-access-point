package pages.truststore;

import ddsl.dcomponents.grid.DGrid;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import ddsl.dcomponents.DomibusPage;
import utils.PROPERTIES;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class TrustStorePage extends DomibusPage {
	public TrustStorePage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, PROPERTIES.TIMEOUT), this);
	}

	@FindBy(css = "#errorLogTable")
	WebElement truststoreTable;

//	@FindBy(css = "#uploadbutton_id")
//	WebElement uploadButton;


	public DGrid grid() {
		return new DGrid(driver, truststoreTable);
	}
}
