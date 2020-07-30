package domibus.ui;


import ddsl.enums.DMessages;
import ddsl.enums.DRoles;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import rest.DomibusRestClient;
import utils.Generator;
import utils.TestRunData;

import java.sql.*;

import static domibus.ui.SeleniumTest.messageSender;

/**
 * This file is referred for ticket 4839
 * Pre-requisite: UI Replication is enabled in domibus.properties
 */
public class UiReplicationRestTest extends SeleniumTest {
	
	/*
	This method will identify all unsynchronized data and then manually synchonize it through rest call
	 */
	@Test(priority = 1, description = "UR-1", groups = {"multiTenancy", "singleTenancy"}, enabled = true)
	public void checkUnsyncedData() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		Boolean isSyncEnabled = Boolean.valueOf(rest.properties().getDomibusPropertyDetail("domibus.ui.replication.enabled").getJSONObject(0).getString("value"));
		if(!isSyncEnabled){
			soft.assertEquals(rest.uiReplication().getCount(null), DMessages.UI_REPLICATION_NOT_ENABLED, "Correct message displayed when calling UI Replication count");
			rest.properties().updateDomibusProperty("domibus.ui.replication.enabled", "true");
			Thread.sleep(5000L);
		}
		
		soft.assertNotEquals(rest.uiReplication().getCount(null), DMessages.UI_REPLICATION_NOT_ENABLED,
				"");
		
		rest.uiReplication().sync(null);
		
		int messagesToSync = rest.uiReplication().extractNoOfRecords(rest.uiReplication().getCount(null));
		soft.assertEquals(messagesToSync,0 , "After sync all messages are synced");
		
		log.debug("messagesToSync = " + messagesToSync);
		
		
		soft.assertAll();
	}
	
	/*
	This method will compare all common columns of tables tb_message_ui & tb_message_log
	 */
	@Test(priority = 2, description = "UR-2", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
	public void compareAllData() throws Exception {
		SoftAssert soft = new SoftAssert();
		
		Boolean isSyncEnabled = Boolean.valueOf(rest.properties().getDomibusPropertyDetail("domibus.ui.replication.enabled").getJSONObject(0).getString("value"));
		if(isSyncEnabled){
			rest.uiReplication().sync(null);
			rest.properties().updateDomibusProperty("domibus.ui.replication.enabled", "false");
			Thread.sleep(5000L);
		}
		
		
		rest.sendMessages(5, null);
		JSONArray messagesUnsynced = rest.messages().getListOfMessages(null);
		
		rest.properties().updateDomibusProperty("domibus.ui.replication.enabled", "true");
		Thread.sleep(5000L);
		String countResp = rest.uiReplication().getCount(null);
		int unsyncedMessCount = rest.uiReplication().extractNoOfRecords(countResp);
		
		soft.assertEquals(unsyncedMessCount, 5, "Number of unsynced mess is equal to the number of mess we created");
		
		rest.uiReplication().sync(null);
		
		countResp = rest.uiReplication().getCount(null);
		unsyncedMessCount = rest.uiReplication().extractNoOfRecords(countResp);
		soft.assertEquals(unsyncedMessCount, 0, "Number of unsynced mess is 0 after sync");
		
		JSONArray messagesSynced = rest.messages().getListOfMessages(null);
		
		for (int i = 0; i < messagesSynced.length(); i++) {
			JSONObject synced  = messagesSynced.getJSONObject(i);
			boolean found = false;
			for (int j = 0; j < messagesUnsynced.length(); j++) {
				JSONObject unsynced = messagesUnsynced.getJSONObject(j);
				if(unsynced.getString("messageId").equalsIgnoreCase(synced.getString("messageId"))){
					found = true;
				}
			}
			
			soft.assertTrue(found , "Message with ID is found: " + synced.getString("messageId"));
		}
		
		
		soft.assertAll();
	}

}