package pages.pmode.parties.modal;

import ddsl.dcomponents.DComponent;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DButton;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

public class IdentifiersSection extends DComponent {
	public IdentifiersSection(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	@FindBy(css = "#identifierTable")
	protected WebElement identifierTable;

	@FindBy(css = "mat-dialog-content div:nth-child(3) button:nth-child(1)")
	protected WebElement newIdentifierButton;

	@FindBy(css = "mat-dialog-content div:nth-child(3) button:nth-child(2)")
	protected WebElement editIdentifierButton;

	@FindBy(css = "mat-dialog-content div:nth-child(3) button:nth-child(3)")
	protected WebElement delIdentifierButton;

	public DGrid getIdentifierTable() {
		return new DGrid(driver, identifierTable);
	}

	public DButton getNewIdentifierButton() {
		return weToDButton(newIdentifierButton);
	}

	public DButton getEditIdentifierButton() {
		return weToDButton(editIdentifierButton);
	}

	public DButton getDelIdentifierButton() {
		return weToDButton(delIdentifierButton);
	}
}


