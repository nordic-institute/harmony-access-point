package domibus.messaging;

import com.sun.jersey.api.client.ClientResponse;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

public class MyResult {
	
	
	private String sentXML;
	
	private Integer responseStatus;
	private String responseEntity;
	
	private boolean validXML;
	private String messID;
	private String errorDetail;
	
	private DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	private DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	private Document doc;
	
	private String[] stackTraceIdk = {"Exception", "Caused", "apache", "Stax", "cxf", "catalina", "thread", "at org.", "fault"};
	
	
	
	
	
	public MyResult(String sentXML, ClientResponse response) throws ParserConfigurationException {
		this.sentXML = sentXML;
		this.responseStatus = response.getStatus();
		this.responseEntity = response.getEntity(String.class);
		
		try {
			doc = dBuilder.parse(new InputSource(new StringReader(responseEntity)));
			validXML = true;
			try {
				messID = getMessID(responseEntity);
				errorDetail = getErrMess(responseEntity);
			} catch (Exception e) {
			
			}
		} catch (Exception e) {
			validXML = false;
		}
		
	}
	
	private String getMessID(String entity) throws Exception{
		return doc.getElementsByTagName("messageID").item(0).getTextContent();
	}
	
	private String getErrMess(String entity) throws Exception{
		return doc.getElementsByTagName("eb:ErrorDetail").item(0).getTextContent();
	}
	
	public boolean containsStackTrace(){
		String completeEnt = responseEntity.toLowerCase();
		for (String s : stackTraceIdk) {
			if(completeEnt.contains(s.toLowerCase())){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString() {
		return "MyResult{" +
				"sentXML='" + sentXML + '\'' +
				", responseStatus=" + responseStatus +
				", responseEntity='" + responseEntity + '\'' +
				", messID='" + messID + '\'' +
				", errorDetail='" + errorDetail + '\'' +
				'}';
	}
	
	public String getSentXML() {
		return sentXML;
	}
	
	public void setSentXML(String sentXML) {
		this.sentXML = sentXML;
	}
	
	public Integer getResponseStatus() {
		return responseStatus;
	}
	
	public void setResponseStatus(Integer responseStatus) {
		this.responseStatus = responseStatus;
	}
	
	public String getResponseEntity() {
		return responseEntity;
	}
	
	public void setResponseEntity(String responseEntity) {
		this.responseEntity = responseEntity;
	}
	
	public String getMessID() {
		return messID;
	}
	
	public void setMessID(String messID) {
		this.messID = messID;
	}
	
	public String getErrorDetail() {
		return errorDetail;
	}
	
	public void setErrorDetail(String errorDetail) {
		this.errorDetail = errorDetail;
	}
	
	public boolean isValidXML() {
		return validXML;
	}
	
	public void setValidXML(boolean validXML) {
		this.validXML = validXML;
	}
}
