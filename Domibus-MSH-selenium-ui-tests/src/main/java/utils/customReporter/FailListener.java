package utils.customReporter;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
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

	Logger log = LoggerFactory.getLogger("ROOT");

	@Override
	public void onStart(ITestContext context) {
		 log.info("Tests methods to run - " + context.getSuite().getAllMethods().size());
	}


	@Override
	public void onTestFailure(ITestResult result) {
		takeScreenshot(result);
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		takeScreenshot(result);
	}

	private void takeScreenshot(ITestResult result){
		String time = new SimpleDateFormat("dd-MM_HH-mm-ss").format(Calendar.getInstance().getTime());
		String testMeth = result.getName();
		String className = result.getTestClass().getRealClass().getSimpleName();
		String outputPath = ((BaseTest) result.getInstance()).data.getReportsFolder();
		String filename = String.format("%s%s_%s_%s.png", outputPath, className, testMeth, time);

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
