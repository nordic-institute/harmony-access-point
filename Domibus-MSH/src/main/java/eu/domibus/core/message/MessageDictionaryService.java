package eu.domibus.core.message;

import eu.domibus.api.model.AgreementRefEntity;
import eu.domibus.api.model.PartProperty;

public interface MessageDictionaryService {
    AgreementRefEntity findOrCreateAgreement(String value, String type);
    PartProperty findOrCreatePartProperty(final String name, String value, String type);
}
