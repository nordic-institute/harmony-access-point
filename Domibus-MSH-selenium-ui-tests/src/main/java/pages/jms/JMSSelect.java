package pages.jms;

import ddsl.dcomponents.Select;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * @author Catalin Comanici
 * @description:
 * @since 4.1
 */
public class JMSSelect extends Select {
	public JMSSelect(WebDriver driver, WebElement container) {
		super(driver, container);
	}

	public int selectQueueWithMessages() throws Exception{

		List<String> queues = getOptionsTexts();

		int noOfMessages = 0;
		for (String queue : queues) {
			String striped  = queue.substring(queue.indexOf("(")+1, queue.indexOf(")")).trim();
			int noOfMess = Integer.valueOf(striped);
			if(noOfMess>0){
				selectOptionByText(queue);
				noOfMessages = noOfMess;
				break;
			}
		}

		return noOfMessages;
	}

	public int selectQueueWithMessagesNotDLQ() throws Exception{

		List<String> queues = getOptionsTexts();

		int noOfMessages = 0;
		for (String queue : queues) {
			String striped  = queue.substring(queue.indexOf("(")+1, queue.indexOf(")")).trim();
			int noOfMess = Integer.valueOf(striped);
			if(noOfMess>0 && !queue.contains("DLQ")){
				selectOptionByText(queue);
				noOfMessages = noOfMess;
				break;
			}
		}

		return noOfMessages;
	}

	public void selectDLQQueue() throws Exception{

		List<String> queues = getOptionsTexts();

		for (String queue : queues) {
			if(queue.contains("DLQ")){
				selectOptionByText(queue);
				return;
			}
		}
		throw new RuntimeException(new Exception("DLQ queue not found"));
	}






}
