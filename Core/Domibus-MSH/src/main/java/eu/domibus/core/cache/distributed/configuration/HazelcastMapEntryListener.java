package eu.domibus.core.cache.distributed.configuration;

import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.MapEvent;
import com.hazelcast.map.listener.*;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import java.io.Serializable;

/**
 * @author Cosmin Baciu
 * @since 5.1
 */
public class HazelcastMapEntryListener implements
        EntryAddedListener<String, String>,
        EntryRemovedListener<String, String>,
        EntryUpdatedListener<String, String>,
        EntryEvictedListener<String, String>,
        EntryLoadedListener<String, String>,
        MapEvictedListener,
        MapClearedListener,
        EntryExpiredListener,
        Serializable {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(HazelcastMapEntryListener.class);

    @Override
    public void entryAdded(EntryEvent<String, String> event) {
        LOG.debug("Distributed cache entry added [{}]", event);
    }

    @Override
    public void entryRemoved(EntryEvent<String, String> event) {
        LOG.debug("Distributed cache entry removed [{}]", event);
    }

    @Override
    public void entryUpdated(EntryEvent<String, String> event) {
        LOG.debug("Distributed cache entry updated [{}]", event);
    }

    @Override
    public void entryEvicted(EntryEvent<String, String> event) {
        LOG.debug("Distributed cache entry evicted [{}]", event);
    }

    @Override
    public void entryLoaded(EntryEvent<String, String> event) {
        LOG.debug("Distributed cache entry loaded [{}]", event);
    }

    @Override
    public void entryExpired(EntryEvent event) {
        LOG.debug("Distributed cache entry expired [{}]", event);
    }

    @Override
    public void mapEvicted(MapEvent event) {
        LOG.debug("Distributed cache map evicted [{}]", event);
    }

    @Override
    public void mapCleared(MapEvent event) {
        LOG.debug("Distributed cache map cleared [{}]", event);
    }


}