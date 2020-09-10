package eu.domibus.plugin.jms;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.services.DomainContextExtService;
import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.messaging.MessageNotFoundException;
import eu.domibus.plugin.Submission;
import eu.domibus.plugin.handler.MessageRetriever;
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

    protected MessageRetriever messageRetriever;

    public BackendJMSQueueService(DomibusPropertyExtService domibusPropertyExtService,
                                  DomainContextExtService domainContextExtService,
                                  MessageRetriever messageRetriever) {
        this.domibusPropertyExtService = domibusPropertyExtService;
        this.domainContextExtService = domainContextExtService;
        this.messageRetriever = messageRetriever;
    }

    /**
     * Tries to get first the configured queue using routing properties. Returns the default queue in case no routing queue is found.
     *
     * @param messageId                  The message id for which the queue is determined
     * @param defaultQueueProperty       The property name used to get the default queue
     * @param routingQueuePrefixProperty The property prefix used to get the routing queue
     * @return the default or the routing queue
     * @throws DefaultJmsPluginException in case the queue could not be determined or the message is not found
     */
    public String getJMSQueue(String messageId, String defaultQueueProperty, String routingQueuePrefixProperty) {
        Submission submission;
        try {
            submission = messageRetriever.browseMessage(messageId);
        } catch (MessageNotFoundException e) {
            throw new DefaultJmsPluginException("Could not find message with id [" + messageId + "]", e);
        }
        QueueContext queueContext = new QueueContext(messageId, submission.getService(), submission.getAction());
        return getJMSQueue(queueContext, defaultQueueProperty, routingQueuePrefixProperty);
    }

    /**
     * Tries to get first the configured queue using routing properties. Returns the default queue in case no routing queue is found.
     *
     * @param queueContext               The queue context used for determining the queue
     * @param defaultQueueProperty       The property name used to get the default queue
     * @param routingQueuePrefixProperty The property prefix used to get the routing queue
     * @return the default or the routing queue
     * @throws DefaultJmsPluginException in case the queue could not be determined
     */
    public String getJMSQueue(QueueContext queueContext, String defaultQueueProperty, String routingQueuePrefixProperty) {
        String queueValue = getQueueValue(queueContext, defaultQueueProperty, routingQueuePrefixProperty);
        if (StringUtils.isEmpty(queueValue)) {
            throw new DefaultJmsPluginException("Error getting the queue value for default property [" + defaultQueueProperty + "] and routing queue prefix [" + routingQueuePrefixProperty + "]");
        }
        return queueValue;
    }

    /**
     * Tries to get first the configured queue using routing properties. Returns the default queue in case no routing queue is found.
     *
     * @param queueContext               The queue context used for determining the queue
     * @param defaultQueueProperty       The property name used to get the default queue
     * @param routingQueuePrefixProperty The property prefix used to get the routing queue
     * @return the default or the routing queue
     */
    protected String getQueueValue(QueueContext queueContext, String defaultQueueProperty, String routingQueuePrefixProperty) {
        final DomainDTO currentDomain = domainContextExtService.getCurrentDomain();
        List<String> routingQueuePrefixNameList = domibusPropertyExtService.getNestedProperties(routingQueuePrefixProperty);

        if (CollectionUtils.isEmpty(routingQueuePrefixNameList)) {
            final String queueValue = domibusPropertyExtService.getProperty(currentDomain, defaultQueueProperty);
            LOG.debug("Using queue [{}] configured for property [{}]", queueValue, defaultQueueProperty);
            return queueValue;
        }

        String routingQueueValue = getRoutingQueueValue(routingQueuePrefixNameList, routingQueuePrefixProperty, queueContext, currentDomain);
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
     * @param routingQueuePrefixList     The routing queue properties with a specific prefix
     * @param routingQueuePrefixProperty
     * @param queueContext
     * @param currentDomain
     * @return
     */
    protected String getRoutingQueueValue(List<String> routingQueuePrefixList, String routingQueuePrefixProperty, QueueContext queueContext, DomainDTO currentDomain) {
        for (String routingQueuePrefixName : routingQueuePrefixList) {
            String queueValue = getRoutingQueue(routingQueuePrefixProperty, routingQueuePrefixName, queueContext, currentDomain);
            if (StringUtils.isNotBlank(queueValue)) {
                return queueValue;
            }
        }
        return null;
    }

    /**
     * Returns the routing queue for which the configured service and action value matches the values from the Submission.
     *
     * @param routingQueuePrefixProperty The routing queue prefix property eg jmsplugin.queue.reply.routing
     * @param routingQueuePrefixName     The routing queue prefix name eg routingQueuePrefixName=rule1
     * @param queueContext               The queue context used for determining the queue
     * @param currentDomain              the current domain
     * @return the routing queue in case it matches or null otherwise
     */
    protected String getRoutingQueue(String routingQueuePrefixProperty, String routingQueuePrefixName, QueueContext queueContext, DomainDTO currentDomain) {
        String servicePropertyName = getQueuePropertyName(routingQueuePrefixProperty, routingQueuePrefixName, "service");
        String service = domibusPropertyExtService.getProperty(currentDomain, servicePropertyName);
        LOG.debug("Determined service value [{}] using property [{}]", service, servicePropertyName);

        String actionPropertyName = getQueuePropertyName(routingQueuePrefixProperty, routingQueuePrefixName, "action");
        String action = domibusPropertyExtService.getProperty(currentDomain, actionPropertyName);
        LOG.debug("Determined action value [{}] using property [{}]", action, actionPropertyName);

        boolean matches = matchesQueueContext(service, action, queueContext);
        if (!matches) {
            LOG.debug("Service [{}] and action [{}] pair does not matches the queue context [{}]", service, action, queueContext);
            return null;
        }
        LOG.debug("Service [{}] and action [{}] pair matches queue context [{}]", service, action, queueContext);

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
        return routingQueuePrefixProperty + "." + routingQueuePrefixName + "." + suffix;
    }

    protected boolean matchesQueueContext(String service, String action, QueueContext queueContext) {
        if (StringUtils.isNotBlank(service) && StringUtils.isNotBlank(action)) {
            LOG.debug("Matching submission using service and action");
            return matchesService(service, queueContext) && matchesAction(action, queueContext);
        }
        if (StringUtils.isNotBlank(service)) {
            LOG.debug("Matching submission using only service");
            return matchesService(service, queueContext);
        }
        if (StringUtils.isNotBlank(action)) {
            LOG.debug("Matching submission using only action");
            return matchesAction(action, queueContext);
        }
        LOG.debug("Submission not matched: both service and action are null");
        return false;
    }

    protected boolean matchesService(String service, QueueContext queueContext) {
        boolean serviceMatches = StringUtils.equals(service, queueContext.getService());
        LOG.debug("Service [{}] matches Submission with message id [{}]?  [{}]", service, queueContext.getMessageId(), serviceMatches);
        return serviceMatches;
    }

    protected boolean matchesAction(String action, QueueContext queueContext) {
        boolean actionMatches = StringUtils.equals(action, queueContext.getAction());
        LOG.debug("Action [{}] matches Submission with message id [{}]? [{}]", action, queueContext.getMessageId(), actionMatches);
        return actionMatches;
    }
}
