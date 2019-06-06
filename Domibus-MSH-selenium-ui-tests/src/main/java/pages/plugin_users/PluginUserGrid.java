package pages.plugin_users;

import ddsl.dcomponents.grid.DGrid;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.awt.*;
import java.awt.event.InputEvent;

/**
 * @author Catalin Comanici
 * @description:
 * @since 4.1
 */
public class PluginUserGrid extends DGrid {
	public PluginUserGrid(WebDriver driver, WebElement container) {
		super(driver, container);
	}

	private int mask = InputEvent.BUTTON1_DOWN_MASK;


	@Override
	public void doubleClickRow(int rowNumber) throws Exception {

		log.info("double clicking row ... " + rowNumber);
		if (rowNumber < 0) {
			throw new Exception("Row number too low " + rowNumber);
		}
		if (rowNumber >= gridRows.size()) {
			throw new Exception("Row number too high " + rowNumber);
		}

		Point coordinates = gridRows.get(rowNumber).getLocation();

		try {
			Robot robot = new Robot();
			robot.mouseMove(coordinates.getX()+50,coordinates.getY()+120);

			robot.mousePress(mask);
			robot.mouseRelease(mask);
			robot.mousePress(mask);
			robot.mouseRelease(mask);

		} catch (AWTException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void selectRow(int rowNumber) throws Exception {
		log.info("clicking row ... " + rowNumber);
		if (rowNumber < 0) {
			throw new Exception("Row number too low " + rowNumber);
		}
		if (rowNumber >= gridRows.size()) {
			throw new Exception("Row number too high " + rowNumber);
		}

		Point coordinates = gridRows.get(rowNumber).getLocation();

		try {
			Robot robot = new Robot();
			robot.mouseMove(coordinates.getX()+50,coordinates.getY()+120);

			robot.mousePress(mask);
			robot.mouseRelease(mask);

		} catch (AWTException e1) {
			e1.printStackTrace();
		}

	}


}
