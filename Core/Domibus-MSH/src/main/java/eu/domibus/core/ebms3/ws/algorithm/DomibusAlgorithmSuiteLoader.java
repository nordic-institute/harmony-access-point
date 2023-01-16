
package eu.domibus.core.ebms3.ws.algorithm;

import eu.domibus.common.model.configuration.SecurityProfile;
import eu.domibus.core.cxf.DomibusBus;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.cxf.Bus;
import org.apache.cxf.ws.policy.AssertionBuilderRegistry;
import org.apache.cxf.ws.policy.builder.primitive.PrimitiveAssertion;
import org.apache.cxf.ws.policy.builder.primitive.PrimitiveAssertionBuilder;
import org.apache.cxf.ws.security.policy.custom.AlgorithmSuiteLoader;
import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.builders.xml.XMLPrimitiveAssertionBuilder;
import org.apache.wss4j.policy.SPConstants;
import org.apache.wss4j.policy.model.AlgorithmSuite;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

import static eu.domibus.core.ebms3.ws.algorithm.DomibusAlgorithmSuite.*;


/**
 * This class implements a custom {@link org.apache.cxf.ws.security.policy.custom.AlgorithmSuiteLoader} in order to enable the domibus gateway to support:
 * <ol>
 * <li>the gcm variant of the aes algorithm</li>
 * <li>a key transport algorithm without SHA1 dependencies</li>
 * </ol>
 * NOTE: GCM is supported by Apache CXF via {@link org.apache.cxf.ws.security.policy.custom.DefaultAlgorithmSuiteLoader} but at time of writing (15.10.2014)
 * the corresponding AlgorithmSuites do not support digest Algorithms other than SHA1.
 *
 * @author Christian Koch, Stefan Mueller
 */
@Service("algorithmSuiteLoader")
public class DomibusAlgorithmSuiteLoader implements AlgorithmSuiteLoader {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusAlgorithmSuiteLoader.class);

    public static final String E_DELIVERY_ALGORITHM_NAMESPACE = "http://e-delivery.eu/custom/security-policy";

    protected DomibusBus domibusBus;

    protected DomibusAlgorithmSuite domibusAlgorithmSuite;

    public DomibusAlgorithmSuiteLoader(final DomibusBus bus) {
        this.domibusBus = bus;
    }

    @PostConstruct
    public void load() {
        if (this.domibusBus == null) {
            LOG.warn("Domibus bus is null");
            return;
        }
        domibusBus.setExtension(this, AlgorithmSuiteLoader.class);
        registerBuilders();
    }

    /**
     * Registers the builders for Domibus custom security policies QNames
     *
     */
    protected void registerBuilders() {
        final AssertionBuilderRegistry reg = domibusBus.getExtension(AssertionBuilderRegistry.class);
        if (reg != null) {
            final Map<QName, Assertion> assertions = new HashMap<>();
            QName qName = new QName(E_DELIVERY_ALGORITHM_NAMESPACE, BASIC_128_GCM_SHA_256_RSA);
            assertions.put(qName, new PrimitiveAssertion(qName));
            qName = new QName(E_DELIVERY_ALGORITHM_NAMESPACE, BASIC_128_GCM_SHA_256_MGF_SHA_256_RSA);
            assertions.put(qName, new PrimitiveAssertion(qName));
            qName = new QName(E_DELIVERY_ALGORITHM_NAMESPACE, BASIC_128_GCM_SHA_256_MGF_SHA_256_ECC);
            assertions.put(qName, new PrimitiveAssertion(qName));

            reg.registerBuilder(new PrimitiveAssertionBuilder(assertions.keySet()) {
                @Override
                public Assertion build(final Element element, final AssertionBuilderFactory fact) {
                    if (XMLPrimitiveAssertionBuilder.isOptional(element)
                            || XMLPrimitiveAssertionBuilder.isIgnorable(element)) {
                        return super.build(element, fact);
                    }
                    final QName q = new QName(element.getNamespaceURI(), element.getLocalName());
                    return assertions.get(q);
                }
            });
        }
    }

    @Override
    public AlgorithmSuite getAlgorithmSuite(final Bus bus, final SPConstants.SPVersion version, final Policy nestedPolicy) {
        domibusAlgorithmSuite = new DomibusAlgorithmSuite(version, nestedPolicy);
        return domibusAlgorithmSuite;
    }

    public AlgorithmSuiteType getAlgorithmSuiteType(SecurityProfile securityProfile) {
        return domibusAlgorithmSuite.getAlgorithmSuiteType(securityProfile);
    }
}
