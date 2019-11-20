package ddsl.dobjects;

import ddsl.dcomponents.DComponent;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class Select extends DComponent {


    public Select(WebDriver driver, WebElement container) {
        super(driver);
		log.debug("initialize select");
        PageFactory.initElements(new AjaxElementLocatorFactory(container, data.getTIMEOUT()), this);

        this.selectContainer = container;
        //	extractOptionIDs();
    }

    protected List<String> optionIDs = new ArrayList<String>();
    protected WebElement selectContainer;


    @FindBy(css = "[class*=\"select-arrow\"]")
    protected WebElement expandBtn;

    @FindBy(css = "span[class*=\"-select-value\"]")
    protected WebElement selectedOptionValue;

    @FindBy(css = "[class*=\"select-content ng-trigger ng-trigger-fadeInContent\"]")
    protected WebElement optionContainer;

    private By options = By.cssSelector(".mat-select-panel > mat-option");

    private DObject getSelectContainer() {
        return new DObject(driver, selectContainer);
    }

	public DButton getExpandBtn() {
		return new DButton(driver, expandBtn);
	}

    private DObject getSelectedOptionElement() {
        return new DObject(driver, selectedOptionValue);
    }

    public String getSelectedValue() throws Exception {
        try {
            return getSelectedOptionElement().getText();
        } catch (Exception e) {
        }
        return null;
    }

    private void extractOptionIDs() {
        // note: the select needs to be expanded to extract the options 

//		necessary hardcoded wait due to the way the option IDs are populated in the select
        wait.forXMillis(300);

        List<WebElement> optionElements = driver.findElements(options);
        List<String> idsAttributes = optionElements.stream().map(el -> el.getAttribute("id")).collect(Collectors.toList());
        optionIDs.addAll(idsAttributes);

        log.debug(optionIDs.size() + " option ids identified : " + optionIDs);
    }

    public boolean isDisplayed() throws Exception {
        return getExpandBtn().isEnabled();
    }

    protected void expand() throws Exception {
        try {
            getExpandBtn().click();
            wait.forElementToBeGone(expandBtn);
            if (this.optionIDs.size() == 0) extractOptionIDs();
        } catch (Exception e) {
            log.warn("Could not expand : ", e);
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

    public boolean selectOptionByText(String text) throws Exception {
        log.debug("selecting option by text: " + text);

        if (StringUtils.isEmpty(text)) {
            return false;
        }

        List<DObject> optionObj = getOptionElements();
        if (optionObj.size() == 0) {
            log.warn("select has no options");
        }

        wait.forElementToHaveText(optionObj.get(optionObj.size() - 1).element);

        for (DObject dObject : optionObj) {

        if (StringUtils.equalsIgnoreCase(dObject.getText(), text)) {
                dObject.click();
                return true;
            }
        }

        return false;
    }

    public boolean selectOptionByIndex(int index) throws Exception {
        if (index >= optionIDs.size() || index < 0) {
            return false;
        }

        getOptionElements().get(index).click();
        return true;
    }


    public List<String> getOptionsTexts() throws Exception {
        List<String> texts = new ArrayList<>();
        List<DObject> options = getOptionElements();

        for (int i = 0; i < options.size(); i++) {
            texts.add(options.get(i).getText());
        }

        return texts;
    }
}
