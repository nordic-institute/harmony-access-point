package ddsl.dobjects.multi_select;

import ddsl.dobjects.Select;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * @author Catalin Comanici
 * @since 4.1.2
 */
public class MultiSelect extends Select {

	public MultiSelect(WebDriver driver, WebElement container) {
		super(driver, container);
	}

	public boolean selectOptionByText(String text) throws Exception {
		boolean result = super.selectOptionByText(text);
		clickVoidSpace();
		return result;
	}

	public boolean selectOptionByIndex(int index) throws Exception {
		boolean result = super.selectOptionByIndex(index);
		clickVoidSpace();
		return result;
	}

	public List<String> getOptionsTexts() throws Exception {
		List<String> texts = super.getOptionsTexts();
		clickVoidSpace();
		return texts;
	}


}
