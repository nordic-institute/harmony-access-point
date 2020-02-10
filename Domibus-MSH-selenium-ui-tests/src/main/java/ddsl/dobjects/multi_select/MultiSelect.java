package ddsl.dobjects.multi_select;

import ddsl.dobjects.DObject;
import ddsl.dobjects.Select;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Catalin Comanici
 * @since 4.1.2
 */
public class MultiSelect extends Select {

	public MultiSelect(WebDriver driver, WebElement container) {
		super(driver, container);
		PageFactory.initElements(new AjaxElementLocatorFactory(container, data.getTIMEOUT()), this);
	}

	public boolean selectOptionByText(String text) throws Exception{
		boolean result = super.selectOptionByText(text);
		clickVoidSpace();
		return result;
	}

	public boolean selectOptionByIndex(int index) throws Exception {
		boolean result = super.selectOptionByIndex(index);
		clickVoidSpace();
		return result;
	}

	public List<String> getOptionsTexts() throws Exception{
		List<String> texts = super.getOptionsTexts();
		clickVoidSpace();
		return texts;
	}





}
