package eu.domibus.plugin.jms;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.handler.MessageRetriever;
import eu.domibus.plugin.jms.property.JmsPluginPropertyManager;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author Cosmin Baciu
 * @since 4.2
 */
public class BackendJMSQueueService {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(BackendJMSQueueService.class);

    protected DomibusPropertyExtService domibusPropertyExtService;

    protected DomainContextExtService domainContextExtService;

    protected JmsPluginPropertyManager jmsPluginPropertyManager;

    protected MessageRetriever messageRetriever;

    public BackendJMSQueueService(DomibusPropertyExtService domibusPropertyExtService,
                                  DomainContextExtService domainContextExtService,
                                  JmsPluginPropertyManager jmsPluginPropertyManager,
                                  MessageRetriever messageRetriever) {
        this.domibusPropertyExtService = domibusPropertyExtService;
        this.domainContextExtService = domainContextExtService;
        this.jmsPluginPropertyManager = jmsPluginPropertyManager;
        this.messageRetriever = messageRetriever;
    }

    public String getJMSQueue(String messageId, String defaultQueueProperty, String routingQueuePrefixProperty) {
        String queueValue = getQueueValue(messageId, defaultQueueProperty, routingQueuePrefixProperty);
        if (StringUtils.isEmpty(queueValue)) {
            throw new DefaultJmsPluginException("Error getting the queue value for default property [" + defaultQueueProperty + "] and routing queue prefix [" + routingQueuePrefixProperty + "]");
        }
        return queueValue;
    }

    /**
     * Tries to get first the configured queue using routing properties. Returns the default queue in case no routing queue is found.
     *
     * @param messageId
     * @param defaultQueueProperty
     * @param routingQueuePrefixProperty
     * @return
     */
    protected String getQueueValue(String messageId, String defaultQueueProperty, String routingQueuePrefixProperty) {
        Submission submission;
        try {
            submission = messageRetriever.browseMessage(messageId);
        } catch (MessageNotFoundException e) {
            throw new DefaultJmsPluginException("Could not find message with id [" + messageId + "]");
        }

        final DomainDTO currentDomain = domainContextExtService.getCurrentDomain();
        String routingQueuePrefix = getQueuePrefix(currentDomain, routingQueuePrefixProperty);
        List<String> routingQueuePrefixNameList = jmsPluginPropertyManager.getNestedProperties(routingQueuePrefix);

        if (CollectionUtils.isEmpty(routingQueuePrefixNameList)) {
            final String queueValue = domibusPropertyExtService.getProperty(currentDomain, defaultQueueProperty);
            LOG.debug("Using queue [{}] configured for property [{}]", queueValue, defaultQueueProperty);
            return queueValue;
        }

        String routingQueueValue = getRoutingQueueValue(routingQueuePrefixNameList, routingQueuePrefixProperty, submission, currentDomain);
        if (StringUtils.isNotBlank(routingQueueValue)) {
            return routingQueueValue;
        }
        final String queueValue = domibusPropertyExtService.getProperty(currentDomain, defaultQueueProperty);
        LOG.debug("No routing queue is matching. Using queue [{}] configured for property [{}]", queueValue, defaultQueueProperty);
        return queueValue;
    }

    /**
     * Tries to get the configured queue using routing properties. Returns the first matching routing queue if any.
     *
     * @param routingQueuePrefixList
     * @param routingQueuePrefixProperty
     * @param submission
     * @param currentDomain
     * @return
     */
    protected String getRoutingQueueValue(List<String> routingQueuePrefixList, String routingQueuePrefixProperty, Submission submission, DomainDTO currentDomain) {
        for (String routingQueuePrefixName : routingQueuePrefixList) {
            String queueValue = getRoutingQueue(routingQueuePrefixProperty, routingQueuePrefixName, submission, currentDomain);
            if (StringUtils.isNotBlank(queueValue)) {
                return queueValue;
            }
        }
        return null;
    }

