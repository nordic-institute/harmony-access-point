package rest;

import com.sun.jersey.api.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomibusRestException extends Exception {
	
	protected Logger log = LoggerFactory.getLogger(this.getClass());
	
	public DomibusRestException(String message, ClientResponse response) {
		super(String.format("%s \n %s \n %s \n",
				message,
				"STATUS = " + response.getStatus(),
				"CONTENT = " + response.getEntity(String.class)));
	}
}
