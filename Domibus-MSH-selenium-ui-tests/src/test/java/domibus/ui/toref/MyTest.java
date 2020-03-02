package domibus.ui.toref;

import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import utils.BaseTest;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

/**
 * @author Catalin Comanici
 * @since 4.1.2
 */
public class MyTest{// extends BaseTest {


	@Test
	public void test() throws Exception{
		File file = new File("C:\\Users\\User\\REPOS\\domibus\\Domibus-MSH-selenium-ui-tests\\src\\main\\resources\\pmodes\\doNothingInvalidRed.xml");

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);

		Node parties = doc.getElementsByTagName("parties").item(0);

		for (int i = 0; i < parties.getChildNodes().getLength(); i++) {
			Node currentNode = parties.getChildNodes().item(i);

//			if(currentNode.get)



		}





	}


}
