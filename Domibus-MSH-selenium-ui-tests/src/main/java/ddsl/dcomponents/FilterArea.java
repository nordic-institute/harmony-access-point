package ddsl.dcomponents;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class FilterArea extends DComponent {
	
	@FindBy(id = "advancedlink_id")
	WebElement advancedLink;
	@FindBy(id = "basiclink_id")
	WebElement basicLink;
	@FindBy(id = "searchbutton_id")
	WebElement searchButton;
	
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
	
	public void expandArea() throws Exception {
		if (!filterAreaExpanded()) {
			weToDLink(advancedLink).click();
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
		
	}
	
	
}
