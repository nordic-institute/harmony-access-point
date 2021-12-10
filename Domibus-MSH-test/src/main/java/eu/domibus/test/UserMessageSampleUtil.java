package eu.domibus.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.api.model.UserMessage;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class UserMessageSampleUtil {

    public UserMessage getUserMessageTemplate() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        UserMessage userMessage = mapper.readValue(getResourceAsString("resource/dataset/messages/UserMessageTemplate.json"), UserMessage.class);
        return userMessage;
    }

    protected String getResourceAsString(String resourceName) throws IOException {
        ClassPathResource json = new ClassPathResource(this.getClass().getPackage().getName().replaceAll("\\.","\\/") + "/" + resourceName);
        return IOUtils.toString(json.getInputStream(), StandardCharsets.UTF_8);
    }
}
