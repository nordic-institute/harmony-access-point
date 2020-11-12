package eu.domibus.plugin.webService.backend.rules;

import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.plugin.webService.backend.WSBackendMessageType;
import eu.domibus.plugin.webService.exception.WSPluginException;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static eu.domibus.plugin.webService.backend.rules.WSPluginDispatchRulesService.*;
import static org.junit.Assert.*;

/**
 * @author François Gautier
 * @since 5.0
 */
@RunWith(JMockit.class)
public class WSPluginDispatchRulesServiceTest {

    @Tested
    private WSPluginDispatchRulesService wsPluginDispatchRulesService;

    @Injectable
    private DomibusPropertyExtService domibusPropertyExtService;

    @Test
    public void getIndex_null() {
        assertNull(wsPluginDispatchRulesService.getIndex(null));
    }

    @Test
    public void getIndex_blank() {
        assertNull(wsPluginDispatchRulesService.getIndex(""));
    }

    @Test
    public void getIndex_noInteger() {
        assertNull(wsPluginDispatchRulesService.getIndex("nope"));
    }

    @Test
    public void getIndex_noInteger2() {
        assertNull(wsPluginDispatchRulesService.getIndex(PUSH_RULE_PREFIX + ".something.else"));
    }

    @Test
    public void getIndex_wrongPrefix() {
        assertNull(wsPluginDispatchRulesService.getIndex("nope1.neither"));
    }

    @Test
    public void getIndex_ok() {
        assertEquals(4, (int) wsPluginDispatchRulesService.getIndex(PUSH_RULE_PREFIX + 4 + ".something.else"));
    }

    @Test
    public void setRetryInformation_null() {
        WSPluginDispatchRuleBuilder ruleBuilder = new WSPluginDispatchRuleBuilder(1);
        wsPluginDispatchRulesService.setRetryInformation(ruleBuilder, null);
        WSPluginDispatchRule build = ruleBuilder.build();
        assertNull(build.getRetry());
        assertEquals(0, build.getRetryCount());
        assertEquals(0, build.getRetryTimeout());
        assertNull(build.getRetryStrategy());
    }

    @Test
    public void setRetryInformation_empty() {
        WSPluginDispatchRuleBuilder ruleBuilder = new WSPluginDispatchRuleBuilder(1);
        wsPluginDispatchRulesService.setRetryInformation(ruleBuilder, "");
        WSPluginDispatchRule build = ruleBuilder.build();
        assertEquals("", build.getRetry());
        assertEquals(0, build.getRetryCount());
        assertEquals(0, build.getRetryTimeout());
        assertNull(build.getRetryStrategy());
    }

    @Test
    public void setRetryInformation_ok() {
        WSPluginDispatchRuleBuilder ruleBuilder = new WSPluginDispatchRuleBuilder(1);
        wsPluginDispatchRulesService.setRetryInformation(ruleBuilder, "60;5;CONSTANT");
        WSPluginDispatchRule build = ruleBuilder.build();
        assertEquals("60;5;CONSTANT", build.getRetry());
        assertEquals(5, build.getRetryCount());
        assertEquals(60, build.getRetryTimeout());
        assertEquals(WSPluginRetryStrategy.CONSTANT, build.getRetryStrategy());
    }

    @Test(expected = WSPluginException.class)
    public void setRetryInformation_NumberFormatException() {
        WSPluginDispatchRuleBuilder ruleBuilder = new WSPluginDispatchRuleBuilder(1);
        wsPluginDispatchRulesService.setRetryInformation(ruleBuilder, "60:5:CONSTANT");
    }

    @Test(expected = WSPluginException.class)
    public void setRetryInformation_OutOfBound() {
        WSPluginDispatchRuleBuilder ruleBuilder = new WSPluginDispatchRuleBuilder(1);
        wsPluginDispatchRulesService.setRetryInformation(ruleBuilder, "60;5");
    }

