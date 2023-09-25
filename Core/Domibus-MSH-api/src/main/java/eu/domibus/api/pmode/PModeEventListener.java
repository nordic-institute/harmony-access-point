package eu.domibus.api.pmode;

import java.security.cert.X509Certificate;
import java.util.List;

public interface PModeEventListener {

    String getName();

    void onRefreshPMode();


    void afterDynamicDiscoveryLookup(String finalRecipientValue,
                                     String finalRecipientUrl,
                                     String partyName,
                                     String partyType,
                                     List<String> partyProcessNames,
                                     String certificateCn,
                                     final X509Certificate certificate);
}
