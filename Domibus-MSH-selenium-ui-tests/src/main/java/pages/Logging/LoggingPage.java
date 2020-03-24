package pages.Logging;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dcomponents.grid.Pagination;
import ddsl.dobjects.Checkbox;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import ddsl.dobjects.DObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.List;

public class LoggingPage extends DomibusPage {

    public LoggingPage(WebDriver driver) {
        super(driver);
        log.debug("Change Password  page init");
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
    }

    @FindBy(id = "loggerName_id")
    private WebElement packageClassInputField;

    @FindBy(id = "showClasses_id")
    private WebElement showClassCheckbox;

    @FindBy(id = "searchbutton_id")
    private WebElement searchButton;

    @FindBy(id = "resetbutton_id")
    private WebElement resetButton;

    @FindBy(id = "loggingTable")
    private WebElement gridContainer;

    @FindBy(css = ".mat-button-toggle.mat-button-toggle-checked>label>div")
    public List<WebElement> selectedLoggerLevelValue;

    @FindBy(css = ".mat-button-toggle.mat-button-toggle>label>div")
    public List<WebElement> loggerLevelValue;

    @FindBy(css = ".datatable-body-cell.sort-active>div>span[title*='cxf']")
    public List<WebElement> sselectedLoggerLevelValue;


    public DInput getPackageClassInputField() {
        return new DInput(driver, packageClassInputField);
    }

    public DButton getSearchButton() {
        return new DButton(driver, searchButton);
    }

    public DButton getResetButton() {
        return new DButton(driver, resetButton);
    }

    public Checkbox getShowClassesCheckbox() {
        return new Checkbox(driver, showClassCheckbox);
    }

    public boolean isLoaded() throws Exception {
        return (getPackageClassInputField().isPresent()
                && getSearchButton().isEnabled()
                && getResetButton().isEnabled()
                && !getShowClassesCheckbox().isChecked()
        );
    }

    public Pagination getPagination() {
        return new Pagination(driver);
    }

    public DGrid grid() {
        return new DGrid(driver, gridContainer);
    }

    public String getloggerLevelvalue(int rownumber) throws Exception {
        if (rownumber > selectedLoggerLevelValue.size()) {
            log.info(("if row number is greater than row size for first page"));
            return "";
        }
        return new DObject(driver, selectedLoggerLevelValue.get(rownumber)).getText();
    }

    public void setLoggerLevel(String levelName) throws Exception {

        String currentLoggerLevel = new DObject(driver, selectedLoggerLevelValue.get(0)).getText();

        if (levelName.equals(currentLoggerLevel)) {
            log.info("Requested logger level is same as default one");
        } else if (levelName.equals("TRACE") || levelName.equals("DEBUG") || levelName.equals("WARN")
                || levelName.equals("ERROR") || levelName.equals("OFF") || levelName.equals("ALL") || levelName.equals("INFO")) {

            for (int i = 0; i < loggerLevelValue.size(); i++) {

                if (new DObject(driver, loggerLevelValue.get(i)).getText().equals(levelName)) {
                    new DObject(driver, loggerLevelValue.get(i)).click();
                }
            }

            log.info("Update logger level for row 0 : " + new DObject(driver, selectedLoggerLevelValue.get(0)).getText());

        } else {
            log.info("Some random data is passed as a Logger level");
        }

    }

    public List<WebElement> getCssWithName(String string) {
        return driver.findElements(By.cssSelector(".datatable-body-cell.sort-active>div>span[title*='" + string + "']"));

    }

    public Boolean getLoggerName(String searchString, int rowNumber) {
        List<WebElement> loggerEelement = getCssWithName(searchString);

        return loggerEelement.get(rowNumber).getText().contains(searchString);

    }

}
