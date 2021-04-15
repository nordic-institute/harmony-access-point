package eu.domibus.core.message;

import eu.domibus.api.model.ServiceEntity;
import eu.domibus.core.dao.BasicDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import org.springframework.transaction.annotation.Transactional;

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
        ServiceEntity service = findByValue(value);
        if (service != null) {
            return service;
        }
        ServiceEntity newService = new ServiceEntity();
        newService.setValue(value);
        newService.setType(type);
         create(newService);
        return newService;
    }

    public ServiceEntity findByValue(final String value) {
        final TypedQuery<ServiceEntity> query = this.em.createNamedQuery("Service.findByValue", ServiceEntity.class);
        query.setParameter("VALUE", value);
        return DataAccessUtils.singleResult(query.getResultList());
    }
}
