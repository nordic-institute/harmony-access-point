package eu.domibus.spring;

import eu.domibus.api.configuration.DomibusConfigurationService;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletContext;

public class DomibusConfigLocationProvider {

    public String getDomibusConfigLocation(ServletContext servletContext) {
        String contextPath = servletContext.getContextPath();
        //TODO possible to get domibusConfigLocation from JNDI: java:comp/env/contextPath
        //https://medium.com/byteagenten/simple-application-configuration-with-tomcat-5cad0a551ba6
        String domibusConfigLocation = System.getProperty(DomibusConfigurationService.DOMIBUS_CONFIG_LOCATION);
        String domibusConfigLocationInitParameter = servletContext.getInitParameter("domibusConfigLocation");
        if (StringUtils.isNotEmpty(domibusConfigLocationInitParameter)) {
            //TODO add logging
            domibusConfigLocation = domibusConfigLocationInitParameter;
        }
        domibusConfigLocation = "c:/DEV/domibus-tomcat-4.0/domibus/conf/domibus";
        return domibusConfigLocation;
    }
}
