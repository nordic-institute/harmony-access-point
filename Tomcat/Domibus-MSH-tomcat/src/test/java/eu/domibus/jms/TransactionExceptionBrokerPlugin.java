package eu.domibus.jms;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.transaction.xa.XAException;

import org.apache.activemq.broker.BrokerPluginSupport;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.TransactionId;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

/**
 * <p>An ActiveMQ broker plugin intended to intercept any XAException usually happening when a MessageListenerContainer
 * fails to shutdown in a clean fashion.</p><br />
 *
 * <p>This plugin intercepts XAException having a particular message suffix (i.e. {@value #XA_EXCEPTION_MESSAGE_PREFIX}).
 * In case such an exception occurs, it rollbacks the current transaction and prevents any further commits. Tests can
 * autowired this plugin and inspect whether the exception has occurred or not, using {@link #isTestFailed()}.</p>
 *
 * @author Sebastian-Ion TINCU
 * @since 4.2
 */
@Component
public class TransactionExceptionBrokerPlugin extends BrokerPluginSupport {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TransactionExceptionBrokerPlugin.class);

    public static final String XA_EXCEPTION_MESSAGE_PREFIX = "XAException: Cannot do 2 phase commit if the transaction has not been prepared";

    private AtomicBoolean exceptionThrown = new AtomicBoolean();

    @Override
    public void commitTransaction(ConnectionContext context, TransactionId xid, boolean onePhase) throws Exception {
        if(exceptionThrown.get()) {
            LOG.info("Exception has been previously thrown so preventing commit");
            context.getBroker().rollbackTransaction(context, xid);
            return;
        }

        try {
            super.commitTransaction(context, xid, onePhase);
        } catch (XAException exception) {
            if (ExceptionUtils.getMessage(exception).startsWith(XA_EXCEPTION_MESSAGE_PREFIX)) {
                LOG.error("An XAException occurred stating that the 2 phase commit cannot be completed because the XA transaction has not yet been prepared");
                exceptionThrown.set(true);
            }
            throw exception;
        }
    }

    public boolean isTestFailed() {
        return exceptionThrown.get();
    }
}