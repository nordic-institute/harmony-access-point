package ddsl.dcomponents;

import ddsl.dobjects.DButton;
import ddsl.dobjects.DObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import utils.TestRunData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class Select extends DComponent {


	public Select(WebDriver driver, WebElement container) {
		super(driver);
		log.info("initialize select");
		PageFactory.initElements(new AjaxElementLocatorFactory(container, data.getTIMEOUT()), this);

		this.selectContainer = container;
		extractOptionIDs();
	}

	protected List<String> optionIDs = new ArrayList<String>();
	protected WebElement selectContainer;


	@FindBy(css = "span[class*=\"select-arrow\"]")
	protected WebElement expandBtn;

	@FindBy(css = "span[class*=\"-select-value\"]")
	protected WebElement selectedOptionValue;

	@FindBy(css = "[class*=\"select-content ng-trigger ng-trigger-fadeInContent\"]")
	protected WebElement optionContainer;

	private DObject getSelectContainer() {
		return new DObject(driver, selectContainer);
	}
	public DButton getExpandBtn() {
		return new DButton(driver, expandBtn);
	}
	private DObject getSelectedOptionElement() {
		return new DObject(driver, selectedOptionValue);
	}
	public String getSelectedValue() throws Exception{return getSelectedOptionElement().getText();}

	private void extractOptionIDs() {
		wait.forElementToBeVisible(selectContainer);
		wait.forAttributeNotEmpty(selectContainer, "aria-owns");

//		necessary hardcoded wait due to the way the option IDs are populated in the select
		wait.forXMillis(300);

		String[] idsAttributes = selectContainer.getAttribute("aria-owns").trim().split(" ");
		optionIDs.addAll(Arrays.asList(idsAttributes));

		log.info("option ids identified");
	}

	public boolean isDisplayed() throws Exception{
		return getExpandBtn().isEnabled();
	}

	public void expand() throws Exception{
		try {
			getExpandBtn().click();
		} catch (Exception e) {

		}
	}

	protected List<DObject> getOptionElements() throws Exception {
		expand();
		List<DObject> optionObj = new ArrayList<>();

		for (int i = 0; i < optionIDs.size(); i++) {
			String optionId = optionIDs.get(i);
			WebElement option = driver.findElement(By.id(optionId));
			optionObj.add(new DObject(driver, option));
		}
		return optionObj;
	}

	public boolean selectOptionByText(String text) throws Exception{
		List<DObject> optionObj = getOptionElements();

		wait.forElementToHaveText(optionObj.get(optionObj.size()-1).element);

		for (DObject dObject : optionObj) {

			if(dObject.getText().equalsIgnoreCase(text)){
				dObject.click();
				return true;
			}
		}

		return false;
	}

	public boolean selectOptionByIndex(int index) throws Exception {
		if (index >= optionIDs.size() || index<0) {
			return false;
		}

		getOptionElements().get(index).click();
		return true;
	}


	public List<String> getOptionsTexts() throws Exception{
		List<String> texts = new ArrayList<>();
		List<DObject> options = getOptionElements();

		for (int i = 0; i < options.size(); i++) {
			texts.add(options.get(i).getText());
		}

		return texts;
	}
}
