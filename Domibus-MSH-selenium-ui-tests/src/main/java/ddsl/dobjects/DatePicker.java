package ddsl.dobjects;

import ddsl.dcomponents.DComponent;
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
	//------------ Header controls --------------------------------------------
	@FindBy(css = "div.md2-calendar-header-year")
	private WebElement headerY;
	@FindBy(css = "div.md2-calendar-header-date")
	private WebElement headerM;
	@FindBy(css = "div.md2-calendar-header-hours")
	private WebElement headerH;
	@FindBy(css = "div.md2-calendar-header-minutes")
	private WebElement headerMin;
	@FindBy(className = "md2-datepicker-value")
	private WebElement input;
//------------------------------------------------------------------------
	@FindBy(css = "span.md2-datepicker-arrow")
	private WebElement expandoBtn;
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
		PageFactory.initElements(new AjaxElementLocatorFactory(container, data.getTIMEOUT()), this);
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
			weToDButton(expandoBtn).click();
			PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
		}
	}
	
	public void selectDate(String date) throws Exception {
		log.debug("inputting date... " + date);
		
		DInput pickerInput = new DInput(driver, input);
		pickerInput.fill(date);
	}
	
	public String getSelectedDate() {
		return new DInput(driver, input).getText().trim();
	}
	
	public void clearSelectedDate() throws Exception {
		new DInput(driver, input).clear();
	}
	
	public void selectDate(Date date) {
		try {
			expandoBtn.click();
		} catch (Exception e) {
		}
	}
	
	public void selectYear(int year) throws Exception {
		
		weToDobject(headerY).click();
		DButton prev = weToDButton(previousBtn);
		DButton next = weToDButton(nextBtn);
		DObject curVal = weToDobject(currentValue);
		
		int selectedYear = Integer.valueOf(curVal.getText());
		
		if (selectedYear == year) {
			return;
		}
		DButton toPush;
		if (selectedYear < year) {
			toPush = next;
		} else {
			toPush = prev;
		}
		while (selectedYear != year) {
			toPush.click();
			selectedYear = Integer.valueOf(curVal.getText());
		}
		
	}
	
	public void selectMonth(String month) {
	
	
	}
	
	
}
