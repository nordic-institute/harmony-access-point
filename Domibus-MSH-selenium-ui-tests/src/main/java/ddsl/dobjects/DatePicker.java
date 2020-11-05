package ddsl.dobjects;

import ddsl.dcomponents.DComponent;
import org.apache.commons.lang3.time.DateUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.Date;
import java.util.List;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class DatePicker extends DComponent {
	private final String dateFormat = "dd/MM/yyyy HH:mm";
	private final WebElement container;

	//------------ Header controls --------------------------------------------
	@FindBy(css = "div.md2-calendar-header-year")
	private WebElement headerY;
	@FindBy(css = "div.md2-calendar-header-date")
	private WebElement headerM;
	@FindBy(css = "div.md2-calendar-header-hours")
	private WebElement headerH;
	@FindBy(css = "div.md2-calendar-header-minutes")
	private WebElement headerMin;

	@FindBy(css = "div.md2-calendar-header")
	private WebElement header;

//------------------------------------------------------------------------

//	@FindBy(css = "span.md2-datepicker-arrow")
	private WebElement expandBtn;

//	@FindBy(className = "md2-datepicker-value")
	private WebElement input;

	//-------------Prev/Next controls ----------------------------------------
	@FindBy(css = "div.md2-calendar-previous-button")
	private WebElement previousBtn;
	@FindBy(css = "div.md2-calendar-period-button")
	private WebElement currentValue;
	@FindBy(css = "div.md2-calendar-next-button")
	private WebElement nextBtn;
	private By pickerBtnLct = By.cssSelector("div.md2-calendar-body-cell-content");
//------------------------------------------------------------------------
	
	//-------------Calendar items selectors-----------------------------------
//	@FindBy(css = "div.md2-calendar-body-cell-content")
//	private List<WebElement> pickerBtns;
	@FindBy(css = "div.md2-calendar-body-selected")
	private WebElement selectedPickerBtn;
	@FindBy(css = "div.md2-clock-hours")
	private WebElement clockHourContainer;
	@FindBy(css = "div.md2-clock-minutes")
	private WebElement clockMinuteContainer;
	@FindBy(css = "div.md2-clock-hours div.md2-clock-cell")
	private List<WebElement> clockHourItems;
	@FindBy(css = "div.md2-clock-minutes div.md2-clock-cell")
	private List<WebElement> clockMinuteItems;
	
	public DatePicker(WebDriver driver, WebElement container) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);

		this.container = container;
	}


	public void selectDate(Date date) {
		try {
			expandWidget();

//			String  currentListedDate = weToDobject(header).getText();
//			Date listedDate = DateUtils.parseDate(currentListedDate, "yyyy EEE, MMM dd HH:mm");
//
//			listedDate.
//			while (listedDate)



		} catch (Exception e) {
		}
	}

	private WebElement getInput(){
		input = container.findElement(By.cssSelector("md2-datepicker-value"));
		return input;
	}
	private WebElement getExpandBtn(){
		expandBtn = container.findElement(By.cssSelector("span.md2-datepicker-arrow"));
		return expandBtn;
	}


	private boolean isExpanded() {
		try {
			return weToDobject(currentValue).isVisible();
		} catch (Exception e) {
		}
		return false;
	}
	
	public void expandWidget() throws Exception {
		if (!isExpanded()) {
			weToDButton(getExpandBtn()).click();
		}
	}










	//	used don't change
	public void selectDate(String date) throws Exception {
		log.debug("inputting date... " + date);

		DInput pickerInput = new DInput(driver, getInput());
		pickerInput.fill(date);
	}
//	used don't change
	public String getSelectedDate() {
		return new DInput(driver, getInput()).getText().trim();
	}
//	used don't change
	public void clearSelectedDate() throws Exception {
		new DInput(driver, getInput()).clear();
	}

	
}
