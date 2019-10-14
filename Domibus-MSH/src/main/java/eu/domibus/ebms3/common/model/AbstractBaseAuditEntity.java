package eu.domibus.ebms3.common.model;

import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
@XmlTransient
@MappedSuperclass
public class AbstractBaseAuditEntity extends AbstractBaseEntity {

}
