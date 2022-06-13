package pages.connectionMon;


public enum STATUS{
	SUCCESS, ERROR, UNKNOWN;

	public String getColor(){
		switch (this){
			case UNKNOWN:
				return "orange";
			case SUCCESS:
				return "green";
			case ERROR:
				return "red";
		}
		return null;
	}


}