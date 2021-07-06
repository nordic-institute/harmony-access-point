package eu.domibus.core.message;

import eu.domibus.api.model.ServiceEntity;
import eu.domibus.core.dao.BasicDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Repository
public class ServiceDao extends BasicDao<ServiceEntity> {

    public ServiceDao() {
        super(ServiceEntity.class);
    }

    @Transactional
    public ServiceEntity findOrCreateService(String value, String type) {
        if(StringUtils.isBlank(value)) {
            return null;
        }

        ServiceEntity service = findExistingService(value, type);
        if (service != null) {
            return service;
        }
        ServiceEntity newService = new ServiceEntity();
        newService.setValue(value);
        newService.setType(StringUtils.isNotBlank(type) ? type : null);
        create(newService);
        return newService;
    }

    protected ServiceEntity findExistingService(final String value, String type) {
        if (StringUtils.isNotBlank(type)) {
            return findByValueAndType(value, type);
        }
        return findByValue(value);
    }

    protected ServiceEntity findByValueAndType(final String value, final String type) {
        final TypedQuery<ServiceEntity> query = this.em.createNamedQuery("Service.findByValueAndType", ServiceEntity.class);
        query.setParameter("VALUE", value);
        query.setParameter("TYPE", type);
        return DataAccessUtils.singleResult(query.getResultList());
    }

    protected ServiceEntity findByValue(final String value) {
        final TypedQuery<ServiceEntity> query = this.em.createNamedQuery("Service.findByValue", ServiceEntity.class);
        query.setParameter("VALUE", value);
        return DataAccessUtils.singleResult(query.getResultList());
    }
}
