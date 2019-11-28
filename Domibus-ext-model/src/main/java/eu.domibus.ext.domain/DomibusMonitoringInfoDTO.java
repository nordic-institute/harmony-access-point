package eu.domibus.ext.domain;

import java.util.List;

public class DomibusMonitoringInfoDTO {

   protected List<ServiceInfoDTO> services;
/*    private DataBaseInfoDTO dataBaseInfoDTO;
    private JmsBrokerInfoDTO jmsBrokerInfoDTO;
    private QuartzInfoDTO quartzInfoDTO;*/

//    public DataBaseInfoDTO getDataBaseInfoDTO() {
//        return dataBaseInfoDTO;
//    }
//
//    public void setDataBaseInfoDTO(DataBaseInfoDTO dataBaseInfoDTO) {
//        this.dataBaseInfoDTO = dataBaseInfoDTO;
//    }
//
//    public JmsBrokerInfoDTO getJmsBrokerInfoDTO() {
//        return jmsBrokerInfoDTO;
//    }
//
//    public void setJmsBrokerInfoDTO(JmsBrokerInfoDTO jmsBrokerInfoDTO) {
//        this.jmsBrokerInfoDTO = jmsBrokerInfoDTO;
//    }
//
//
//    public QuartzInfoDTO getQuartzInfoDTO() {
//        return quartzInfoDTO;
//    }
//
//    public void setQuartzInfoDTO(QuartzInfoDTO quartzInfoDTO) {
//        this.quartzInfoDTO = quartzInfoDTO;
//    }

//    @Override
//    public String toString() {
//        return "DomibusMonitoringInfoDTO{" +
//                "dataBaseInfo=" + dataBaseInfoDTO +
//                ", jmsBrokerInfoDTO=" + jmsBrokerInfoDTO +
//                ", quartzInfoDTO=" + quartzInfoDTO +
//                '}';
//    }


    public List<ServiceInfoDTO> getServices() {
        return services;
    }

    public void setServices(List<ServiceInfoDTO> services) {
        this.services = services;
    }

    @Override
    public String toString() {
        return "DomibusMonitoringInfoDTO{" +
                "services=" + services +
                '}';
    }
}
