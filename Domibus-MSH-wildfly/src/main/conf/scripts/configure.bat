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
SET "DB_NAME=domibus?autoReconnect=true^&useSSL=false"
SET DB_PORT=3306
SET DB_USER=edelivery
SET DB_PASS=edelivery
SET JDBC_CONNECTION_URL=jdbc:mysql://%DB_HOST%:%DB_PORT%/!DB_NAME!
SET JDBC_DRIVER_DIR=%JBOSS_HOME%\modules\system\layers\base\com\mysql\main
SET JDBC_DRIVER_NAME=mysql-connector-java-X.Y.Z.jar

:: Oracle configuration
:: SET DB_TYPE=Oracle
:: SET DB_HOST=localhost
:: SET DB_PORT=1521
:: SET DB_USER=edelivery_user
:: SET DB_PASS=edelivery_password
:: SET JDBC_CONNECTION_URL="jdbc:oracle:thin:@%DB_HOST%:%DB_PORT%[:SID|/Service]"
:: SET JDBC_DRIVER_DIR=%JBOSS_HOME%\modules\system\layers\base\com\oracle\main
:: SET JDBC_DRIVER_NAME=ojdbc-X.Y.Z.jar

:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:::::::::::::::::::::::::::::: The following part is not to be modified by the users ::::::::::::::::::::::::::::::::::::
:::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

:: The name of the file containing common configuration
SET CLI_DOMIBUS_CONFIGURATION_COMMON=resources\domibus-configuration-common.cli

ECHO --------------JBOSS_HOME: %JBOSS_HOME%
ECHO --------------SERVER_CONFIG: %SERVER_CONFIG%
ECHO --------------DB_TYPE: %DB_TYPE%
ECHO --------------DB_HOST: %DB_HOST%
ECHO --------------DB_NAME: %DB_NAME%
ECHO --------------DB_PORT: %DB_PORT%
ECHO --------------DB_USER: %DB_USER%
ECHO --------------DB_PASS: %DB_PASS%
ECHO --------------JDBC_CONNECTION_URL: %JDBC_CONNECTION_URL%
ECHO --------------JDBC_DRIVER_DIR: %JDBC_DRIVER_DIR%
ECHO --------------JDBC_DRIVER_NAME: %JDBC_DRIVER_NAME%

ECHO --------------Configure Wildfly to resolve parameter values from properties files
@PowerShell -Command "(Get-Content %JBOSS_HOME%/bin/jboss-cli.xml) -replace '<resolve-parameter-values>false</resolve-parameter-values>', '<resolve-parameter-values>true</resolve-parameter-values>' | Out-File -encoding UTF8 %JBOSS_HOME%/bin/jboss-cli.xml"

ECHO --------------Prepare
SET > env.properties
@PowerShell -Command "(Get-Content env.properties) -replace '\\', '\\' | Out-File -encoding ASCII env.properties"
@PowerShell -Command "(Get-Content env.properties) -replace '\^&', '&' | Out-File -encoding ASCII env.properties"

ECHO --------------Configure Wildfly
%JBOSS_HOME%\bin\jboss-cli.bat --file=resources\domibus-configuration-%DB_TYPE%.cli --properties=env.properties

ECHO --------------Clean
DEL env.properties
