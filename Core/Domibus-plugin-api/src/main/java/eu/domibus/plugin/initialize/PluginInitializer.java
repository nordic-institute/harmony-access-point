package eu.domibus.plugin.initialize;

/**
 * Interface to be implemented by Domibus plugins/extensions to perform initialization logic.
 * The methods of this interface are executed by Domibus after Spring context has been initialized.
 *
 * @author Cosmin Baciu
 * @since 5.1.1
 */
public interface PluginInitializer {

    /**
     * Returns the initializer name. It can be set to the plugin/extension name or any other value.
     * @return The initializer name
     */
    String getName();

    /**
     * Executes without any locking mechanism. In a cluster environment it is possible that this method executes in parallel on multiple nodes from the cluster.
     */
    void initializeNonSynchronized();

    /**
     * Executes with locking mechanism only in a cluster environment, in a single node it executes without locking. In a cluster environment only one node executes this method at any given time.
     */
    void initializeWithLockIfNeeded();
}
