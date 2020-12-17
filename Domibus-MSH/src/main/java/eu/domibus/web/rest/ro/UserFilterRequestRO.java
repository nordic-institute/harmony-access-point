package eu.domibus.web.rest.ro;

import eu.domibus.api.security.AuthRole;

import java.io.Serializable;

/**
 * @since 4.2
 * @author Soumya Chandran
 */
public class UserFilterRequestRO implements Serializable {
    private int pageStart = 0;
    private int pageSize = 10;
    private AuthRole authRole;
    private String userName;
    private String deleted;

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

    public AuthRole getAuthRole() {
        return authRole;
    }

    public void setAuthRole(AuthRole authRole) {
        this.authRole = authRole;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getDeleted() {
        return deleted;
    }

    public void setDeleted(String deleted) {
        this.deleted = deleted;
    }


   }
