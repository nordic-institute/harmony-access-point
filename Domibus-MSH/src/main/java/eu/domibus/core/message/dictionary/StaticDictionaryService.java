package eu.domibus.core.message.dictionary;

import eu.domibus.core.jms.DomainsAware;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public interface StaticDictionaryService extends DomainsAware {

    void createStaticDictionaryEntries();
}
