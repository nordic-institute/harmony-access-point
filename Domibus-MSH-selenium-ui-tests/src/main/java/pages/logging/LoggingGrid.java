package pages.logging;

import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.HashMap;
import java.util.List;

public class LoggingGrid extends DGrid {
	
	public LoggingGrid(WebDriver driver, WebElement container) {
		super(driver, container);
		PageFactory.initElements(new AjaxElementLocatorFactory(container, data.getTIMEOUT()), this);
		
	}
	
	By buttonSelector = By.tagName("mat-button-toggle");
	By selectedButtonSelector = By.cssSelector("mat-button-toggle.mat-button-toggle-checked");
	
	@Override
	public HashMap<String, String>  getRowInfo(int rowNumber) throws Exception {
		log.info("getting row info for row number " + rowNumber);
		
		HashMap<String, String> info = new HashMap<>();
		
		WebElement rowElement = getRowElement(rowNumber);
		List<WebElement> cells = rowElement.findElements(cellSelector);
		
		info.put("Logger Name", cells.get(0).getText());
		info.put("Logger Level", weToDobject(rowElement.findElement(selectedButtonSelector)).getText());
		
		log.debug("got info " + info);
		return info;
	}
	
	
	public HashMap<String, String>  setLoggLevel(int rowNumber, String level) throws Exception {
		
		HashMap<String, String> info = new HashMap<>();
		
		WebElement rowElement = getRowElement(rowNumber);
		List<WebElement> cells = rowElement.findElements(cellSelector);
		
		info.put("Logger Name", cells.get(0).getText());
		
		List<WebElement> levelBtns = rowElement.findElements(buttonSelector);
		for (WebElement btn : levelBtns) {
			DObject button = weToDobject(btn);
			if(button.getText().equalsIgnoreCase(level)){
				button.click();
				wait.forXMillis(300);
				break;
			}
		}
		
		info.put("Logger Level", weToDobject(rowElement.findElement(selectedButtonSelector)).getText());
		
		log.debug("got info " + info);
		return info;
	}
}
