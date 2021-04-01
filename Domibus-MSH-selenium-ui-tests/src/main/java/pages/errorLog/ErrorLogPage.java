package pages.errorLog;

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


public class ErrorLogPage extends DomibusPage {
	ErrFilters filters = new ErrFilters(driver);
	@FindBy(id = "pageGridId")
	private WebElement errorLogTableContainer;
	@FindBy(css = "#totimestamp_id >div >button")
	public WebElement errorToClock;
	@FindBy(css = "#totimestamp_id")
	public WebElement errorTo;
	@FindBy(css = "#notifiedto_id div button")
	public WebElement notifiedToClock;
	@FindBy(css = "#notifiedto_id")
	public WebElement notifiedTo;

	public ErrorLogPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public ErrFilters filters() {
		return filters;
	}

	public DGrid grid() {
		return new DGrid(driver, errorLogTableContainer);
	}


}
