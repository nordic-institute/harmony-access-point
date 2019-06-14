package ddsl.dcomponents.grid;

import ddsl.dcomponents.DComponent;
import ddsl.dcomponents.popups.InfoModal;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.TestRunData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * @author Catalin Comanici
 * @version 4.1
 */
public class DGrid extends DComponent {

	public DGrid(WebDriver driver, WebElement container) {
		super(driver);
		log.info("init grid ...");
		PageFactory.initElements(new AjaxElementLocatorFactory(container, data.getTIMEOUT()), this);
	}

	@FindBy(css = "span.datatable-header-cell-wrapper > span")
	protected List<WebElement> gridHeaders;

	@FindBy(css = "datatable-row-wrapper > datatable-body-row")
	protected List<WebElement> gridRows;

	protected By cellSelector = By.tagName("datatable-body-cell");

	@FindBy(id = "saveascsvbutton_id")
	protected WebElement downloadCSVButton;

	@FindBy(tagName = "datatable-progress")
	protected WebElement progressBar;


	//	------------------------------------------------
	public Pagination getPagination() {
		return new Pagination(driver);
	}

	public DButton getDownloadCSVButton() {
		return new DButton(driver, downloadCSVButton);
	}

	public boolean isPresent() {
		boolean isPresent = false;
		try {
			isPresent = getColumnNames().size() > 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isPresent;
	}

	protected ArrayList<String> getColumnNames() throws Exception {
		ArrayList<String> columnNames = new ArrayList<>();
		for (int i = 0; i < gridHeaders.size(); i++) {
			columnNames.add(new DObject(driver, gridHeaders.get(i)).getText());
		}
		return columnNames;
	}

	public void selectRow(int rowNumber) throws Exception {
		log.info("selecting row with number ... " + rowNumber);
		if (rowNumber < gridRows.size()) {
			new DObject(driver, gridRows.get(rowNumber)).click();
			wait.forAttributeToContain(gridRows.get(rowNumber), "class", "active");
		}
	}

	public void doubleClickRow(int rowNumber) throws Exception {

		log.info("double clicking row ... " + rowNumber);
		if (rowNumber < 0) {
			throw new Exception("Row number too low " + rowNumber);
		}
		if (rowNumber >= gridRows.size()) {
			throw new Exception("Row number too high " + rowNumber);
		}

		Actions action = new Actions(driver);
		action.doubleClick(gridRows.get(rowNumber)).perform();
	}

	public int getRowsNo() {
		return gridRows.size();
	}

	public void waitForRowsToLoad() {
		try {
			wait.forElementToBe(progressBar);
			wait.forElementToBeGone(progressBar);
		} catch (Exception e) {

		}
	}

	public int getIndexOf(Integer columnIndex, String value) throws Exception {
		for (int i = 0; i < gridRows.size(); i++) {
			WebElement rowContainer = gridRows.get(i);
			String rowValue = new DObject(driver, rowContainer.findElements(cellSelector).get(columnIndex)).getText();
			if (rowValue.equalsIgnoreCase(value)) {
				return i;
			}
		}
		return -1;
	}

	public int scrollTo(String columnName, String value) throws Exception {
		ArrayList<String> columnNames = getColumnNames();
		if (!columnNames.contains(columnName)) {
			throw new Exception("Selected column name '" + columnName + "' is not visible in the present grid");
		}

		int columnIndex = -1;
		for (int i = 0; i < columnNames.size(); i++) {
			if (columnNames.get(i).equalsIgnoreCase(columnName)) {
				columnIndex = i;
			}
		}

		Pagination pagination = getPagination();
		pagination.skipToFirstPage();
		int index = getIndexOf(columnIndex, value);

		while (index < 0 && pagination.hasNextPage()) {
			pagination.goToNextPage();
			index = getIndexOf(columnIndex, value);
		}

		return index;
	}

	public void scrollToAndSelect(String columnName, String value) throws Exception {
		int index = scrollTo(columnName, value);
		if(index < 0){throw new Exception("Cannot select row because it doesn't seem to be in grid");}
		selectRow(index);
	}

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
		return info;
	}

	public HashMap<String, String> getRowInfo(String columnName, String value) throws Exception {
		int index = scrollTo(columnName, value);
		return getRowInfo(index);
	}

	public void sortBy(String columnName) throws Exception {
		for (int i = 0; i < gridHeaders.size(); i++) {
			DObject column = new DObject(driver, gridHeaders.get(i));
			if(column.getText().equalsIgnoreCase(columnName)){
				column.click();
				return;
			}
		}
		throw new Exception("Column name not present in the grid");
	}

	public void scrollToAndDoubleClick(String columnName, String value) throws Exception {
		int index = scrollTo(columnName, value);
		doubleClickRow(index);

//		necessary wait if the method is to remain generic
//		otherwise we need to know what modal is going to be opened so we know what to expect
		wait.forXMillis(1000);
	}

	public List<HashMap<String, String>> getAllRowInfo() throws Exception {
		List<HashMap<String, String>> allRowInfo = new ArrayList<>();

		Pagination pagination = getPagination();
		pagination.skipToFirstPage();

		do {
			for (int i = 0; i < getRowsNo(); i++) {
				allRowInfo.add(getRowInfo(i));
			}
			if (pagination.hasNextPage()) {
				pagination.goToNextPage();
			} else {
				break;
			}
		} while (true);
		return allRowInfo;
	}

	public int getSelectedRowIndex() throws Exception{
		for (int i = 0; i < gridRows.size(); i++) {
			if(new DObject(driver, gridRows.get(i)).getAttribute("class").contains("active")){
				return i;
			}
		}
		return -1;
	}

}
