package pages.Alert;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.grid.GridControls;
import ddsl.dcomponents.grid.Pagination;
import ddsl.dcomponents.popups.Dialog;
import ddsl.dobjects.Checkbox;
import ddsl.dobjects.DButton;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.json.JSONArray;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AlertPage extends DomibusPage {
    public AlertPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
    }

    @FindBy(id = "alertsTable")
    public WebElement gridContainer;

    @FindBy(id = "alertsHeader_id")
    public WebElement alertsPageHeader;

    @FindBy(id = "alertsSaveButton")
    public WebElement alertSaveButton;

    @FindBy(id = "alertsCancelButton")
    public WebElement alertCancelButton;


    public DGrid grid() {
        return new DGrid(driver, gridContainer);
    }

    public AlertFilters filters() {
        return new AlertFilters(driver);
    }

    public GridControls gridControl() {
        return new GridControls(driver);
    }

    public Dialog confirmationPopup() {
        return new Dialog(driver);
    }

    public DButton getSaveButton() {
        return new DButton(driver, alertSaveButton);
    }

    public DButton getCancelButton() {
        return new DButton(driver, alertCancelButton);
    }

    public WebElement getCssForProcessedCheckBox(int rowNumber) {
        return driver.findElement(By.id("processed" + rowNumber + "_id"));
    }

    public void compareParamData(int j, int totalUsers, int totalMessages, JSONArray userList, JSONArray messageList) throws Exception {

        if (grid().getRowInfo(j).containsValue("USER_LOGIN_FAILURE")) {
            log.info("Check Alert type is USER_LOGIN_FAILURE");

            String user = grid().getRowSpecificColumnVal(j, "Parameters").split(",")[0];
            log.info("Extract userName from Parameters ");
            for (int k = 0; k < totalUsers; k++) {
                if (userList.getJSONObject(k).getString("userName").equals(user)) {
                    log.info("Shown user is from current domain");
                }

            }
        } else if (grid().getRowInfo(j).containsValue("MSG_STATUS_CHANGED")) {
            log.info("Check if Alert Type is MSG_STATUS_CHANGED");
            String messageId = grid().getRowSpecificColumnVal(j, "Parameters").split(",")[0];
            log.info("Extract message id from parameters field");

            for (int k = 0; k < totalMessages; k++) {
                if (messageList.equals(messageId)) {
                    log.info("Message belongs to current domain");
                }
            }
        } else if (grid().getRowInfo(j).containsValue("USER_ACCOUNT_DISABLED")) {
            log.info("Check if Alert type is USER_ACCOUNT_DISABLED");
            String user = grid().getRowSpecificColumnVal(j, "Parameters").split(",")[0];
            log.info("Extract userName from parameters page");
            for (int k = 0; k < totalUsers; k++) {
                if (userList.getJSONObject(k).getString("userName").equals(user)) {
                    log.info("Shown disabled user is from current domain");
                }

            }
        }
    }

    public List<String> getCSVSpecificColumnData(String filename, String columnName) throws Exception {

        Reader reader = Files.newBufferedReader(Paths.get(filename));
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader().withIgnoreHeaderCase()
                .withTrim());
        List<CSVRecord> records = csvParser.getRecords();
        List<String> columnValue= new ArrayList<>();

        for (int i = 0; i < records.size(); i++) {
            columnValue.add(records.get(i).get(columnName));
        }

        return columnValue;

    }
}

