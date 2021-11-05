package eu.domibus.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
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
        ObjectMapper mapper = new ObjectMapper();
        //Resource userMessageTemplate = new ClassPathResource("dataset/messages/UserMessageTemplate.json");
       // String jsonStr = new String(IOUtils.toByteArray(userMessageTemplate.getInputStream()), StandardCharsets.UTF_8);
        // TODO: François Gautier 29-10-21 GSon to be removed EDELIVERY-8617
     //   UserMessage userMessage = new Gson().fromJson(jsonStr, UserMessage.class);
        UserMessage userMessage = mapper.readValue(getResourceAsString("resource/dataset/messages/UserMessageTemplate.json"), UserMessage.class);
        return userMessage;
    }

    protected String getResourceAsString(String resourceName) throws IOException {
        ClassPathResource json = new ClassPathResource(this.getClass().getPackage().getName().replaceAll("\\.","\\/") + "/" + resourceName);
        return IOUtils.toString(json.getInputStream(), StandardCharsets.UTF_8);
    }
}
