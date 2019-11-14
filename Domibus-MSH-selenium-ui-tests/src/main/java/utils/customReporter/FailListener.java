package utils.customReporter;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;
import utils.BaseTest;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * @author Catalin Comanici
 * @since 4.1.2
 */
public class FailListener implements ITestListener {

	@Override
	public void onTestFailure(ITestResult result) {
		String time = new SimpleDateFormat("dd-MM_HH-mm-ss").format(Calendar.getInstance().getTime());
		String testMeth = result.getName();
		String className = result.getTestClass().getRealClass().getSimpleName();
		String filename = String.format("%s_%s_%s.jpg", className, testMeth, time);



		try {
			WebDriver driver = ((BaseTest) result.getInstance()).driver;
			((BaseTest) result.getInstance()).log.info("copying screenshot to " + filename);
			TakesScreenshot scrShot = ((TakesScreenshot) driver);
			File srcFile = scrShot.getScreenshotAs(OutputType.FILE);
			FileUtils.copyFile(srcFile, new File(filename));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


}
