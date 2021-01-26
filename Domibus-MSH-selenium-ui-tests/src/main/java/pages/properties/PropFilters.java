package pages.properties;

import ddsl.dcomponents.FilterArea;
import ddsl.dobjects.Checkbox;
import ddsl.dobjects.DInput;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

public class PropFilters extends FilterArea {


	@FindBy(id = "filterPropertyName_id")
	WebElement nameInputLct;

	@FindBy(id = "filterPropertyType_id")
	WebElement typeInputLct;

	@FindBy(id = "filterPropertyModule_id")
	WebElement moduleInputLct;

	@FindBy(id = "filterPropertyValue_id")
	WebElement valueInputLct;

	@FindBy(id = "includeSuperProperties_id")
	WebElement showDomainChkLct;

	public PropFilters(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public void filterBy(String name, String type, String module, String value, Boolean showDomain) throws Exception {

		log.info("Filtering properties by name {}, type {}, module {}, value {}, showDomain {}", name, type, module, value, showDomain);

		expandArea();

		if (StringUtils.isNotEmpty(name)) {
			weToDInput(nameInputLct).fill(name);
		}

		if (StringUtils.isNotEmpty(type)) {
			weToDInput(typeInputLct).fill(type);
		}

		if (StringUtils.isNotEmpty(module)) {
			weToDInput(moduleInputLct).fill(module);
		}
		if (StringUtils.isNotEmpty(value)) {
			weToDInput(valueInputLct).fill(value);
		}

		if (null != showDomain) {
			weToCheckbox(showDomainChkLct).set(showDomain);
		}

		clickSearch();

	}

	public Checkbox getShowDomainChk() {
		return weToCheckbox(showDomainChkLct);
	}

	public DInput getNameInput() {
		return weToDInput(nameInputLct);
	}

	public DInput getTypeInput() {
		return weToDInput(typeInputLct);
	}

	public DInput getModuleInput() {
		return weToDInput(moduleInputLct);
	}

	public DInput getValueInput() {
		return weToDInput(valueInputLct);
	}


}
