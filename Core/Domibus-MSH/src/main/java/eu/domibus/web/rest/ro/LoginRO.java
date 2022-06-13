package eu.domibus.web.rest.ro;

import eu.domibus.api.validators.SkipWhiteListed;

import java.io.Serializable;

/**
 * @author Cosmin Baciu
 * @since 3.3
 */
public class LoginRO implements Serializable {

    private String username;

    @SkipWhiteListed
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
