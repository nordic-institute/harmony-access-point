```
Release checklist: 
    Replace the Domibus war
    Replace the default plugin(s) property file(s) and jar(s) into "conf/domibus/plugins/config" respectively into "conf/domibus/plugins/lib"
    Replace the dss extension property files and jar into "conf/domibus/extensions/config" respectively into "conf/domibus/extensions/lib"
    Run the appropriate DB upgrade script (eg mysql-5.1.1-to-5.1.2-upgrade.ddl for MySQL or oracle-5.1.1-to-5.1.2-upgrade.ddl for Oracle)
    Upgrade the domibus.properties and logback.xml files present in "conf/domibus" and in case of multitenancy "conf/domibus/domains/domain_name"
    Upgrade the ehcache.xml file present in "conf/domibus/internal"
    Tomcat only: Upgarde the file activemq.xml present in "conf/domibus/internal"
    Wildfly only: Upgrade the file standalone-full.xml present in "domibus/standalone/configuration"
    WebLogic only: Execute the WLST API script import.py (from "/conf/domibus/scripts/upgrades") 5.0-to-5.0.8-WeblogicCluster.properties or 5.0-to-5.0.8-WeblogicSingleServer.properties
                     wlstapi.cmd ../scripts/import.py --property ../5.0-to-5.0.8-WeblogicCluster.properties
```


# Domibus upgrade information
## Domibus 5.1.9 (from 5.1.8)
                - Replace the Domibus war and the default plugin(s) config file(s), property file(s) and jar(s) 
                - Replace the default dss extension jar into "/domibus/conf/domibus/extensions/lib"
                - Remove these properties from the properties file(s): domibus.ongoingMessagesSanitizing.worker.delay.hours and domibus.ongoingMessagesSanitizing.worker.cron
                - Rename properties: 
                        domibus.ongoingMessagesSanitizing.alert.email.body to domibus.ongoingMessagesSanitizing.alert.mail.body
                        domibus.ongoingMessagesSanitizing.alert.email.subject to domibus.ongoingMessagesSanitizing.alert.mail.subject
                - Run the appropriate DB upgrade script (mysql-5.1.8-to-5.1.9-upgrade.ddl for MySQL or oracle-5.1.8-to-5.1.9-upgrade.ddl for Oracle)
## Domibus 5.1.8 (from 5.1.7)
                - Replace the Domibus war and the default plugin(s) config file(s), property file(s) and jar(s) 
                - Replace the default dss extension jar into "/domibus/conf/domibus/extensions/lib"
                - Run the appropriate DB upgrade script (mysql-5.1.4-to-5.1.8-upgrade.ddl for MySQL or oracle-5.1.4-to-5.1.8-upgrade.ddl for Oracle)
## Domibus 5.1.7 (from 5.1.6)
                - Replace the Domibus war and the default plugin(s) config file(s), property file(s) and jar(s) 
                - Replace the default dss extension jar into "/domibus/conf/domibus/extensions/lib"
                - Add the following lines in conf/domibus/logback.xml after the "org.apache.cxf" logger 
                     <!--  Fix for known issue in CXF library -->
                     <logger name="org.apache.cxf.io.DelayedCachedOutputStreamCleaner" level="ERROR">
                                 <appender-ref ref="file"/>
                     </logger>
                - No sql updates on the database schemas for both mysql and oracle
## Domibus 5.1.6 (from 5.1.5)
                - Replace the Domibus war and the default plugin(s) config file(s), property file(s) and jar(s) 
                - Replace the default dss extension jar into "/domibus/conf/domibus/extensions/lib"
                - No sql updates on the database schemas for both mysql and oracle
## Domibus 5.1.5 (from 5.1.4)
                - Replace the Domibus war and the default plugin(s) config file(s), property file(s) and jar(s) 
                - Replace the default dss extension jar into "/domibus/conf/domibus/extensions/lib"
                - No sql updates on the database schemas for both mysql and oracle
#### Weblogic only
                - Execute the WLST API script remove.py (from "/conf/domibus/scripts/upgrades") 5.1.4-to-5.1.5-WeblogicRemoveJDBCDatasource.properties to remove the eDeliveryDs datasource:
                     wlstapi.cmd ../scripts/remove.py --property ../5.1.4-to-5.1.5-WeblogicRemoveJDBCDatasource.properties
                - Execute the WLST API script import.py (from "/conf/domibus/scripts/upgrades") 5.1.4-to-5.1.5-WeblogicSingleServer.properties or 5.1.4-to-5.1.5-WeblogicCluster.properties to add the eDeliveryDs datasource with new configuration: 
                     wlstapi.cmd ../scripts/import.py --property ../5.1.4-to-5.1.5-WeblogicCluster.properties

## Domibus 5.1.4 (from 5.1.3)
                - Replace the Domibus war and the default plugin(s) config file(s), property file(s) and jar(s) 
                - Run the appropriate DB upgrade script (mysql-5.1.2-to-5.1.4-upgrade.ddl for MySQL or oracle-5.1.2-to-5.1.4-upgrade.ddl for Oracle)
                - Update property name from "domibus.cacerts.validation.enabled" to "domibus.cacerts.download.enabled"
                - Marked 'mustUnderstand' attribute from Domibus MSH Default WS Plugin Stubs V2 webservicePlugin-header.xsd as deprecated. The attribute will be removed in 6.0

#### PULL only if not already applied
                - Update the roles in the pull processes to reflect the correct matching of From party role matches the initiatorRole and To party role matches the responderRole. If you are using the sample pModes, 
                replace
                     <process name="tc2Process"
                     mep="oneway"
                     binding="pull"
                     initiatorRole="defaultInitiatorRole"
                     responderRole="defaultResponderRole">
                with
                     <process name="tc2Process"
                     mep="oneway"
                     binding="pull"
                     initiatorRole="defaultResponderRole"
                     responderRole="defaultInitiatorRole">
                A workaround for this change is to disable the roles validation by setting domibus.partyinfo.roles.validation.enabled=false in domibus.properties
     
#### Tomcat only if not already applied
                - Add values for quartz data source properties if defaults are not good
                     #the name of the DataSource class provided by the JDBC driver
                     domibus.quartz.datasource.driverClassName=${domibus.datasource.driverClassName}
                     #the JDBC url for the DB
                     domibus.quartz.datasource.url=${domibus.datasource.url}
                     #default authentication username used when obtaining Connections from the underlying driver
                     domibus.quartz.datasource.user=${domibus.datasource.user}
                     #the default authentication password used when obtaining Connections from the underlying driver
                     domibus.quartz.datasource.password=${domibus.datasource.password}
                     #HikariCP specific
                     #Controls the maximum lifetime of a connection in the pool (in seconds)
                     domibus.quartz.datasource.maxLifetime=1800
                     #Controls the maximum amount of time (in seconds) that a client will wait for a connection from the pool
                     domibus.quartz.datasource.connectionTimeout=30
                     #Controls the maximum amount of time (in seconds) that a connection is allowed to sit idle in the pool
                     domibus.quartz.datasource.idleTimeout=600
                     #Controls the maximum size that the pool is allowed to reach, including both idle and in-use connections
                     domibus.quartz.datasource.maxPoolSize=10
                     #Controls the minimum number of idle connections that HikariCP tries to maintain in the pool
                     domibus.quartz.datasource.minimumIdle=1
     
