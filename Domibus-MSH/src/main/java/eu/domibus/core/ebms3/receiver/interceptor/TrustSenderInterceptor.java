package eu.domibus.core.ebms3.receiver.interceptor;

import com.google.common.collect.Lists;
import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.pki.DomibusCertificateException;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.common.ErrorCode;
import eu.domibus.common.MSHRole;
import eu.domibus.core.certificate.CertificateExchangeType;
import eu.domibus.core.crypto.Wss4JMultiDomainCryptoProvider;
import eu.domibus.core.ebms3.EbMS3Exception;
import eu.domibus.core.ebms3.receiver.token.BinarySecurityTokenReference;
import eu.domibus.core.ebms3.receiver.token.TokenReference;
import eu.domibus.core.ebms3.receiver.token.TokenReferenceExtractor;
import eu.domibus.core.ebms3.sender.client.DispatchClientDefaultProvider;
import eu.domibus.core.ebms3.sender.client.MSHDispatcher;
import eu.domibus.core.message.MessageExchangeConfiguration;
import eu.domibus.ebms3.common.model.MessageInfo;
import eu.domibus.ebms3.common.model.MessageType;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.binding.soap.SoapFault;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.MessageUtils;
import org.apache.cxf.ws.security.wss4j.CXFRequestData;
import org.apache.cxf.ws.security.wss4j.StaxSerializer;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.WSDocInfo;
import org.apache.wss4j.dom.engine.WSSConfig;
import org.apache.wss4j.dom.engine.WSSecurityEngine;
import org.apache.wss4j.dom.str.EncryptedKeySTRParser;
import org.apache.wss4j.dom.str.STRParserParameters;
import org.apache.wss4j.dom.str.STRParserResult;
import org.apache.wss4j.dom.util.WSSecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONRECEIVING;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING;

/**
 * This interceptor is responsible of the trust of an incoming messages.
 * Useful info on this topic are here: http://tldp.org/HOWTO/SSL-Certificates-HOWTO/x64.html
 *
 * @author Martini Federico
 * @since 3.3
 */
