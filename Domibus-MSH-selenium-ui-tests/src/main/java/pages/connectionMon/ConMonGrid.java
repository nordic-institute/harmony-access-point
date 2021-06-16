package pages.connectionMon;

import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DButton;
import ddsl.dobjects.DObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.List;

public class ConMonGrid extends DGrid {

	@FindBy(css = "datatable-row-wrapper > datatable-body-row div.connection-status.ng-star-inserted div span")
	public List<WebElement> sentRecvStatusDetail;
	@FindBy(css = "div[class=\"connection-status ng-star-inserted\"] >span >mat-icon")
	public List<WebElement> connectionStatusIcons;


	public ConMonGrid(WebDriver driver, WebElement container) {
		super(driver, container);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);

		this.container = container;
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

	public DButton getActionButton(String buttonName,int i){
		WebElement actionIcon;
		try {
			actionIcon = gridRows.get(i).findElements(cellSelector).get(3).findElement(By.cssSelector("button[tooltip=\"" + buttonName + "\"]"));
		}catch(Exception e){
			return null ;
		}
		return new DButton(driver, actionIcon);
	}

	public String getSendRecStatus(String process, int rowIndex) throws Exception {
		if(process.equals("Send")){
			return sentRecvStatusDetail.get(2*rowIndex).getText();
		}
		else if(process.equalsIgnoreCase("Receive")){
			return sentRecvStatusDetail.get(2 * rowIndex+1).getText();

		}
			throw new Exception("Other than send or receive status is demanded");
	}



}
