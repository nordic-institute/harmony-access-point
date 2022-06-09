package eu.domibus.web.converter;

import com.fasterxml.jackson.core.JsonGenerator;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static eu.domibus.core.security.configuration.AbstractWebSecurityConfigurerAdapter.DOMIBUS_EXTERNAL_API_PREFIX;
import static eu.domibus.core.security.configuration.AbstractWebSecurityConfigurerAdapter.PLUGIN_API_PREFIX;

/**
 * @author Cosmin Baciu, Soumya Chandran
 * @since 3.3
 */
public class CustomMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(CustomMappingJackson2HttpMessageConverter.class);

    protected static final List<String> EXTERNAL_API_URLS = Arrays.asList(DOMIBUS_EXTERNAL_API_PREFIX, PLUGIN_API_PREFIX);

    public void setJsonPrefix(String jsonPrefix) {
        super.setJsonPrefix(fixNewLineCharacter(jsonPrefix));
    }

    protected String fixNewLineCharacter(String text) {
        return StringUtils.replace(text, "\\n", "\n");
    }

    @Override
    protected void writePrefix(JsonGenerator generator, Object object) throws IOException {
        // Get the current Request object from threadLocal
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String requestUrl = request.getRequestURL().toString();
        LOG.debug("Current Request URL [{}]", requestUrl);

        if (isExternalAPI(requestUrl)) {
            LOG.debug("Skipping writing prefix. Request URL [{}] is an external API", requestUrl);
            return;
        }

        super.writePrefix(generator, object);
    }

    protected boolean isExternalAPI(String requestUrl) {

        return EXTERNAL_API_URLS.stream().anyMatch(apiURL -> requestUrl.contains(apiURL));
    }
}
