package ddsl.dcomponents;

import ddsl.dobjects.Select;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;


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
		wait.forElementToContainText(pg.pageTitle, text);
		return selectResult;
	}
	
	
	public String selectAnotherDomain() throws Exception {
		
		String currentDomain = getSelectedValue();
		List<String> options = getOptionsTexts();

		String newDomain = null;
		for (String option : options) {
			
			if(!StringUtils.equalsIgnoreCase(option, currentDomain)){
				selectOptionByText(option);
				newDomain = option;
			}
		}
		
		if(StringUtils.isEmpty(newDomain)){
			return null;
		}
		
		DomibusPage pg = new DomibusPage(driver);
		wait.forElementToContainText(pg.pageTitle, newDomain);
		
		return newDomain;
	}

	@Override
	public boolean selectOptionByIndex(int index) throws Exception {
		String text = getOptionsTexts().get(index);
		boolean selectResult = super.selectOptionByIndex(index);

		DomibusPage pg = new DomibusPage(driver);
		wait.forElementToContainText(pg.pageTitle, text);
		return selectResult;
	}
	
	
}
