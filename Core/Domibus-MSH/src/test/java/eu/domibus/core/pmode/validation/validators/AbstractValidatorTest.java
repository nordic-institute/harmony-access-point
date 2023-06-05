package eu.domibus.core.pmode.validation.validators;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import eu.domibus.common.model.configuration.Configuration;
import eu.domibus.common.model.configuration.Party;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author musatmi, idragusa
 * @since 3.3
 */
public abstract class AbstractValidatorTest {

    protected ObjectMapper mapper = new ObjectMapper();

    static class YourClassKeyDeserializer extends KeyDeserializer {
        @Override
        public Party deserializeKey(final String key, final DeserializationContext ctxt) throws IOException, JsonProcessingException {
            final Party party = new Party();
            party.setName(key);
            return party;
        }
    }

    @Before
    public void init() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addKeyDeserializer(Party.class, new YourClassKeyDeserializer());
        mapper.registerModule(simpleModule);
    }

    protected Configuration newConfiguration(String configurationFile) throws IOException {
        return mapper.readValue(getResourceAsString(configurationFile), Configuration.class);
    }

    protected String getResourceAsString(String resourceName) throws IOException {
        ClassPathResource json = new ClassPathResource(this.getClass().getPackage().getName().replaceAll("\\.","\\/") + "/" + resourceName);
        return IOUtils.toString(json.getInputStream(), StandardCharsets.UTF_8);
    }

    protected byte[] getPModeAsByteArray(String resourceName) throws IOException {
        ClassPathResource res = new ClassPathResource(this.getClass().getPackage().getName().replaceAll("\\.","\\/") + "/" + resourceName);
        return IOUtils.toByteArray(res.getInputStream());
    }

}