package pages.logging;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.Checkbox;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.List;

public class LoggingPage extends DomibusPage {

	public LoggingPage(WebDriver driver) {
		super(driver);
		log.debug("Change Password  page init");
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	@FindBy(id = "loggerName_id")
	private WebElement searchInputField;

	@FindBy(id = "showClasses_id")
	private WebElement showClassCheckbox;

	@FindBy(id = "searchbutton_id")
	private WebElement searchButton;

	@FindBy(id = "resetbutton_id")
	private WebElement resetButton;

	@FindBy(id = "pageGridId")
	private WebElement gridContainer;

	@FindBy(css = ".mat-button-toggle.mat-button-toggle-checked>label>div")
	public List<WebElement> selectedLoggerLevelValue;

	@FindBy(css = ".mat-button-toggle.mat-button-toggle>label>div")
	public List<WebElement> loggerLevelValue;

	@FindBy(css = ".datatable-body-cell.sort-active>div>span[title*='cxf']")
	public List<WebElement> sselectedLoggerLevelValue;


	public DInput getSearchInputField() {
		return new DInput(driver, searchInputField);
	}

	public DButton getSearchButton() {
		return new DButton(driver, searchButton);
	}

	public DButton getResetButton() {
		return new DButton(driver, resetButton);
	}

	public Checkbox getShowClassesCheckbox() {
		return new Checkbox(driver, showClassCheckbox);
	}

	public boolean isLoaded() throws Exception {
		return (getSearchInputField().isPresent()
				&& getSearchButton().isEnabled()
				&& getResetButton().isEnabled()
				&& !getShowClassesCheckbox().isChecked()
		);
	}


	public DGrid grid() {
		return new DGrid(driver, gridContainer);
	}

	public LoggingGrid loggingGrid() {
		return new LoggingGrid(driver, gridContainer);
	}

	public void search(String term) throws Exception {
		log.info("Searching for: " + term);

		wait.forAttributeNotEmpty(searchInputField, "value");
		wait.forXMillis(500);

		getSearchInputField().fill(term);

		log.info("Click on search button");
		getSearchButton().click();

		grid().waitForRowsToLoad();
	}

	public void setLoggingLevel(String level, String item) throws Exception {
		search(item);

		log.info(String.format("Setting log level to {} for {}", level, item));
		LoggingGrid grid = loggingGrid();
		int index = grid.scrollTo("Logger Name", item);
		if (index < 0) {
			throw new Exception("Item is not found, log level is not changed");
		}

		grid.setLoggLevel(index, level);


	}

}
