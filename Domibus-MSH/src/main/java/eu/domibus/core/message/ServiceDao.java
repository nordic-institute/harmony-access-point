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
        ServiceEntity service = findByValueAndType(value,type);
        if (service != null) {
            return service;
        }
        ServiceEntity newService = new ServiceEntity();
        newService.setValue(value);
        newService.setType(type);
        create(newService);
        return newService;
    }

    public ServiceEntity findByValueAndType(final String value, final String type) {
        final TypedQuery<ServiceEntity> query = this.em.createNamedQuery("Service.findByValueAndType", ServiceEntity.class);
        query.setParameter("VALUE", value);
        query.setParameter("TYPE", type);
        return DataAccessUtils.singleResult(query.getResultList());
    }
}
