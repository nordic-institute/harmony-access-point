package eu.domibus.core.multitenancy.dao;


import eu.domibus.core.dao.BasicDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;


/**
 * @author Cosmin Baciu
 * @since 4.0
 */
@Repository
public class UserDomainDaoImpl extends BasicDao<UserDomainEntity> implements UserDomainDao {

    public UserDomainDaoImpl() {
        super(UserDomainEntity.class);
    }

    @Override
    public String findDomainByUser(String userName) {
        UserDomainEntity userDomainEntity = findUserDomainEntity(userName);
        if (userDomainEntity == null) {
            return null;
        }
        return userDomainEntity.getDomain();
    }

    @Override
    public String findPreferredDomainByUser(String userName) {
        UserDomainEntity userDomainEntity = findUserDomainEntity(userName);
        if (userDomainEntity == null) {
            return null;
        }
        return userDomainEntity.getPreferredDomain();
    }

    @Override
    public List<UserDomainEntity> listPreferredDomains() {
        TypedQuery<UserDomainEntity> namedQuery = em.createNamedQuery("UserDomainEntity.findPreferredDomains", UserDomainEntity.class);
        return namedQuery.getResultList();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void setDomainByUser(String userName, String domainCode) {
        UserDomainEntity userDomainEntity = findUserDomainEntity(userName);
        if (userDomainEntity != null) {
            userDomainEntity.setDomain(domainCode);
            this.update(userDomainEntity);
        } else {
            userDomainEntity = new UserDomainEntity();
            userDomainEntity.setUserName(userName);
            userDomainEntity.setDomain(domainCode);
            this.create(userDomainEntity);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void setPreferredDomainByUser(String userName, String domainCode) {
        UserDomainEntity userDomainEntity = findUserDomainEntity(userName);
        if (userDomainEntity != null) {
            userDomainEntity.setPreferredDomain(domainCode);
            this.update(userDomainEntity);
        } else {
            userDomainEntity = new UserDomainEntity();
            userDomainEntity.setUserName(userName);
            userDomainEntity.setPreferredDomain(domainCode);
            this.create(userDomainEntity);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void deleteDomainByUser(String userName) {
        UserDomainEntity userDomainEntity = findUserDomainEntity(userName);
        if (userDomainEntity != null) {
            this.delete(userDomainEntity);
        }
    }

    private UserDomainEntity findUserDomainEntity(String userName) {
        TypedQuery<UserDomainEntity> namedQuery = em.createNamedQuery("UserDomainEntity.findByUserName", UserDomainEntity.class);
        namedQuery.setParameter("USER_NAME", userName);
        try {
            return namedQuery.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}