#### Weblogic only if not already applied
                - Execute the WLST API script remove.py (from "/conf/domibus/scripts/upgrades") 5.0-to-5.0.8-Weblogic-removeObsoleteJDBCDatasource.properties to remove the edeliveryNonXA datasource:
                     wlstapi.cmd ../scripts/remove.py --property ../5.0-to-5.0.8-Weblogic-removeObsoleteJDBCDatasource.properties
                - Execute the WLST API script import.py (from "/conf/domibus/scripts/upgrades") 5.0-to-5.0.8-WeblogicCluster.properties or 5.0-to-5.0.8-WeblogicSingleServer.properties to add the eDeliveryQuartzDs datasource: 
                     wlstapi.cmd ../scripts/import.py --property ../5.0-to-5.0.8-WeblogicCluster.properties
    
#### Wildfly only if not already applied
                - In file "cef_edelivery_path/domibus/standalone/configuration/standalone-full.xml" replace the following datasource and pool names from

                     .............................
                     <datasource jndi-name="java:/jdbc/cipaeDeliveryNonXADs" pool-name="eDeliveryMysqlNonXADS" enabled="true" use-ccm="true">
                     .....................    
                to
                     .............................
                     <datasource jndi-name="java:/jdbc/cipaeDeliveryQuartzDs" pool-name="eDeliveryMysqlQuartzDS" enabled="true" use-ccm="true">
                     .....................
                and from Oracle
                     <datasource jndi-name="java:/jdbc/cipaeDeliveryNonXADs" pool-name="eDeliveryOracleNonXADS" enabled="true" use-ccm="true">
                     .............................
                to Oracle
                     <datasource jndi-name="java:/jdbc/cipaeDeliveryQuartzDs" pool-name="eDeliveryOracleQuartzDS" enabled="true" use-ccm="true">
                     .............................

## Domibus 5.1.3 (from 5.1.2)
                - Replace the Domibus war and the default plugin(s) config file(s), property file(s) and jar(s)

### Multitenancy only
                - In all domain specific logback.xml files (ie. all *-logback.xml files under the domain folders present in conf/domibus/domains) delete the element <property name="domainName" value="your_domain_name" scope="local" /> and replace all occurences of the string ${domainName} with your_domain_name 

## Domibus 5.1.2 (from 5.1.1)
                - Replace the Domibus war and the default plugin(s) config file(s), property file(s) and jar(s)
                - Replace the default dss extension jar into "/domibus/conf/domibus/extensions/lib"
                - Update the properties domibus.pmode.validation.action.pattern and domibus.pmode.validation.service.value.pattern in case of backward compatibility issues regarding the action and service values
                - Run the appropriate DB upgrade script (mysql-5.1.1-to-5.1.2-upgrade.ddl for MySQL or oracle-5.1.1-to-5.1.2-upgrade.ddl for Oracle)
                - Remove property domibus.dynamicdiscovery.peppolclient.mode
                - Any custom dss-cache settings should be moved from /conf/domibus/internal/ehcache.xml to /conf/domibus/extensions/config/dss-extension-ehcache.xml
                - It is now possible to specify also the hardcoded algorithm suite name (e.g Basic128GCMSha256) instead of the placeholder: ${algorithmSuitePlaceholder} in order to keep backward compatibility.
                The use of the placeholder is required for the new security profiles configuration.
## Domibus 5.1.1 (from 5.1)
                - Replace the Domibus war and the default plugin(s) config file(s), property file(s) and jar(s)
                - Replace the default dss extension jar into "/domibus/conf/domibus/extensions/lib"
                - Run the appropriate DB upgrade script (mysql-5.1-to-5.1.1-upgrade.ddl for MySQL or oracle-5.1-to-5.1.1-upgrade.ddl for Oracle)
                - Remove property domibus.fourcornermodel.enabled if configured
                - In all eDeliveryAS4Policy xml files, the hardcoded algorithm suite name defined in AsymmetricBinding/Policy/AlgorithSuite/ (e.g Basic128GCMSha256MgfSha256) was replaced with the placeholder: ${algorithmSuitePlaceholder} which will be automatically replaced in code according to the security setup
                - Replace/update all policy files that have the AsymmetricBinding/Policy/AlgorithSuite tag defined(e.g. eDeliveryAS4Policy.xml, eDeliveryAS4Policy_BST.xml, eDeliveryAS4Policy_BST_PKIP.xml,eDeliveryAS4Policy_IS.xml, signOnly.xml etc.) to accomodate this change
                 The policy xml config files can be found in the Domibus distribution inside the file domibus-msh-distribution-5.1.1-application_server_name-configuration.zip under the folder /policies or inside the file domibus-msh-distribution-5.1.1-application_server_name-full.zip under the folder domibus/conf/domibus/policies
                - Update all the domibus.UI.title.name domain property names to domibus.ui.title.name
                - Update all the property names prefixed with domibus.metrics.sl4j to domibus.metrics.slf4j 
## Domibus 5.1 (from 5.0.8)
                - Update the file cef_edelivery_path/domibus/conf/domibus/internal/activemq.xml and make sure the <property-placeholder> section has the attribute system-properties-mode="ENVIRONMENT". Ideally the line should look exactly like this: <context:property-placeholder system-properties-mode="ENVIRONMENT" ignore-resource-not-found="false" ignore-unresolvable="false"/>
                - Update the "/conf/domibus/internal/ehcache.xml" cache definitions file by removing domainValidity if exists
                - Update your logback.xml configuration so that logs contain the correct origin line number. At the begginging of your <configuration> declare the conversion word domibusLine: 
                <conversionRule conversionWord="domibusLine" converterClass="eu.domibus.logging.DomibusLineOfCallerConverter" />
                And then change your log pattern layouts by replacing %L and %line with %domibusLine. For example, the pattern:
                    <property name="encoderPattern" value="%d{ISO8601} [%X{d_user}] [%X{d_domain}] [%X{d_messageId}] [%X{d_messageEntityId}] [%thread] %5p %c{1}:%L - %m%n" scope="global"/>
                should become:
                    <property name="encoderPattern" value="%d{ISO8601} [%X{d_user}] [%X{d_domain}] [%X{d_messageId}] [%X{d_messageEntityId}] [%thread] %5p %c{1}:%domibusLine - %m%n" scope="global"/>
                 o [MySQL only]
                    - Changed MySQL dialect property from MySQL5InnoDBDialect to MySQL8Dialect in the domibus.properties file:
                            domibus.entityManagerFactory.jpaProperty.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
                o [Oracle only]
                    - multitenancy:
                        - domain schemas:
                            - grant privileges to the general schema using oracle-5.1-multi-tenancy-rights.sql, updating the schema names before execution
                            Please note that this script execution is required even though it may have been executed before.
                - [Custom plugins] For custom plugins the interface to Domibus has changed.
                    - getErrorsForMessage(String messageId) became @deprecated and now throws MessageNotFoundException and DuplicateMessageException 
                    - getStatus(String messageId) became @deprecated and now throws DuplicateMessageException
                    - DuplicateMessageException is thrown in the self sending scenario, when two messages ACKNOWLEDGED and RECEIVED have the same messageId.
                    - Both methods should be replaced with the equivalent method that receives also the AP Role as parameter to differentiate between sent and received messages.
                    - The default list of message statuses for which notifications are sent has changed. As a consequence, the plugins that rely on notifications should customize this list of statuses in their properties file. For example, FS-Plugin should also include PAYLOAD_PROCESSED in the property fsplugin.messages.notifications from fs-plugin.properties.
                        - the new (5.1) list of statuses that trigger push notifications: MESSAGE_RECEIVED, MESSAGE_SEND_FAILURE, MESSAGE_RECEIVED_FAILURE, MESSAGE_SEND_SUCCESS, MESSAGE_STATUS_CHANGE, MESSAGE_DELETED, MESSAGE_DELETE_BATCH, PAYLOAD_SUBMITTED, PAYLOAD_PROCESSED
                        - the previous list of statuses that trigger push notifications: MESSAGE_RECEIVED, MESSAGE_SEND_FAILURE, MESSAGE_RECEIVED_FAILURE, MESSAGE_SEND_SUCCESS, MESSAGE_STATUS_CHANGE
