package pages.jms;

import ddsl.dcomponents.DatePicker;
import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.Select;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.TestRunData;

import java.util.List;

/**
 * @author Catalin Comanici
 * @description:
 * @since 4.1
 */
public class JMSFilters extends DomibusPage {
	public JMSFilters(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	@FindBy(css = "#jmsQueueSelector")
	WebElement jmsQueueSelect;

	@FindBy(css = "#jmsSelectorinput")
	WebElement jmsSelectorInput;

	@FindBy(css = "#jmsTypeInput")
	WebElement jmsTypeInput;

	@FindBy(css = "#jmsFromDatePicker")
	WebElement jmsFromDatePicker;
	@FindBy(css = "#jmsToDatePicker")
	WebElement jmsToDatePicker;

	@FindBy(css = "#jmsSearchButton")
	WebElement jmsSearchButton;


	public JMSSelect getJmsQueueSelect() {
		return new JMSSelect(driver ,jmsQueueSelect);
	}

	public DInput getJmsSelectorInput() {
		return new DInput(driver , jmsSelectorInput);
	}

	public DInput getJmsTypeInput() {
		return new DInput(driver, jmsTypeInput);
	}

	public DatePicker getJmsFromDatePicker() {
		return new DatePicker(driver, jmsFromDatePicker);
	}

	public DatePicker getJmsToDatePicker() {
		return new DatePicker(driver, jmsToDatePicker);
	}

	public DButton getJmsSearchButton() {
		return new DButton(driver, jmsSearchButton);
	}

	public boolean isLoaded() throws Exception{
		return (getJmsQueueSelect().isDisplayed()
				&& getJmsTypeInput().isEnabled()
				&& getJmsSearchButton().isEnabled()
				&& getJmsSelectorInput().isEnabled()
				);
	}


}
