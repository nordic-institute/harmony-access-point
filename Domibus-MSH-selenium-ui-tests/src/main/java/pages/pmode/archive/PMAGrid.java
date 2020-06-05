package pages.pmode.archive;

import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DButton;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.HashMap;

/**
 * @author Catalin Comanici
 * @since 4.1.2
 */
public class PMAGrid extends DGrid {
	public PMAGrid(WebDriver driver, WebElement container) {
		super(driver, container);
		PageFactory.initElements(new AjaxElementLocatorFactory(container, data.getTIMEOUT()), this);
	}

	public boolean isActionEnabledForRow(int rowNo, String actionName) throws Exception {
		DButton button = weToDButton(gridRows.get(rowNo).findElement(By.cssSelector("button[tooltip = " + actionName + "]")));
		return button.isEnabled();
	}

	public void clickAction(int rowNo, String actionName) throws Exception {
		DButton button = weToDButton(gridRows.get(rowNo).findElement(By.cssSelector("button[tooltip = " + actionName + "]")));
		button.click();
	}

	@Override
	public HashMap<String, String> getRowInfo(int rowNumber) throws Exception {
		HashMap<String, String> rowInfo = super.getRowInfo(rowNumber);
		if (rowInfo.get("Description").contains("[CURRENT]: ")) {
			String strippedDesc = rowInfo.get("Description").replace("[CURRENT]: ", "");
			rowInfo.put("Description", strippedDesc);
			rowInfo.put("Current", String.valueOf(true));
		} else {
			rowInfo.put("Current", String.valueOf(false));
		}
		return rowInfo;
	}
}