### DB upgrade script
                - Follow the upgrade procedures described bellow to upgrade to the latest version of Domibus 5.0.x. For example, upgrade to Domibus 5.0.4 (from 5.0.3), then upgrade to Domibus 5.0.5, then upgrade to Domibus 5.0.6 etc.

                - Run the appropriate DB upgrade script:
                    o [Oracle only]
                        - single tenancy: oracle-5.0.8-to-5.1-upgrade.ddl, oracle-5.0.8-to-5.1-data-upgrade.ddl
                        - multitenancy:
                            - general schema: oracle-5.0.8-to-5.1-multi-tenancy-upgrade.ddl
                            - domain schemas: oracle-5.0.8-to-5.1-upgrade.ddl, oracle-5.0.8-to-5.1-data-upgrade.ddl
                  o [MySQL only]
                      The scripts below - please adapt to your local configuration (i.e. users, database names) - can be run using either:
                          - the root user, specifying the target databases as part of the command. For example, for single tenancy:
                                  mysql -u root -p domibus < mysql-5.0.8-to-5.1-upgrade.ddl
                                  mysql -u root -p domibus < mysql-5.0.8-to-5.1-data-upgrade.ddl
                              or, for multitenancy:
                                  mysql -u root -p domibus_general < mysql-5.0.8-to-5.1-multi-tenancy-upgrade.ddl
                                  mysql -u root -p domibus_domain_1 < mysql-5.0.8-to-5.1-upgrade.ddl
                                  mysql -u root -p domibus_domain_1 < mysql-5.0.8-to-5.1-data-upgrade.ddl
                          - the non-root user (e.g. edelivery): for which the root user must first relax the conditions on function creation by granting the SYSTEM_VARIABLES_ADMIN right to the non-root user:
                                  GRANT SYSTEM_VARIABLES_ADMIN ON *.* TO 'edelivery'@'localhost';
                            and then specifying the target databases as part of the command. For example, for single tenancy:
                                   mysql -u edelivery -p domibus < mysql-5.0.8-to-5.1-upgrade.ddl
                                   mysql -u edelivery -p domibus < mysql-5.0.8-to-5.1-data-upgrade.ddl
                               or, for multitenancy:
                                   mysql -u edelivery -p domibus_general < mysql-5.0.8-to-5.1-multi-tenancy-upgrade.ddl
                                   mysql -u edelivery -p domibus_domain_1 < mysql-5.0.8-to-5.1-upgrade.ddl
                                   mysql -u edelivery -p domibus_domain_1 < mysql-5.0.8-to-5.1-data-upgrade.ddl.
## Domibus 5.0.8 (from 5.0.7):
                - Update the properties domibus.pmode.validation.action.pattern and domibus.pmode.validation.service.value.pattern in case of backward compatibility issues regarding the action and service values
                - Replace the Domibus war and the default plugin(s) config file(s), property file(s) and jar(s)
                - Replace the default dss extension jar into  "/conf/domibus/extensions/lib"
                - Run the appropriate DB upgrade script (`mysql-5.0.5-to-5.0.8-upgrade.ddl` for MySQL or `oracle-5.0.5-to-5.0.8-upgrade.ddl` for Oracle)
                - Update all the `domibus.UI.title.name` domain property names to `domibus.ui.title.name`
                - Update all the property names prefixed with `domibus.metrics.sl4j` to `domibus.metrics.slf4j`
                - Any custom dss-cache settings should be moved from `/conf/domibus/internal/ehcache.xml` to `/conf/domibus/extensions/config/dss-extension-ehcache.xml`

### PULL only
                - Update the roles in the pull processes to reflect the correct matching of From party role matches the initiatorRole and To party role matches the responderRole. If you are using the sample pModes, 
                replace
                     <process name="tc2Process"
                     mep="oneway"
                     binding="pull"
                     initiatorRole="defaultInitiatorRole"
                     responderRole="defaultResponderRole">
                with
                     <process name="tc2Process"
                     mep="oneway"
                     binding="pull"
                     initiatorRole="defaultResponderRole"
                     responderRole="defaultInitiatorRole">

                A workaround for this change is to disable the roles validation by setting domibus.partyinfo.roles.validation.enabled=false in domibus.properties

### Tomcat only
                - Add values for quartz data source properties if defaults are not good
                     #the name of the DataSource class provided by the JDBC driver
                     domibus.quartz.datasource.driverClassName=${domibus.datasource.driverClassName}
                     #the JDBC url for the DB
                     domibus.quartz.datasource.url=${domibus.datasource.url}
                     #default authentication username used when obtaining Connections from the underlying driver
                     domibus.quartz.datasource.user=${domibus.datasource.user}
                     #the default authentication password used when obtaining Connections from the underlying driver
                     domibus.quartz.datasource.password=${domibus.datasource.password}
                     #HikariCP specific
                     #Controls the maximum lifetime of a connection in the pool (in seconds)
                     domibus.quartz.datasource.maxLifetime=1800
                     #Controls the maximum amount of time (in seconds) that a client will wait for a connection from the pool
                     domibus.quartz.datasource.connectionTimeout=30
                     #Controls the maximum amount of time (in seconds) that a connection is allowed to sit idle in the pool
                     domibus.quartz.datasource.idleTimeout=600
                     #Controls the maximum size that the pool is allowed to reach, including both idle and in-use connections
                     domibus.quartz.datasource.maxPoolSize=10
                     #Controls the minimum number of idle connections that HikariCP tries to maintain in the pool
                     domibus.quartz.datasource.minimumIdle=1

### Weblogic only
                - Execute the WLST API script remove.py (from "/conf/domibus/scripts/upgrades") 5.0-to-5.0.8-Weblogic-removeObsoleteJDBCDatasource.properties to remove the edeliveryNonXA datasource:
                     wlstapi.cmd ../scripts/remove.py --property ../5.0-to-5.0.8-Weblogic-removeObsoleteJDBCDatasource.properties
                - Execute the WLST API script import.py (from "/conf/domibus/scripts/upgrades") 5.0-to-5.0.8-WeblogicCluster.properties or 5.0-to-5.0.8-WeblogicSingleServer.properties to add the eDeliveryQuartzDs datasource: 
                     wlstapi.cmd ../scripts/import.py --property ../5.0-to-5.0.8-WeblogicCluster.properties

### Wildfly only
                - In file "cef_edelivery_path/domibus/standalone/configuration/standalone-full.xml" replace the following datasource and pool names from

                     .............................
                     <datasource jndi-name="java:/jdbc/cipaeDeliveryNonXADs" pool-name="eDeliveryMysqlNonXADS" enabled="true" use-ccm="true">
                     .....................    
                to
                     .............................
                     <datasource jndi-name="java:/jdbc/cipaeDeliveryQuartzDs" pool-name="eDeliveryMysqlQuartzDS" enabled="true" use-ccm="true">
                     .....................
                and from Oracle
                     <datasource jndi-name="java:/jdbc/cipaeDeliveryNonXADs" pool-name="eDeliveryOracleNonXADS" enabled="true" use-ccm="true">
                     .............................
                to Oracle
                     <datasource jndi-name="java:/jdbc/cipaeDeliveryQuartzDs" pool-name="eDeliveryOracleQuartzDS" enabled="true" use-ccm="true">
                     .............................

