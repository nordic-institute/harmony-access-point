package eu.domibus.api.monitoring;

import java.util.List;
import java.util.Objects;

public class DomibusMonitoringInfo {

    protected List<ServiceInfo> services;


    public List<ServiceInfo> getServices() {
        return services;
    }

    public void setServices(List<ServiceInfo> services) {
        this.services = services;
    }

    //    protected DataBaseInfo dataBaseInfo;
//    protected JmsBrokerInfo jmsBrokerInfo;
//    protected QuartzInfo quartzInfo;

//    public DataBaseInfo getDataBaseInfo() {
//        return dataBaseInfo;
//    }
//
//    public void setDataBaseInfo(DataBaseInfo dataBaseInfo) {
//        this.dataBaseInfo = dataBaseInfo;
//    }
//
//    public JmsBrokerInfo getJmsBrokerInfo() {
//        return jmsBrokerInfo;
//    }
//
//    public void setJmsBrokerInfo(JmsBrokerInfo jmsBrokerInfo) {
//        this.jmsBrokerInfo = jmsBrokerInfo;
//    }
//
//    public QuartzInfo getQuartzInfo() {
//        return quartzInfo;
//    }
//
//    public void setQuartzInfo(QuartzInfo quartzInfo) {
//        this.quartzInfo = quartzInfo;
//    }

//    @Override
//    public String toString() {
//        return "DomibusMonitoringInfo{" +
//                "dataBaseInfo=" + dataBaseInfo +
//                ", jmsBrokerInfo=" + jmsBrokerInfo +
//                ", quartzInfo=" + quartzInfo +
//                '}';
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        DomibusMonitoringInfo that = (DomibusMonitoringInfo) o;
//        return Objects.equals(dataBaseInfo, that.dataBaseInfo) &&
//                Objects.equals(jmsBrokerInfo, that.jmsBrokerInfo) &&
//                Objects.equals(quartzInfo, that.quartzInfo);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hash(dataBaseInfo, jmsBrokerInfo, quartzInfo);
//    }
}
