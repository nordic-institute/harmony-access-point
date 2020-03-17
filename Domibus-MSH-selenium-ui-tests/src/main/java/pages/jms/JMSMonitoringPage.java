package pages.jms;

import ddsl.dcomponents.DatePicker;
import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DButton;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.TestRunData;

import java.util.List;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class JMSMonitoringPage extends DomibusPage {
    public JMSMonitoringPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
    }

    @FindBy(css = "#errorLogTable")
    public WebElement messagesTableGrid;

    @FindBy(css = "#jmsCancelButton")
    public WebElement cancelButton;

    @FindBy(css = "#jmsSaveButton")
    public WebElement saveButton;

    @FindBy(css = "#jmsMoveButton")
    public WebElement moveButton;

    @FindBy(css = "#jmsDeleteButton")
    public WebElement deleteButton;

    @FindBy(css = "input[class=md2-datepicker-value]:nth-child(2)")
    public List<WebElement> receivedDateField;

    @FindBy(css = "div[class=selectionCriteria] span[class=mat-select-value-text]")
    public List<WebElement> sourceFieldQueueName;

    public DGrid grid() {
        return new DGrid(driver, messagesTableGrid);
    }

    public JMSFilters filters() {
        return new JMSFilters(driver);
    }

    public DButton getCancelButton() {
        return new DButton(driver, cancelButton);
    }

    public DButton getSaveButton() {
        return new DButton(driver, saveButton);
    }

    public DButton getMoveButton() {
        return new DButton(driver, moveButton);
    }

    public DButton getDeleteButton() {
        return new DButton(driver, deleteButton);
    }




    public boolean isLoaded() throws Exception {
        return (grid().isPresent()
                && filters().isLoaded()
                && getCancelButton().isPresent()
                && getSaveButton().isPresent()
                && getMoveButton().isPresent()
                && getDeleteButton().isPresent()
        );
    }

    public String getCountFromQueueName(String queueName) {
        if(queueName.lastIndexOf("(")<0){
            return null;
        } else {
            int startIndex = queueName.lastIndexOf("(");
            int endIndex = queueName.lastIndexOf(")");
            return queueName.substring(startIndex + 1, endIndex);
        }
    }



}
