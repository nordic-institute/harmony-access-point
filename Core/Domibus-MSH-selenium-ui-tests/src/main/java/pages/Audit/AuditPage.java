package pages.Audit;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;


public class AuditPage extends DomibusPage {

	public static final String Table_FieldLabel = "Table";
	public static final String User_FieldLabel = "User";
	public static final String Action_FieldLabel = "Action";
	@FindBy(id = "pageGridId")
	public WebElement gridContainer;

	public AuditPage(WebDriver driver) {
		super(driver);
		log.debug("Audit page init");
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);

	}

	public DGrid grid() {
		return new DGrid(driver, gridContainer);
	}

	public AuditSearchFilters getFilters() {
		return new AuditSearchFilters(driver);
	}

	public AuditFilters filters() {
		return new AuditFilters(driver);
	}

}


