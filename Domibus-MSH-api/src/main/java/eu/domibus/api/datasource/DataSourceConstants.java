package eu.domibus.api.datasource;

/**
 * @author Ion Perpegel
 * @since 5.0
 */
public final class DataSourceConstants {

    /*
     * DOMIBUS_JDBC_DATA_SOURCE is used for domibus operarations (entity manager)
     */
    public static final String DOMIBUS_JDBC_DATA_SOURCE = "domibusJDBC-dataSource";
    /*
     * DOMIBUS_JDBC_NON_XA_DATA_SOURCE is used for quartz
     */
    public static final String DOMIBUS_JDBC_NON_XA_DATA_SOURCE = "domibusJDBC-nonXADataSource";

    private DataSourceConstants() {

    }
}
