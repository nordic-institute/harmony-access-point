package domibus.messaging;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.file.FileDataBodyPart;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import domibus.ui.RestTest;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class FTest extends RestTest {
	
//	List<String> suspisiousStr = new ArrayList<>();
//	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
//	DocumentBuilder dBuilder;
//	String[] keys = {"${Timestamp}",
//			"${MessageId}",
//			"${RefToMessageId}",
//			"${PartyId_type_1}",
//			"${Role_1}",
//			"${PartyId_type_2}",
//			"${Role_2}",
//			"${Service_type}",
//			"${Service}",
//			"${Action}",
//			"${ConversationId}",
//			"${Mess_Properties_name_1}",
//			"${Mess_Properties_1}",
//			"${Mess_Properties_name_2}",
//			"${Mess_Properties_2}",
//			"${PartInfo_href}",
//			"${PartInfo_Property_name}",
//			"${PartInfo_Property}",
//			"${payload_payloadId}",
//			"${payload_contentType}",
//			"${mess_value}"};
//
//
//	@Test(dataProvider = "readInvalidStrings")
//	public void test11(String evilStr) throws Exception {
//
//		Client client = Client.create();
//		client.addFilter(new HTTPBasicAuthFilter("padmin-01", "QW!@qw12"));
//		WebResource resource = client.resource(data.getUiBaseUrl());
//
////		String plainXML = getXMLFromFile();
////		String evilXML = modifyTagContent(evilStr, "Timestamp", plainXML);
////		String evilXML = modifyTagContent(evilStr, "Role", plainXML);
////		String evilXML = modifyTagContent(evilStr, "MessageId", plainXML);
////		String evilXML = modifyTagContent(evilStr, "RefToMessageId", plainXML);
////		String evilXML = modifyTagContent(evilStr, "value", plainXML);
////		String evilXML = modifyTagContent(evilStr, "Role", plainXML);
//		String evilXML = modifXml(evilStr);
//		System.out.println("evilXML = " + evilXML);
//
//		ClientResponse response = resource.path("services/backend")
//				.accept(MediaType.APPLICATION_XML)
//				.type(MediaType.APPLICATION_XML)
//				.post(ClientResponse.class, evilXML);
//
//		System.out.println("response.getStatus() = " + response.getStatus());
//		System.out.println("response.getEntity() = " + response.getEntity(String.class));
//
//		if(response.getStatus() == 200){
//			suspisiousStr.add(evilStr);
//		}
//	}
//
//
//	@Test(dataProvider = "readInvalidStrings")
//	public void testas4(String evilStr) throws Exception {
//
//		Client client = Client.create();
//		client.addFilter(new HTTPBasicAuthFilter("admin", "123456"));
//		WebResource resource = client.resource(data.getUiBaseUrl());
//
//
//		String path = "C:\\Users\\User\\AppData\\Roaming\\JetBrains\\IdeaIC2020.1\\scratches\\messas4.xml";
//		String contents = new String(Files.readAllBytes(Paths.get(path)));
//
//		String path_mess = "C:\\Users\\User\\AppData\\Roaming\\JetBrains\\IdeaIC2020.1\\scratches\\message";
//		File file = new File(path_mess);
//
//		FileDataBodyPart filePart = new FileDataBodyPart("message", file);
//		FormDataMultiPart multipartEntity = new FormDataMultiPart();
//
//
//		ClientResponse response = resource.path("/domibus/services/msh")
//				.type(MediaType.MULTIPART_FORM_DATA_TYPE)
//				.type("application/soap+xml")
//				.accept(MediaType.APPLICATION_XML)
//				.accept(MediaType.TEXT_PLAIN_TYPE)
//				.post(ClientResponse.class, multipartEntity);
//
//		System.out.println("response.getStatus() = " + response.getStatus());
//		System.out.println("response.getEntity() = " + response.getEntity(String.class));
//
//		if(response.getStatus() == 200){
//			suspisiousStr.add(evilStr);
//		}
//	}
//
//
//
//	@AfterTest
//	public void printSuspiciousStr(){
//		System.out.println("suspisiousStr = " + suspisiousStr);
//	}
//
//	@Test(dataProvider = "readInvalidStrings")
//	public void testInjectionStrInProperties(String evilStr) throws Exception {
//
//		Client client = Client.create();
//		client.addFilter(new HTTPBasicAuthFilter("padmin-01", "QW!@qw12"));
//		WebResource resource = client.resource(data.getUiBaseUrl());
//
//		String evilXML = modifXml(evilStr);
////		System.out.println("evilXML = " + evilXML);
//
//		ClientResponse response = resource.path("services/backend")
//				.accept(MediaType.APPLICATION_XML)
//				.type(MediaType.APPLICATION_XML)
//				.post(ClientResponse.class, evilXML);
//
//		System.out.println("response.getStatus() = " + response.getStatus());
//		System.out.println("response.getEntity() = " + response.getEntity(String.class));
//
//		if(response.getStatus() == 200){
//			suspisiousStr.add(evilStr);
//		}
//		Assert.assertEquals(response.getStatus(), 500);
//	}
//
//	private String modifXml(String evilStr) throws IOException {
//		String stubxml = getXMLFromFile();
//		for (String key : keys) {
//			stubxml = stubxml.replace(key, evilStr);
//		}
//		return stubxml;
//	}
//
//
//
//	private String getXMLFromFile() throws IOException {
////		String path = "C:\\Users\\User\\AppData\\Roaming\\JetBrains\\IdeaIC2020.1\\scratches\\scratch_mess.xml";
//		String path = "C:\\Users\\User\\AppData\\Roaming\\JetBrains\\IdeaIC2020.1\\scratches\\scratch.xml";
//		String contents = new String(Files.readAllBytes(Paths.get(path)));
//		return contents;
//	}
//
////	private String modifyTagContent(String newContent, String tagName, String xml) throws IOException {
////		String newXML = StringUtils.EMPTY;
////		int beginIndex = xml.indexOf(tagName) + tagName.length()+1;
////		int endIndex = StringUtils.indexOf(xml, tagName, beginIndex)-5;
////
////		String oldContent = xml.substring(beginIndex, endIndex);
////		newXML = xml.replace(oldContent, newContent);
////
////		return newXML;
////	}
////
////
//	private String modifXml(String update) throws Exception{
//		dBuilder= dbFactory.newDocumentBuilder();
//		Document doc = dBuilder.parse(new File("C:\\Users\\User\\AppData\\Roaming\\JetBrains\\IdeaIC2020.1\\scratches\\scratch_mess.xml"));
//		NodeList toModify = doc.getElementsByTagName("ns:Property");
//		for (int i = 0; i < toModify.getLength(); i++) {
//			if(toModify.item(i).getParentNode().getNodeName().contains("Part")){
//				continue;
//			}
//			Node item = toModify.item(i).getFirstChild();
//			item.setNodeValue(update);
//		}
//		return printDoc(doc);
//	}
//
//
//	public String printDoc(Document doc){
//		OutputFormat format = new OutputFormat(doc);
//		format.setIndenting(false);
//		Writer out = new StringWriter();
//		XMLSerializer serializer = new XMLSerializer(out, format);
//		try {
//			serializer.serialize(doc);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return out.toString();
//	}


}
