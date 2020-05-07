package ddsl.dcomponents;

import ddsl.dobjects.DObject;
import ddsl.dobjects.Select;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;


/**
 * @author Catalin Comanici

 * @version 4.1
 */


public class DomainSelector extends Select {

	public DomainSelector(WebDriver driver, WebElement container) {
		super(driver, container);
	}

	@Override
	public boolean selectOptionByText(String text) throws Exception {
		boolean selectResult = super.selectOptionByText(text);
		DomibusPage pg = new DomibusPage(driver);
		wait.forElementToContainText (pg.pageTitle, text);
		return selectResult;
	}

	@Override
	public boolean selectOptionByIndex(int index) throws Exception {
		String text = getOptionsTexts().get(index);
		boolean selectResult = super.selectOptionByIndex(index);

		DomibusPage pg = new DomibusPage(driver);
		wait.forElementToContainText (pg.pageTitle, text);
		return selectResult;
	}
}
