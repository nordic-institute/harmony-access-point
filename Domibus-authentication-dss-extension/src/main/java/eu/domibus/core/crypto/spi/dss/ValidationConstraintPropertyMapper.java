package eu.domibus.core.crypto.spi.dss;

import eu.domibus.ext.services.DomibusPropertyExtService;
import eu.europa.esig.dss.validation.process.MessageTag;

import java.util.List;
import java.util.Map;

import static eu.domibus.core.crypto.spi.dss.DssExtensionPropertyManager.CONSTRAINTS_PREFIX;


/**
 * @author Thomas Dussart
 * @since 4.1
 *
 * Load multiple ConstraintInternal objects based on Domibus nested property mechanism.
 * @see ConstraintInternal
 */
public class ValidationConstraintPropertyMapper extends PropertyGroupMapper<ConstraintInternal> {

    private static final String NAME = "name";

    private static final String STATUS = "status";

    public ValidationConstraintPropertyMapper(final DomibusPropertyExtService domibusPropertyExtService) {
        super(domibusPropertyExtService);
    }

    public List<ConstraintInternal> map() {
        return super.map(
                CONSTRAINTS_PREFIX
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
