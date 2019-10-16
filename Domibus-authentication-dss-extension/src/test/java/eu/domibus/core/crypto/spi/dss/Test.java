package eu.domibus.core.crypto.spi.dss;

import eu.europa.esig.dss.detailedreport.jaxb.XmlCertificate;
import eu.europa.esig.dss.detailedreport.jaxb.XmlDetailedReport;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class Test {

    public static void main(String[] args) throws JAXBException {

        JAXBContext jaxbContext = JAXBContext.newInstance(XmlDetailedReport.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        XmlDetailedReport xmlDetailedReport = new XmlDetailedReport();
        xmlDetailedReport.setCertificate(new XmlCertificate());
        marshaller.marshal(xmlDetailedReport,System.out);
    }
}
