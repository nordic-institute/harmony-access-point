package pages.jms;

import ddsl.dcomponents.DomibusPage;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DInput;
import ddsl.dobjects.DatePicker;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class JMSFilters extends DomibusPage {
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

	public JMSFilters(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public void clickSearch() throws Exception {
		log.info("clicking search");
		weToDButton(jmsSearchButton).click();
	}

	public JMSSelect getJmsQueueSelect() {
		return new JMSSelect(driver, jmsQueueSelect);
	}

	public DInput getJmsSelectorInput() {
		return new DInput(driver, jmsSelectorInput);
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

	public boolean isLoaded() throws Exception {
		return (getJmsQueueSelect().isDisplayed()
				&& getJmsTypeInput().isEnabled()
				&& getJmsSearchButton().isEnabled()
				&& getJmsSelectorInput().isEnabled()
		);
	}


}
