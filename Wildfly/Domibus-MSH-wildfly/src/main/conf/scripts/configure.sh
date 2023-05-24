#!/bin/bash

########################################################################################################################
########################## The following properties need to be modified by the users ###################################
########################################################################################################################

# The location where the Wildfly instance is installed
JBOSS_HOME=/path/to/wildfly

# The name of the standalone configuration file that need to be updated
SERVER_CONFIG=standalone-full.xml

# MySQL configuration
DB_TYPE=MySQL
DB_HOST=localhost
DB_NAME=domibus?autoReconnect=true\&useSSL=false\&useLegacyDatetimeCode=false\&serverTimezone=UTC
DB_PORT=3306
DB_USER=edelivery
DB_PASS=edelivery
JDBC_CONNECTION_URL=jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}
MYSQL_JDBC_DRIVER_DIR=${JBOSS_HOME}/modules/system/layers/base/com/mysql/main
MYSQL_JDBC_DRIVER_NAME=mysql-connector-java-X.Y.Z.jar

# Oracle configuration
#DB_TYPE=Oracle
#DB_HOST=localhost
#DB_PORT=1521
#DB_USER=edelivery_user
#DB_PASS=edelivery_password
#JDBC_CONNECTION_URL="jdbc:oracle:thin:@${DB_HOST}:${DB_PORT}[:SID|/Service]"
#ORACLE_JDBC_DRIVER_DIR=${JBOSS_HOME}/modules/system/layers/base/com/oracle/main
#ORACLE_JDBC_DRIVER_NAME=ojdbc-X.Y.Z.jar

########################################################################################################################
############################ The following part is not to be modified by the users #####################################
########################################################################################################################

# The name of the files containing configuration
CLI_MYSQL_JDBC_DRIVER=resources/mysql/01-jdbc-driver.cli
CLI_MYSQL_DATA_SOURCES=resources/mysql/02-data-sources.cli
CLI_ORACLE_JDBC_DRIVER=resources/oracle/01-jdbc-driver.cli
CLI_ORACLE_DATA_SOURCES=resources/oracle/02-data-sources.cli
CLI_COMMON_MESSAGING=resources/common/01-messaging.cli
CLI_COMMON_NON_CLUSTER=resources/common/02-non-cluster.cli
CLI_COMMON_CLUSTER=resources/common/02-cluster.cli
CLI_COMMON_INTERFACES=resources/common/03-interfaces.cli

echo "-------------- JBOSS_HOME: ${JBOSS_HOME}"
echo "-------------- SERVER_CONFIG: ${SERVER_CONFIG}"
echo "-------------- DB_TYPE: ${DB_TYPE}"
echo "-------------- DB_HOST: ${DB_HOST}"
echo "-------------- DB_NAME: ${DB_NAME}"
echo "-------------- DB_PORT: ${DB_PORT}"
echo "-------------- DB_USER: ${DB_USER}"
echo "-------------- DB_PASS: ${DB_PASS}"
echo "-------------- JDBC_CONNECTION_URL: ${JDBC_CONNECTION_URL}"
echo "-------------- MYSQL_JDBC_DRIVER_DIR: ${MYSQL_JDBC_DRIVER_DIR}"
echo "-------------- MYSQL_JDBC_DRIVER_NAME: ${MYSQL_JDBC_DRIVER_NAME}"
echo "-------------- ORACLE_JDBC_DRIVER_DIR: ${ORACLE_JDBC_DRIVER_DIR}"
echo "-------------- ORACLE_JDBC_DRIVER_NAME: ${ORACLE_JDBC_DRIVER_NAME}"

echo "-------------- Configure Wildfly to resolve parameter values from properties files"
sed -i.bak "s/<resolve-parameter-values>false<\/resolve-parameter-values>/\
<resolve-parameter-values>true<\/resolve-parameter-values>/" \
$JBOSS_HOME/bin/jboss-cli.xml

echo "-------------- Prepare"
export JBOSS_HOME SERVER_CONFIG DB_TYPE DB_HOST DB_NAME DB_PORT DB_PORT DB_USER DB_PASS JDBC_CONNECTION_URL \
MYSQL_JDBC_DRIVER_DIR MYSQL_JDBC_DRIVER_NAME ORACLE_JDBC_DRIVER_DIR ORACLE_JDBC_DRIVER_NAME \
CLI_MYSQL_JDBC_DRIVER CLI_MYSQL_DATA_SOURCES CLI_ORACLE_JDBC_DRIVER CLI_ORACLE_DATA_SOURCES CLI_COMMON_MESSAGING \
CLI_COMMON_NON_CLUSTER CLI_COMMON_CLUSTER CLI_COMMON_INTERFACES
printenv > env.properties

echo "-------------- Configure Wildfly"
${JBOSS_HOME}/bin/jboss-cli.sh --file=resources/domibus-configuration-${DB_TYPE}.cli --properties=env.properties

echo "-------------- Clean"
rm env.properties
