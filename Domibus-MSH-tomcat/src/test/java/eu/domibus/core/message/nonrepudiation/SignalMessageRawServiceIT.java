package eu.domibus.core.message.nonrepudiation;

import eu.domibus.AbstractIT;
import eu.domibus.api.model.SignalMessage;
import eu.domibus.common.MessageDaoTestUtil;
import eu.domibus.core.message.signal.SignalMessageDao;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Date;

/**
 * @author François Gautier
 * @since 5.0
 */
public class SignalMessageRawServiceIT extends AbstractIT {

    public static final String RAW_XML = "TEST";
    @Autowired
    private SignalMessageRawService signalMessageRawService;

    @Autowired
    private MessageDaoTestUtil messageDaoTestUtil;
    @Autowired
    private SignalMessageDao signalMessageDao;
    @Autowired
    protected SignalMessageRawEnvelopeDao signalMessageRawEnvelopeDao;


    @Test
    public void noSignalFound() {
        try {
            signalMessageRawService.saveSignalMessageRawService("", 1L);
            Assert.fail();
        } catch (EntityNotFoundException e) {
            //OK
        }
    }

    @Test
    @Transactional
    public void SignalFoundNoRaw() {
        messageDaoTestUtil.createSignalMessageLog("msg1", new Date());
        SignalMessage msg1 = signalMessageDao.findByUserMessageIdWithUserMessage("msg1");

        signalMessageRawService.saveSignalMessageRawService(RAW_XML, msg1.getEntityId());

        Assert.assertEquals(RAW_XML, signalMessageRawEnvelopeDao.findSignalMessageByUserMessageId("msg1").getRawXmlMessage());
    }
}
