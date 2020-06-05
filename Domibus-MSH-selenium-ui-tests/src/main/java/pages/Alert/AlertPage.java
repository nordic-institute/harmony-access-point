package pages.Alert;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

public class AlertPage extends DomibusPage {
	@FindBy(id = "pageGridId")
	public WebElement gridContainer;
	@FindBy(id = "alertsHeader_id")
	public WebElement alertsPageHeader;

	public AlertPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public DGrid grid() {
		return new DGrid(driver, gridContainer);
	}

	public AlertFilters filters() {
		return new AlertFilters(driver);
	}


}
