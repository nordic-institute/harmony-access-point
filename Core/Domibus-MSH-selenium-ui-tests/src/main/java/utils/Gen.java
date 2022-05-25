package utils;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class Gen {
	
	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	private static final String ALPHA_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXY";

	
	public static String randomAlphaNumeric(int count) {
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
		}
		return builder.toString();
	}

	public static String rndStr(int count) {
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int) (Math.random() * ALPHA_STRING.length());
			builder.append(ALPHA_STRING.charAt(character));
		}
		return builder.toString();
	}

	public static Integer randomNumber(int range) {
		return Double.valueOf(Math.random() * range).intValue();
	}

	public static String randomNumberOfLen(int len) {

		StringBuilder build = new StringBuilder();

		for (int i = 0; i < len; i++) {
			build.append(Double.valueOf(Math.random() * 10).intValue());
		}

		return build.toString();
	}

	public static String randomCertIdStr() {
		StringBuilder build = new StringBuilder();
		String.format("CN=%s,O=%s,C=BE:%s", rndStr(5), rndStr(10), randomAlphaNumeric(10));
		return build.toString();
	}

	public static String randomOrigUsrStr() {
		return String.format("urn:oasis:names:tc:ebcore:partyid-type:%s:%s", rndStr(5), rndStr(5));
	}

	public static String randomPartyIdValStr() {
		StringBuilder build = new StringBuilder();
		build.append( String.format("urn:oasis:names:tc:ebcore:partyid-type:%s", rndStr(5)));
		return build.toString();
	}

	public static String randomEndpoint() {
		StringBuilder build = new StringBuilder();
		build.append( String.format("http://%s.com", rndStr(15)));
		return build.toString();
	}


	
}