@Service
public class TrustSenderInterceptor extends WSS4JInInterceptor {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TrustSenderInterceptor.class);

    public static final QName KEYINFO = new QName("http://www.w3.org/2000/09/xmldsig#", "KeyInfo");

    public static final String X_509_V_3 = "X509v3";

    public static final String X_509_PKIPATHV_1 = "X509PKIPathv1";

    public static final String ID = "Id";

    @Autowired
    protected DomibusPropertyProvider domibusPropertyProvider;

    @Autowired
    protected Wss4JMultiDomainCryptoProvider crypto;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private TokenReferenceExtractor tokenReferenceExtractor;


    public TrustSenderInterceptor() {
        super(false);
    }

    /**
     * Intercepts a message to verify that the sender is trusted.
     * <p>
     * There will be two validations:
     * a) the sender certificate is valid and not revoked and
     * b) the sender party name is included in the CN of the certificate
     *
     * @param message the incoming CXF soap message to handle
     */
    @Override
    public void handleMessage(final SoapMessage message) throws Fault {
        if (!domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING)) {
            LOG.warn("No trust verification of sending certificate");
            return;
        }
        String messageId = (String) message.getExchange().get(MessageInfo.MESSAGE_ID_CONTEXT_PROPERTY);
        if (!isMessageSecured(message)) {
            LOG.debug("Message does not contain security info ==> skipping sender trust verification.");
            return;
        }

        boolean isPullMessage = false;
        MessageType messageType = (MessageType) message.get(MSHDispatcher.MESSAGE_TYPE_IN);
        if (messageType != null && messageType.equals(MessageType.SIGNAL_MESSAGE)) {
            LOG.debug("PULL Signal Message");
            isPullMessage = true;
        }

        String senderPartyName;
        String receiverPartyName;
        if (isPullMessage) {
            senderPartyName = getReceiverPartyName(message);
            receiverPartyName = getSenderPartyName(message);
        } else {
            senderPartyName = getSenderPartyName(message);
            receiverPartyName = getReceiverPartyName(message);
        }
        LOG.putMDC(DomibusLogger.MDC_FROM, senderPartyName);
        LOG.putMDC(DomibusLogger.MDC_TO, receiverPartyName);

        LOG.debug("Validating sender certificate for party [{}]", senderPartyName);
        List<? extends Certificate> certificateChain = getSenderCertificateChain(message);

        if (!checkCertificateValidity(certificateChain, senderPartyName, isPullMessage)) {
            EbMS3Exception ebMS3Ex = new EbMS3Exception(ErrorCode.EbMS3ErrorCode.EBMS_0101, "Sender [" + senderPartyName + "] certificate is not valid or has been revoked", messageId, null);
            ebMS3Ex.setMshRole(MSHRole.RECEIVING);
            throw new Fault(ebMS3Ex);
        }
    }

    protected Boolean checkCertificateValidity(List<? extends Certificate> certificateChain, String sender, boolean isPullMessage) {
        if (domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONRECEIVING)) {
            LOG.debug("Validating sender certificate chain on receiving [{}]", certificateChain);
            try {
                if (!certificateService.isCertificateChainValid(certificateChain)) {
                    LOG.error("Cannot receive message: sender certificate is not valid or it has been revoked [" + sender + "]");
                    return false;
                }
                LOG.debug("[Pull:{}] - Sender certificate exists and is valid [{}]", isPullMessage, sender);
            } catch (DomibusCertificateException dce) {
                LOG.error("Could not verify if the certificate chain is valid for alias " + sender, dce);
                return false;
            }
        }
        return true;
    }

    private boolean isMessageSecured(SoapMessage msg) {
        try {
            final boolean messageSecure = (getSecurityHeader(msg) == null) ? false : true;
            if (!messageSecure) {
                msg.put(CertificateExchangeType.getKey(), CertificateExchangeType.NONE.name());
            }
            return messageSecure;
        } catch (Exception ex) {
            LOG.error("Error while getting security info", ex);
            return false;
        }
    }

    private Element getSecurityHeader(SoapMessage msg) throws SOAPException, WSSecurityException {

        SOAPMessage doc = msg.getContent(SOAPMessage.class);
        return WSSecurityUtil.getSecurityHeader(doc.getSOAPHeader(), null, true);
    }

    private String getSenderPartyName(SoapMessage message) {
        List<String> contents = getPmodeKeyValues(message);
        if (CollectionUtils.isNotEmpty(contents)) {
            return contents.get(0);
        }
        return null;
    }

    private String getReceiverPartyName(SoapMessage message) {
        List<String> contents = getPmodeKeyValues(message);
        if (CollectionUtils.isNotEmpty(contents) && contents.size() > 1) {
            return contents.get(1);
        }
        return null;
    }

    protected List<String> getPmodeKeyValues(SoapMessage message) {
        String pmodeKey = (String) message.get(DispatchClientDefaultProvider.PMODE_KEY_CONTEXT_PROPERTY);
        if (StringUtils.isEmpty(pmodeKey)) {
            return null;
        }

        return Arrays.asList(StringUtils.splitByWholeSeparator(pmodeKey, MessageExchangeConfiguration.PMODEKEY_SEPARATOR));
    }

    protected List<? extends Certificate> getSenderCertificateChain(SoapMessage msg) {
        boolean utWithCallbacks = MessageUtils.getContextualBoolean(msg, "ws-security.validate.token", true);
        super.translateProperties(msg);
        CXFRequestData requestData = new CXFRequestData();
        WSSConfig config = (WSSConfig) msg.getContextualProperty(WSSConfig.class.getName());
        WSSecurityEngine engine;
        if (config != null) {
            engine = new WSSecurityEngine();
            engine.setWssConfig(config);
        } else {
            engine = super.getSecurityEngine(utWithCallbacks);
            if (engine == null) {
                engine = new WSSecurityEngine();
            }
            config = engine.getWssConfig();
        }

        requestData.setWssConfig(config);
        requestData.setEncryptionSerializer(new StaxSerializer());

        SoapVersion version = msg.getVersion();
        SAAJInInterceptor.INSTANCE.handleMessage(msg);
        try {
            requestData.setMsgContext(msg);
            decodeAlgorithmSuite(requestData);
            requestData.setDecCrypto(crypto);
            // extract certificate from KeyInfo
            final Element securityHeader = getSecurityHeader(msg);
            final TokenReference tokenReference = tokenReferenceExtractor.extractTokenReference(securityHeader);
            if (tokenReference == null) {
                msg.put(CertificateExchangeType.getKey(), CertificateExchangeType.KEY_INFO.name());
                final List<? extends Certificate> certificateChain = getCertificateFromKeyInfo(requestData, securityHeader);
                if (CollectionUtils.isEmpty(certificateChain)) {
                    throw new SoapFault("CertificateException: Could not extract the certificate for validation", version.getSender());
                }
                addSerializedCertificateToMessage(msg, certificateChain, CertificateExchangeType.KEY_INFO);
                return certificateChain;
            } else {
                BinarySecurityTokenReference binarySecurityTokenReference = (BinarySecurityTokenReference) tokenReference;
                final List<? extends Certificate> certificateChain = getCertificateFromBinarySecurityToken(securityHeader, binarySecurityTokenReference);
                addSerializedCertificateToMessage(msg, certificateChain, CertificateExchangeType.BINARY_SECURITY_TOKEN);
                final Certificate certificate = certificateService.extractLeafCertificateFromChain(certificateChain);
                if (certificate == null) {
                    throw new SoapFault("CertificateException: Could not extract the certificate for validation", version.getSender());
                }
                return certificateChain;
            }
        } catch (CertificateException certEx) {
            throw new SoapFault("CertificateException", certEx, version.getSender());
        } catch (WSSecurityException wssEx) {
            throw new SoapFault("WSSecurityException", wssEx, version.getSender());
        } catch (SOAPException soapEx) {
            throw new SoapFault("SOAPException", soapEx, version.getSender());
        } catch (URISyntaxException uriEx) {
            throw new SoapFault("SOAPException", uriEx, version.getSender());
        }
    }

    private void addSerializedCertificateToMessage(SoapMessage msg, final List<? extends Certificate> certificateChain, CertificateExchangeType binarySecurityToken) {
        msg.put(CertificateExchangeType.getKey(), binarySecurityToken.name());
        final String chain = certificateService.serializeCertificateChainIntoPemFormat(certificateChain);
        msg.put(CertificateExchangeType.getValue(), chain);
    }

    protected String getTextFromElement(Element element) {
        StringBuffer buf = new StringBuffer();
        NodeList list = element.getChildNodes();
        boolean found = false;
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (node.getNodeType() == Node.TEXT_NODE) {
                buf.append(node.getNodeValue());
                found = true;
            }
        }
        return found ? buf.toString() : null;
    }

    protected List<? extends Certificate> getCertificateFromBinarySecurityToken(Element securityHeader, BinarySecurityTokenReference tokenReference) throws WSSecurityException, CertificateException, URISyntaxException {

        URI uri = new URI(tokenReference.getUri());
        URI valueTypeUri = new URI(tokenReference.getValueType());
        final String uriFragment = uri.getFragment();
        final String valueType = valueTypeUri.getFragment();
        LOG.debug("Signing binary token uri:[{}] and ValueType:[{}]", uriFragment, valueType);
        NodeList binarySecurityTokenElement = securityHeader.getElementsByTagNameNS(WSConstants.WSSE_NS, WSConstants.BINARY_TOKEN_LN);
        //NodeList binarySecurityTokenElement = securityHeader.getElementsByTagName("wsse:BinarySecurityToken");
        if (LOG.isDebugEnabled()) {
            Node item = null;
            if (binarySecurityTokenElement != null) {
                item = binarySecurityTokenElement.item(0);
            }
            LOG.debug("binarySecurityTokenElement:[{}], binarySecurityTokenElement.item:[{}]", binarySecurityTokenElement, item);
        }
        if (binarySecurityTokenElement == null || binarySecurityTokenElement.item(0) == null) {
            return null;
        }


        LOG.debug("binarySecurityTokenElement.length:[{}]", binarySecurityTokenElement.getLength());
        for (int i = 0; i < binarySecurityTokenElement.getLength(); i++) {
            final Node item = binarySecurityTokenElement.item(i);
            final NamedNodeMap attributes = item.getAttributes();
            final int length = attributes.getLength();
            LOG.debug("item:[{}], item attributes:[{}], attributes length:[{}]", item, attributes, length);
            Node id = null;
            for (int j = 0; j < length; j++) {
                final Node bstAttribute = attributes.item(j);
                LOG.debug("bstAttribute:[{}]", bstAttribute);
                if (ID.equalsIgnoreCase(bstAttribute.getLocalName())) {
                    id = bstAttribute;
                    break;
                }
            }

            if (LOG.isDebugEnabled()) {
                if (id != null) {
                    LOG.debug("id.getNodeValue:[{}], uriFragment:[{}]", id.getNodeValue(), uriFragment);
                }
                LOG.debug("id:[{}]", id);
            }

            if (id != null && uriFragment.equalsIgnoreCase(id.getNodeValue())) {
                String certString = getTextFromElement((Element) item);
                if (certString == null || certString.isEmpty()) {
                    LOG.debug("certString:[{}]", certString);
                    return null;
                }
                LOG.debug("certificate value type:[{}]", valueType);
                if (X_509_V_3.equalsIgnoreCase(valueType)) {
                    String certStr = ("-----BEGIN CERTIFICATE-----\n" + certString + "\n-----END CERTIFICATE-----\n");
                    InputStream in = new ByteArrayInputStream(certStr.getBytes());
                    CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                    final Certificate certificate = certFactory.generateCertificate(in);
                    return Lists.newArrayList(certificate);
                } else if (X_509_PKIPATHV_1.equalsIgnoreCase(valueType)) {
                    final byte[] bytes = Base64.decodeBase64(certString);
                    org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory certificateFactory = new org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory();
                    return certificateFactory.engineGenerateCertPath(new ByteArrayInputStream(bytes)).getCertificates();

                }
            }
        }
        return null;
    }


    protected List<? extends Certificate> getCertificateFromKeyInfo(CXFRequestData data, Element securityHeader) throws WSSecurityException {

        X509Certificate[] certs;

        EncryptedKeySTRParser decryptedBytes;
        Element secTokenRef = tokenReferenceExtractor.getSecTokenRef(securityHeader);
        /* CXF class which has to be initialized in order to parse the Security token reference */
        STRParserParameters encryptedEphemeralKey1 = new STRParserParameters();
        data.setWsDocInfo(new WSDocInfo(securityHeader.getOwnerDocument()));
        encryptedEphemeralKey1.setData(data);
        encryptedEphemeralKey1.setStrElement(secTokenRef);
        decryptedBytes = new EncryptedKeySTRParser();
        /* This Apache CXF call will look for a certificate in the Truststore whose Subject Key Identifier bytes matches the <wsse:SecurityTokenReference><wsse:KeyIdentifier> bytes */
        STRParserResult refList = decryptedBytes.parseSecurityTokenReference(encryptedEphemeralKey1);
        certs = refList.getCertificates();

        if (certs == null || certs.length < 1) {
            LOG.warn("No certificate found");
            return null;
        }
        return Arrays.asList(certs);
    }

}


