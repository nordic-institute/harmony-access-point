package eu.domibus.core.message;

import eu.domibus.api.model.Service;
import eu.domibus.core.dao.BasicDao;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;

/**
 * @author Cosmin Baciu
 * @since 5.0
 */
@Repository
public class ServiceDao extends BasicDao<Service> {

    public ServiceDao() {
        super(Service.class);
    }

    public Service findOrCreateService(String value, String type) {
        Service service = findByValue(value);
        if (service != null) {
            return service;
        }
        Service newService = new Service();
        newService.setValue(value);
        newService.setType(type);
        create(newService);
        return newService;
    }

    public Service findByValue(final String value) {
        final TypedQuery<Service> query = this.em.createNamedQuery("Service.findByValue", Service.class);
        query.setParameter("VALUE", value);
        return DataAccessUtils.singleResult(query.getResultList());
    }
}