## Domibus 5.0.7 (from 5.0.6):
                - Replace the Domibus war and the default plugin(s) config file(s), property file(s) and jar(s)
                - Update the file `cef_edelivery_path/domibus/conf/domibus/internal/activemq.xml` and make sure the <property-placeholder> section has the attribute `system-properties-mode="ENVIRONMENT"`. The line should look exactly like this:
                 `<context:property-placeholder system-properties-mode="ENVIRONMENT" ignore-resource-not-found="false" ignore-unresolvable="false"/>`
## Domibus 5.0.6 (from 5.0.5):
                - Replace the Domibus war and the default plugin(s) config file(s), property file(s) and jar(s)
                - Replace the default dss extension jar into "/domibus/conf/domibus/extensions/lib"
### Partitioning (only oracle)
#### If the database was not partitioned
               - Run as edelivery_user: @oracle-5.0.5-to-5.0.6-partitioning-upgrade.ddl
## Domibus 5.0.5 (from 5.0.4):
                - Replace the Domibus war and the default plugin(s) config file(s), property file(s) and jar(s)
                - Replace the default dss extension jar into  "/conf/domibus/extensions/lib"
                - Run the appropriate DB upgrade script (mysql-5.0.2-to-5.0.5-upgrade.ddl for MySQL or oracle-5.0.2-to-5.0.5-upgrade.ddl for Oracle)
### Partitioning (only oracle)
#### Situation A: upgrading an existing 5.0.4 database, that contains user messages and was partitioned
                    - Run as edelivery_user:
    @oracle-5.0-partition-upgrade-procedures.sql
    EXECUTE MIGRATE_5_0_4_PARTITIONED_TO_5_0_5;

#### Situation B: upgrading an existing 5.0.4 database, that contains user messages and was not partitioned
               - Run as edelivery_user:
    @oracle-5.0.2-to-5.0.5-partitioning-upgrade.ddl
    @oracle-5.0.5-partitioning-fix.ddl
## Domibus 5.0.4 (from 5.0.3):
                - Replace the Domibus war
                - Replace the default plugin(s) property file(s) and jar(s) into "/domibus/conf/domibus/plugins/config" respectively into "/domibus/conf/domibus/plugins/lib"
### Partitioning (only oracle)
#### Situation A: upgrading an existing 5.0.3 database, that contains user messages and was partitioned
- no changes needed
#### Situation B: upgrading an existing 5.0.3 database, that contains user messages and was not partitioned
                    - Run as sys:
    GRANT EXECUTE ON DBMS_LOCK TO <edelivery_user>;

                - Run as edelivery_user:
    @oracle-5.0-partition-upgrade-procedures.sql
    EXECUTE MIGRATE_5_0_3_UNPARTITIONED_TO_5_0_4;
    @oracle-5.0-partition-detail-tables.sql
    @oracle-5.0-create-partitions-job.sql
## Domibus 5.0.3 (from 5.0.2):
                - Replace the Domibus war
### Partitioning (only oracle)
#### Situation A: upgrading an existing 5.0.2 database, that contains user messages and was partitioned
                    - Run as edelivery_user:
    @oracle-5.0-partition-upgrade-procedures.sql
    EXECUTE MIGRATE_5_0_2_PARTITIONED_TO_5_0_3;
#### Situation B: upgrading an existing 5.0.2 database, that contains user messages and was not partitioned
                    - Run as sys:
    GRANT EXECUTE ON DBMS_LOCK TO <edelivery_user>;

                - Run as edelivery_user:
    @oracle-5.0-partition-upgrade-procedures.sql
    EXECUTE MIGRATE_5_0_2_UNPARTITIONED_TO_5_0_3;
    @oracle-5.0-partition-detail-tables.sql
    @oracle-5.0-create-partitions-job.sql
## Domibus 5.0.2 (from 5.0.1):
                - Replace the Domibus war
                - Run the appropriate DB upgrade script(mysql-5.0.1-to-5.0.2-upgrade.ddl for MySQL or oracle-5.0.1-to-5.0.2-upgrade.ddl for Oracle)
### Partitioning (only oracle)
#### Situation A: partitioning an existing 5.0.2 database, that contains user messages and was not partitioned

                    - Run as sys:
    GRANT EXECUTE ON DBMS_LOCK TO <edelivery_user>;

                - Run as edelivery_user:
    @oracle-5.0-partition-upgrade-procedures.sql
    EXECUTE MIGRATE_5_0_1_PARTITIONED_TO_5_0_2;
    @oracle-5.0-partition-detail-tables.sql
    @oracle-5.0-create-partitions-job.sql
#### Situation B: partitioning an empty 5.0.2 database that was not partitioned

                - Run as edelivery_user:  @oracle-5.0.2-partitioning.ddl

## Domibus 5.0.1 (from 5.0):
                - Replace the Domibus war
                - Run the appropriate DB upgrade script(mysql-5.0-to-5.0.1-upgrade.ddl for MySQL or oracle-5.0-to-5.0.1-upgrade.ddl for Oracle)