    @Test
    public void initRules_noRuleFound() {
        new Expectations() {{
            domibusPropertyExtService.getAllNestedProperties(WSPLUGIN_PUSH_PREFIX);
            times = 1;
            result = new ArrayList<>();
        }};
        List<WSPluginDispatchRule> wsPluginDispatchRules = wsPluginDispatchRulesService.generateRules();
        assertEquals(0, wsPluginDispatchRules.size());
    }

    @Test
    public void initRules_2rules() {
        String first = "first";
        String second = "second";
        new Expectations(wsPluginDispatchRulesService) {{
            domibusPropertyExtService.getAllNestedProperties(WSPLUGIN_PUSH_PREFIX);
            times = 1;
            result = Arrays.asList("second", "first");

            wsPluginDispatchRulesService.getIndex(first);
            result = 1;
            times = 1;

            wsPluginDispatchRulesService.getIndex(second);
            result = 3;
            times = 1;

            domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + 1 + PUSH_RULE_DESCRIPTION);
            result = "desc1";
            times = 1;
            domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + 1 + PUSH_RULE_RECIPIENT);
            result = "recipient1";
            times = 1;
            domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + 1 + PUSH_RULE_ENDPOINT);
            result = "endPoint1";
            times = 1;
            domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + 1 + PUSH_RULE_RETRY);
            result = "1;1;CONSTANT";
            times = 1;
            domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + 1 + PUSH_RULE_TYPE);
            result = "SEND_SUCCESS";
            times = 1;

            domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + 3 + PUSH_RULE_DESCRIPTION);
            result = "desc3";
            times = 1;
            domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + 3 + PUSH_RULE_RECIPIENT);
            result = "recipient3";
            times = 1;
            domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + 3 + PUSH_RULE_ENDPOINT);
            result = "endPoint3";
            times = 1;
            domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + 3 + PUSH_RULE_RETRY);
            result = "3;3;CONSTANT";
            times = 1;
            domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + 3 + PUSH_RULE_TYPE);
            result = "SEND_SUCCESS";
            times = 1;
        }};
        List<WSPluginDispatchRule> wsPluginDispatchRules = wsPluginDispatchRulesService.generateRules();
        assertEquals(2, wsPluginDispatchRules.size());
        WSPluginDispatchRule firstRule = wsPluginDispatchRules.get(0);
        assertEquals("desc1", firstRule.getDescription());
        assertEquals("recipient1", firstRule.getRecipient());
        assertEquals("endPoint1", firstRule.getEndpoint());
        assertEquals("1;1;CONSTANT", firstRule.getRetry());
        WSPluginDispatchRule secondRule = wsPluginDispatchRules.get(1);
        assertEquals("desc3", secondRule.getDescription());
        assertEquals("recipient3", secondRule.getRecipient());
        assertEquals("endPoint3", secondRule.getEndpoint());
        assertEquals("3;3;CONSTANT", secondRule.getRetry());
    }

    @Test
    public void getRules(@Mocked WSPluginDispatchRule wsPluginDispatchRule) {
        new Expectations(wsPluginDispatchRulesService) {{
            wsPluginDispatchRulesService.getRules();
            result = Collections.singletonList(wsPluginDispatchRule);
            times = 1;
        }};
        wsPluginDispatchRulesService.getRules("recipient");
    }

    @Test
    public void getTypes_ok() {
        List<WSBackendMessageType> types = wsPluginDispatchRulesService.getTypes("SEND_SUCCESS,RECEIVE_FAIL");
        assertThat(types, CoreMatchers.hasItems(WSBackendMessageType.SEND_SUCCESS, WSBackendMessageType.RECEIVE_FAIL));
    }

    @Test(expected = WSPluginException.class)
    public void getTypes_noType() {
        wsPluginDispatchRulesService.getTypes("");
    }

    @Test(expected = WSPluginException.class)
    public void getTypes_typeDoesntExists() {
        wsPluginDispatchRulesService.getTypes("NOPE");
    }
}