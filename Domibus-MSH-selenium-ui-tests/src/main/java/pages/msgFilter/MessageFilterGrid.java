package pages.msgFilter;

import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.Checkbox;
import ddsl.dobjects.DButton;
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
	
	By rowActionUpSelector = By.cssSelector("[tooltip=\"Move Up\"]");
	By rowActionDownSelector = By.cssSelector("[tooltip=\"Move Down\"]");
	By rowActionEditSelector = By.cssSelector("[tooltip=\"Edit\"]");
	By rowActionDeleteSelector = By.cssSelector("[tooltip=\"Delete\"]");

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
	
	
	public void rowMoveUp(int index) throws Exception {
		pressActionButton(index,"Move Up");
	}
	public void rowMoveDown(int index) throws Exception {
		pressActionButton(index,"Move Down");
	}
	public void rowEdit(int index) throws Exception {
		pressActionButton(index,"Edit");
	}
	public void rowDelete(int index) throws Exception {
		pressActionButton(index,"Delete");
	}
	
	
	public void pressActionButton(int index, String actionName) throws Exception {
		log.debug("index =" + index);
		log.debug("action =" + actionName);
		
		WebElement row = gridRows.get(index);
		By selector;
		
		log.info("identifying proper selector for action");
		switch (actionName){
			case "Move Up":
				selector = rowActionUpSelector;
				break;
			case "Move Down":
				selector = rowActionDownSelector;
				break;
			case "Edit":
				selector = rowActionEditSelector;
				break;
			case "Delete":
				selector = rowActionDeleteSelector;
				break;
			default:
				throw new Exception("Action name is invalid");
		}
		log.info("action selector identified and will be performed");
		weToDButton(row.findElement(selector)).click();
		wait.forXMillis(500);
	}
	
}
