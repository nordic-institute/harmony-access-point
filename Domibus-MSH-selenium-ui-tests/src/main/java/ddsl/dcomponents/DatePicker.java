package ddsl.dcomponents;

import ddsl.dobjects.DInput;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.TestRunData;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class DatePicker extends DComponent {
	public DatePicker(WebDriver driver, WebElement container) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(container, data.getTIMEOUT()), this);
	}

	private final String dateFormat = "dd/MM/yyyy HH:mm";


	@FindBy(className = "md2-datepicker-value")
	private WebElement input;

	public void selectDate(String date) throws Exception {
		log.info("inputting date... " + date);

		DInput pickerInput = new DInput(driver, input);
		pickerInput.fill(date);
	}

	public String getSelectedDate(){
		return new DInput(driver, input).getText().trim();
	}

	public void clearSelectedDate() throws Exception{
		new DInput(driver, input).clear();
	}
}
