package domibus;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.model.service.util.ExceptionUtil;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.Reporter;
import rest.DomibusRestClient;
import utils.TestRunData;

import java.io.File;
import java.io.IOException;

public class AdvancedExtentReportListener implements ITestListener {
	private static ExtentReports extent = new ExtentReports();
	private static TestRunData data = new TestRunData();
	static ExtentSparkReporter htmlReporter = new ExtentSparkReporter("extent-report.html");

	static {

		htmlReporter.config().setTheme(Theme.DARK);
		htmlReporter.config().setTimelineEnabled(false);
		htmlReporter.config().enableOfflineMode(true);

		extent.setReportUsesManualConfiguration(true);

		extent.setSystemInfo("Configuration: ", data.getConfig());
		extent.setSystemInfo("Multitenancy: ", "" + data.isMultiDomain());
		try {
			extent.setSystemInfo("Build: ", new DomibusRestClient().getBuildInfo());
			extent.setSystemInfo("Browser", data.getRunBrowser());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	public void onStart(ITestContext context) {

		htmlReporter.config().setDocumentTitle(context.getSuite().getName() + "Run report");
		htmlReporter.config().setReportName(context.getSuite().getName());

		extent.attachReporter(htmlReporter);
		context.getAllTestMethods();
	}

	public static ExtentTest createTest(ITestResult result) {

		String description = result.getMethod().getDescription();
		String jiraKey = description.split(" - ")[0].trim();
		String summary = description.split(" - ")[1].trim();

		String htmlDesc = String.format("<p><a href=\"https://localhost/%s/\">%s</a> - %s</p>", jiraKey, jiraKey, summary);

		ExtentTest test = extent.createTest(result.getMethod().getRealClass().getSimpleName() + "." + result.getName(), htmlDesc);
		return test;
	}


	public void onTestStart(ITestResult iTestResult) {

	}

	public void onTestSuccess(ITestResult result) {
		ExtentTest test = createTest(result);

		String steps = MarkupHelper.createOrderedList(Reporter.getOutput(result)).getMarkup();

		test.pass(steps);
	}


	public void onTestFailure(ITestResult result) {
		ExtentTest test = createTest(result);

		takeScreenshot(result);

		String steps = MarkupHelper.createOrderedList(Reporter.getOutput(result)).getMarkup();
		String exception = MarkupHelper.createCodeBlock(ExceptionUtil.getStackTrace(result.getThrowable())).getMarkup();

		test.addScreenCaptureFromPath(result.getName().toString().trim() + ".png");

		test.fail(steps + "<br>" + exception);
	}


	public void onTestSkipped(ITestResult result) {
		ExtentTest test = createTest(result);

		String steps = MarkupHelper.createOrderedList(Reporter.getOutput(result)).getMarkup();
		String exception = MarkupHelper.createCodeBlock(ExceptionUtil.getStackTrace(result.getThrowable())).getMarkup();

		test.fail(steps + "<br>" + exception);
	}

	public void onTestFailedButWithinSuccessPercentage(ITestResult iTestResult) {

	}

	public void onFinish(ITestContext context) {
		extent.flush();
	}

	private void takeScreenshot(ITestResult result) {

		System.out.println("***** Error " + result.getName() + " test has failed *****");
		String methodName = result.getName().toString().trim();

		WebDriver driver = ((BaseTest) result.getInstance()).driver;
		File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

		try {
			FileUtils.copyFile(scrFile, new File(methodName + ".png"));
			System.out.println("***Placed screen shot in " + " ***");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