## Domibus 5.0 (from 4.2.13)

  ### Multitenancy only
                    - domibus.security.keystore.* and domibus.security.truststore.* properties are used only the first time domibus starts and persisted in the DB to be used from there on;
                    - Create a folder named "domains" in "conf/domibus" and, inside it, create a new folder for every domain (e.g. conf/domibus/domains/domain1)
                    - Move the super-domibus.properties file into "conf/domibus/domains"
                    - For each domain, move its properties file into the domain folder (e.g. move domain1-domibus.properties into conf/domibus/domains/domain1/)
                    - For each domain, move its logback file into the domain folder (e.g. move domain1-logback.xml into conf/domibus/domains/domain1/)
                      and then update the reference to the domain logback file in the main logback.xml file, in the multitenancy section
                    - For each domain, move domain_name_clientauthentication.xml file into the domain folder (e.g. move conf/domibus/domain1_clientauthentication.xml
                      into conf/domibus/domains/domain1/domain1_clientauthentication.xml)
                    - For each domain, create a "keystores" folder (e.g. conf/domibus/domains/domain1/keystores) and move inside it the keystores used by that domain;
                      update the "domibus.security.keystore.location" and "domibus.security.truststore.location" paths in the domain properties file
                    - Plugins:
                        * Create a folder named "domains" in "conf/domibus/plugins/config" and, inside it, create a new folder for every domain (e.g. conf/domibus/plugins/config/domains/domain1)
                        * FS-Plugin multitenancy installation: move any domain specific properties from fs-plugin.properties
                        to a domain specific property file (e.g. conf/domibus/plugins/config/domains/domain1/domain1-fs-plugin.properties)
                        * WS-Plugin multitenancy installation: move any domain specific properties from ws-plugin.properties
                        to a domain specific property file (e.g. conf/domibus/plugins/config/domains/domain1/domain1-ws-plugin.properties)
                        * JMS-Plugin multitenancy installation: move any domain specific properties from jms-plugin.properties
                        to a domain specific property file (e.g. conf/domibus/plugins/config/domains/domain1/domain1-jms-plugin.properties)
                    Please note that these changes need to be done for the "default" domain too, and that the properties in the properties files are still prefixed with the domain name.

  ### Tomcat only

                        o [MySQL only]
                            o update the "domibus.datasource.url" properties:
                                domibus.datasource.url=jdbc:mysql://${domibus.database.serverName}:${domibus.database.port}/${domibus.database.schema}?useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC

                        o in file "cef_edelivery_path/domibus/conf/domibus/internal/activemq.xml":
                                  - in the destinations section add the following queues:
                                             .............................
                                             <destinations>
                                                 .............................
                                                  <queue id="wsPluginSendQueue" physicalName="${wsplugin.send.queue:domibus.wsplugin.send.queue}"/>
                                                 .............................
                                             </destinations>
                                             .............................
                                  -  in the redeliveryPolicyEntries section add the following entries:
                                             .............................
                                             <redeliveryPolicyEntries>
                                                 .............................
                                                 <redeliveryPolicy queue="${wsplugin.send.queue:domibus.wsplugin.send.queue}" maximumRedeliveries="0"/>
                                                 .............................
                                             </redeliveryPolicyEntries>
                                             .............................
                         o If you use custom queues please update the following in the file "cef_edelivery_path/domibus/conf/domibus/internal/activemq.xml".
                           If you don't use custom queues just replace the old file with the new file version:
                             - in the policies section add the following:
                              .............................
                                            <policyEntries>
                                             .............................
                                                 <policyEntry queue="domibus.internal.earchive.notification.queue">
                                                     <deadLetterStrategy>
                                                         <!--<individualDeadLetterStrategy queuePrefix="DLQ."/>-->
                                                         <sharedDeadLetterStrategy processExpired="false">
                                                             <deadLetterQueue>
                                                                 <queue physicalName="domibus.internal.earchive.notification.dlq"/>
                                                             </deadLetterQueue>
                                                         </sharedDeadLetterStrategy>
                                                     </deadLetterStrategy>
                                                     <dispatchPolicy>
                                                         <priorityDispatchPolicy/>
                                                     </dispatchPolicy>
                                                 </policyEntry>
                             - in the destinations section add the following queues:
                                        .............................
                                        <destinations>
                                            .............................
                                             <queue id="eArchiveQueue" physicalName="domibus.internal.earchive.queue"/>
                                             <queue id="eArchiveNotificationQueue" physicalName="domibus.internal.earchive.notification.queue"/>
                                             <queue id="eArchiveNotificationDLQ" physicalName="domibus.internal.earchive.notification.dlq"/>
                              -  in the redeliveryPolicyEntries section add the following entries:
                                             .............................
                                             <redeliveryPolicyEntries>
                                                 .............................
                                                 <redeliveryPolicy queue="domibus.internal.earchive.queue" maximumRedeliveries="0"/>
                                                 <redeliveryPolicy queue="domibus.internal.earchive.notification.queue" maximumRedeliveries="6" redeliveryDelay="1800000"/>
                                                 <redeliveryPolicy queue="domibus.internal.earchive.notification.dlq" maximumRedeliveries="0"/>
                              -  in the discardingDLQBrokerPlugin update the dropOnly parameter value as below:
                                            - original:
                                                <discardingDLQBrokerPlugin dropAll="false" dropOnly="domibus.internal.dispatch.queue domibus.internal.pull.queue domibus.internal.alert.queue" reportInterval="10000"/>
                                            -new configuration:
                                                <discardingDLQBrokerPlugin dropAll="false" dropOnly="domibus.internal.dispatch.queue domibus.internal.pull.queue domibus.internal.alert.queue domibus.internal.earchive.queue domibus.internal.earchive.notification.dlq" reportInterval="10000"/>
  ### Wildfly only
                         o in file "cef_edelivery_path/domibus/standalone/configuration/standalone-full.xml":
                          - add the following queues in the destination section
                                      .............................
                                      <jms-destinations>
                                          .............................
                                           <jms-queue name="DomibusEArchiveQueue" entries="java:/jms/domibus.internal.earchive.queue java:/jms/queue/DomibusEArchiveQueue" durable="true"/>
                                           <jms-queue name="DomibusEArchiveNotificationQueue" entries="java:/jms/domibus.internal.earchive.notification.queue java:/jms/queue/DomibusEArchiveNotificationQueue" durable="true"/>
                                           <jms-queue name="DomibusEArchiveNotificationDLQ" entries="java:/jms/domibus.internal.earchive.notification.dlq java:/jms/queue/DomibusEArchiveNotificationDLQ" durable="true"/>
                                          .............................
                                      </jms-destinations>
                                      .............................
                           -  in the address-settings section
                                   o add the following address-setting configurations:
                                          .............................
                                          <address-settings>
                                              .............................
                                              <address-setting name="jms.queue.DomibusEArchiveQueue" expiry-address="jms.queue.ExpiryQueue" max-delivery-attempts="0"/>
                                              <address-setting name="jms.queue.DomibusEArchiveNotificationQueue" expiry-address="jms.queue.DomibusEArchiveNotificationDLQ" max-delivery-attempts="6"/>
                                              <address-setting name="jms.queue.DomibusEArchiveNotificationDLQ" expiry-address="jms.queue.ExpiryQueue" max-delivery-attempts="0"/>
                                              .............................
                                          </address-settings>
                                          .............................
  ### Weblogic only
                        o execute the WLST API script remove.py (from "/conf/domibus/scripts/upgrades") 4.2-to-5.0-Weblogic-removeJDBCDatasources.properties to remove the 2 datasources of 4.2 (wlstapi.cmd ../scripts/remove.py --property ../deleteDatasources.properties)
                        o execute the WLST API script import.py (from "/conf/domibus/scripts/upgrades") 4.2-to-5.0-WeblogicSingleServer.properties for single server deployment or 4.2-to-5.0-WeblogicCluster.properties for cluster deployment
                        o [MySQL only]
                            o update the JDBC connection URL value in the Admin Console for your data sources by appending "&amp;useLegacyDatetimeCode=false&amp;serverTimezone=UTC" (without surrounding quotes) to their end:
                                jdbc:mysql://localhost:3306/domibus?autoReconnect=true&amp;useSSL=false
                                    should be changed to
                                jdbc:mysql://localhost:3306/domibus?autoReconnect=true&amp;useSSL=false&amp;useLegacyDatetimeCode=false&amp;serverTimezone=UTC
  ### DB upgrade script
                - Run the appropriate DB upgrade script:
                    o [Oracle only]
                        - single tenancy: oracle-4.2.6-to-5.0-upgrade.ddl
                        - multitenancy:
                            - general schema: oracle-4.2.6-to-5.0-multi-tenancy-upgrade.ddl
                            - domain schemas: oracle-4.2.6-to-5.0-upgrade.ddl
                    o [MySQL only]
                        The scripts below - please adapt to your local configuration (i.e. users, database names) - can be run using either:
                            - the root user, specifying the target databases as part of the command. For example, for single tenancy:
                                    mysql -u root -p domibus < mysql-4.2.6-to-5.0-upgrade.ddl
                                or, for multitenancy:
                                    mysql -u root -p domibus_general < mysql-4.2.6-to-5.0-multi-tenancy-upgrade.ddl
                                    mysql -u root -p domibus_domain_1 < mysql-4.2.6-to-5.0-upgrade.ddl
                            - the non-root user (e.g. edelivery): for which the root user must first relax the conditions on function creation by granting the SYSTEM_VARIABLES_ADMIN right to the non-root user:
                                    GRANT SYSTEM_VARIABLES_ADMIN ON *.* TO 'edelivery'@'localhost';
                              and then specifying the target databases as part of the command. For example, for single tenancy:
                                     mysql -u edelivery -p domibus < mysql-4.2.6-to-5.0-upgrade.ddl
                                 or, for multitenancy:
                                     mysql -u edelivery -p domibus_general < mysql-4.2.6-to-5.0-multi-tenancy-upgrade.ddl
                                     mysql -u edelivery -p domibus_domain_1 < mysql-4.2.6-to-5.0-upgrade.ddl.
  ### Data upgrade
                - Data upgrade scripts should be run in order to migrate data from old tables to the new tables:
   #### Oracle only
                        Domibus application (.war) should be stopped while running these:
                            - single tenancy:
                                - step 1: oracle-4.2.6-to-5.0-data-upgrade-step1.ddl (it will drop and then recreate new version of the tables - errors which appear during dropping could be ignored)
                                - UTC date migration step: execute the migrate procedure from the MIGRATE_42_TO_50_utc_conversion package providing the correct TIMEZONE parameter - i.e. the timezone ID in which the date time values have been previously saved (e.g. 'Europe/Brussels') -;
                                - step 2: oracle-4.2.6-to-5.0-data-upgrade-step2.ddl (it will create the package for data upgrade, run the upgrade procedure)
                                If upgrade procedure fails step 1 and step 2 could be run again. Once upgrade procedure ends successfully we could proceed to step 3
                                - step 3: oracle-4.2.6-to-5.0-data-upgrade-step3.ddl (this step will finish the upgrade - during this step 4.2 version of the tables will be renamed to OLD_);
                                This step isn't reversible so it must be executed once step 1 and step 2 are successful
                                - (Optional) step 4: oracle-4.2.6-to-5.0-data-upgrade-step4.ddl (during this step the original tables and the upgrade subprograms are dropped)
                                This step isn't reversible so it must be executed once step 1, step 2 and step3 are successful
                                - (Optional) partitioning: oracle-5.0-partitioning.ddl (if you further plan on using Oracle partitions in an Enterprise Editions database)
                            - multitenancy:
                                - general schema:
                                    - step 1: oracle-4.2.6-to-5.0-data-upgrade-multi-tenancy-step1.ddl (it will drop and then recreate new version of the tables - errors which appear during dropping could be ignored)
                                    - UTC date migration step: execute the migrate_multitenancy procedure from the MIGRATE_42_TO_50_utc_conversion package providing the correct TIMEZONE parameter - i.e. the timezone ID in which the date time values have been previously saved (e.g. 'Europe/Brussels') -;
                                    - step 2: oracle-4.2.6-to-5.0-data-upgrade-multi-tenancy-step2.ddl (it will create the package for data upgrade, run the upgrade procedure)
                                    If upgrade procedure fails step 1 and step 2 could be run again. Once upgrade procedure ends successfully we could proceed to step 3
                                    - step 3: oracle-4.2.6-to-5.0-data-upgrade-multi-tenancy-step3.ddl (this step will finish the upgrade - during this step 4.2 version of the tables will be renamed to OLD_);
                                    This step isn't reversible so it must be executed once step 1 and step 2 are successful
                                    - (Optional) step 4: oracle-4.2.6-to-5.0-data-upgrade-multi-tenancy-step4.ddl (during this step the original tables and the upgrade subprograms are dropped)
                                    This step isn't reversible so it must be executed once step 1, step 2 and step3 are successful
                                - domain schemas:
                                    - step 1: oracle-4.2.6-to-5.0-data-upgrade-step1.ddl (it will drop and then recreate new version of the tables - errors which appear during dropping could be ignored)
                                    - UTC date migration step: execute the migrate procedure from the MIGRATE_42_TO_50_utc_conversion package providing the correct TIMEZONE parameter - i.e. the timezone ID in which the date time values have been previously saved (e.g. 'Europe/Brussels') -;
                                    - step 2: oracle-4.2.6-to-5.0-data-upgrade-step2.ddl (it will create the package for data upgrade, run the upgrade procedure)
                                    If upgrade procedure fails step 1 and step 2 could be run again. Once upgrade procedure ends successfully we could proceed to step 3
                                    - step 3: oracle-4.2.6-to-5.0-data-upgrade-step3.ddl (this step will finish the upgrade - during this step 4.2 version of the tables will be renamed to OLD_);
                                    This step isn't reversible so it must be executed once step 1 and step 2 are successful
                                    - (Optional) step 4: oracle-4.2.6-to-5.0-data-upgrade-step4.ddl (during this step the original tables and the upgrade subprograms are dropped)
                                    This step isn't reversible so it must be executed once step 1, step 2 and step3 are successful
                                    - (Optional) partitioning: oracle-5.0-partitioning.ddl (if you further plan on using Oracle partitions in an Enterprise Editions database)
                                    - grant privileges to the general schema using oracle-5.0-multi-tenancy-rights.sql, updating the schema names before execution

