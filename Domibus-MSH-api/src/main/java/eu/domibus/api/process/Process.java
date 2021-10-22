package eu.domibus.api.process;

/**
 * @author Thomas Dussart
 * @since 4.0
 */
public class Process {

    private long id;

    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
