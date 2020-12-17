package pages.SplitAndJoin;

import ddsl.dcomponents.DomibusPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.asserts.SoftAssert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.io.*;

public class SplitAndJoinPage extends DomibusPage {
	public final WebDriverWait longWait = new WebDriverWait(driver, data.getLongWait());

	public SplitAndJoinPage(WebDriver driver) {
		super(driver);
		PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
	}

	public boolean checkIfNodeExists(Document document, String xpathExpression) throws Exception {
		boolean matches = false;
		log.debug("Create XPathFactory object");
		XPathFactory xpathFactory = XPathFactory.newInstance();
		log.debug("Create XPath object");
		XPath xpath = xpathFactory.newXPath();
		try {
			log.debug("Create XPathExpression object");
			XPathExpression expr = xpath.compile(xpathExpression);
			log.debug("Evaluate expression result on XML document");
			NodeList nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
			if (nodes != null && nodes.getLength() > 0) {
				matches = true;
			}
		} catch (XPathExpressionException e) {
			log.error("EXCEPTION: ", e);
		}
		return matches;
	}


	public long getFileFolderSize(File dir) {
		long size = 0;
		if (dir.isDirectory()) {
			for (File file : dir.listFiles()) {
				if (file.isFile()) {
					size += file.length();
				} else
					size += getFileFolderSize(file);
			}
		} else if (dir.isFile()) {

			size += dir.length();
		}
		return size;
	}

	public void copyFile(File aFile, String path) {
		InputStream inStream = null;
		OutputStream outStream = null;
		try {
			File bfile = new File(path + aFile.getName());
			inStream = new FileInputStream(aFile);
			outStream = new FileOutputStream(bfile);
			byte[] buffer = new byte[1024];
			int length;
			log.debug("copy the file content in bytes");
			while ((length = inStream.read(buffer)) > 0) {
				outStream.write(buffer, 0, length);
			}
			inStream.close();
			outStream.close();
			log.info("File is copied successfully");

			log.info("Copied file name is :" + bfile.getName());
		} catch (IOException e) {
			log.error("EXCEPTION: ", e);
		}
	}

	public double getFileSize(File afile) {
		long fileSize = 0;
		fileSize = getFileFolderSize(afile);
		double sizeMB = (double) fileSize / 1024 / 1024;
		String s = " MB";
		log.info("File size is :" + afile.getName() + " :" + sizeMB + s);
		return sizeMB;
	}

	public String getSplittingConf(String nodeName, String childNodeName, SoftAssert soft, String attributeName, String tagName, String path) throws Exception {
		NodeList nodes = getNodeFromXml(path, nodeName);
		NodeList childNodes = getNodeFromXml(path, nodeName);
		for (int i = 0; i < nodes.getLength(); i++) {
			Element splittingconfDetails = (Element) nodes.item(i);
			for (int j = 0; j < childNodes.getLength(); j++) {
				Element name = (Element) splittingconfDetails.getElementsByTagName(tagName).item(j);
				log.info("splittingConfiguration attribute " + attributeName + " value is :" + name.getAttribute(attributeName));
				soft.assertTrue(name.getAttribute(attributeName) != null, " attribute is present for splitting conf");
				return name.getAttribute(attributeName);
			}
		}
		return "";
	}

	public void checkSplitAndJoinActivation(String nodeName, String childNodeName, SoftAssert soft, String attributeName, String tagName, String attributeName1, String path) throws Exception {
		NodeList nodes = getNodeFromXml(path, nodeName);
		NodeList childNodes = getNodeFromXml(path, childNodeName);
		log.info("Leg configuration count : " + childNodes.getLength());
		for (int k = 0; k < nodes.getLength(); k++) {
			Element legConfDetails = (Element) nodes.item(k);
			for (int l = 0; l < childNodes.getLength(); l++) {
				Element conf = (Element) legConfDetails.getElementsByTagName(tagName).item(l);
				if (conf.getAttribute(attributeName).equals("default")) {
					log.info("Splitting configuration activated in LegConfiguration " +
							" with configuration name : " + conf.getAttribute("splitting") + "with leg name : " + conf.getAttribute(attributeName1));
				}
			}
		}

	}

	public NodeList getNodeFromXml(String path, String nodeName) throws Exception {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(new File(path));
		NodeList nodes = doc.getElementsByTagName(nodeName);
		return nodes;
	}

	public int getFragCount(Double filesize, Integer fragmentsize) {
		double reminder = filesize / fragmentsize;
		if (reminder < 1) {
			return 1;
		} else {
			int frag = (int) Math.round(reminder);
			return frag;
		}
	}


}











