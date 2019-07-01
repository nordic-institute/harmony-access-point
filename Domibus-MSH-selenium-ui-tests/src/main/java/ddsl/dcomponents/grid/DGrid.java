package ddsl.dcomponents.grid;

import ddsl.dcomponents.DComponent;
import ddsl.dcomponents.popups.InfoModal;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DObject;
import jdk.nashorn.internal.runtime.ScriptObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.testng.asserts.SoftAssert;
import utils.TestRunData;
import utils.TestUtils;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;


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

	public GridControls getGridCtrl() {
		return new GridControls(driver);
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

	public ArrayList<String> getColumnNames() throws Exception {
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
		if (index < 0) {
			throw new Exception("Cannot select row because it doesn't seem to be in grid");
		}
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
			if (column.getText().equalsIgnoreCase(columnName)) {
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

	public int getSelectedRowIndex() throws Exception {
		for (int i = 0; i < gridRows.size(); i++) {
			if (new DObject(driver, gridRows.get(i)).getAttribute("class").contains("active")) {
				return i;
			}
		}
		return -1;
	}

	public boolean columnsVsCheckboxes() throws Exception {

		HashMap<String, Boolean> columnStatus = getGridCtrl().getAllCheckboxStatuses();
		ArrayList<String> visibleColumns = getColumnNames();

		List<String> checkedColumns = new ArrayList<>();
		for (String k : columnStatus.keySet()) {
			if (columnStatus.get(k) == true) {
				checkedColumns.add(k);
			}
		}

		if (visibleColumns.size() != checkedColumns.size()) {
			return false;
		}

		Collections.sort(visibleColumns);
		Collections.sort(checkedColumns);

		for (int i = 0; i < visibleColumns.size(); i++) {
			if (!visibleColumns.get(i).equalsIgnoreCase(checkedColumns.get(i))) {
				return false;
			}
		}

		return true;
	}

	public List<String> getValuesOnColumn(String columnName) throws Exception {
		List<HashMap<String, String>> allInfo = getAllRowInfo();
		List<String> values = new ArrayList<>();

		for (int i = 0; i < allInfo.size(); i++) {
			String val = allInfo.get(i).get(columnName);
			if (null != val) {
				values.add(val);
			}
		}
		return values;
	}




	public void assertControls(SoftAssert soft)throws Exception{


		getGridCtrl().showCtrls();
		List<String> chkOptions = new ArrayList<>();
		chkOptions.addAll(getGridCtrl().getAllCheckboxStatuses().keySet());

		checkShowLink(soft);
		checkHideLink(soft);
		checkModifyVisibleColumns(soft, chkOptions);
		checkAllLink(soft, chkOptions);
		checkNoneLink(soft);
		checkChangeNumberOfRows(soft);
	}

	private void checkShowLink(SoftAssert soft) throws Exception{
		//-----------Show
		getGridCtrl().showCtrls();
		soft.assertTrue(columnsVsCheckboxes(), "Columns and checkboxes are in sync");

	}
	private void checkHideLink(SoftAssert soft) throws Exception{
		//-----------Hide
		getGridCtrl().hideCtrls();
		soft.assertTrue(!getGridCtrl().areCheckboxesVisible(), "Hide Columns hides checkboxes");
	}
	private void checkModifyVisibleColumns(SoftAssert soft, List<String> chkOptions) throws Exception{
		//-----------Show - Modify - Hide
		for (String colName : chkOptions) {
			getGridCtrl().showCtrls();
			getGridCtrl().checkBoxWithLabel(colName);
			soft.assertTrue(columnsVsCheckboxes());

			getGridCtrl().uncheckBoxWithLabel(colName);
			soft.assertTrue(columnsVsCheckboxes());
		}
	}
	private void checkAllLink(SoftAssert soft, List<String> chkOptions) throws Exception{
		//-----------All link
		getGridCtrl().showCtrls();
		getGridCtrl().getAllLnk().click();
		getGridCtrl().hideCtrls();

		List<String> visibleColumns = getColumnNames();
		soft.assertTrue(CollectionUtils.isEqualCollection(visibleColumns, chkOptions), "All the desired columns are visible");
	}
	private void checkNoneLink(SoftAssert soft) throws Exception{
		//-----------None link
		getGridCtrl().showCtrls();
		getGridCtrl().getNoneLnk().click();
		getGridCtrl().hideCtrls();

		List<String> noneColumns = getColumnNames();
		soft.assertTrue(noneColumns.size() == 0, "All the desired columns are visible");

	}
	private void checkChangeNumberOfRows(SoftAssert soft) throws Exception{
		//----------Rows
		getGridCtrl().showCtrls();
		getGridCtrl().getAllLnk().click();

		int rows = getPagination().getTotalItems();
		getPagination().getPageSizeSelect().selectOptionByText("25");
		waitForRowsToLoad();

		soft.assertTrue(getPagination().getActivePage() == 1, "pagination is reset to 1 after changing number of items per page");

		if(rows > 10){
			soft.assertTrue(getRowsNo() > 10, "Number of rows is bigger than 10");
			soft.assertTrue(getRowsNo() <= 25, "Number of rows is less or equal to 25");
		}

		if(rows > 25){
			soft.assertTrue(getPagination().hasNextPage(), "If there are more than 25 items there are more than one pages");
		}
	}



	public void checkCSVAgainstGridInfo(String filename, SoftAssert soft) throws Exception {
		Reader reader = Files.newBufferedReader(Paths.get(filename));
		CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase()
				.withTrim());
		List<CSVRecord> records = csvParser.getRecords();


		List<HashMap<String, String>> gridInfo = getAllRowInfo();

		soft.assertTrue(CollectionUtils.isEqualCollection(gridHeaders, csvParser.getHeaderMap().keySet()), "Headers between grid and CSV file match");

		for (int i = 0; i < gridInfo.size(); i++) {
			HashMap<String, String> gridRecord = gridInfo.get(i);
			CSVRecord record = records.get(i);
			soft.assertTrue(csvRowVsGridRow(record, gridRecord), "compared rows " + i);
		}
	}

	public boolean csvRowVsGridRow(CSVRecord record, HashMap<String, String> gridRow) throws ParseException {

		for (String key : gridRow.keySet()) {
			if (key.equalsIgnoreCase("Actions")) {
				continue;
			}

			if (isUIDate(gridRow.get(key))) {
				if (!csvVsUIDate(record.get(key), gridRow.get(key))) {
					return false;
				}
			} else {
				if (!gridRow.get(key).equalsIgnoreCase(record.get(key))) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean csvVsUIDate(String csvDateStr, String uiDateStr) throws ParseException {
		Date csvDate = TestRunData.CSV_DATE_FORMAT.parse(csvDateStr);
		Date uiDate = TestRunData.UI_DATE_FORMAT.parse(uiDateStr);

		return csvDate.equals(uiDate);
	}

	public boolean isUIDate(String string) {
		Date uiDate = null;
		try {
			uiDate = TestRunData.UI_DATE_FORMAT.parse(string);
		} catch (ParseException e) {
			return false;
		}
		return true;
	}


}
