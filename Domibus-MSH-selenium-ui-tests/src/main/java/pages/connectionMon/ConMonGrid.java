package pages.connectionMon;

import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

public class ConMonGrid extends DGrid {
	
	public ConMonGrid(WebDriver driver, WebElement container) {
		super(driver, container);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}
	
	public void enableMonitoringForParty(String partyName) throws Exception {
		int rowIndex = scrollTo("Party", partyName);
		
		if(rowIndex <0){
			throw new Exception("Party name not found in grid");
		}
		
		WebElement toggle = gridRows.get(rowIndex).findElements(cellSelector).get(1).findElement(By.tagName("mat-slide-toggle"));
		
		toggle.click();
		wait.forXMillis(500);
	}
	
	public boolean isMonitoringEnabled(String partyName) throws Exception {
		int rowIndex = scrollTo("Party", partyName);
		
		if(rowIndex <0){
			throw new Exception("Party name not found in grid");
		}
		
		WebElement input = gridRows.get(rowIndex).findElements(cellSelector).get(1).findElement(By.cssSelector("mat-slide-toggle input"));
		boolean isChecked = Boolean.valueOf(input.getAttribute("aria-checked"));
		return isChecked;
	}
	
	public String getLastSent(String partyName) throws Exception {
		int rowIndex = scrollTo("Party", partyName);
		
		if(rowIndex <0){
			throw new Exception("Party name not found in grid");
		}
		
		WebElement lsEl = gridRows.get(rowIndex).findElements(cellSelector).get(2).findElement(By.cssSelector("div > div > div:nth-child(2) > span"));
		
		return new DObject(driver, lsEl).getText();
	}
	
	public String getLastReceived(String partyName) throws Exception {
		int rowIndex = scrollTo("Party", partyName);
		
		if(rowIndex <0){
			throw new Exception("Party name not found in grid");
		}
		
		WebElement lsEl = gridRows.get(rowIndex).findElements(cellSelector).get(2).findElement(By.cssSelector("div > div > div:nth-child(3) > span"));
		
		return new DObject(driver, lsEl).getText();
	}
	
	public DButton openDetails(String partyName) throws Exception {
		int rowIndex = scrollTo("Party", partyName);
		
		if(rowIndex <0){
			throw new Exception("Party name not found in grid");
		}
		
		WebElement lsEl = gridRows.get(rowIndex).findElements(cellSelector).get(3).findElement(By.cssSelector("button[tooltip=\"Details\"]"));
		
		return new DButton(driver, lsEl);
	}
	
	public DButton refreshData(String partyName) throws Exception {
		int rowIndex = scrollTo("Party", partyName);
		
		if(rowIndex <0){
			throw new Exception("Party name not found in grid");
		}
		
		WebElement lsEl = gridRows.get(rowIndex).findElements(cellSelector).get(3).findElement(By.cssSelector("button[tooltip=\"Refresh\"]"));
		
		return new DButton(driver, lsEl);
	}
	
	public DButton sendTestMessage(String partyName) throws Exception {
		int rowIndex = scrollTo("Party", partyName);
		
		if(rowIndex <0){
			throw new Exception("Party name not found in grid");
		}
		
		WebElement lsEl = gridRows.get(rowIndex).findElements(cellSelector).get(3).findElement(By.cssSelector("button[tooltip=\"Send\"]"));
		
		return new DButton(driver, lsEl);
	}
	
	
	
	
	
	
}
