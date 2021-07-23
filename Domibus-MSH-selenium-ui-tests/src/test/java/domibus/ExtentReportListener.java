package domibus;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.Markup;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;
import rest.DomibusRestClient;
import utils.TestRunData;

import java.util.Calendar;


public class ExtentReportListener implements ITestListener {


		private static ExtentReports extent = new ExtentReports();
		private static TestRunData data = new TestRunData();
		static ExtentSparkReporter htmlReporter = new ExtentSparkReporter(data.getReportsFolder() + "/extent-report.html");

		static {
			htmlReporter.config().setDocumentTitle("Test Run report");
			htmlReporter.config().setReportName("Run report - " + Calendar.getInstance().getTime());
			htmlReporter.config().setTheme(Theme.DARK);
			htmlReporter.config().setTimelineEnabled(false);
			htmlReporter.config().enableOfflineMode(true);

			extent.attachReporter(htmlReporter);
			extent.setReportUsesManualConfiguration(true);
			extent.setSystemInfo("Configuration: ",data.getConfig());
			extent.setSystemInfo("Multitenancy: ","" + data.isMultiDomain());
			try {
				extent.setSystemInfo("Build: ", new DomibusRestClient().getBuildInfo());
			} catch (Exception e) { }
		}

		public static void closeReport(){
			extent.flush();
		}

		public static ExtentTest createTest(ITestResult result){
			ExtentTest test = extent.createTest(result.getMethod().getRealClass().getSimpleName() + "." + result.getName());
			return test;
		}


		public void onTestSuccess(ITestResult result) {
			ExtentTest test = createTest(result);

			test.pass("PASS");
		}


		public void onTestFailure(ITestResult result) {
			ExtentTest test = createTest(result);

			StringBuffer buffer = new StringBuffer();
			StackTraceElement[] stacktrace = result.getThrowable().getStackTrace();
			for (StackTraceElement element : stacktrace) {
				buffer.append(element.toString());
				buffer.append("<br>");
			}

			test.fail(buffer.toString());
		}


		public void onTestSkipped(ITestResult result) {
			ExtentTest test = createTest(result);

			test.skip(String.valueOf(result.getSkipCausedBy()));
		}

		public void onFinish(ITestContext context) {
			closeReport();
		}


	}
