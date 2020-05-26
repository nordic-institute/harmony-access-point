package pages.pmode.parties;

import ddsl.dcomponents.DomibusPage;
import ddsl.dcomponents.grid.DGrid;
import ddsl.dobjects.DButton;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.w3c.dom.Document;
import pages.pmode.current.PModeCofirmationModal;
import pages.pmode.current.PModeCurrentPage;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @author Catalin Comanici

 * @since 4.1
 */
public class PModePartiesPage extends DomibusPage {
	public PModePartiesPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	@FindBy(css = "#pageGridId")
	WebElement gridContainer;

	@FindBy(css = "#cancelButtonId")
	WebElement cancelButton;

	@FindBy(css = "#saveButtonId")
	WebElement saveButton;

	@FindBy(css = "#addButtonId")
	WebElement newButton;

	@FindBy(css = "#editButtonId")
	WebElement editButton;

	@FindBy(css = "#deleteButtonId")
	WebElement deleteButton;

	public DButton getCancelButton() {
		return new DButton(driver, cancelButton);
	}

	public DButton getSaveButton() {
		return new DButton(driver,saveButton);
	}

	public DButton getNewButton() {
		return new DButton(driver,newButton);
	}

	public DButton getEditButton() {
		return new DButton(driver,editButton);
	}

	public DButton getDeleteButton() {
		return new DButton(driver,deleteButton);
	}

	public DGrid grid() {
		return new DGrid(driver, gridContainer);
	}

	public PartiesFilters filters(){
		return new PartiesFilters(driver);
	}

}
