
class DomibusConstants {	
    public static final def SUPER_USER = "super"
    public static final def SUPER_USER_PWD = "DomibusEdel-12345"
    public static final def DEFAULT_ADMIN_USER = "admin"
    public static final def DEFAULT_ADMIN_USER_PWD = "DomibusEdel-12345"
	public static final def MSG_STATUS_MAX_WAIT_TIME = 120_000 // Maximum time to wait to check the message status.
	public static final def MSG_STATUS_MAX_WAIT_TIME_EXT = 180_000 // Maximum time to wait to check the message status extended.
	public static final def MSG_STATUS_STEP_WAIT_TIME = 2_000 // Time to wait before re-checking the message status.
	public static final def CLUSTER_WAIT_TIME=15000	// Time to wait for property changes to be propagated accross clusters
    public static final def TRUSTSTORE_PASSWORD = "test123"	
	public static final def FS_DEF_MAP = [FS_DEF_SENDER:"domibus-blue",FS_DEF_P_TYPE:"urn:oasis:names:tc:ebcore:partyid-type:unregistered",FS_DEF_S_ROLE:"http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/initiator",FS_DEF_RECEIVER:"domibus-red",FS_DEF_R_ROLE:"http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/responder",FS_DEF_AGR_TYPE:"DUM",FS_DEF_AGR:"DummyAgr",FS_DEF_SRV_TYPE:"tc20",FS_DEF_SRV:"bdx:noprocess",FS_DEF_ACTION:"TC20Leg1",FS_DEF_CID:"cid:message",FS_DEF_PAY_NAME:"PayloadName.xml",FS_DEF_MIME:"text/xml",FS_DEF_OR_SENDER:"urn:oasis:names:tc:ebcore:partyid-type:unregistered:C1",FS_DEF_FIN_RECEIVER:"urn:oasis:names:tc:ebcore:partyid-type:unregistered:C4",FS_DEF_PROC_TYPE:"PUSH"]
	public static final def SLEEP_DELAY = 50_000
	public static final def DEFAULT_LOG_LEVEL = "1"
}
