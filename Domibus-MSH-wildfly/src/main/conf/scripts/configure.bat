@ECHO off
SETLOCAL EnableDelayedExpansion

::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:::::::::::::::::::::::::::: The following properties need to be modified by the users :::::::::::::::::::::::::::::::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

:: The location where the Wildfly instance is installed
SET JBOSS_HOME=C:\path\to\wildfly

:: The name of the standalone configuration file that need to be updated
SET SERVER_CONFIG=standalone-full.xml

:: MySQL configuration
SET DB_TYPE=MySQL
SET DB_HOST=localhost
SET "DB_NAME=domibus?autoReconnect=true^&useSSL=false^&useLegacyDatetimeCode=false^&serverTimezone=UTC"
SET DB_PORT=3306
SET DB_USER=edelivery
SET DB_PASS=edelivery
SET JDBC_CONNECTION_URL=jdbc:mysql://%DB_HOST%:%DB_PORT%/!DB_NAME!
SET MYSQL_JDBC_DRIVER_DIR=%JBOSS_HOME%\modules\system\layers\base\com\mysql\main
SET MYSQL_JDBC_DRIVER_NAME=mysql-connector-java-X.Y.Z.jar

:: Oracle configuration
:: SET DB_TYPE=Oracle
:: SET DB_HOST=localhost
:: SET DB_PORT=1521
:: SET DB_USER=edelivery_user
:: SET DB_PASS=edelivery_password
:: SET JDBC_CONNECTION_URL="jdbc:oracle:thin:@%DB_HOST%:%DB_PORT%[:SID|/Service]"
:: SET ORACLE_JDBC_DRIVER_DIR=%JBOSS_HOME%\modules\system\layers\base\com\oracle\main
:: SET ORACLE_JDBC_DRIVER_NAME=ojdbc-X.Y.Z.jar

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:::::::::::::::::::::::::::::: The following part is not to be modified by the users ::::::::::::::::::::::::::::::::::::
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

:: The name of the files containing configuration
SET CLI_MYSQL_JDBC_DRIVER=resources\mysql\01-jdbc-driver.cli
SET CLI_MYSQL_DATA_SOURCES=resources\mysql\02-data-sources.cli
SET CLI_ORACLE_JDBC_DRIVER=resources\oracle\01-jdbc-driver.cli
SET CLI_ORACLE_DATA_SOURCES=resources\oracle\02-data-sources.cli
SET CLI_COMMON_MESSAGING=resources\common\01-messaging.cli
SET CLI_COMMON_NON_CLUSTER=resources\common\02-non-cluster.cli
SET CLI_COMMON_CLUSTER=resources\common\02-cluster.cli

ECHO -------------- JBOSS_HOME: %JBOSS_HOME%
ECHO -------------- SERVER_CONFIG: %SERVER_CONFIG%
ECHO -------------- DB_TYPE: %DB_TYPE%
ECHO -------------- DB_HOST: %DB_HOST%
ECHO -------------- DB_NAME: %DB_NAME%
ECHO -------------- DB_PORT: %DB_PORT%
ECHO -------------- DB_USER: %DB_USER%
ECHO -------------- DB_PASS: %DB_PASS%
ECHO -------------- JDBC_CONNECTION_URL: %JDBC_CONNECTION_URL%
ECHO -------------- MYSQL_JDBC_DRIVER_DIR: %MYSQL_JDBC_DRIVER_DIR%
ECHO -------------- MYSQL_JDBC_DRIVER_NAME: %MYSQL_JDBC_DRIVER_NAME%
ECHO -------------- ORACLE_JDBC_DRIVER_DIR: %ORACLE_JDBC_DRIVER_DIR%
ECHO -------------- ORACLE_JDBC_DRIVER_NAME: %ORACLE_JDBC_DRIVER_NAME%

ECHO -------------- Configure Wildfly to resolve parameter values from properties files
@PowerShell -Command "(Get-Content %JBOSS_HOME%/bin/jboss-cli.xml) -replace '<resolve-parameter-values>false</resolve-parameter-values>', '<resolve-parameter-values>true</resolve-parameter-values>' | Out-File -encoding UTF8 %JBOSS_HOME%/bin/jboss-cli.xml"

ECHO -------------- Prepare
SET > env.properties
@PowerShell -Command "(Get-Content env.properties) -replace '\\', '\\' | Out-File -encoding ASCII env.properties"
@PowerShell -Command "(Get-Content env.properties) -replace '\^&', '&' | Out-File -encoding ASCII env.properties"

ECHO -------------- Configure Wildfly
%JBOSS_HOME%\bin\jboss-cli.bat --file=resources\domibus-configuration-%DB_TYPE%.cli --properties=env.properties

ECHO -------------- Clean
DEL env.properties
