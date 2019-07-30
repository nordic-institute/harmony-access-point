package ddsl.dobjects;

import ddsl.dcomponents.DComponent;
import ddsl.dobjects.DInput;
import ddsl.dobjects.DObject;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.TestRunData;

import java.util.Date;
import java.util.List;


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

//------------ Header controls --------------------------------------------
	@FindBy(css = "div.md2-calendar-header-year")
	private WebElement headerY;
	@FindBy(css = "div.md2-calendar-header-date")
	private WebElement headerM;
	@FindBy(css = "div.md2-calendar-header-hours")
	private WebElement headerH;
	@FindBy(css = "div.md2-calendar-header-minutes")
	private WebElement headerMin;
//------------------------------------------------------------------------

	@FindBy(className = "md2-datepicker-value")
	private WebElement input;

	@FindBy(css = "span.md2-datepicker-arrow")
	private WebElement expandoBtn;

//-------------Prev/Next controls ----------------------------------------
	@FindBy(css = "div.md2-calendar-previous-button")
	private WebElement previousBtn;
	@FindBy(css = "div.md2-calendar-period-button")
	private WebElement currentValue;
	@FindBy(css = "div.md2-calendar-next-button")
	private WebElement nextBtn;
//------------------------------------------------------------------------

//-------------Calendar items selectors-----------------------------------
	@FindBy(css = "div.md2-calendar-body-cell-content")
	private List<WebElement> dayBtns;

	@FindBy(css = "div.md2-calendar-body-selected")
	private WebElement selectedDayBtn;

	@FindBy(css = "div.md2-clock-hours")
	private WebElement clockHourContainer;

	@FindBy(css = "div.md2-clock-minutes")
	private WebElement clockMinuteContainer;

	@FindBy(css = "div.md2-clock-hours div.md2-clock-cell")
	private List<WebElement> clockHourItems;

	@FindBy(css = "div.md2-clock-minutes div.md2-clock-cell")
	private List<WebElement> clockMinuteItems;




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

	public void selectDate(Date date) throws Exception {
		try {
			expandoBtn.click();
		}catch (Exception e){}
	}

	private void selectYear(int year) throws Exception{
		new DObject(driver, headerY).click();


	}



}
