package eu.domibus.ext.rest;


import eu.domibus.core.spi.validation.UserMessageValidatorSpi;
import eu.domibus.ext.delegate.services.payload.PayloadExtDelegate;
import eu.domibus.ext.rest.error.ExtExceptionHelper;
import eu.domibus.ext.services.PayloadExtService;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {PayloadExtResourceTest.ContextConfiguration.class})
public class PayloadExtResourceTest {

    @Autowired
    UserMessageValidatorSpi userMessageValidatorSpi;

    @Autowired
    UserMessagePayloadExtResource payloadExtResource;


    @Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    static class ContextConfiguration {


        @Bean
        public UserMessagePayloadExtResource userMessagePayloadExtResource() {
            return new UserMessagePayloadExtResource();
        }

        @Bean
        public UserMessageValidatorSpi userMessageValidatorSpi() {
            return Mockito.mock(UserMessageValidatorSpi.class);
        }
        @Bean
        public ExtExceptionHelper extExceptionHelper() {
            return Mockito.mock(ExtExceptionHelper.class);
        }

        @Bean
        public PayloadExtService payloadExtService(UserMessageValidatorSpi userMessageValidatorSpi) {
            return new PayloadExtDelegate(userMessageValidatorSpi);
        }

    }

    @Test
    public void testPayloadValidation() throws IOException {
        final String fileContent = "filecontent";
        final ByteArrayInputStream contentStream = new ByteArrayInputStream(fileContent.getBytes());
        final String contentType = "application/pdf";
        MockMultipartFile multipartFile = new MockMultipartFile("file", "myfile.pdf", contentType, contentStream);

        ArgumentCaptor<ByteArrayInputStream> responseArgumentCaptor = ArgumentCaptor.forClass(ByteArrayInputStream.class);

        payloadExtResource.validatePayload(multipartFile);

        verify(userMessageValidatorSpi, times(1)).validatePayload(responseArgumentCaptor.capture(), eq(contentType));

        final ByteArrayInputStream value = responseArgumentCaptor.getValue();
        final byte[] bytes = IOUtils.toByteArray(value);
        final String captured = new String(bytes);
        Assert.assertEquals(fileContent, captured);
    }


}