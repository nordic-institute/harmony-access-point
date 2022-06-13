package pages.jms;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DButton;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class JMSMonitoringPage extends DomibusPage {
	@FindBy(css = "#pageGridId")
	public WebElement messagesTableGrid;
	@FindBy(css = "#cancelButtonId")
	public WebElement cancelButton;
	@FindBy(css = "#saveButtonId")
	public WebElement saveButton;
	@FindBy(css = "#jmsMoveButton")
	public WebElement moveButton;
	@FindBy(css = "#deleteButtonId")
	public WebElement deleteButton;

	public JMSMonitoringPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public DGrid grid() {
		return new DGrid(driver, messagesTableGrid);
	}

	public JMSFilters filters() {
		return new JMSFilters(driver);
	}

	public DButton getCancelButton() {
		return new DButton(driver, cancelButton);
	}

	public DButton getSaveButton() {
		return new DButton(driver, saveButton);
	}

	public DButton getMoveButton() {
		return new DButton(driver, moveButton);
	}

	public DButton getDeleteButton() {
		return new DButton(driver, deleteButton);
	}

	public boolean isLoaded() throws Exception {
		return (grid().isPresent()
				&& filters().isLoaded()
				&& getCancelButton().isPresent()
				&& getSaveButton().isPresent()
				&& getMoveButton().isPresent()
				&& getDeleteButton().isPresent()
		);
	}

}
