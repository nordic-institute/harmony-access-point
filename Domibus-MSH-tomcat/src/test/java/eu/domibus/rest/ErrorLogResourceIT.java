package eu.domibus.rest;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.MSHRole;
import eu.domibus.common.ErrorCode;
import eu.domibus.core.error.ErrorLogDao;
import eu.domibus.core.error.ErrorLogEntry;
import eu.domibus.core.message.MshRoleDao;
import eu.domibus.web.rest.ErrorLogResource;
import eu.domibus.web.rest.ro.ErrorLogFilterRequestRO;
import eu.domibus.web.rest.ro.ErrorLogResultRO;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@Rollback
public class ErrorLogResourceIT extends AbstractIT {

    @Autowired
    ErrorLogResource errorLogResource;

    @Autowired
    ErrorLogDao errorLogDao;

    @Autowired
    MshRoleDao mshRoleDao;

    private String mockMessageId = "9008713e-1912-460c-97b3-40ec12a29f49@domibus.eu";

    @Before
    public void setUp() {
        createEntries();
    }

    @Test
    public void testFindErrorLogEntries() {
        ErrorLogFilterRequestRO filters = new ErrorLogFilterRequestRO();

        ErrorLogResultRO result = errorLogResource.getErrorLog(filters);

        Assert.assertTrue(result.getErrorLogEntries().size() > 0);
    }

    @Test
    public void testCsv() {
        ErrorLogFilterRequestRO filters = new ErrorLogFilterRequestRO();

        ResponseEntity<String> result = errorLogResource.getCsv(filters);
        String csv = result.getBody();

        Assert.assertTrue(csv.contains(mockMessageId));
    }

    private void createEntries() {
        ErrorLogEntry logEntry = new ErrorLogEntry();
        logEntry.setMessageInErrorId(mockMessageId);
        logEntry.setMshRole(mshRoleDao.findOrCreate(MSHRole.SENDING));
        logEntry.setErrorCode(ErrorCode.EBMS_0004);
        logEntry.setTimestamp(new Date());
        errorLogDao.create(logEntry);
    }

}
