package eu.domibus.web.rest.ro;

import java.io.Serializable;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 */
public class PropertyResponseRO implements Serializable {

    private int count;

    private List<DomibusPropertyRO> items;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<DomibusPropertyRO> getItems() {
        return items;
    }

    public void setItems(List<DomibusPropertyRO> items) {
        this.items = items;
    }
}
