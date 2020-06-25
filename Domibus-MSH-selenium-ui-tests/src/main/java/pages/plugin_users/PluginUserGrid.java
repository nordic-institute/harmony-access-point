package pages.plugin_users;

import ddsl.dcomponents.grid.DGrid;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.awt.event.InputEvent;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class PluginUserGrid extends DGrid {
	WebElement container;
	private int mask = InputEvent.BUTTON1_DOWN_MASK;

	public PluginUserGrid(WebDriver driver, WebElement container) {
		super(driver, container);
		this.container = container;
	}

	@Override
	public void doubleClickRow(int rowNumber) throws Exception {

		log.debug("double clicking row ... " + rowNumber);
		if (rowNumber < 0) {
			throw new Exception("Row number too low " + rowNumber);
		}
		if (rowNumber >= gridRows.size()) {
			throw new Exception("Row number too high " + rowNumber);
		}

		WebElement target = gridRows.get(rowNumber).findElements(cellSelector).get(0);

		Actions action = new Actions(driver);
		action.doubleClick(target).perform();
	}

	@Override
	public void selectRow(int rowNumber) throws Exception {
		log.debug("clicking row ... " + rowNumber);
		if (rowNumber < 0) {
			throw new Exception("Row number too low " + rowNumber);
		}
		if (rowNumber >= gridRows.size()) {
			throw new Exception("Row number too high " + rowNumber);
		}

		gridRows.get(rowNumber).findElements(cellSelector).get(0).click();
	}


}