#### MySQL only
                        The scripts below - please adapt to your local configuration (i.e. users, database names) - can be run using either:
                    	    - the root user, specifying the target databases as part of the command. For example, for single tenancy:
                                    mysql -u root -p domibus < mysql-4.2.6-to-5.0-data-upgrade-step1.ddl
                                or, for multitenancy:
                                    mysql -u root -p domibus_general < mysql-4.2.6-to-5.0-data-upgrade-multi-tenancy-step1.ddl
                                    mysql -u root -p domibus_domain_1 < mysql-4.2.6-to-5.0-data-upgrade-step1.ddl
                            - or the non-root user (e.g. edelivery): for which the root user must first relax the conditions on function creation by granting the SYSTEM_VARIABLES_ADMIN right to the non-root user:
                                    GRANT SYSTEM_VARIABLES_ADMIN ON *.* TO 'edelivery'@'localhost';
                              and then specifying the target databases as part of the command. For example, for single tenancy:
                                     mysql -u edelivery -p domibus < mysql-4.2.6-to-5.0-data-upgrade-step1.ddl
                                 or, for multitenancy:
                                     mysql -u edelivery -p domibus_general < mysql-4.2.6-to-5.0-data-upgrade-multi-tenancy-step1.ddl
                                     mysql -u edelivery -p domibus_domain_1 < mysql-4.2.6-to-5.0-data-upgrade-step1.ddl.

                        Domibus application (.war) should be stopped while running these:
                            - single tenancy:
                                - step 1: mysql-4.2.6-to-5.0-data-upgrade-step1.ddl (it will drop and then recreate new version of the tables - errors which appear during dropping could be ignored)
                                - UTC date migration step
                                    1. Identify your current named time zone such as 'Europe/Brussels', 'US/Eastern', 'MET' or 'UTC' (e.g. issue SELECT @@GLOBAL.time_zone, @@SESSION.time_zone;)
                                    2. Populate the MySQL time zone tables if not already done: https://dev.mysql.com/doc/refman/8.0/en/time-zone-support.html#time-zone-installation
                                    3. call the MIGRATE_42_TO_50_utc_conversion procedure providing the correct TIMEZONE named time zone parameter identified above - i.e. the timezone ID in which the date time values have been previously saved -;
                                - step 2: mysql-4.2.6-to-5.0-data-upgrade-step2.ddl (it will create the package for data upgrade, run the upgrade procedure)
                                If upgrade procedure fails step 1 and step 2 could be run again. Once upgrade procedure ends successfully we could proceed to step 3
                                - step 3: mysql-4.2.6-to-5.0-data-upgrade-step3.ddl (this step will finish the upgrade - during this step 4.2 version of the tables will be renamed to OLD_);
                                This step isn't reversible so it must be executed once step 1 and step 2 are successful
                                - (Optional) step 4: mysql-4.2.6-to-5.0-data-upgrade-step4.ddl (during this step the original tables and the upgrade subprograms are dropped)
                                This step isn't reversible so it must be executed once step 1, step 2 and step3 are successful
                            - multitenancy:
                                - general database:
                                    - step 1: mysql-4.2.6-to-5.0-data-upgrade-multi-tenancy-step1.ddl (it will drop and then recreate new version of the tables - errors which appear during dropping could be ignored)
                                    - UTC date migration step
                                        1. Identify your current named time zone such as 'Europe/Brussels', 'US/Eastern', 'MET' or 'UTC' (e.g. issue SELECT @@GLOBAL.time_zone, @@SESSION.time_zone;)
                                        2. Populate the MySQL time zone tables if not already done: https://dev.mysql.com/doc/refman/8.0/en/time-zone-support.html#time-zone-installation
                                        3. call the MIGRATE_42_TO_50_utc_conversion_multitenancy procedure providing the correct TIMEZONE named time zone parameter identified above - i.e. the timezone ID in which the date time values have been previously saved -;
                                    - step 2: mysql-4.2.6-to-5.0-data-upgrade-multi-tenancy-step2.ddl (it will create the package for data upgrade, run the upgrade procedure)
                                    If upgrade procedure fails step 1 and step 2 could be run again. Once upgrade procedure ends successfully we could proceed to step 3
                                    - step 3: mysql-4.2.6-to-5.0-data-upgrade-multi-tenancy-step3.ddl (this step will finish the upgrade - during this step 4.2 version of the tables will be renamed to OLD_);
                                    This step isn't reversible so it must be executed once step 1 and step 2 are successful
                                    - (Optional) step 4: mysql-4.2.6-to-5.0-data-upgrade-multi-tenancy-step4.ddl (during this step the original tables and the upgrade subprograms are dropped)
                                    This step isn't reversible so it must be executed once step 1, step 2 and step3 are successful
                                - domain databases:
                                    - step 1: mysql-4.2.6-to-5.0-data-upgrade-step1.ddl (it will drop and then recreate new version of the tables - errors which appear during dropping could be ignored)
                                    - UTC date migration step
                                        1. Identify your current named time zone such as 'Europe/Brussels', 'US/Eastern', 'MET' or 'UTC' (e.g. issue SELECT @@GLOBAL.time_zone, @@SESSION.time_zone;)
                                        2. Populate the MySQL time zone tables if not already done: https://dev.mysql.com/doc/refman/8.0/en/time-zone-support.html#time-zone-installation
                                        3. call the MIGRATE_42_TO_50_utc_conversion procedure providing the correct TIMEZONE named time zone parameter identified above - i.e. the timezone ID in which the date time values have been previously saved -;
                                    - step 2: mysql-4.2.6-to-5.0-data-upgrade-step2.ddl (it will create the package for data upgrade, run the upgrade procedure)
                                    If upgrade procedure fails step 1 and step 2 could be run again. Once upgrade procedure ends successfully we could proceed to step 3
                                    - step 3: mysql-4.2.6-to-5.0-data-upgrade-step3.ddl (this step will finish the upgrade - during this step 4.2 version of the tables will be renamed to OLD_);
                                    This step isn't reversible so it must be executed once step 1 and step 2 are successful
                                    - (Optional) step 4: mysql-4.2.6-to-5.0-data-upgrade-step4.ddl (during this step the original tables and the upgrade subprograms are dropped)
                                    This step isn't reversible so it must be executed once step 1, step 2 and step3 are successful
  ### Cache
                - Update the "/conf/domibus/internal/ehcache.xml" cache definitions file:
                    - If you use custom caches definitions defined in this file replace the old file with the new file and perform the following steps:
                        Replace:     <cache alias="policyCache">
                                         <expiry>
                                             <ttl>3600</ttl>
                                         </expiry>
                                         <heap unit="MB">5</heap>
                                     </cache>
                        With:
                                     <cache alias="policyCache">
                                        <expiry>
                                            <ttl>3600</ttl>
                                        </expiry>
                                        <heap unit="entries">5000</heap>
                                     </cache>
                        Or with <cache alias="policyCache" uses-template="ttl-3600-heap-5000"/> if you want to reuse cache-template "ttl-3600-heap-5000" already defined by Domibus
                    - If you don't use custom caches just replace the old file with the new file version
                    - Add a new cache key named "domibusPropertyMetadata"
                    - [Wildfly only]
                        o [MySQL only]
                            o in standalone-full.xml, update the connectionUrl element for your data sources by appending "&amp;useLegacyDatetimeCode=false&amp;serverTimezone=UTC" (without surrounding quotes) to their end:
                                <connection-url>jdbc:mysql://localhost:3306/domibus?autoReconnect=true&amp;useSSL=false</connection-url>
                                    should be changed to
                                <connection-url>jdbc:mysql://localhost:3306/domibus?autoReconnect=true&amp;useSSL=false&amp;useLegacyDatetimeCode=false&amp;serverTimezone=UTC</connection-url>
                       o in file "cef_edelivery_path/domibus/standalone/configuration/standalone-full.xml":
                            - add the following queues
                                .............................
                                <subsystem xmlns="urn:jboss:domain:messaging-activemq:3.0">
                                    <server name="default">
                                    .............................
                                     <address-setting name="jms.queue.DomibusWSPluginSendQueue" expiry-address="jms.queue.ExpiryQueue" max-delivery-attempts="0"/>

                                    <jms-queue name="DomibusWSPluginSendQueue" entries="java:/jms/domibus.wsplugin.send.queue java:/jms/queue/DomibusWSPluginSendQueue" durable="true"/>
                                    .............................
                                    </server>
                                </subsystem>
                                .............................
                    - [Tomcat only]

  ### Domibus properties changes:
                        o Modify the Domibus properties file "\conf\domibus\domibus.properties":
                                  - rename the "domibus.jms.XAConnectionFactory.maxPoolSize" property to "domibus.jms.connectionFactory.maxPoolSize" (if present)
                                  - remove these properties:
                                            - domibus.datasource.xa.*
                                            - domibus.entityManagerFactory.jpaProperty.hibernate.transaction.factory_class
                                            - domibus.entityManagerFactory.jpaProperty.hibernate.transaction.jta.platform
                                            - all properties starting with domibus.ui.replication.enabled
                                            - domibus.jms.queue.ui.replication
                                  - replace the value of the "domibus.entityManagerFactory.jpaProperty.hibernate.connection.driver_class" property as follows:
                                            - set the value to "com.mysql.cj.jdbc.Driver" if it was "com.mysql.cj.jdbc.MysqlXADataSource"
                                            - set the value to "oracle.jdbc.driver.OracleDriver" if it was "oracle.jdbc.xa.client.OracleXADataSource"
                                  - rename the "domibus.dynamicdiscovery.partyid.responder.role" and the "domibus.dynamicdiscovery.partyid.type" properties with Oasis and Peppol specific ones:
                                            - domibus.dynamicdiscovery.peppolclient.partyid.responder.role
                                            - domibus.dynamicdiscovery.oasisclient.partyid.responder.role
                                            - domibus.dynamicdiscovery.peppolclient.partyid.type
                                            - domibus.dynamicdiscovery.oasisclient.partyid.type
                        o [Tomcat only]
                            - Remove all properties under the section "Atomikos"( all com.atomikos.* properties)
                        o [WebLogic only]
                            - in the WebLogic console, change the eDeliveryConnectionFactory JMS connection factory from XA to non-XA (section eDeliveryConnectionFactory->Configuration->Transactions: uncheck "XA Connection Factory Enabled")
                        o [Wildfly only]
                            - in the file "cef_edelivery_path/domibus/standalone/configuration/standalone-full.xml":
                                    - remove the <xa-datasource jndi-name="java:/jdbc/cipaeDeliveryDs"...> datasource
                                    - clone the <datasource jndi-name="java:/jdbc/cipaeDeliveryNonXADs"...> datasource and set the jndi-name attribute to "java:/jdbc/cipaeDeliveryDs" and the pool-name attribute to either "eDeliveryMysqlDS" or "eDeliveryOracleDS"
  ### Other common changes
                - Replace the Domibus war
                - The minimum password length for users has increased to 16 and it is recommended to change them for the existing users
                - Rename conf/domibus/default_clientauthentication.xml to conf/domibus/clientauthentication.xml
                - Replace the default plugins property files and jars into "conf/domibus/plugins/config" respectively into "/conf/domibus/plugins/lib"
                - Custom 4.2.x plugins are no longer compatible with Domibus 5.0 and must be adapted to use the new plugin api. For more details please check the Plugin Cookbook.
                  The following API has been removed:
                    o classes: eu.domibus.submission.WeblogicNotificationListenerService, eu.domibus.plugin.NotificationListener, eu.domibus.plugin.NotificationListenerService, eu.domibus.common.JMSConstants,
                               eu.domibus.plugin.QueueMessageLister, eu.domibus.plugin.MessageLister, eu.domibus.core.plugin.notification.QueueMessageListerConfiguration
                    o methods: eu.domibus.plugin.BackendConnector.listPendingMessages(), eu.domibus.plugin.AbstractBackendConnector.listPendingMessages()
                               eu.domibus.plugin.BackendConnector.messageSendFailed(java.lang.String), eu.domibus.plugin.BackendConnector.messageSendSuccess(java.lang.String), eu.domibus.plugin.BackendConnector.deliverMessage(java.lang.String),
                               eu.domibus.ext.services.DomibusPropertyManagerExt.setKnownPropertyValue(java.lang.String, java.lang.String, java.lang.String), eu.domibus.common.DeliverMessageEvent.setFinalRecipient(java.lang.String),
                               eu.domibus.common.DeliverMessageEvent.getFinalRecipient(java.lang.String), eu.domibus.common.DeliverMessageEvent.getProperties(), eu.domibus.common.MessageDeletedBatchEvent.getMessageIds,
                               eu.domibus.common.MessageDeletedBatchEvent.setMessageIds, eu.domibus.common.MessageSendFailedEvent.getProperties, eu.domibus.common.MessageSendSuccessEvent.getProperties,
                               eu.domibus.common.MessageStatusChangeEvent.getProperties, eu.domibus.common.PayloadAbstractEvent.getProperties, eu.domibus.ext.services.DomibusPropertyExtService.getDomainProperty(eu.domibus.ext.domain.DomainDTO, java.lang.String),
                               eu.domibus.ext.services.DomibusPropertyExtService.setDomainProperty, eu.domibus.ext.services.DomibusPropertyExtService.getDomainProperty, eu.domibus.ext.services.DomibusPropertyExtService.getDomainResolvedProperty,
                               eu.domibus.ext.services.DomibusPropertyExtService.getResolvedProperty, eu.domibus.ext.services.PModeExtService.updatePModeFile(byte[], java.lang.String)
