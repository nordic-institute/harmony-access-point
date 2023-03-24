package ddsl.dcomponents;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.List;
import java.util.stream.Collectors;

public class FilterArea extends DComponent {

    @FindBy(id = "advancedlink_id")
    WebElement advancedLink;
    @FindBy(id = "basiclink_id")
    WebElement basicLink;
    @FindBy(id = "searchbutton_id")
    WebElement searchButton;

    @FindBy(id = "resetButton_id")
    WebElement resetButton;

    @FindBy(name = "filterForm")
    WebElement filterForm;

    public FilterArea(WebDriver driver) {
        super(driver);
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
    }

    protected boolean filterAreaExpanded() {
        boolean expanded = false;
        try {
            expanded = weToDLink(basicLink).isVisible();
        } catch (Exception e) {
        }
        return expanded;
    }

    public void expandArea() {
        try {
            if (!filterAreaExpanded()) {
                weToDLink(advancedLink).click();
            }
        } catch (Exception e) {
        }
    }

    public void contractArea() throws Exception {
        if (filterAreaExpanded()) {
            weToDLink(basicLink).click();
        }
    }

    public void clickSearch() throws Exception {
        log.info("clicking search");
        weToDButton(searchButton).click();
        waitForRowsToLoad();
    }

    public void clickReset() throws Exception {
        log.info("clicking reset filters");
        weToDButton(resetButton).click();
    }

    public void fillAllInputs(String text) throws Exception {
        log.info("filling all inputs");
        expandArea();

        filterForm.findElements(By.cssSelector("input:not(.md2-datepicker-value)")).stream().filter(input -> input.getAttribute("type").equals("text")).forEach(input -> {
            try {
                weToDInput(input).fill(text);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public List<String> getAllInputsTexts() throws Exception {
        log.info("getting listed texts from all inputs");

        expandArea();

        return filterForm.findElements(By.cssSelector("input:not(.md2-datepicker-value)")).stream().filter(input -> input.getAttribute("type").equals("text")).map(input -> input.getAttribute("value")).collect(Collectors.toList());
    }


}
