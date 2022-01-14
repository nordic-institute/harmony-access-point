package eu.domibus.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.domibus.api.model.UserMessage;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class UserMessageSampleUtil {

    public UserMessage getUserMessageTemplate() throws IOException {
        Resource userMessageTemplate = new ClassPathResource("dataset/messages/UserMessageTemplate.json");
        String jsonStr = new String(IOUtils.toByteArray(userMessageTemplate.getInputStream()), StandardCharsets.UTF_8);
        ObjectMapper mapper = new ObjectMapper();
        UserMessage userMessage = mapper.readValue(jsonStr, UserMessage.class);
        return userMessage;
    }

    /*protected String getResourceAsString(String resourceName) throws IOException {
        ClassPathResource json = new ClassPathResource(this.getClass().getPackage().getName().replaceAll("\\.", "\\/") + "/" + resourceName);
        return IOUtils.toString(json.getInputStream(), StandardCharsets.UTF_8);
    }*/
}
