package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Reporter;

import java.util.Arrays;
import java.util.Formatter;
import java.util.List;

public class MyLogger {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	public void info(String message) {
		Reporter.log(message);
		logger.info(message);
	}

	public void error(String message) {
		Reporter.log(message);
		logger.error(message);
	}

	public void debug(String message) {
		Reporter.log(message);
		logger.debug(message);
	}

	public void info(String... messages) {
		String template = messages[0];
		List<String> values = Arrays.asList(messages).subList(1, messages.length);
		String finalMessage = new Formatter().format(template, values).toString();
		Reporter.log(finalMessage);
		logger.info(finalMessage);
	}

	public void error(String... messages) {
		String template = messages[0];
		List<String> values = Arrays.asList(messages).subList(1, messages.length);
		String finalMessage = new Formatter().format(template, values).toString();
		Reporter.log(finalMessage);
		logger.error(finalMessage);
	}

	public void debug(String... messages) {
		String template = messages[0];
		List<String> values = Arrays.asList(messages).subList(1, messages.length);
		String finalMessage = new Formatter().format(template, values).toString();
		Reporter.log(finalMessage);
		logger.debug(finalMessage);
	}

}
