package pages.errorLog;

import ddsl.dobjects.DatePicker;
import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import ddsl.dobjects.DLink;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class ErrorLogPage extends DomibusPage {
	public ErrorLogPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}


	@FindBy(id = "errorLogTable")
	private WebElement errorLogTableContainer;

	ErrFilters filters = new ErrFilters(driver);


	public ErrFilters filters() {
		return filters;
	}
	public DGrid grid() {
		return new DGrid(driver, errorLogTableContainer);
	}


}
