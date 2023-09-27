package eu.domibus.core.dao;

import eu.domibus.api.model.AbstractBaseEntity;

public abstract class SingleValueDictionaryDao<T extends AbstractBaseEntity> extends BasicDao<T> {
    public SingleValueDictionaryDao(final Class typeOfT) {
        super(typeOfT);
    }

    public abstract T findByValue(Object value);
}
