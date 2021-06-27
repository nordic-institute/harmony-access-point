package eu.domibus.core.message;

import eu.domibus.api.model.*;

public interface MessageDictionaryService {

    AgreementRefEntity findOrCreateAgreement(String value, String type);

    PartProperty findOrCreatePartProperty(final String name, String value, String type);

    MessageProperty findOrCreateMessageProperty(final String name, String value, String type);

    PartyId findOrCreateParty(String value, String type);

    PartyRole findOrCreateRole(String value);

    ActionEntity findOrCreateAction(String value);

    ServiceEntity findOrCreateService(String value, String type);

    MpcEntity findOrCreateMpc(String value);

    TimezoneOffset findOrCreateTimezoneOffset(String timezoneId, int offsetSeconds);

    void createStaticDictionaryEntries();
}
