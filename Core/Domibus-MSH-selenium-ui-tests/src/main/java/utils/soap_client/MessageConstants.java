package utils.soap_client;

/**
 * @author Catalin Comanici
 * @since 4.1
 */
public class MessageConstants {
	
	public static final String From_Party_Id = "domibus-blue";
	public static final String To_Party_Id = "domibus-red";
	public static final String AP_Role = "SENDING";
	public static final String Message_Type = "USER_MESSAGE";
	public static final String Notification_Status = "NOT_REQUIRED";
	public static final String Original_Sender = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1";
	public static final String Final_Recipient = "urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4";
	
	public static final String Message_Content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<hello>world</hello>";
	
	
}
