package pages.Alert;

import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;

public class AlertsGrid extends DGrid {
	
	public AlertsGrid(WebDriver driver, WebElement container) {
		super(driver, container);
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
		
		if (columns.indexOf("Processed") > 0) {
			info.put("Processed", getCheckBoxStatus("Processed", columns, cells));
		}
		info.put("Deleted", "false");
		
		// bug already reported for extra column alert description
//		info.put("Alert Description", "");
//		info.put("Attempts", info.get("Sent Attempts"));
//		info.remove("Sent Attempts");
		
		return info;
	}
	
	private String getCheckBoxStatus(String checkboxName, List<String> columns, List<WebElement> cells) throws Exception {
		int chkIndex = columns.indexOf(checkboxName);
		WebElement webElement = cells.get(chkIndex).findElement(By.cssSelector("input[type=checkbox]"));
		return String.valueOf(weToCheckbox(webElement).isChecked());
	}
	
	public void markAsProcessed(int index) throws Exception {
		List<String> columns = getColumnNames();
		List<WebElement> cells = gridRows.get(index).findElements(cellSelector);
		
		int chkIndex = columns.indexOf("Processed");
		WebElement webElement = cells.get(chkIndex).findElement(By.cssSelector("label > div"));
		weToCheckbox(webElement).check();
	}
}
