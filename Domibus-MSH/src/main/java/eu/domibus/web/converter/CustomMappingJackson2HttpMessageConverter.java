package eu.domibus.web.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author Cosmin Baciu, Soumya Chandran
 * @since 3.3
 */
public class CustomMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(CustomMappingJackson2HttpMessageConverter.class);
    private String newJsonPrefix;

    public void setJsonPrefix(String jsonPrefix) {
        newJsonPrefix = fixNewLineCharacter(jsonPrefix);
        super.setJsonPrefix(newJsonPrefix);
    }

    protected String fixNewLineCharacter(String text) {
        return StringUtils.replace(text, "\\n", "\n");
    }

    @Override
    protected void writePrefix(JsonGenerator generator, Object object) throws IOException {

        // Get the current Request object from threadLocal
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String Url = request.getRequestURL().toString();
        LOG.debug("Current Request URL:- [{}]", Url);
        if (Url.contains("/ext")) {
            generator.writeRaw("");
        } else {
            generator.writeRaw(this.newJsonPrefix);
        }
        String jsonpFunction =
                (object instanceof MappingJacksonValue ? ((MappingJacksonValue) object).getJsonpFunction() : null);
        if (jsonpFunction != null) {
            generator.writeRaw("/**/");
            generator.writeRaw(jsonpFunction + "(");
        }
    }
}
