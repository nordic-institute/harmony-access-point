package pages.msgFilter;

import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.Checkbox;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * @author Catalin Comanici
 * @description:
 * @since 4.1
 */
public class MessageFilterGrid extends DGrid {

	By persistedChkSelector = By.cssSelector("input[type=checkbox]");

	public MessageFilterGrid(WebDriver driver, WebElement container) {
		super(driver, container);
	}

	public Checkbox getPersisted(int rowIndex) throws Exception {
		int colIndex = getColumnNames().indexOf("Persisted");
		if (colIndex < 0) {
			return null;
		}
		if (rowIndex < 0 || rowIndex > gridRows.size()) {
			return null;
		}
		WebElement element = gridRows.get(rowIndex).findElement(persistedChkSelector);

		return new Checkbox(driver, element);
	}
}