    /**
     * Returns the routing queue for which the configured service and action value matches the values from the Submission.
     *
     * @param routingQueuePrefixProperty
     * @param routingQueuePrefixName
     * @param submission
     * @param currentDomain
     * @return
     */
    protected String getRoutingQueue(String routingQueuePrefixProperty, String routingQueuePrefixName, Submission submission, DomainDTO currentDomain) {
        String servicePropertyName = getQueuePropertyName(routingQueuePrefixProperty, routingQueuePrefixName, "service");
        String service = domibusPropertyExtService.getProperty(currentDomain, servicePropertyName);
        LOG.debug("Determined service value [{}] using property [{}]", service, servicePropertyName);


        String actionPropertyName = getQueuePropertyName(routingQueuePrefixProperty, routingQueuePrefixName, "action");
        String action = domibusPropertyExtService.getProperty(currentDomain, actionPropertyName);
        LOG.debug("Determined action value [{}] using property [{}]", action, actionPropertyName);

        boolean matches = matchesSubmission(service, action, submission);
        if (!matches) {
            LOG.debug("Service [{}] and action [{}] pair does not matches Submission [{}]", service, action, submission);
            return null;
        }
        LOG.debug("Service [{}] and action [{}] pair matches Submission [{}]", service, action, submission);

        String routingQueueNameProperty = getQueuePropertyName(routingQueuePrefixProperty, routingQueuePrefixName, "queue");
        String queueValue = domibusPropertyExtService.getProperty(currentDomain, routingQueueNameProperty);
        LOG.debug("Determined queue value [{}] using property [{}]", queueValue, routingQueueNameProperty);

        if (StringUtils.isEmpty(queueValue)) {
            throw new DefaultJmsPluginException("No configured queue found for property [" + routingQueueNameProperty + "]");
        }
        return queueValue;
    }


    /**
     * Composes the property name using the given parameters
     * <p>
     * Eg. Given routingQueuePrefixProperty=jmsplugin.queue.reply.routing, routingQueuePrefixName=rule1, suffix=service it will return jmsplugin.queue.reply.routing.rule1.service
     *
     * @param routingQueuePrefixProperty
     * @param routingQueuePrefixName
     * @param suffix
     * @return
     */
    protected String getQueuePropertyName(String routingQueuePrefixProperty, String routingQueuePrefixName, String suffix) {
        return routingQueuePrefixProperty + routingQueuePrefixName + "." + suffix;
    }

    /**
     * Composes the property name based on the domain and queue prefix
     * <p>
     * Eg. Given domain code = digit and queue prefix = jmsplugin.queue.reply.routing it will return digit.jmsplugin.queue.reply.routing
     *
     * @param currentDomain
     * @param queuePrefix
     * @return
     */
    protected String getQueuePrefix(DomainDTO currentDomain, String queuePrefix) {
        if (currentDomain == null) {
            LOG.debug("Using queue prefix [{}]", queuePrefix);
            return queuePrefix;
        }
        if(DomainDTO.DEFAULT_DOMAIN.equals(currentDomain)) {
            LOG.debug("Using queue prefix [{}]", queuePrefix);
            return queuePrefix;
        }
        String result = currentDomain.getCode() + "." + queuePrefix;
        LOG.debug("Using queue prefix [{}]", result);
        return result;
    }

    protected boolean matchesSubmission(String service, String action, Submission submission) {
        String submissionService = submission.getService();
        String submissionAction = submission.getAction();

        boolean serviceMatches = StringUtils.equals(service, submissionService);
        if (!serviceMatches) {
            LOG.debug("Service [{}] does not matches Submission with message id [{}]", service, submission.getMessageId());
            return false;
        }

        boolean actionMatches = StringUtils.equals(action, submissionAction);
        if (!actionMatches) {
            LOG.debug("Action [{}] does not matches Submission with message id [{}]", action, submission.getMessageId());
            return false;
        }
        return true;
    }
}
