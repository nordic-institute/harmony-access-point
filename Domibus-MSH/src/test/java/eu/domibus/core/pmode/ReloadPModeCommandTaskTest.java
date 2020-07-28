package eu.domibus.core.pmode;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.core.crypto.api.MultiDomainCryptoService;
import eu.domibus.core.pmode.provider.PModeProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@RunWith(JMockit.class)
public class ReloadPModeCommandTaskTest {

    @Tested
    ReloadPModeCommandTask reloadPModeCommandTask;

    @Injectable
    protected PModeProvider pModeProvider;

    @Injectable
    protected MultiDomainCryptoService multiDomainCryptoService;

    @Injectable
    protected DomainContextProvider domainContextProvider;

    @Test
    public void canHandle() {
        assertTrue(reloadPModeCommandTask.canHandle(Command.RELOAD_PMODE));
    }

    @Test
    public void canHandleWithDifferentCommand() {
        assertFalse(reloadPModeCommandTask.canHandle("anothercommand"));
    }

    @Test
    public void execute(@Injectable Map<String, String> properties,
                        @Injectable Domain domain) {
        new Expectations() {{
            domainContextProvider.getCurrentDomain();
            result = domain;
        }};

        reloadPModeCommandTask.execute(properties);

        new Verifications() {{
            pModeProvider.refresh();
            multiDomainCryptoService.refreshTrustStore(domain);
        }};
    }
}