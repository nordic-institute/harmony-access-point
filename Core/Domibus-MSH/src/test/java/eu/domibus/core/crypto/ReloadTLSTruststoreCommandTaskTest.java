package eu.domibus.core.crypto;

import eu.domibus.api.cluster.Command;
import eu.domibus.api.cxf.TLSReaderService;
import eu.domibus.api.multitenancy.Domain;
import eu.domibus.api.multitenancy.DomainContextProvider;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;
import mockit.Verifications;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class ReloadTLSTruststoreCommandTaskTest {

    @Tested
    ReloadTLSTruststoreCommandTask reloadTLSTruststoreCommandTask;

    @Injectable
    TLSReaderService tlsReaderService;

    @Injectable
    DomainContextProvider domainContextProvider;

    @Test
    public void canHandle() {
        assertTrue(reloadTLSTruststoreCommandTask.canHandle(Command.RELOAD_TLS_TRUSTSTORE));
    }

    @Test
    public void canHandleWithDifferentCommand() {
        assertFalse(reloadTLSTruststoreCommandTask.canHandle("another_command"));
    }

    @Test
    public void execute(@Injectable Map<String, String> properties, @Injectable Domain domain, @Injectable String domainCode) {
        new Expectations() {{
            domainContextProvider.getCurrentDomain();
            result = domain;
            domain.getCode();
            result = domainCode;
        }};

        reloadTLSTruststoreCommandTask.execute(properties);

        new Verifications() {{
            tlsReaderService.reset(domainCode);
        }};
    }
}