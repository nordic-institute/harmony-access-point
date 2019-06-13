package eu.domibus.web.rest.ro;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ion Perpegel
 * @since 4.1.1
 */
public class PropertyResponseRO implements Serializable {

    private int count;

    private List<PropertyRO> items;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<PropertyRO> getItems() {
        return items;
    }

    public void setItems(List<PropertyRO> items) { this.items = items; }
}
