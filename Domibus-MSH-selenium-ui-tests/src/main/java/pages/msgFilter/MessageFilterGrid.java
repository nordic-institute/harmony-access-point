package pages.msgFilter;

import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.Checkbox;
import ddsl.dobjects.DObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;

/**
 * @author Catalin Comanici
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


	@Override
	public HashMap<String, String> getRowInfo(int rowNumber) throws Exception {
		if (rowNumber < 0) {
			throw new Exception("Row number too low " + rowNumber);
		}
		if (rowNumber > gridRows.size()) {
			throw new Exception("Row number too high " + rowNumber);
		}
		HashMap<String, String> info = new HashMap<>();

		List<String> columns = getColumnNames();
		List<WebElement> cells = gridRows.get(rowNumber).findElements(cellSelector);

		for (int i = 0; i < columns.size(); i++) {
			info.put(columns.get(i), new DObject(driver, cells.get(i)).getText());
		}

		String isPersisted = String.valueOf(getPersisted(rowNumber).isChecked());
		info.put("Persisted", isPersisted);

		return info;
	}
}
