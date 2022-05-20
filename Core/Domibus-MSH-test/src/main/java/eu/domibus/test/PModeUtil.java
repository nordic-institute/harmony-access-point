package eu.domibus.test;

import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.core.pmode.ConfigurationDAO;
import eu.domibus.core.pmode.provider.PModeProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.XmlProcessingException;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Service
public class PModeUtil {

    protected static final int SERVICE_PORT = 8892;

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PModeUtil.class);

    @Autowired
    protected PModeProvider pModeProvider;

    @Autowired
    protected ConfigurationDAO configurationDAO;

    public void uploadPmode(Integer redHttpPort) throws IOException, XmlProcessingException {
        final InputStream inputStream = new ClassPathResource("dataset/pmode/PModeTemplate.xml").getInputStream();
        String pmodeText = IOUtils.toString(inputStream, "UTF-8");
        if (redHttpPort != null) {
            LOG.info("Using wiremock http port [{}]", redHttpPort);
            pmodeText = pmodeText.replace(String.valueOf(SERVICE_PORT), String.valueOf(redHttpPort));
        }

        final Configuration pModeConfiguration = pModeProvider.getPModeConfiguration(pmodeText.getBytes("UTF-8"));
        configurationDAO.updateConfiguration(pModeConfiguration);
        pModeProvider.refresh();
    }

    public void uploadPmode(Integer redHttpPort, Map<String, String> toReplace) throws IOException, XmlProcessingException {
        final InputStream inputStream = new ClassPathResource("dataset/pmode/PModeTemplate.xml").getInputStream();

        String pmodeText = IOUtils.toString(inputStream, "UTF-8");
        if (toReplace != null) {
            pmodeText = replace(pmodeText, toReplace);
        }
        if (redHttpPort != null) {
            LOG.info("Using wiremock http port [{}]", redHttpPort);
            pmodeText = pmodeText.replace(String.valueOf(SERVICE_PORT), String.valueOf(redHttpPort));
        }

        final Configuration pModeConfiguration = pModeProvider.getPModeConfiguration(pmodeText.getBytes("UTF-8"));
        configurationDAO.updateConfiguration(pModeConfiguration);
        pModeProvider.refresh();
    }

    public void uploadPmode() throws IOException, XmlProcessingException {
        uploadPmode(null);
    }

    protected String replace(String body, Map<String, String> toReplace) {
        for (String key : toReplace.keySet()) {
            body = body.replaceAll(key, toReplace.get(key));
        }
        return body;
    }
}
