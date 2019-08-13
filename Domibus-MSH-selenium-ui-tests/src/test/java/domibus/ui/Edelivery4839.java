package domibus.ui;


import ddsl.enums.DRoles;
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
import java.util.HashMap;
import static domibus.ui.BaseTest.messageSender;

public class Edelivery4839 {

    private Connection connection;
    private static ResultSet rs;
    private static ResultSet rs1;


    protected final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    TestRunData data = new TestRunData();
    DomibusRestClient rest = new DomibusRestClient();
    HashMap<String, String> Table1Data = new HashMap<>();
    HashMap<String, String> Table2Data = new HashMap<>();

    @BeforeClass
    public void setUp() {
        String databaseURL = "jdbc:mysql://localhost:3306/domibus_aug1";
        String user = "root";
        String password = "abc@4321";
        connection = null;


        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            log.info("Connecting to Database...");
            connection = DriverManager.getConnection(databaseURL, user, password);
            if (connection != null) {
                log.info("Connected to the Database...");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
//Pre-requisite: UI replication should be enabled for particular Access Point and cron job is set for every 1 minute.There should not be
// any difference between both the tables before script execution. if yes follow admin guide

    @Test
    public void getDataFromDataBase() throws Exception {
        // admin console message sending part
        SoftAssert soft = new SoftAssert();
        String user = Generator.randomAlphaNumeric(10);
        String messageRefID = Generator.randomAlphaNumeric(10);
        String conversationID = Generator.randomAlphaNumeric(10);
        rest.createPluginUser(user, DRoles.ADMIN, data.getDefaultTestPass(), null);
        rest.uploadPMode("pmodes/Edelivery4386_PMode-blue.xml", null);
        String messageID = messageSender.sendMessage(user, data.getDefaultTestPass(), messageRefID, conversationID);
        rest.uploadPMode("pmodes/pmode-blue.xml", null);
        String messageID1 = messageSender.sendMessage(user, data.getDefaultTestPass(), messageRefID, conversationID);
        rest.uploadPMode("pmodes/Edelivery-blue.xml", null);
        String messageID2 = messageSender.sendMessage(user, data.getDefaultTestPass(), messageRefID, conversationID);
        Thread.sleep(data.getUiReplicationcronTime());


        try {
            //database part
            String query = "select MESSAGE_ID, MESSAGE_STATUS from tb_message_ui";
            String query1 = "select MESSAGE_ID,MESSAGE_STATUS from tb_message_log";
            CallableStatement cstmt = connection.prepareCall("{call sampleProcedure()}");
            rs = cstmt.executeQuery(query);
            log.info("Loading message id & status from table tb_message_ui");
            while (rs.next()) {
                Table1Data.put(rs.getString(1), rs.getString(2));
                System.out.println("Data from table tb_message_ui " + Table1Data);
            }
            cstmt.getMoreResults();
            log.info("Loading message id & status from table tb_message_log");
            rs1 = cstmt.executeQuery(query1);
            while (rs1.next()) {
                Table2Data.put(rs1.getString(1), rs1.getString(2));
                System.out.println("Data from table tb_message _log " + Table2Data);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        System.out.println("Both table data is synchronized :  " + Table1Data.equals(Table2Data));


    }

    @AfterClass
    public void tearDown() {
        if (connection != null) {
            try {
                log.info("Closing Database Connection...");
                connection.close();

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}