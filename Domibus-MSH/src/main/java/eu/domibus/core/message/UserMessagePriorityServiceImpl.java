package eu.domibus.core.message;

import eu.domibus.api.message.UserMessageException;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.property.DomibusPropertyMetadataManagerSPI;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
@Service
public class UserMessagePriorityServiceImpl implements UserMessagePriorityService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(UserMessagePriorityServiceImpl.class);
    public static final String ACTION_LIST_SEPARATOR = ",";

    protected DomainContextProvider domainContextProvider;
    protected DomibusPropertyProvider domibusPropertyProvider;

    public UserMessagePriorityServiceImpl(DomainContextProvider domainContextProvider,
                                          DomibusPropertyProvider domibusPropertyProvider) {
        this.domainContextProvider = domainContextProvider;
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    /**
     * Tries to get first the configured priority with matches the provided service and action.
     *
     * @param userMessageService The UserMessage service
     * @param userMessageAction  The UserMessage action
     * @return the configured priority or null otherwise
     */
    @Override
    public Integer getPriority(String userMessageService, String userMessageAction) {
        List<String> priorityRuleNames = domibusPropertyProvider.getNestedProperties(DomibusPropertyMetadataManagerSPI.DOMIBUS_DISPATCHER_PRIORITY);

        if (CollectionUtils.isEmpty(priorityRuleNames)) {
            LOG.debug("No dispatcher priority rules defined");
            return null;
        }

        String priorityValueString = getPriorityValue(priorityRuleNames, userMessageService, userMessageAction);
        if (StringUtils.isBlank(priorityValueString)) {
            LOG.debug("No dispatcher priority found for service [{}] and action [{}]", userMessageService, userMessageAction);
            return null;
        }
        Integer priorityValue = convertPriorityToInteger(priorityValueString);
        validatePriority(priorityValue);
        return priorityValue;
    }

    protected Integer convertPriorityToInteger(String priorityValueString) {
        Integer priorityValue = null;
        try {
            priorityValue = Integer.valueOf(priorityValueString);
        } catch (NumberFormatException e) {
            throw new UserMessageException("Priority [" + priorityValueString + "] is not a valid integer");
        }
        return priorityValue;
    }

    protected void validatePriority(Integer priorityValue) {
        if (priorityValue < 0 || priorityValue > 9) {
            throw new UserMessageException("Priority [" + priorityValue + "] is out of range");
        }
    }


    /**
     * Tries to get first the configured priority with matches the provided service and action.
     *
     * @param priorityRuleNames  The dispatcher priority rule names
     * @param userMessageService The UserMessage service
     * @param userMessageAction  The UserMessage action
     * @return the configured priority or null otherwise
     */
    protected String getPriorityValue(List<String> priorityRuleNames, String userMessageService, String userMessageAction) {
        for (String priorityRuleName : priorityRuleNames) {
            String priority = getMatchingPriority(priorityRuleName, userMessageService, userMessageAction);
            if (StringUtils.isNotBlank(priority)) {
                return priority;
            }
        }
        return null;
    }

    /**
     * Tries to get first the configured priority with matches the provided service and action.
     *
     * @param priorityRuleName   The dispatcher priority rule name
     * @param userMessageService The UserMessage service
     * @param userMessageAction  The UserMessage action
     * @return the configured priority or null otherwise
     */
    protected String getMatchingPriority(String priorityRuleName, String userMessageService, String userMessageAction) {
        String servicePropertyName = getPriorityPropertyName(DomibusPropertyMetadataManagerSPI.DOMIBUS_DISPATCHER_PRIORITY, priorityRuleName, "service");
        String servicePropertyValue = domibusPropertyProvider.getProperty(servicePropertyName);
        LOG.debug("Determined service value [{}] using property [{}]", servicePropertyValue, servicePropertyName);

        String actionPropertyName = getPriorityPropertyName(DomibusPropertyMetadataManagerSPI.DOMIBUS_DISPATCHER_PRIORITY, priorityRuleName, "action");
        String actionPropertyValue = domibusPropertyProvider.getProperty(actionPropertyName);
        LOG.debug("Determined action value [{}] using property [{}]", actionPropertyValue, actionPropertyName);

        boolean matches = matchesServiceAndAction(userMessageService, userMessageAction, servicePropertyValue, actionPropertyValue);
        if (!matches) {
            LOG.debug("Property values pair: service [{}] and action [{}] does not matches UserMessage service [{}] and action [{}] ", servicePropertyValue, actionPropertyValue, userMessageService, userMessageAction);
            return null;
        }
        LOG.debug("Property values pair: service [{}] and action [{}] pair matches UserMessage service [{}] and action [{}] ", servicePropertyValue, actionPropertyValue, userMessageService, userMessageAction);

        String priorityPropertyName = getPriorityPropertyName(DomibusPropertyMetadataManagerSPI.DOMIBUS_DISPATCHER_PRIORITY, priorityRuleName, "value");
        String priorityPropertyValue = domibusPropertyProvider.getProperty(priorityPropertyName);
        LOG.debug("Determined priority value [{}] using property [{}]", priorityPropertyValue, priorityPropertyName);

        if (StringUtils.isEmpty(priorityPropertyValue)) {
            throw new UserMessageException("No configured priority value found for property [" + priorityPropertyName + "]");
        }
        return priorityPropertyValue;
    }

    /**
     * Checks if the UserMessage service and action matches the provided service and action
     *
     * @param userMessageService   The UserMessage service
     * @param userMessageAction    The UserMessage action
     * @param servicePropertyValue The provided service value
     * @param actionPropertyValue  The provided list of actions, separated by comma
     * @return true if there is a match
     */
    protected boolean matchesServiceAndAction(String userMessageService, String userMessageAction, String servicePropertyValue, String actionPropertyValue) {
        if (StringUtils.isNotBlank(servicePropertyValue) && StringUtils.isNotBlank(actionPropertyValue)) {
            LOG.debug("Matching using service and action");
            return matchesService(userMessageService, servicePropertyValue) && matchesActionList(userMessageAction, actionPropertyValue);
        }
        if (StringUtils.isNotBlank(servicePropertyValue)) {
            LOG.debug("Matching using only service");
            return matchesService(userMessageService, servicePropertyValue);
        }
        if (StringUtils.isNotBlank(actionPropertyValue)) {
            LOG.debug("Matching using only action");
            return matchesActionList(userMessageAction, actionPropertyValue);
        }
        LOG.debug("Not matching: both service and action are null");
        return false;
    }

    protected boolean matchesService(String userMessageService, String servicePropertyValue) {
        boolean serviceMatches = StringUtils.equalsIgnoreCase(userMessageService, servicePropertyValue);
        LOG.debug("Property service value [{}] matches UserMessage service [{}]?  [{}]", servicePropertyValue, userMessageService, serviceMatches);
        return serviceMatches;
    }

    /**
     * Checks if the UserMessage action matches the provided action
     *
     * @param userMessageAction   The UserMessage action
     * @param actionPropertyValue The list of actions separated by comma
     * @return true if any of provided list of actions matches the UserMessage action
     */
    protected boolean matchesActionList(String userMessageAction, String actionPropertyValue) {
        String[] actionList = StringUtils.split(actionPropertyValue, ACTION_LIST_SEPARATOR);
        if (ArrayUtils.isEmpty(actionList)) {
            LOG.debug("Provided action value is empty or does not contain a list of actions separated by [{}]", ACTION_LIST_SEPARATOR);
            return false;
        }
        for (String actionValue : actionList) {
            boolean matchesAction = matchesAction(userMessageAction, StringUtils.trim(actionValue));
            if (matchesAction) {
                return true;
            }
        }
        LOG.debug("Property action value [{}] does not match UserMessage action [{}]", actionPropertyValue, userMessageAction);
        return false;
    }

    protected boolean matchesAction(String userMessageAction, String actionPropertyValue) {
        boolean actionMatches = StringUtils.equalsIgnoreCase(userMessageAction, actionPropertyValue);
        LOG.debug("Property action value [{}] matches UserMessage action [{}]? [{}]", actionPropertyValue, userMessageAction, actionMatches);
        return actionMatches;
    }

    /**
     * Composes the property name using the given parameters
     */
    protected String getPriorityPropertyName(String propertyPrefix, String priorityRuleName, String suffix) {
        return propertyPrefix + "." + priorityRuleName + "." + suffix;
    }

}
