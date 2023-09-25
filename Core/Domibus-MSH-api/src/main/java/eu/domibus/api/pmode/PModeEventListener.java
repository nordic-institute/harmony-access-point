package eu.domibus.api.pmode;

import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Listens to events triggered by the Pmode management eg refresh pmode, etc
 *
 * @author Cosmin Baciu
 * @since 5.1.1
 */
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
