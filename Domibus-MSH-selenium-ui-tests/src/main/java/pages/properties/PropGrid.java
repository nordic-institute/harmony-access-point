package pages.properties;

import ddsl.dcomponents.grid.DGrid;
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


}
