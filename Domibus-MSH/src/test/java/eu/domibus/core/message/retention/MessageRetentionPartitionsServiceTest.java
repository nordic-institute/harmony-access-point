package eu.domibus.core.message.retention;

import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.api.security.AuthUtils;
import eu.domibus.api.security.functions.AuthenticatedProcedure;
import eu.domibus.api.util.DatabaseUtil;
import eu.domibus.core.message.UserMessageDao;
import eu.domibus.core.pmode.ConfigurationDAO;
import eu.domibus.core.pmode.provider.PModeProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * @author idragusa
 * @since 5.0
 */
@RunWith(JMockit.class)
public class MessageRetentionPartitionsServiceTest {

    @Tested
    MessageRetentionPartitionsService messageRetentionPartitionsService;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    PModeProvider pModeProvider;

    @Injectable
    UserMessageDao userMessageDao;

    @Test
    public void deleteExpiredMessagesTest() {

        List<String> mpcs = new ArrayList<>();
        mpcs.add("mpc1");
        mpcs.add("mpc2");
        new Expectations() {{
            pModeProvider.getRetentionDownloadedByMpcURI("mpc1");
            result = 1200;

            pModeProvider.getRetentionDownloadedByMpcURI("mpc2");
            result = 1440;

            pModeProvider.getRetentionUndownloadedByMpcURI(anyString);
            result = 1300;

            pModeProvider.getRetentionSentByMpcURI(anyString);
            result = 600;

            pModeProvider.getMpcURIList();
            result = mpcs;
        }};

        messageRetentionPartitionsService.deleteExpiredMessages();

    }
}