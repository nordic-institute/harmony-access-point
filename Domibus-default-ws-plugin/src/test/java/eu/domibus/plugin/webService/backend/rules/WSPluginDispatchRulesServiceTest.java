package eu.domibus.plugin.webService.backend.rules;

import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.backend.WSBackendMessageType;
import eu.domibus.plugin.webService.backend.reliability.strategy.WSPluginRetryStrategyType;
import eu.domibus.plugin.webService.exception.WSPluginException;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.commons.lang3.StringUtils;
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

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginDispatchRulesServiceTest.class);

    public static final String RULE_NAME_1 = "red1";
    public static final String RULE_NAME_3 = "red3";
    @Tested
    private WSPluginDispatchRulesService rulesService;

    @Injectable
    private DomibusPropertyExtService domibusPropertyExtService;

    @Test
    public void setRetryInformation_null() {
        WSPluginDispatchRuleBuilder ruleBuilder = new WSPluginDispatchRuleBuilder(RULE_NAME_1);
        rulesService.setRetryInformation(ruleBuilder, null);
        WSPluginDispatchRule build = ruleBuilder.build();
        assertNull(build.getRetry());
        assertNull(build.getRetryCount());
        assertNull(build.getRetryTimeout());
        assertNull(build.getRetryStrategy());
    }

    @Test
    public void setRetryInformation_empty() {
        WSPluginDispatchRuleBuilder ruleBuilder = new WSPluginDispatchRuleBuilder(RULE_NAME_1);
        rulesService.setRetryInformation(ruleBuilder, "");
        WSPluginDispatchRule build = ruleBuilder.build();
        assertEquals("", build.getRetry());
        assertNull(build.getRetryCount());
        assertNull(build.getRetryTimeout());
        assertNull(build.getRetryStrategy());
    }

    @Test
    public void setRetryInformation_ok() {
        WSPluginDispatchRuleBuilder ruleBuilder = new WSPluginDispatchRuleBuilder(RULE_NAME_1);
        rulesService.setRetryInformation(ruleBuilder, "60;5;CONSTANT");
        WSPluginDispatchRule build = ruleBuilder.build();
        assertEquals("60;5;CONSTANT", build.getRetry());
        assertEquals(5, build.getRetryCount().intValue());
        assertEquals(60, build.getRetryTimeout().intValue());
        assertEquals(WSPluginRetryStrategyType.CONSTANT, build.getRetryStrategy());
    }

    @Test(expected = WSPluginException.class)
    public void setRetryInformation_NumberFormatException() {
        WSPluginDispatchRuleBuilder ruleBuilder = new WSPluginDispatchRuleBuilder(RULE_NAME_1);
        rulesService.setRetryInformation(ruleBuilder, "60:5:CONSTANT");
    }

    @Test(expected = WSPluginException.class)
    public void setRetryInformation_OutOfBound() {
        WSPluginDispatchRuleBuilder ruleBuilder = new WSPluginDispatchRuleBuilder(RULE_NAME_1);
        rulesService.setRetryInformation(ruleBuilder, "60;5");
    }

    @Test
    public void initRules_noRuleFound() {
        new Expectations() {{
            domibusPropertyExtService.getNestedProperties("wsplugin.push.rules");
            times = 1;
            result = new ArrayList<>();
        }};
        List<WSPluginDispatchRule> wsPluginDispatchRules = rulesService.generateRules();
        assertEquals(0, wsPluginDispatchRules.size());

        new FullVerifications() {
        };
    }

    @Test
    public void initRules_2rules() {

        new Expectations(rulesService) {{
            domibusPropertyExtService.getNestedProperties("wsplugin.push.rules");
            times = 1;
            result = Arrays.asList(RULE_NAME_1, RULE_NAME_3);

            domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + RULE_NAME_1);
            result = "desc1";
            times = 1;
            domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + RULE_NAME_1 + PUSH_RULE_RECIPIENT);
            result = "recipient1";
            times = 1;
            domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + RULE_NAME_1 + PUSH_RULE_ENDPOINT);
            result = "endPoint1";
            times = 1;
            domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + RULE_NAME_1 + PUSH_RULE_RETRY);
            result = "1;1;CONSTANT";
            times = 1;
            domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + RULE_NAME_1 + PUSH_RULE_TYPE);
            result = "SEND_SUCCESS";
            times = 1;

            domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + RULE_NAME_3);
            result = "desc3";
            times = 1;
            domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + RULE_NAME_3 + PUSH_RULE_RECIPIENT);
            result = "recipient3";
            times = 1;
            domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + RULE_NAME_3 + PUSH_RULE_ENDPOINT);
            result = "endPoint3";
            times = 1;
            domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + RULE_NAME_3 + PUSH_RULE_RETRY);
            result = "3;3;CONSTANT";
            times = 1;
            domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + RULE_NAME_3 + PUSH_RULE_TYPE);
            result = "SEND_SUCCESS";
            times = 1;
        }};

        List<WSPluginDispatchRule> wsPluginDispatchRules = rulesService.generateRules();
        assertEquals(2, wsPluginDispatchRules.size());
        WSPluginDispatchRule firstRule = wsPluginDispatchRules.get(0);
        assertEquals("desc1", firstRule.getDescription());
        assertEquals("recipient1", firstRule.getRecipient());
        assertEquals(RULE_NAME_1, firstRule.getRuleName());
        assertEquals("1;1;CONSTANT", firstRule.getRetry());
        WSPluginDispatchRule secondRule = wsPluginDispatchRules.get(1);
        assertEquals("desc3", secondRule.getDescription());
        assertEquals("recipient3", secondRule.getRecipient());
        assertEquals(RULE_NAME_3, secondRule.getRuleName());
        assertEquals("3;3;CONSTANT", secondRule.getRetry());
    }

    @Test
    public void getRulesByRecipient(@Mocked WSPluginDispatchRule wsPluginDispatchRule) {
        new Expectations(rulesService) {{
            rulesService.getRules();
            result = Collections.singletonList(wsPluginDispatchRule);
            times = 1;
        }};
        rulesService.getRulesByRecipient("recipient");
    }

    @Test
    public void getTypes_ok() {
        List<WSBackendMessageType> types = rulesService.getTypes("SEND_SUCCESS,RECEIVE_FAIL");
        assertThat(types, CoreMatchers.hasItems(WSBackendMessageType.SEND_SUCCESS, WSBackendMessageType.RECEIVE_FAIL));
    }

    @Test(expected = WSPluginException.class)
    public void getTypes_noType() {
        rulesService.getTypes("");
    }

    @Test(expected = WSPluginException.class)
    public void getTypes_typeDoesntExists() {
        rulesService.getTypes("NOPE");
    }

    @Test
    public void getRules() {

        new Expectations(rulesService) {{
            rulesService.generateRules();
            result = Collections.singletonList(new WSPluginDispatchRuleBuilder("test1").build());
            result = Arrays.asList(
                    new WSPluginDispatchRuleBuilder("test20").build(),
                    new WSPluginDispatchRuleBuilder("test21").build());
            times = 2;
        }};
        LOG.putMDC(DomibusLogger.MDC_DOMAIN, "test1");
        assertEquals(1, rulesService.getRules().size());
        LOG.putMDC(DomibusLogger.MDC_DOMAIN, "test2");
        assertEquals(2, rulesService.getRules().size());
        LOG.putMDC(DomibusLogger.MDC_DOMAIN, "test1");
        assertEquals(1, rulesService.getRules().size());

        new FullVerifications() {
        };

    }

    @Test
    public void getRulesByName_found() {
        WSPluginDispatchRule rule1 = new WSPluginDispatchRuleBuilder(RULE_NAME_1).build();
        WSPluginDispatchRule rule3 = new WSPluginDispatchRuleBuilder(RULE_NAME_3).build();

        new Expectations(rulesService) {{
            rulesService.getRules();
            result = Arrays.asList(rule1, rule3);
            times = 1;
        }};
        List<WSPluginDispatchRule> rulesByName = rulesService.getRulesByName(RULE_NAME_1);

        assertEquals(1, rulesByName.size());
        assertEquals(rule1, rulesByName.get(0));
    }

    @Test
    public void getOneRule_found(@Mocked WSPluginDispatchRule wsPluginDispatchRule) {
        WSPluginDispatchRule rule1 = new WSPluginDispatchRuleBuilder(RULE_NAME_1).build();

        new Expectations(rulesService) {{
            rulesService.getRulesByName(RULE_NAME_1);
            result = rule1;
            times = 1;
        }};
        WSPluginDispatchRule ruleFound = rulesService.getOneRule(RULE_NAME_1);

        assertEquals(rule1, ruleFound);
    }

    @Test
    public void getOneRule_notFound() {
        WSPluginDispatchRule rule3 = new WSPluginDispatchRuleBuilder(RULE_NAME_3).build();

        new Expectations(rulesService) {{
            rulesService.getRulesByName(RULE_NAME_1);
            result = rule3;
            times = 1;
        }};
        WSPluginDispatchRule ruleFound = rulesService.getOneRule(RULE_NAME_1);

        assertNotNull(ruleFound);
        assertNotNull(StringUtils.EMPTY, ruleFound.getRuleName());
    }

    @Test
    public void getStrategy_found() {
        WSPluginDispatchRule rule1 = new WSPluginDispatchRuleBuilder(RULE_NAME_1)
                .withRetryStrategy(WSPluginRetryStrategyType.CONSTANT)
                .build();

        new Expectations(rulesService) {{
            rulesService.getRulesByName(RULE_NAME_1);
            result = rule1;
            times = 1;
        }};
        WSPluginRetryStrategyType ruleFound = rulesService.getStrategy(RULE_NAME_1);

        assertEquals(WSPluginRetryStrategyType.CONSTANT, ruleFound);
    }

    @Test
    public void getStrategy_noRules() {
        new Expectations(rulesService) {{
            rulesService.getRulesByName(RULE_NAME_1);
            result = new ArrayList<>();
            times = 1;
        }};
        WSPluginRetryStrategyType strategyFound = rulesService.getStrategy(RULE_NAME_1);

        assertNull(strategyFound);
    }
}