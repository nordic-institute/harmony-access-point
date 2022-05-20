package domibus.ui.rest;

import com.sun.jersey.api.client.ClientResponse;
import domibus.ui.RestTest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PModeArchiveRestTest extends RestTest {
	
	@Test(description = "PMA-1")
	public void listArchive() throws Exception {
		SoftAssert soft = new SoftAssert();
		String uploadPath = "rest_pmodes/pmode-blue.xml";
		
		
		for (String domain : domains) {
			
			JSONArray pmodeArchivesBefore = rest.pmode().getPmodesList(domain);
			rest.pmode().uploadPMode(uploadPath, domain);
			JSONArray pmodeArchivesAfter = rest.pmode().getPmodesList(domain);
			soft.assertTrue(pmodeArchivesAfter.length() - 1 == pmodeArchivesBefore.length()
					, "Archive increased by one after pmode upload");
			
			int noOfCurrent = 0;
			for (int i = 0; i < pmodeArchivesAfter.length(); i++) {
				JSONObject obj = pmodeArchivesAfter.getJSONObject(i);
				if (obj.getBoolean("current")) {
					noOfCurrent++;
				}
			}
			soft.assertTrue(noOfCurrent == 1, "Only one pmode marked as current");
		}
		soft.assertAll();
	}
	
	@Test(description = "PMA-5")
	public void deleteFromArchive() throws Exception {
		SoftAssert soft = new SoftAssert();
		String uploadPath = "rest_pmodes/pmode-blue.xml";
		
		for (String domain : domains) {
			JSONArray archive = rest.pmode().getPmodesList(domain);
			
			for (int i = 0; i < 5 - archive.length(); i++) {
				rest.pmode().uploadPMode(uploadPath, domain);
			}
			
			archive = rest.pmode().getPmodesList(domain);
			
			List<Integer> ids = extractIDsFromArchiveList(archive);
			Collections.sort(ids);
			List<String> toDel = getSubsListOfIds(0, 2, ids);
			rest.pmode().deletePmode(domain, toDel);
			
			archive = rest.pmode().getPmodesList(domain);
			
			for (int i = 0; i < archive.length(); i++) {
				JSONObject entry = archive.getJSONObject(i);
				soft.assertFalse(toDel.contains(String.valueOf(entry.getInt("id"))), "Deleted ids does not contain current entry id");
			}
		}
		soft.assertAll();
	}
	
	@Test(description = "PMA-5", dataProvider = "readInvalidStrings")
	public void deleteFromArchiveNegativeTest(String evilStr) throws Exception {
		SoftAssert soft = new SoftAssert();
		String uploadPath = "rest_pmodes/pmode-blue.xml";
		
		for (String domain : domains) {
			JSONArray archive = rest.pmode().getPmodesList(domain);
			
			for (int i = 0; i < 5 - archive.length(); i++) {
				rest.pmode().uploadPMode(uploadPath, domain);
			}
			
			JSONArray oldarchive = rest.pmode().getPmodesList(domain);
			
			ClientResponse response = rest.pmode().deletePmode(domain, Arrays.asList(new String[]{evilStr}));
			
			JSONArray newarchive = rest.pmode().getPmodesList(domain);
			
			validateInvalidResponse(response, soft);
			
			soft.assertEquals(oldarchive.length(), newarchive.length(), "No elements were deleted");
		}
		soft.assertAll();
	}
	
	@Test(description = "PMA-4")
	public void restoreFromArchive() throws Exception {
		SoftAssert soft = new SoftAssert();
		String uploadPath = "rest_pmodes/pmode-blue.xml";
		
		for (String domain : domains) {
			JSONArray archive = rest.pmode().getPmodesList(domain);
			
			for (int i = 0; i < 5 - archive.length(); i++) {
				rest.pmode().uploadPMode(uploadPath, domain);
			}
			
			archive = rest.pmode().getPmodesList(domain);
			
			List<Integer> ids = extractIDsFromArchiveList(archive);
			Collections.sort(ids);
			
			rest.pmode().restorePmode(domain, String.valueOf(ids.get(0)));
			
			archive = rest.pmode().getPmodesList(domain);
			List<Integer> newids = extractIDsFromArchiveList(archive);
			
			soft.assertTrue(newids.size() - 1 == ids.size(), "One element added to the archive list");
			String description = archive.getJSONObject(0).getString("description");
			soft.assertTrue(description.contains("Restored version of"), "One element added to the archive list");
			
		}
		soft.assertAll();
	}
	
	@Test(description = "PMA-4", dataProvider = "readInvalidStrings")
	public void restoreFromArchiveNegativeTests(String evilStr) throws Exception {
		SoftAssert soft = new SoftAssert();
		String uploadPath = "rest_pmodes/pmode-blue.xml";
		
		for (String domain : domains) {
			JSONArray archive = rest.pmode().getPmodesList(domain);
			
			for (int i = 0; i < 5 - archive.length(); i++) {
				rest.pmode().uploadPMode(uploadPath, domain);
			}
			
			archive = rest.pmode().getPmodesList(domain);
			
			ClientResponse response = rest.pmode().restorePmode(domain, evilStr);
			validateInvalidResponse(response, soft);
			
			JSONArray newarchive = rest.pmode().getPmodesList(domain);
			
			soft.assertEquals(archive.length(), newarchive.length(), "Lists should have the same number of elements");
			
		}
		soft.assertAll();
	}
	
	private List<Integer> extractIDsFromArchiveList(JSONArray archive) {
		List<Integer> ids = new ArrayList<>();
		for (int i = 0; i < archive.length(); i++) {
			ids.add(archive.getJSONObject(i).getInt("id"));
		}
		return ids;
	}
	
	private List<String> getSubsListOfIds(int beginIndex, int endIndex, List<Integer> ids) {
		List<String> toRet = new ArrayList<>();
		List<Integer> sublist = ids.subList(beginIndex, endIndex);
		for (int i = 0; i < sublist.size(); i++) {
			toRet.add(String.valueOf(sublist.get(i)));
		}
		return toRet;
	}
	
}