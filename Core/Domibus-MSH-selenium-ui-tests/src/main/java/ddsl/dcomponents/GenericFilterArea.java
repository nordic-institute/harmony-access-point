package ddsl.dcomponents;

import ddsl.dobjects.Checkbox;
import ddsl.dobjects.DInput;
import ddsl.dobjects.DatePicker;
import ddsl.dobjects.Select;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.*;

public class GenericFilterArea extends FilterArea{

	@FindBy(css = "input:not(input.md2-datepicker-value, input.mat-checkbox-input")
	List<WebElement> inputs;

	@FindBy(tagName = "mat-checkbox")
	List<WebElement> checkBoxes;

	@FindBy(tagName = "md2-datepicker")
	List<WebElement> datePickers;

	@FindBy(tagName = "mat-select")
	List<WebElement> selects;

	WebElement container = null;


	public GenericFilterArea(WebDriver driver) {
		super(driver);

		try {
			this.container = driver.findElement(By.name("filterForm"));
			PageFactory.initElements(new AjaxElementLocatorFactory(container, data.getTIMEOUT()), this);
		} catch (Exception e) {	}
	}

	public boolean isPresent(){
		return null != container;
	}

	public List<WebElement> getInputs() {
		return inputs;
	}

	public List<WebElement> getCheckBoxes() {
		return checkBoxes;
	}

	public List<WebElement> getDatePickers() {
		return datePickers;
	}

	public List<WebElement> getSelects() {
		return selects;
	}

	public WebElement getContainer() {
		return container;
	}

	public void setValueForInputs(String value) throws Exception {
		for (WebElement input : inputs) {
			new DInput(driver, input).fill(value);
		}
	}

	public void setStateCheckBoxes(Boolean checked) throws Exception {
		for (WebElement checkBox : checkBoxes) {
			new Checkbox(driver, checkBox).set(checked);
		}
	}

	public void setValueForDatePickers(Date date) throws Exception {
		for (WebElement datePicker : datePickers) {
			new DatePicker(driver, datePicker).selectDate(date);
		}
	}

	public void setSelectsRndValues() throws Exception {
		for (WebElement select : selects) {
			Select dselect = new Select(driver, select);

			String selectedVal = dselect.getSelectedValue();
			List<String> values = dselect.getOptionsTexts();

			for (String value : values) {
				if(!StringUtils.equalsIgnoreCase(value, selectedVal) && StringUtils.isNotEmpty(value)){
					dselect.selectOptionByText(value);
					break;
				}
			}
		}
	}

	public HashMap<String, String> getValueFromInputs() throws Exception {
		HashMap<String, String> vals = new HashMap<>();
		for (WebElement input : inputs) {
			DInput dinput = new DInput(driver, input);
			vals.put(dinput.getAttribute("placeholder"), dinput.getText());
		}
		return vals;
	}

	public HashMap<String, Boolean> getValueFromCheckboxes() throws Exception {
		HashMap<String, Boolean> vals = new HashMap<>();
		for (WebElement checkBox : checkBoxes) {
			Checkbox chk = new Checkbox(driver, checkBox);
			vals.put(chk.getAttribute("name"), chk.isChecked());
		}
		return vals;
	}

	public HashMap<String, String> getValueFromDatePickers() throws Exception {
		HashMap<String, String> vals = new HashMap<>();
		for (WebElement datp : datePickers) {
			DatePicker datePicker = new DatePicker(driver, datp);
			vals.put(datp.getAttribute("name"), datePicker.getSelectedDate());
		}
		return vals;
	}

	public HashMap<String, String> getValueFromSelects() throws Exception {
		HashMap<String, String> vals = new HashMap<>();
		for (WebElement select : selects) {
			Select dselect = new Select(driver, select);
			vals.put(select.getAttribute("name"), dselect.getSelectedValue());
		}
		return vals;
	}

	public HashMap<String, String> gellAllListedValues() throws Exception {
		HashMap<String, String> vals = new HashMap<>();
		vals.putAll(getValueFromInputs());
		vals.putAll(getValueFromDatePickers());
		vals.putAll(getValueFromSelects());

		HashMap<String, Boolean> chkVals = new HashMap<>();
		for (Map.Entry<String, Boolean> entry : chkVals.entrySet()) {
			vals.put(entry.getKey(), entry.getValue().toString());
		}
		return vals;
	}



}


