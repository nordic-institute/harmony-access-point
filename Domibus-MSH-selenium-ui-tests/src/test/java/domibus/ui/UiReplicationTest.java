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
import static domibus.ui.SeleniumTest.messageSender;

/**
 * This file is referred for ticket 4839
 * Pre-requisite: UI Replication is enabled in domibus.properties
 */
public class UiReplicationTest {

    private Connection connection;
    private static ResultSet rs;
    private static ResultSet rs1;
    protected final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    TestRunData data = new TestRunData();
    DomibusRestClient rest = new DomibusRestClient();

    @BeforeClass
    public void setUp() {
        String databaseURL = "jdbc:mysql://localhost:3306/domibus_aug";
        String user = "root";
        String password = "abc@4321";


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

    /*
    This method will identify all unsynchronized data and then manually synchonize it through rest call
     */
    @Test(priority = 1, description = "UR-1", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
    public void getDataFromDataBase() throws Exception {
        log.info("Generate random username for plugin user");
        String user = Generator.randomAlphaNumeric(10);
        log.info("Generate random messageRefId");
        String messageRefID = Generator.randomAlphaNumeric(10);
        log.info("Generate random conversation id");
        String conversationID = Generator.randomAlphaNumeric(10);
        log.info("Create plugin user with rest call");
        rest.pluginUsers().createPluginUser(user, DRoles.ADMIN, data.defaultPass(), null);

        for (int i = 0; i < 50; i++) {
            log.info("Upload pmode");
            rest.pmode().uploadPMode("pmodes/Edelivery-blue.xml", null);
            log.info("send message through rest call");
            String messageID = messageSender.sendMessage(user, data.defaultPass(), messageRefID, conversationID);
        }
        log.info("wait for database table data to be updated ");
        Thread.sleep(1000);

        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("select * from V_MESSAGE_UI_DIFF");
            rs.last();
            log.info("Printing total no of unsynchronized data");
            log.info("V_MESSAGE_UI_DIFF table rows before synchronization: " + rs.getRow());

            if (rs.getRow() > 0) {
                log.info("Synchronize record through rest call");
                rest.syncRecord();
            }
            ResultSet rs1 = stmt.executeQuery("select * from V_MESSAGE_UI_DIFF");
            rs1.last();
            log.info("V_MESSAGE_UI_DIFF table rows after synchronization: " + rs1.getRow());

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /*
    This method will compare all common columns of tables tb_message_ui & tb_message_log
     */
    @Test(priority = 2, description = "UR-2", groups = {"multiTenancy", "singleTenancy"}, enabled = false)
    public void compareAllData() throws Exception {
        SoftAssert soft = new SoftAssert();
        String[] dataArray1 = new String[13];
        String[] dataArray2 = new String[13];

        try {
            String query = "select MESSAGE_ID,MESSAGE_STATUS,NOTIFICATION_STATUS,MSH_ROLE,MESSAGE_TYPE,DELETED,RECEIVED,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX,NEXT_ATTEMPT,FAILED,RESTORED,MESSAGE_SUBTYPE from tb_message_ui";
            String query1 = "select MESSAGE_ID,MESSAGE_STATUS,NOTIFICATION_STATUS,MSH_ROLE,MESSAGE_TYPE,DELETED,RECEIVED,SEND_ATTEMPTS,SEND_ATTEMPTS_MAX,NEXT_ATTEMPT,FAILED,RESTORED,MESSAGE_SUBTYPE from tb_message_log";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            log.info("Extracting records from table tb_message_ui");
            while (rs.next()) {
                dataArray1[0] = rs.getString(1);
                dataArray1[1] = rs.getString(2);
                dataArray1[2] = rs.getString(3);
                dataArray1[3] = rs.getString(4);
                dataArray1[4] = rs.getString(5);
                dataArray1[6] = rs.getString(7);
                dataArray1[7] = rs.getString(8);
                dataArray1[8] = rs.getString(9);

            }

            log.info("Extracting records from table tb_message_log");
            ResultSet rs1 = stmt.executeQuery(query1);
            while (rs1.next()) {
                dataArray2[0] = rs1.getString(1);
                dataArray2[1] = rs1.getString(2);
                dataArray2[2] = rs1.getString(3);
                dataArray2[3] = rs1.getString(4);
                dataArray2[4] = rs1.getString(5);
                dataArray2[6] = rs1.getString(7);
                dataArray2[7] = rs1.getString(8);
                dataArray2[8] = rs1.getString(9);
                ;
            }

            log.info("Validating all common columns of both tables having not null values");
            soft.assertTrue(dataArray1[0].equals(dataArray2[0]), "Message-id column data in both tables are equal");
            soft.assertTrue(dataArray1[1].equals(dataArray2[1]), "Message_status column data in both tables are equal");
            soft.assertTrue(dataArray1[2].equals(dataArray2[2]), "Notification_status column data in both tables are equal");
            soft.assertTrue(dataArray1[3].equals(dataArray2[3]), "Msh_Role column data in both tables are equal");
            soft.assertTrue(dataArray1[4].equals(dataArray2[4]), "Message_type data in both tables are equal ");
            soft.assertTrue(dataArray1[6].equals(dataArray2[6]), "Received data in both tables are equal");
            soft.assertTrue(dataArray1[7].equals(dataArray2[7]), "Send_attempts data in both tables are equal");
            soft.assertTrue(dataArray1[8].equals(dataArray2[8]), "Send_attempts-max data in both tables are equal");
            log.info("All values are validated");

        } catch (SQLException ex) {
            ex.printStackTrace();
        }


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