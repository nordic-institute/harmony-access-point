package pages.users;

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
 * @version 4.1
 */


public class UsersGrid extends DGrid {
	public UsersGrid(WebDriver driver, WebElement container) {
		super(driver, container);
	}

	public boolean isDeleted(String username) throws Exception {
		int index = scrollTo("Username", username);
		WebElement row = gridRows.get(index);
		int colIndex = getColumnNames().indexOf("Username");
		WebElement cell = row.findElements(cellSelector).get(colIndex);
		String classes = cell.findElement(By.tagName("span")).getAttribute("class");

		return classes.contains("user-deleted");
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

		if (columns.indexOf("Active") > 0) {
			info.put("Active", getCheckBoxStatus("Active", columns, cells));
		}
		if (columns.indexOf("Deleted") > 0) {
			info.put("Deleted", getCheckBoxStatus("Deleted", columns, cells));
		}

		return info;
	}

	private String getCheckBoxStatus(String checkboxName, List<String> columns, List<WebElement> cells) throws Exception {
		int chkIndex = columns.indexOf(checkboxName);
		return String.valueOf(new Checkbox(driver, cells.get(chkIndex).findElement(By.cssSelector("input[type=checkbox]"))).isChecked());
	}


}
