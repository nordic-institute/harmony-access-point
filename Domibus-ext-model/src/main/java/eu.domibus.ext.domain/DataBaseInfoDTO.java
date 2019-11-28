package eu.domibus.ext.domain;

public class DataBaseInfoDTO extends ServiceInfoDTO {

    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return "DataBaseInfoDTO{" +
                "name='" + name + '\'' +
                ", status=" + status +
                '}';
    }
}
