package eu.domibus.web.rest.ro;

import eu.domibus.api.validators.SkipWhiteListed;

import java.io.Serializable;

/**
 * @author Ion Perpegel
 * @since 4.1
 */
public class ChangePasswordRO implements Serializable {

    @SkipWhiteListed
    private String currentPassword;

    @SkipWhiteListed
    private String newPassword;

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

}
