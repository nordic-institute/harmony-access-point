package pages.domains;

import ddsl.dcomponents.DomibusPage;
import ddsl.dobjects.DButton;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

public class DomainPage extends DomibusPage {

    @FindBy(css = "#routerHolder > div > ng-component > button")
    public WebElement refreshDomainsBtn;

    public DomainPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);

    }

    public DomainsGrid grid() {
        return new DomainsGrid(driver);
    }

    public DButton getRefreshDomainsBtn() {
        return weToDButton(refreshDomainsBtn);
    }
}
