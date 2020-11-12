package eu.domibus.plugin.webService.backend.rules;

import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.plugin.webService.backend.WSBackendMessageType;
import eu.domibus.plugin.webService.exception.WSPluginException;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.common.util.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;

/**
 * @author François Gautier
 * @since 5.0
 */
@Service
public class WSPluginDispatchRulesService {

    public static final String WSPLUGIN_PUSH_PREFIX = "wsplugin.push";
    public static final String PUSH_RULE_PREFIX = WSPLUGIN_PUSH_PREFIX + ".rule";
    public static final String PUSH_RULE_DESCRIPTION = ".description";
    public static final String PUSH_RULE_RECIPIENT = ".recipient";
    public static final String PUSH_RULE_ENDPOINT = ".endpoint";
    public static final String PUSH_RULE_RETRY = ".retry";
    public static final String PUSH_RULE_TYPE = ".type";

    private final DomibusPropertyExtService domibusPropertyExtService;
    private volatile List<WSPluginDispatchRule> rules;
    private final Object rulesLock = new Object();

    public WSPluginDispatchRulesService(DomibusPropertyExtService domibusPropertyExtService) {
        this.domibusPropertyExtService = domibusPropertyExtService;
    }

    public List<WSPluginDispatchRule> getRules() {
        if (rules == null) {
            synchronized (rulesLock) {
                if (rules == null) {
                    rules = generateRules();
                }
            }
        }
        return rules;
    }

    protected List<WSPluginDispatchRule> generateRules() {
        List<String> nestedProperties = domibusPropertyExtService.getAllNestedProperties(WSPLUGIN_PUSH_PREFIX);
        List<WSPluginDispatchRuleBuilder> builderSortedByIndex = nestedProperties
                .stream()
                .map(this::getIndex)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Integer::compareTo)
                .map(WSPluginDispatchRuleBuilder::new)
                .collect(toList());

        List<WSPluginDispatchRule> result = new ArrayList<>();
        for (WSPluginDispatchRuleBuilder ruleBuilder : builderSortedByIndex) {
            ruleBuilder.withDescription(domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + ruleBuilder.getIndex() + PUSH_RULE_DESCRIPTION));
            ruleBuilder.withRecipient(domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + ruleBuilder.getIndex() + PUSH_RULE_RECIPIENT));
            ruleBuilder.withEndpoint(domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + ruleBuilder.getIndex() + PUSH_RULE_ENDPOINT));
            ruleBuilder.withType(getTypes(domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + ruleBuilder.getIndex() + PUSH_RULE_TYPE)));
            setRetryInformation(ruleBuilder, domibusPropertyExtService.getProperty(PUSH_RULE_PREFIX + ruleBuilder.getIndex() + PUSH_RULE_RETRY));
            result.add(ruleBuilder.build());
        }

        return result;
    }

    protected List<WSBackendMessageType> getTypes(String property) {
        List<WSBackendMessageType> result = new ArrayList<>();
        String[] messageTypes = StringUtils.split(RegExUtils.replaceAll(property, " ",""), ",");

        for (String type : messageTypes) {
            try {
                result.add(WSBackendMessageType.valueOf(type));
            } catch (Exception e) {
                throw new WSPluginException("Type does not exists [" + type + "]. It should be one of "+ Arrays.toString(WSBackendMessageType.values()), e);
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
    public List<WSPluginDispatchRule> getRules(String recipient) {
        return getRules()
                .stream()
                .filter(wsPluginDispatchRule -> equalsAnyIgnoreCase(recipient, wsPluginDispatchRule.getRecipient()))
                .collect(Collectors.toList());
    }

    protected void setRetryInformation(WSPluginDispatchRuleBuilder ruleBuilder, String property) {
        ruleBuilder.withRetry(property);
        try {
            if (StringUtils.isNotBlank(property)) {
                String[] retryValues = StringUtils.split(property, ";");
                ruleBuilder.withRetryTimeout(Integer.parseInt(retryValues[0]));
                ruleBuilder.withRetryCount(Integer.parseInt(retryValues[1]));
                ruleBuilder.withRetryStrategy(WSPluginRetryStrategy.valueOf(retryValues[2]));
            }
        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
            throw new WSPluginException(
                    "The format of the property [" + PUSH_RULE_PREFIX + ruleBuilder.getIndex() + PUSH_RULE_RETRY + "] " +
                            "is incorrect :[" + property + "]. " +
                            "Format: retryTimeout;retryCount;(CONSTANT - SEND_ONCE) (ex: 4;12;CONSTANT)", e);
        }
    }

    protected Integer getIndex(String s) {
        String s1 = StringUtils.substringAfter(s, PUSH_RULE_PREFIX);
        if (StringUtils.isBlank(s1)) {
            return null;
        }
        String s2 = StringUtils.substringBefore(s1, ".");
        if (StringUtils.isBlank(s2)) {
            return null;
        }
        return Integer.parseInt(s2);
    }

}
