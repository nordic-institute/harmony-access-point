package eu.domibus.web.rest.ro;

import eu.domibus.api.security.AuthRole;
import eu.domibus.api.security.AuthType;

import java.io.Serializable;

/**
 * @author Ion Perpegel
 * @since 4.1
 */

public class PluginUserFilterRequestRO implements Serializable {
    private int pageStart = 0;
    private int pageSize = 10;
    private AuthType authType;
    private AuthRole authRole;
    private String originalUser;
    private String userName;

    public int getPageStart() {
        return pageStart;
    }

    public void setPageStart(int pageStart) {
        this.pageStart = pageStart;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public AuthType getAuthType() {
        return authType;
    }

    public void setAuthType(AuthType authType) {
        this.authType = authType;
    }

    public AuthRole getAuthRole() {
        return authRole;
    }

    public void setAuthRole(AuthRole authRole) {
        this.authRole = authRole;
    }

    public String getOriginalUser() {
        return originalUser;
    }

    public void setOriginalUser(String originalUser) {
        this.originalUser = originalUser;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
