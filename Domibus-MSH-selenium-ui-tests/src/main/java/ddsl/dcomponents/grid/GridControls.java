package ddsl.dcomponents.grid;

import ddsl.dcomponents.DComponent;
import ddsl.dobjects.Checkbox;
import ddsl.dobjects.DLink;
import ddsl.dobjects.DObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.HashMap;
import java.util.List;

/**
 * @author Catalin Comanici
 * @description:
 * @since 4.1
 */
public class GridControls extends DComponent {
	public GridControls(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	@FindBy(css = "#routerHolder app-column-picker > div > a")
	WebElement showHideCtrlLnk;

	@FindBy(css = "#all_id")
	WebElement allLnk;

	@FindBy(css = "#none_id")
	WebElement noneLnk;

	@FindBy(css = "#routerHolder app-column-picker div > div:not([class])")
	List<WebElement> chkContainer;

	public DLink getShowHideCtrlLnk() {
		return new DLink(driver, showHideCtrlLnk);
	}

	public DLink getAllLnk() {
		return new DLink(driver, allLnk);
	}

	public DLink getNoneLnk() {
		return new DLink(driver, noneLnk);
	}

	public Boolean getCheckboxStatus(String name) throws Exception{
		for (WebElement chk : chkContainer) {
			WebElement labelFor = chk.findElement(By.cssSelector("label"));
			WebElement checkbox = chk.findElement(By.cssSelector("input"));
			if(new DObject(driver, labelFor).getText().equalsIgnoreCase(name)){
				return new Checkbox(driver, checkbox).isChecked();
			}
		}
		return null;
	}

	public HashMap<String, Boolean> getAllCheckboxStatuses() throws Exception{
		HashMap<String, Boolean> statuses = new HashMap<>();

		for (WebElement chk : chkContainer) {
			DObject labelFor = new DObject(driver, chk.findElement(By.cssSelector("label")));
			Checkbox checkbox = new Checkbox(driver, chk.findElement(By.cssSelector("input")));
			statuses.put(labelFor.getText(), checkbox.isChecked());
		}

		return statuses;
	}






}
