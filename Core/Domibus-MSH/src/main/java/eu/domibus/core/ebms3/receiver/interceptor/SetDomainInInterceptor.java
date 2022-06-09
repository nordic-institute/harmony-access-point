package eu.domibus.core.ebms3.receiver.interceptor;

import eu.domibus.api.model.MSHRole;
import eu.domibus.api.multitenancy.DomainContextProvider;
import eu.domibus.api.multitenancy.DomainService;
import eu.domibus.api.property.DomibusConfigurationService;
import eu.domibus.common.ErrorCode;
import eu.domibus.core.ebms3.EbMS3ExceptionBuilder;
import eu.domibus.core.ebms3.receiver.policy.SetPolicyInInterceptor;
import eu.domibus.logging.IDomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Service
public class SetDomainInInterceptor extends AbstractSoapInterceptor {

    private static final IDomibusLogger LOG = DomibusLoggerFactory.getLogger(SetDomainInInterceptor.class);

    public SetDomainInInterceptor() {
        this(Phase.RECEIVE);
    }

    @Autowired
    protected DomainContextProvider domainContextProvider;

    @Autowired
    protected DomibusConfigurationService domibusConfigurationService;

    protected SetDomainInInterceptor(String phase) {
        super(phase);
        this.addBefore(SetPolicyInInterceptor.class.getName());
    }

    /**
     * Intercepts a message.
     * Interceptors should NOT invoke handleMessage or handleFault
     * on the next interceptor - the interceptor chain will take care of this.
     *
     * @param message the incoming message to handle
     */
    @Override
    public void handleMessage(final SoapMessage message) throws Fault {
        HttpServletRequest httpRequest = (HttpServletRequest) message.get("HTTP.REQUEST");
        String domainCode = StringUtils.lowerCase(getDomainCode(httpRequest));
        LOG.debug("Using domain [{}]", domainCode);
        domainContextProvider.setCurrentDomain(domainCode);
        message.put(DomainContextProvider.HEADER_DOMIBUS_DOMAIN, domainCode);
    }

    protected String getDomainCode(HttpServletRequest httpRequest) {
        if (!domibusConfigurationService.isMultiTenantAware()) {
            LOG.debug("Domibus is not configured for multi-tenancy, using the default domain [{}]", DomainService.DEFAULT_DOMAIN.getCode());
            return DomainService.DEFAULT_DOMAIN.getCode();
        }
        String domainCode = null;
        try {
            domainCode = ServletRequestUtils.getStringParameter(httpRequest, "domain");
        } catch (ServletRequestBindingException e) {
            throw new Fault(new Exception("Could not determine the domain",
                    EbMS3ExceptionBuilder.getInstance()
                            .ebMS3ErrorCode(ErrorCode.EbMS3ErrorCode.EBMS_0004)
                            .message("Could not determine the domain")
                            .mshRole(MSHRole.RECEIVING)
                            .build()));
        }
        if (StringUtils.isEmpty(domainCode)) {
            LOG.debug("No domain specified. Using the default domain");
            domainCode = DomainService.DEFAULT_DOMAIN.getCode();
        }
        return domainCode;
    }


}


