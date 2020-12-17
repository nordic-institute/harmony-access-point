package pages.pmode.parties.modal;

import ddsl.dcomponents.DComponent;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.Checkbox;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

public class ProcessSection extends DComponent {
	@FindBy(css = "#processTable")
	protected WebElement processTable;
	//	By checkBox = By.cssSelector("input[type='checkbox']");
	By checkBox = By.tagName("mat-checkbox");

	public ProcessSection(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public DGrid getProcessTable() {
		return new DGrid(driver, processTable);
	}

	public void editForProcess(String processName, boolean initiator, boolean responder) throws Exception {
		DGrid prGrid = getProcessTable();
		int index = prGrid.scrollTo("Process", processName);
		if (index < 0) {
			throw new Exception("Process not found");
		}

		WebElement initiatorEl = prGrid.getRowElement(index).findElements(checkBox).get(0);
		WebElement responderEl = prGrid.getRowElement(index).findElements(checkBox).get(1);

		Checkbox initiatorCkb = weToCheckbox(initiatorEl);
		Checkbox responderCkb = weToCheckbox(responderEl);

		if (initiator) {
			initiatorCkb.check();
		} else {
			initiatorCkb.uncheck();
		}

		if (responder) {
			responderCkb.check();
		} else {
			responderCkb.uncheck();
		}
	}


}
