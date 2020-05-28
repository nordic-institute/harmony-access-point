package eu.domibus.core.ebms3.receiver.interceptor;

import eu.domibus.api.pki.CertificateService;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.core.ebms3.receiver.token.BinarySecurityTokenReference;
import eu.domibus.core.ebms3.receiver.token.TokenReferenceExtractor;
import eu.domibus.core.ebms3.sender.interceptor.SoapInterceptorTest;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.core.pki.PKIUtil;
import eu.domibus.core.spring.SpringContextProvider;
import mockit.*;
import mockit.integration.junit4.JMockit;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.core.JmsOperations;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONRECEIVING;
import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING;

/**
 * @author idragusa
 * @since 4.0
 */
@RunWith(JMockit.class)
public class TrustSenderInterceptorTest extends SoapInterceptorTest {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TrustSenderInterceptorTest.class);

    private static final String RESOURCE_PATH = "src/test/resources/eu/domibus/ebms3/receiver/";

    private static final String X_509_V_3 = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509v3";

    @Injectable
    CertificateService certificateService;

    @Injectable
    DomibusPropertyProvider domibusPropertyProvider;

    @Injectable
    protected JAXBContext jaxbContextEBMS;

    @Injectable
    TokenReferenceExtractor tokenReferenceExtractor;

    @Tested
    TrustSenderInterceptor trustSenderInterceptor;

    @Bean
    @Qualifier("jmsTemplateCommand")
    public JmsOperations jmsOperations() {
        return Mockito.mock(JmsOperations.class);
    }

    PKIUtil pkiUtil = new PKIUtil();

    @Test
    public void testHandleMessageBinaryToken(@Mocked SpringContextProvider springContextProvider, @Mocked final Element securityHeader, @Mocked final BinarySecurityTokenReference binarySecurityTokenReference, @Mocked X509Certificate x509Certificate) throws XMLStreamException, ParserConfigurationException, JAXBException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, SOAPException, WSSecurityException {
        Document doc = readDocument("dataset/as4/SoapRequestBinaryToken.xml");
        String trustoreFilename = RESOURCE_PATH + "nonEmptySource.jks";
        String trustorePassword = "1234";

        new Expectations() {{
            tokenReferenceExtractor.extractTokenReference(withAny(securityHeader));
            result = binarySecurityTokenReference;
            binarySecurityTokenReference.getUri();
            result = "#X509-99bde7b7-932f-4dbd-82dd-3539ba51791b";
            binarySecurityTokenReference.getValueType();
            result = X_509_V_3;
            certificateService.extractLeafCertificateFromChain((List<X509Certificate>) any);
            result = x509Certificate;
        }};
        testHandleMessage(doc, trustoreFilename, trustorePassword);
    }

    @Test(expected = org.apache.cxf.interceptor.Fault.class)
    public void testSenderTrustFault(@Mocked SpringContextProvider springContextProvider, @Mocked final Element securityHeader, @Mocked final BinarySecurityTokenReference binarySecurityTokenReference, @Mocked X509Certificate x509Certificate) throws XMLStreamException, ParserConfigurationException, JAXBException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, SOAPException, WSSecurityException {
        Document doc = readDocument("dataset/as4/SoapRequestBinaryToken.xml");
        String trustoreFilename = RESOURCE_PATH + "nonEmptySource.jks";
        String trustorePassword = "1234";

        new Expectations() {{
            tokenReferenceExtractor.extractTokenReference(withAny(securityHeader));
            result = binarySecurityTokenReference;
            binarySecurityTokenReference.getUri();
            result = "#X509-99bde7b7-932f-4dbd-82dd-3539ba51791b";
            binarySecurityTokenReference.getValueType();
            result = X_509_V_3;
            certificateService.isCertificateChainValid((List<Certificate>) any);
            result = false;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONRECEIVING);
            result = true;
            certificateService.extractLeafCertificateFromChain((List<X509Certificate>) any);
            result = x509Certificate;
        }};
        testHandleMessage(doc, trustoreFilename, trustorePassword);
    }

    @Test
    public void testSenderTrustNoSenderVerification(@Mocked SpringContextProvider springContextProvider, @Mocked final Element securityHeader, @Mocked BinarySecurityTokenReference binarySecurityTokenReference, @Mocked X509Certificate x509Certificate) throws XMLStreamException, ParserConfigurationException, JAXBException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, SOAPException, WSSecurityException {
        Document doc = readDocument("dataset/as4/SoapRequestBinaryToken.xml");
        String trustoreFilename = RESOURCE_PATH + "nonEmptySource.jks";
        String trustorePassword = "1234";

        new Expectations() {{
            tokenReferenceExtractor.extractTokenReference(withAny(securityHeader));
            result = binarySecurityTokenReference;
            binarySecurityTokenReference.getUri();
            result = "#X509-99bde7b7-932f-4dbd-82dd-3539ba51791b";
            binarySecurityTokenReference.getValueType();
            result = X_509_V_3;
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONRECEIVING);
            result = false;
            certificateService.extractLeafCertificateFromChain((List<X509Certificate>) any);
            result = x509Certificate;
        }};
        testHandleMessage(doc, trustoreFilename, trustorePassword);
        new Verifications() {{
            certificateService.isCertificateChainValid((List<Certificate>) any);
            times = 0;
        }};
    }

    @Test
    public void testGetCertificateFromBinarySecurityTokenX509v3(@Mocked final BinarySecurityTokenReference binarySecurityTokenReference) throws XMLStreamException, ParserConfigurationException, WSSecurityException, CertificateException, URISyntaxException {
        new Expectations() {{
            binarySecurityTokenReference.getUri();
            result = "#X509-9973d6a2-7819-4de2-a3d2-1bbdb2506df8";
            binarySecurityTokenReference.getValueType();
            result = X_509_V_3;
        }};
        Document doc = readDocument("dataset/as4/RawXMLMessageWithSpaces.xml");
        final List<? extends Certificate> xc = trustSenderInterceptor.getCertificateFromBinarySecurityToken(doc.getDocumentElement(), binarySecurityTokenReference);
        Assert.assertNotNull(xc.get(0));
        Assert.assertNotNull(((X509Certificate) xc.get(0)).getIssuerDN());
    }

    @Test
    public void testGetCertificateFromBinarySecurityTokenX509PKIPathv1(@Mocked final BinarySecurityTokenReference binarySecurityTokenReference) throws XMLStreamException, ParserConfigurationException, WSSecurityException, CertificateException, URISyntaxException {
        new Expectations() {{
            binarySecurityTokenReference.getUri();
            result = "#X509-9973d6a2-7819-4de2-a3d2-1bbdb2506df8";
            binarySecurityTokenReference.getValueType();
            result = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-x509-token-profile-1.0#X509PKIPathv1";
        }};
        Document doc = readDocument("dataset/as4/RawXMLMessageWithSpacesAndPkiPath.xml");
        final List<? extends Certificate> certificateFromBinarySecurityToken = trustSenderInterceptor.getCertificateFromBinarySecurityToken(doc.getDocumentElement(), binarySecurityTokenReference);
        Assert.assertNotNull(certificateFromBinarySecurityToken.get(0));
        Assert.assertNotNull(((X509Certificate) certificateFromBinarySecurityToken.get(0)).getIssuerDN());
    }


    protected void testHandleMessage(Document doc, String trustoreFilename, String trustorePassword) throws JAXBException, IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, SOAPException {
        SoapMessage soapMessage = getSoapMessageForDom(doc);

        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING);
            result = true;
        }};
        trustSenderInterceptor.handleMessage(soapMessage);
        String senderPartyName = LOG.getMDC(DomibusLogger.MDC_FROM);
        String receiverPartyName = LOG.getMDC(DomibusLogger.MDC_TO);

        Assert.assertEquals("blue_gw", senderPartyName);
        Assert.assertEquals("red_gw", receiverPartyName);

    }

    @Test
    public void testCheckCertificateValidityEnabled() throws Exception {
        final X509Certificate certificate = pkiUtil.createCertificate(BigInteger.ONE, null);
        final X509Certificate expiredCertificate = pkiUtil.createCertificate(BigInteger.ONE, new DateTime().minusDays(2).toDate(), new DateTime().minusDays(1).toDate(), null);
        List<Certificate> certificateChain = new ArrayList<>();
        certificateChain.add(certificate);
        List<Certificate> expiredCertificateChain = new ArrayList<>();
        expiredCertificateChain.add(expiredCertificate);

        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONRECEIVING);
            result = true;
            certificateService.isCertificateChainValid(certificateChain);
            result = true;
            certificateService.isCertificateChainValid(expiredCertificateChain);
            result = false;

        }};

        Assert.assertTrue(trustSenderInterceptor.checkCertificateValidity(certificateChain, "test sender", false));
        Assert.assertFalse(trustSenderInterceptor.checkCertificateValidity(expiredCertificateChain, "test sender", false));
    }

    @Test
    public void testCheckCertificateValidityDisabled() throws Exception {
        final X509Certificate expiredCertificate = pkiUtil.createCertificate(BigInteger.ONE, new DateTime().minusDays(2).toDate(), new DateTime().minusDays(1).toDate(), null);
        List<Certificate> expiredCertificateChain = new ArrayList<>();
        expiredCertificateChain.add(expiredCertificate);

        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_CERTIFICATE_VALIDATION_ONRECEIVING);
            result = false;
        }};
        Assert.assertTrue(trustSenderInterceptor.checkCertificateValidity(expiredCertificateChain, "test sender", false));
    }

    @Test
    public void testHandleOneTestActivated(@Mocked final SoapMessage message) {
        new Expectations() {{
            domibusPropertyProvider.getBooleanProperty(DOMIBUS_SENDER_TRUST_VALIDATION_ONRECEIVING);
            result = false;
        }};
        trustSenderInterceptor.handleMessage(message);
        new Verifications() {{
            message.getExchange();
            times = 0;
        }};
    }
}