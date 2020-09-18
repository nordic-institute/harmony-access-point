package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.europa.esig.dss.validation.process.MessageTag;

import java.util.List;
import java.util.Map;

/**
 * @author Thomas Dussart
 * @see ConstraintInternal
 * <p>
 * domibus.dss.constraint.name[0]=
 * domibus.dss.constraint.status[0]=
 * <p>
 * domibus.dss.constraint.name[1]=
 * domibus.dss.constraint.status[1]=
 * @since 4.1
 * <p>
 * Load multiple ConstraintInternal objects based on properties with the following format:
 */
public class ValidationConstraintPropertyMapper extends PropertyGroupMapper<ConstraintInternal> {

    private static final String DOMIBUS_DSS_DEFAULT_CONSTRAINT_NAME = "domibus.authentication.dss.constraint";

    private static final String NAME = "name";

    private static final String STATUS = "status";

    public ValidationConstraintPropertyMapper(final DomibusPropertyExtService domibusPropertyExtService) {
        super(domibusPropertyExtService);
    }

    public List<ConstraintInternal> map() {
        return super.map(
                DOMIBUS_DSS_DEFAULT_CONSTRAINT_NAME
        );
    }

    @Override
    protected ConstraintInternal transform(Map<String,String> keyValues) {
        final String constraintName = keyValues.get(NAME);
        if (constraintName == null) {
            throw new IllegalStateException("Constraint name can not be empty");
        }
        final MessageTag constraintEnum = MessageTag.valueOf(constraintName);
        final String constraintStatus = keyValues.get(STATUS);
        return new ConstraintInternal(constraintEnum.name(), constraintStatus);
    }

}
