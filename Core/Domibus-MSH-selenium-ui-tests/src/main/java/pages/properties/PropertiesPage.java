package pages.properties;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class PropertiesPage extends DomibusPage {

	@FindBy(css = "#pageGridId")
	WebElement propGrid;


	public PropertiesPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public DGrid grid() {
		return new DGrid(driver, propGrid);
	}

	public PropGrid propGrid() {
		return new PropGrid(driver, propGrid);
	}

	public PropFilters filters() {
		return new PropFilters(driver);
	}



}
