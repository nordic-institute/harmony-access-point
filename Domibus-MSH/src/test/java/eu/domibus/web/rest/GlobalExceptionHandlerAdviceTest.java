package eu.domibus.web.rest;

import com.google.gson.Gson;
import eu.domibus.api.multitenancy.DomainTaskException;
import eu.domibus.web.rest.error.ErrorHandlerService;
import eu.domibus.web.rest.error.GlobalExceptionHandlerAdvice;
import org.hibernate.HibernateException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.internal.matchers.Contains;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.persistence.RollbackException;
import java.util.Arrays;

import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class GlobalExceptionHandlerAdviceTest {

    private MockMvc mockMvc;

    @InjectMocks
    private GlobalExceptionHandlerAdvice unitUnderTest;

    @Mock
    private PluginUserResource pluginUserResource;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Spy
    private ErrorHandlerService errorHandlerService;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        this.mockMvc = MockMvcBuilders.standaloneSetup(pluginUserResource)
                .setControllerAdvice(unitUnderTest)
                .build();
    }

    private final String exceptionMessage = "Lorem ipsum dolor sit amet";
    private String mockMvcResultContent(ResultMatcher expectedStatus) throws Exception {
        String dummyPayload = "[]";
        MvcResult result = mockMvc.perform(put("/rest/plugin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(dummyPayload)
        )
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(expectedStatus)
                .andReturn();

        return result.getResponse().getContentAsString();
    }

    @Test
    public void testServerErrorHandler() throws Exception {
        Throwable thrown = new DomainTaskException(exceptionMessage);
        doThrow(thrown).when(pluginUserResource).updateUsers(anyList());
        String message = mockMvcResultContent(status().is5xxServerError());
        Assert.assertThat(message, new Contains("\"message\""));
        Assert.assertThat(message, new Contains(exceptionMessage));

        thrown = new RollbackException(exceptionMessage);
        doThrow(thrown).when(pluginUserResource).updateUsers(anyList());
        message = mockMvcResultContent(status().is5xxServerError());
        Assert.assertThat(message, new Contains("\"message\""));
        Assert.assertThat(message, new Contains(exceptionMessage));

        thrown = new HibernateException(exceptionMessage);
        doThrow(thrown).when(pluginUserResource).updateUsers(anyList());
        message = mockMvcResultContent(status().is5xxServerError());
        Assert.assertThat(message, new Contains("\"message\""));
        Assert.assertThat(message, new Contains(exceptionMessage));
    }

    @Test
    public void testBadRequestHandler() throws Exception {
        Throwable thrown = new IllegalArgumentException(exceptionMessage);
        doThrow(thrown).when(pluginUserResource).updateUsers(anyList());
        String message = mockMvcResultContent(status().is4xxClientError());
        Assert.assertThat(message, new Contains("\"message\""));
        Assert.assertThat(message, new Contains(exceptionMessage));
    }

    @Test
    public void shouldHandleMethodArgumentNotValidException() throws Exception {
        // given
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getAllErrors()).thenReturn(Arrays.asList(new FieldError("station", "name", exceptionMessage)));
        MethodParameter methodParameter = mock(MethodParameter.class);
        when(methodParameter.getParameterIndex()).thenReturn(1);
        when(methodParameter.getMethod()).thenReturn(this.getClass().getDeclaredMethods()[0]);
        MethodArgumentNotValidException thrown = new MethodArgumentNotValidException(methodParameter, bindingResult);
        // when
        ResponseEntity<Object> restErrorResponse = unitUnderTest.handleMethodArgumentNotValid(thrown, null, null, null);
        String message = new Gson().toJson(restErrorResponse);
        // then
        Assert.assertThat(message, new Contains(exceptionMessage));
    }
}