## Domibus 4.2.13 (from 4.2.12):
                - Replace the Domibus war
                - Replace the default dss extension jar into  "/conf/domibus/extensions/lib"
## Domibus 4.2.12 (from 4.2.11):
                - Replace the Domibus war
                - Replace the default dss extension jar into  "/conf/domibus/extensions/lib"
## Domibus 4.2.11 (from 4.2.10):
                - Replace the Domibus war
## Domibus 4.2.10 (from 4.2.9):
                - Replace the Domibus war
## Domibus 4.2.9 (from 4.2.8):
                - Replace the Domibus war
 ## Domibus 4.2.8 (from 4.2.7):
                - Replace the Domibus war
 ## Domibus 4.2.7 (from 4.2.6):
                - Replace the Domibus war
 ## Domibus 4.2.6 (from 4.2.5):
                - Please remove the following properties from the file /conf/domibus/extensions/config/authentication-dss-extension.properties:
                        - domibus.authentication.dss.custom.trusted.lists.list1.code
                        - domibus.authentication.dss.lotl.country.code=EU
                        - domibus.dss.data.loader.connection.request.timeout
                - Ehcache has been upgraded to version 3.8.1 affecting "/conf/domibus/internal/ehcache.xml" cache definitions file:
                    - If you use custom caches definitions defined in this file replace the old file with the new file and perform the following steps:
                        Replace:     <cache name="policyCache"
                                            maxBytesLocalHeap="5m"
                                            timeToLiveSeconds="3600"
                                            overflowToDisk="false" >
                                     </cache>
                        With:
                                     <cache alias="policyCache">
                                        <expiry>
                                            <ttl>3600</ttl>
                                        </expiry>
                                        <heap unit="MB">5</heap>
                                     </cache>
                        Or with <cache alias="policyCache" uses-template="ttl-3600-heap-5mb"/> if you want to reuse cache-template "ttl-3600-heap-5mb" already defined by Domibus
                        Replace:
                                Configured dss-cache
                        with:
                                <cache alias="dss-cache"><expiry><ttl>3600</ttl></expiry><heap unit="MB">50</heap></cache>

                    - If you don't use custom caches just replace the old file with the new file version
                - Replace the Domibus war and the default plugin(s) config file(s), property file(s) and jar(s) into "/conf/domibus/plugins/config" respectively into "/conf/domibus/plugins/lib"
                - Run the DB upgrade script for Oracle only oracle-4.2.3-to-4.2.6-upgrade.ddl
 ## Domibus 4.2.5 (from 4.2.4):
                - Replace the Domibus war
                - Replace the default dss extension jar into "/conf/domibus/extensions/lib"
                - Remove all revoked certificates from /conf/domibus/keystores/dss-tls-truststore.p12
 ## Domibus 4.2.4 (from 4.2.3):
                - Replace the Domibus war
 ## Domibus 4.2.3 (from 4.2.2):
                - Run the appropriate DB upgrade script(mysql-4.2.2-to-4.2.3-upgrade.ddl for MySQL or oracle-4.2.2-to-4.2.3-upgrade.ddl for Oracle)
                - Replace the Domibus war
                - Replace the default plugins property files and jars into "conf/domibus/plugins/config" respectively into "/conf/domibus/plugins/lib"
                - Replace the default dss extension jar into  "/conf/domibus/extentions/lib"
 ## Domibus 4.2.2 (from 4.2.1):

                - [MySQL8 only]
                   - Grant XA_RECOVER_ADMIN privilege to the user:
                        In MySQL 8.0, XA_RECOVER is permitted only to users who have the XA_RECOVER_ADMIN privilege. Prior to MySQL 8.0, any user could execute this and discover the XID values of XA transactions by other users.
                        This privilege requirement prevents users from discovering the XID values for outstanding prepared XA transactions other than their own.
                            - GRANT XA_RECOVER_ADMIN ON *.* TO 'edelivery_user'@'localhost';
                    - Execute below command to flush privileges:
                        When we grant some privileges for a user, running this command will reloads the grant tables in the mysql database enabling the changes to take effect without reloading or restarting mysql service.
                            - FLUSH PRIVILEGES;

                - Run the appropriate DB upgrade script(mysql-4.2.1-to-4.2.2-upgrade.ddl for MySQL or oracle-4.2.1-to-4.2.2-upgrade.ddl for Oracle)
                - Replace the Domibus war
                - Replace the default plugins property files and jars into "conf/domibus/plugins/config" respectively into "/conf/domibus/plugins/lib"
                - Change the name of 'domibus.ui.resend.action.enabled.received.minutes' property to 'domibus.action.resend.wait.minutes' in domibus.properties file.
 ## Domibus 4.2.1 (from 4.2):
                - [Oracle only]
                   - Grant access to your user to create stored procedures:
                        Open a command line session and log in (edelivery_user and password are the ones assigned during the Oracle installation):
                            $ sqlplus sys as sysdba
                        Once logged in Oracle execute:
                            GRANT CREATE PROCEDURE TO <edelivery_user>
                - Run the appropriate DB upgrade script(mysql-4.2-to-4.2.1-upgrade.ddl for MySQL or oracle-4.2-to-4.2.1-upgrade.ddl for Oracle)
                - Replace the Domibus war
                - Replace the default plugins property files and jars into "conf/domibus/plugins/config" respectively into "/conf/domibus/plugins/lib"
                - Replace the default dss extention jar into  "/conf/domibus/extensions/lib"
 