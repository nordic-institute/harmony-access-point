package utils;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Catalin Comanici
 * @since 4.1.2
 */
public class DFileUtils {
	
	protected static Logger log = LoggerFactory.getLogger("FileUtils");
	
	
	/*  This method will return total row count having data present in it*/
	public static int getRowCount(String fileName) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
		String input;
		int count = 0;
		while ((input = bufferedReader.readLine()) != null) {
			count++;
		}
		
		log.info("Downloaded file row count including headers row : " + count);
		return count;
	}
	
	/*  This method will verify whether file is downloaded or not   */
	public static Boolean isFileDownloaded(String path) throws Exception {
		int size = new File(path).listFiles().length;
		if (size == 1) {
			log.debug("Folder has file in it ");
			return true;
		} else {
			log.debug("File is not downloaded yet");
			return false;
		}
	}
	
	/*This method will return document from xml */
	public static Document getDocFromXML(String path) throws Exception {
		List<String> results = new ArrayList<String>();
		File[] listOfFiles = new File(path).listFiles();
		
		for (File file : listOfFiles) {
			if (file.isFile()) {
			
			}
			results.add(file.getName());
		}
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		System.out.println(results.get(0));
		String completeFilePath = path + "\\" + results.get(0);
		Document doc = docBuilder.parse(new File(completeFilePath));
		return doc;
	}
	
	/* This method will return extension of downloaded file*/
	public static String getFileExtension(String path) {
		List<String> results = new ArrayList<String>();
		File[] listOfFiles = new File(path).listFiles();
		
		for (File file : listOfFiles) {
			if (file.isFile()) {
			
			}
			results.add(file.getName());
		}
		String completePath = path + "\\" + results.get(0);
		return FilenameUtils.getExtension(completePath);
	}
	
	/* This method will return folder location for downloaded file*/
	public static String getCompleteFileName(String path) {
		List<String> results = new ArrayList<String>();
		File[] listOfFiles = new File(path).listFiles();
		if (listOfFiles.length == 1) {
			
			for (File file : listOfFiles) {
				if (file.isFile()) {
				
				}
				results.add(file.getName());
				
			}
			return results.get(0);
			
		} else {
			for (File file : listOfFiles) {
				if (file.isFile()) {
				
				}
				results.add(file.getName());
			}
			return results.get(1);
			
		}
		
	}
	
	
	public static String getAbsolutePath(String relativePath) {
		return new File(Thread.currentThread().getContextClassLoader().getResource(relativePath).getFile()).getAbsolutePath();
	}
	
	public static String downloadFolderPath() {
		return System.getProperty("user.dir") + File.separator + "downloadFiles";
	}
}
