package pages.properties;

import ddsl.dcomponents.grid.DGrid;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.HashMap;
import java.util.List;

public class PropGrid extends DGrid {

	public PropGrid(WebDriver driver, WebElement container) {
		super(driver, container);
	}

	private final String valKey = "Property Value";

	By rowInput = By.cssSelector("[id *= mat-input]");
	By rowSave = By.cssSelector("button:nth-child(1)");
	By rowUndo = By.cssSelector("button:nth-child(2)");


	@Override
	public HashMap<String, String> getRowInfo(int rowNumber) throws Exception {

		log.info("getting row info for row number " + rowNumber);

		HashMap<String, String> info = super.getRowInfo(rowNumber);

		if(info.containsKey(valKey)){
			WebElement valueElement = getRowElement(rowNumber).findElement(rowInput);
			String value = weToDInput(valueElement).getText();
			info.put("Property Value", value);
		}

		log.debug("got info " + info);
		return info;
	}

	public void setPropertyValue(String propName, String propValue) throws Exception {
		log.info("setting property " + propName + " to value " + propValue);
		int index = scrollTo("Property Name", propName);
		if (index<0){
			throw new Exception("Could not find property");
		}

		String currentValue = getPropRowValue(index);
		if(StringUtils.equalsIgnoreCase(currentValue, propValue)){
			return;
		}

		setPropRowValueAndSave(index, propValue);

	}

	public void pressSave(int rowNumber) throws Exception {
		if (!getColumnNames().contains(valKey)) return;

		WebElement saveElement = getRowElement(rowNumber).findElement(rowSave);
		weToDButton(saveElement).click();
		wait.forElementToBeDisabled(saveElement);
	}

	public void pressUndo(int rowNumber) throws Exception {
		if (!getColumnNames().contains(valKey)) return;

		WebElement undoElement = getRowElement(rowNumber).findElement(rowUndo);
		weToDButton(undoElement).click();
		wait.forElementToBeDisabled(undoElement);
	}

	public String getPropRowValue(int rowNumber) throws Exception {
		if (!getColumnNames().contains(valKey)){
			throw new Exception(valKey + " column is not visible");
		}

		WebElement inputElement = getRowElement(rowNumber).findElement(rowInput);
		return weToDInput(inputElement).getText();
	}

	public void setPropRowValue(int rowNumber, String value) throws Exception {
		if (!getColumnNames().contains(valKey)) return;

		WebElement inputElement = getRowElement(rowNumber).findElement(rowInput);
		weToDInput(inputElement).fill(value);
	}
	public void setPropRowValueAndSave(int rowNumber, String value) throws Exception {
		if (!getColumnNames().contains(valKey)) return;

		WebElement rowElement = getRowElement(rowNumber);
		WebElement inputElement = rowElement.findElement(rowInput);
		weToDInput(inputElement).fill(value);

		WebElement saveElem = rowElement.findElement(rowSave);

		weToDButton(saveElem).click();
		wait.forElementToBeDisabled(saveElem);
		wait.forXMillis(1000);
	}

	public void setPropRowValueAndRevert(int rowNumber, String value) throws Exception {
		if (!getColumnNames().contains(valKey)) return;

		WebElement rowElement = getRowElement(rowNumber);
		WebElement inputElement = rowElement.findElement(rowInput);
		weToDInput(inputElement).fill(value);

		WebElement undoElem = rowElement.findElement(rowUndo);

		wait.forElementToBeEnabled(undoElem);

		weToDButton(undoElem).click();
		wait.forElementToBeDisabled(undoElem);
		wait.forXMillis(1000);
	}


	public String getPropertyValue(String propName) throws Exception {
		log.info("getting value for property " + propName);
		int index = scrollTo("Property Name", propName);
		if (index<0){
			throw new Exception("Could not find property");
		}
		return getPropRowValue(index);
	}
}
