package utils;


/**
 * @author Catalin Comanici
 * @version 4.1
 */


public class Gen {
	
	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
	
	
	public static String randomAlphaNumeric(int count) {
		StringBuilder builder = new StringBuilder();
		while (count-- != 0) {
			int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
			builder.append(ALPHA_NUMERIC_STRING.charAt(character));
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
	
	
}
