package eu.domibus.plugin.webService.backend.rules;

import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.webService.backend.WSBackendMessageType;
import eu.domibus.plugin.webService.backend.reliability.strategy.WSPluginRetryStrategyType;
import eu.domibus.plugin.webService.exception.WSPluginException;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.common.util.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class WSPluginDispatchRulesService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(WSPluginDispatchRulesService.class);

    public static final String PUSH_RULE_BASE = "wsplugin.push.rules";
    public static final String PUSH_RULE_PREFIX = PUSH_RULE_BASE + ".";

    public static final String PUSH_RULE_RECIPIENT = ".recipient";

    public static final String PUSH_RULE_ENDPOINT = ".endpoint";

    public static final String PUSH_RULE_RETRY = ".retry";

    public static final String PUSH_RULE_TYPE = ".type";

    private final DomibusPropertyExtService domibusPropertyExtService;
    private final Map<String, List<WSPluginDispatchRule>> rules = new HashMap<>();

    public WSPluginDispatchRulesService(DomibusPropertyExtService domibusPropertyExtService) {
        this.domibusPropertyExtService = domibusPropertyExtService;
    }

    public List<WSPluginDispatchRule> getRules() {
        String domain = LOG.getMDC(DomibusLogger.MDC_DOMAIN);
        List<WSPluginDispatchRule> domainRules = rules.get(domain);
        if (domainRules == null) {
            synchronized (rules) {
                if (rules.get(domain) == null) {
                    LOG.info("Find the rules of reliability for the domain [{}]", domain);
                    domainRules = generateRules();
                    rules.put(domain, domainRules);
                }
            }
        }
        return domainRules;
    }

    protected List<WSPluginDispatchRule> generateRules() {
        List<String> nestedProperties = domibusPropertyExtService.getNestedProperties(PUSH_RULE_BASE);
        List<WSPluginDispatchRuleBuilder> builderSortedByIndex = nestedProperties
                .stream()
                .map(WSPluginDispatchRuleBuilder::new)
                .collect(toList());

        List<WSPluginDispatchRule> result = new ArrayList<>();
        for (WSPluginDispatchRuleBuilder ruleBuilder : builderSortedByIndex) {
            ruleBuilder.withDescription(domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + ruleBuilder.getRuleName()));
            ruleBuilder.withRecipient(domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + ruleBuilder.getRuleName() + PUSH_RULE_RECIPIENT));
            ruleBuilder.withEndpoint(domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + ruleBuilder.getRuleName() + PUSH_RULE_ENDPOINT));
            ruleBuilder.withType(getTypes(domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + ruleBuilder.getRuleName() + PUSH_RULE_TYPE)));
            setRetryInformation(ruleBuilder, domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + ruleBuilder.getRuleName() + PUSH_RULE_RETRY));
            WSPluginDispatchRule dispatchRule = ruleBuilder.build();
            result.add(dispatchRule);
            LOG.info("WSPlugin reliability dispatch rule found: [{}]", dispatchRule);
        }

        return result;
    }

    protected List<WSBackendMessageType> getTypes(String property) {
        List<WSBackendMessageType> result = new ArrayList<>();
        String[] messageTypes = StringUtils.split(RegExUtils.replaceAll(property, " ", ""), ",");
        LOG.debug("get WSBackendMessageType with property: [{}]", property);
        for (String type : messageTypes) {
            try {
                result.add(WSBackendMessageType.valueOf(trim(type)));
            } catch (Exception e) {
                throw new WSPluginException("Type does not exists [" + type + "]. It should be one of " + Arrays.toString(WSBackendMessageType.values()), e);
            }
        }

        if (CollectionUtils.isEmpty(result)) {
            throw new WSPluginException("No type of notification found for property [" + property + "]");
        }
        return result;
    }

    /**
     * @param recipient of a message
     * @return order set of rules for a given {@param recipient}
     */
    public List<WSPluginDispatchRule> getRulesByRecipient(String recipient) {
        return getRules()
                .stream()
                .filter(wsPluginDispatchRule -> equalsAnyIgnoreCase(recipient, wsPluginDispatchRule.getRecipient()))
                .collect(Collectors.toList());
    }

    protected List<WSPluginDispatchRule> getRulesByName(String ruleName) {
        return getRules()
                .stream()
                .filter(wsPluginDispatchRule -> equalsAnyIgnoreCase(ruleName, wsPluginDispatchRule.getRuleName()))
                .collect(Collectors.toList());
    }

    public WSPluginDispatchRule getRule(String ruleName) {
        return getRulesByName(ruleName).stream().findAny().orElse(new WSPluginDispatchRuleBuilder(EMPTY).build());
    }

    protected void setRetryInformation(WSPluginDispatchRuleBuilder ruleBuilder, String property) {
        ruleBuilder.withRetry(property);
        LOG.debug("set retry information with property value: [{}]", property);
        if (StringUtils.isBlank(property)) {
            return;
        }
        try {
            String[] retryValues = StringUtils.split(RegExUtils.replaceAll(property, " ", ""), ";");
            ruleBuilder.withRetryTimeout(Integer.parseInt(trim(retryValues[0])));
            ruleBuilder.withRetryCount(Integer.parseInt(trim(retryValues[1])));
            ruleBuilder.withRetryStrategy(WSPluginRetryStrategyType.valueOf(trim(retryValues[2])));
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            throw new WSPluginException(
                    "The format of the property [" + PUSH_RULE_BASE + ruleBuilder.getRuleName() + PUSH_RULE_RETRY + "] " +
                            "is incorrect :[" + property + "]. " +
                            "Format: retryTimeout;retryCount;(CONSTANT - SEND_ONCE) (ex: 4;12;CONSTANT)", e);
        }
    }

    public WSPluginRetryStrategyType getStrategy(String ruleName) {
        return getRulesByName(ruleName).stream().findAny().map(WSPluginDispatchRule::getRetryStrategy).orElse(null);
    }
}
