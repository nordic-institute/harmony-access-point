package domibus.messaging;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import domibus.ui.RestTest;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class MessagePenetrationTest extends RestTest {
	
	List<String> errRows = new ArrayList<>();
	Client client = Client.create();
	//	client.addFilter(new HTTPBasicAuthFilter("padmin-01", "QW!@qw12"));
	WebResource resource = client.resource(data.getUiBaseUrl());
	
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder;
	String[] keys = {"${Timestamp}", "${MessageId}", "${RefToMessageId}", "${PartyId_type_1}", "${Role_1}", "${PartyId_type_2}", "${Role_2}", "${Service_type}", "${Service}", "${Action}", "${ConversationId}", "${Mess_Properties_name_1}", "${Mess_Properties_1}", "${Mess_Properties_name_2}", "${Mess_Properties_2}", "${PartInfo_href}", "${PartInfo_Property_name}", "${PartInfo_Property}", "${payload_payloadId}", "${payload_contentType}", "${mess_value}"};
	
	
	@BeforeTest
	public void prep() throws Exception {
		rest.pmode().uploadPMode("./pmodes/selfSending8080.xml", null);
	}
	
	@AfterTest
	public void tear() throws Exception {
		
		File f1 = new File("stackMess.txt");
		if (!f1.exists()) {
			f1.createNewFile();
		}
		FileWriter fileWriter = new FileWriter(f1, true);
		for (String errRow : errRows) {
			fileWriter.write(errRow + "\n");
		}
		
		fileWriter.close();
	}
	
	@Test(dataProvider = "readInvalidStrings")
	public void testMessProperties(String evilStr) throws Exception {
		System.out.println(evilStr);
		
		SoftAssert soft = new SoftAssert();
		
		String evilXML = modifXml(evilStr);
		
		ClientResponse response = resource.path("services/backend")
				.accept(MediaType.APPLICATION_XML)
				.type(MediaType.APPLICATION_XML)
				.post(ClientResponse.class, evilXML);
		
		MyResult result = new MyResult(evilXML, response);
		
		int status = result.getResponseStatus();
		String entity = result.getResponseEntity();
		
		log.info("Status: " + status);
		log.debug("Content: " + entity);
		
		soft.assertTrue(status == 200 || status == 500, "Expected status was 200 or 500 but found: " + status);
		soft.assertTrue(result.isValidXML(), "Response is valid XML");
		
		if (response.getStatus() == 200) {
			soft.assertNotNull(result.getMessID(), "Message ID is not null");
			log.info("got message ID" + result.getMessID());
			
			if (null != result.getMessID()) {
				JSONObject mess = rest.messages().searchMessage(result.getMessID(), null);
				log.debug(mess.toString());
				
				if (!mess.getString("messageId").equalsIgnoreCase(result.getMessID())) {
					soft.fail("ids are not equal");
					log.debug(mess.toString());
				}
				soft.assertEquals(mess.getString("conversationId"), evilStr, "Property conversationId check");
				soft.assertEquals(mess.getString("refToMessageId"), evilStr, "Property refToMessageId check");
				soft.assertEquals(mess.getString("finalRecipient"), evilStr, "Property finalRecipient check");
				soft.assertEquals(mess.getString("originalSender"), evilStr, "Property originalSender check");
				
			}
			
		}
		
		if (response.getStatus() == 500) {
			soft.assertNotNull(result.getErrorDetail(), "Error detail is not null");
			log.info("got error mess " + result.getErrorDetail());
			
			try {
				if (StringUtils.isEmpty(result.getErrorDetail())) {
					System.out.println(evilStr + "\t" + result.getStacktraceMess());
					errRows.add(evilStr + "\t" + result.getStacktraceMess());
				}
			} catch (Exception e) {
				System.out.println("result = " + result);
			}
		}
		
		soft.assertTrue(null != result.getMessID() || null != result.getErrorDetail(), "We either get a mess ID or a error");
		soft.assertFalse(result.containsStackTrace(), "Response contains stack trace elements - " + result.getResponseEntity());
		
		log.debug(result.toString());
		
		soft.assertAll();
		
	}
	
	private String modifXml(String evilStr) throws IOException {
		String stubxml = getXMLFromFile();
		for (String key : keys) {
			stubxml = stubxml.replace(key, evilStr);
		}
		return stubxml;
	}
	
	private String getXMLFromFile() throws IOException {
		String path = "src/test/resources/webservice_template.xml";
		String contents = new String(Files.readAllBytes(Paths.get(path)));
		return contents;
	}
	
	
}
