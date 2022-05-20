package eu.domibus.ext.rest.spring;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.lang.reflect.Type;

/**
 * Instance of the class enables to set custom MappingJackson2HttpMessageConverter for particular
 * java package. For example enable dedicated  ObjectMapper configuration for the service inside the java package.
 *
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class DomibusExtMappingConverter extends MappingJackson2HttpMessageConverter {


    final String basePackage;

    public DomibusExtMappingConverter(String basePackage) {
        this.basePackage = basePackage;
    }

    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        if (StringUtils.startsWith(contextClass.getName(), basePackage)) {
            return super.canRead(type, contextClass, mediaType);
        }
        return false;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        if (StringUtils.startsWith(clazz.getName(), basePackage)) {
            return super.canWrite(clazz, mediaType);
        }
        return false;
    }
}
