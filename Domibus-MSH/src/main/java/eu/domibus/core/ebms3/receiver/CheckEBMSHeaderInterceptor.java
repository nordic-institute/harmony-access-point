package eu.domibus.core.ebms3.receiver;

import eu.domibus.ebms3.common.model.ObjectFactory;
import org.apache.cxf.binding.soap.HeaderUtil;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.interceptor.MustUnderstandInterceptor;
import org.apache.cxf.phase.Phase;

import javax.xml.namespace.QName;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Cosmin Baciu
 * @since 4.1.2
 */
public class CheckEBMSHeaderInterceptor extends AbstractSoapInterceptor {

    public CheckEBMSHeaderInterceptor() {
        super(Phase.PRE_LOGICAL);
        this.addBefore(MustUnderstandInterceptor.MustUnderstandEndingInterceptor.class.getName());

    }


    @Override
    public void handleMessage(final SoapMessage message) {
        HeaderUtil.getHeaderQNameInOperationParam(message).add(ObjectFactory._Messaging_QNAME);
    }

    @Override
    public Set<QName> getUnderstoodHeaders() {
        final Set<QName> understood = new HashSet<>();
        understood.add(ObjectFactory._Messaging_QNAME);
        understood.add(eu.domibus.ebms3.common.model.mf.ObjectFactory._MessageFragment_QNAME);
        return understood;
    }
}