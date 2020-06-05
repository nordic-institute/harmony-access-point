package ddsl.dcomponents.grid;

import ddsl.dcomponents.DComponent;
import ddsl.dobjects.Checkbox;
import ddsl.dobjects.DLink;
import ddsl.dobjects.DObject;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class GridControls extends DComponent {
	@FindBy(css = "#routerHolder app-column-picker > div > a")
	WebElement showHideCtrlLnk;
	@FindBy(css = "#all_id")
	WebElement allLnk;
	@FindBy(css = "#none_id")
	WebElement noneLnk;
	@FindBy(css = "#routerHolder app-column-picker .column-checkbox")
	List<WebElement> chkContainer;
	
	public GridControls(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}
	
	public DLink getShowHideCtrlLnk() {
		return new DLink(driver, showHideCtrlLnk);
	}
	
	public DLink getAllLnk() {
		return new DLink(driver, allLnk);
	}
	
	public DLink getNoneLnk() {
		return new DLink(driver, noneLnk);
	}
	
	public Boolean getCheckboxStatus(String name) throws Exception {
		for (WebElement chk : chkContainer) {
			WebElement labelFor = chk.findElement(By.cssSelector("label"));
			WebElement checkbox = chk.findElement(By.cssSelector("input"));
			if (StringUtils.equalsIgnoreCase(new DObject(driver, labelFor).getText(), name)) {
				return new Checkbox(driver, checkbox).isChecked();
			}
		}
		return null;
	}
	
	public void checkBoxWithLabel(String name) throws Exception {
		boolean found = false;
		for (WebElement chk : chkContainer) {
			WebElement labelFor = chk.findElement(By.cssSelector("label"));
			WebElement checkbox = chk.findElement(By.cssSelector("input"));
			if (StringUtils.equalsIgnoreCase(new DObject(driver, labelFor).getText(), name)) {
				new Checkbox(driver, checkbox, labelFor).check();
				found = true;
			}
		}
		if (!found) {
			throw new Exception("Checkbox not found");
		}
	}
	
	public void uncheckBoxWithLabel(String name) throws Exception {
		boolean found = false;
		for (WebElement chk : chkContainer) {
			WebElement labelFor = chk.findElement(By.cssSelector("label"));
			WebElement checkbox = chk.findElement(By.cssSelector("input"));
			if (StringUtils.equalsIgnoreCase(new DObject(driver, labelFor).getText(), name)) {
				new Checkbox(driver, checkbox, labelFor).uncheck();
				found = true;
			}
		}
		if (!found) {
			throw new Exception("Checkbox not found");
		}
	}
	
	public HashMap<String, Boolean> getAllCheckboxStatuses() throws Exception {
		showCtrls();
		HashMap<String, Boolean> statuses = new HashMap<>();
		
		for (WebElement chk : chkContainer) {
			DObject labelFor = new DObject(driver, chk.findElement(By.cssSelector("label")));
			Checkbox checkbox = new Checkbox(driver, chk.findElement(By.cssSelector("input")));
			statuses.put(labelFor.getText(), checkbox.isChecked());
		}
		
		return statuses;
	}
	
	public List<String> getAllCheckboxLabels() throws Exception {
		showCtrls();
		List<String> labels = new ArrayList<>();
		
		for (WebElement chk : chkContainer) {
			DObject labelFor = new DObject(driver, chk.findElement(By.cssSelector("label")));
			labels.add(labelFor.getText());
		}
		return labels;
	}
	
	public void showCtrls() throws Exception {
		DLink link = getShowHideCtrlLnk();
		wait.forElementToContainText(showHideCtrlLnk, "columns");
		if (StringUtils.equalsIgnoreCase(link.getLinkText(), "Show columns")) {
			link.click();
			wait.forElementToContainText(showHideCtrlLnk, "Hide");
		}
	}
	
	public void hideCtrls() throws Exception {
		DLink link = getShowHideCtrlLnk();
		if (StringUtils.equalsIgnoreCase(link.getLinkText(), "Hide columns")) {
			link.click();
			wait.forElementToContainText(showHideCtrlLnk, "Show");
		}
	}
	
	public boolean areCheckboxesVisible() {
		try {
			return getAllLnk().isVisible();
		} catch (Exception e) {
			return false;
		}
	}
	
	public void showAllColumns() throws Exception {
		showCtrls();
		weToDLink(allLnk).click();
		hideCtrls();
	}
	
	
}
