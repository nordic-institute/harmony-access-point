
[![License badge](https://img.shields.io/badge/license-EUPL-blue.svg)](https://ec.europa.eu/digital-building-blocks/wikis/download/attachments/52601883/eupl_v1.2_en%20.pdf?version=1&modificationDate=1507206778126&api=v2)
[![Documentation badge](https://img.shields.io/badge/docs-latest-brightgreen.svg)](https://ec.europa.eu/digital-building-blocks/wiki/display/CEFDIGITAL/Domibus)
[![Support badge]( https://img.shields.io/badge/support-sof-yellowgreen.svg)](https://ec.europa.eu/digital-building-blocks/wikis/display/CEFDIGITAL/Support)

# Domibus
### Sample implementation, open source project of the eDelivery AS4 Access Point.

<!-- TOC -->
  * [Introduction](#introduction)
  * [GEi overall description](#gei-overall-description)
  * [Build](#build)
    * [Tomcat 9.x](#tomcat-9x)
    * [Wildfly 26.x](#wildfly-26x)
    * [Weblogic 12.2.1.x](#weblogic-1221x)
    * [Weblogic 12.2.1.x with EU Login Support](#weblogic-1221x-with-eu-login-support)
  * [Install and run](#install-and-run)
  * [Testing](#testing)
    * [Ent-to-end tests](#ent-to-end-tests)
    * [Unit Tests](#unit-tests)
  * [Default plugins](#default-plugins)
  * [License](#license)
  * [Support](#support)
<!-- TOC -->
		  
## Introduction

This is the code repository for Domibus, the sample implementation, open source project of the European Commission AS4 Access Point.

Any feedback on this documentation is highly welcome, including bugs, typos
or things you think should be included but aren't. You can use [JIRA](https://ec.europa.eu/digital-building-blocks/tracker/projects/EDELIVERY/issues) to provide feedback.

Following documents are available on the [Domibus release page](https://ec.europa.eu/digital-building-blocks/wikis/display/CEFDIGITAL/Domibus):
*   Quick Start Guide
*   Administration Guide 
*   Testing guide
*   Interface Control Documents of the default plugins
*   Plugin cookbook 
*   Software Architecture Document


[Top](#domibus)

## GEi overall description

The CEF eDelivery Access Point (AP) implements a standardised message exchange protocol that ensures interoperable, secure and reliable data exchange.
Domibus is the Open Source project of the AS4 Access Point maintained by the European Commission. 

If this is your first contact with the CEF eDelivery Access Point, it is highly recommended to check the [CEF eDelivery Access Point Component offering description](https://ec.europa.eu/digital-building-blocks/wikis/download/attachments/46992278/%28CEFeDelivery%29.%28AccessPoint%29.%28COD%29.%28v1.04b%29.pdf?version=1&modificationDate=1493385571398&api=v2) available on the [Access Point Software](https://ec.europa.eu/digital-building-blocks/wikis/display/CEFDIGITAL/Access+Point+software) page.

[Top](#domibus)

## Build

### Tomcat 9.x

In order to build Domibus for Tomcat including all release artifacts use the following profiles:

```shell
mvn clean install -Ptomcat,default-plugins,database,sample-configuration,UI,distribution
```

[Top](#domibus)

### Wildfly 26.x

In order to build Domibus for Tomcat including all release artifacts use the following profiles:

```shell
mvn clean install -Pwildfly,default-plugins,database,sample-configuration,UI,distribution
```

[Top](#domibus)

### Weblogic 12.2.1.x

In order to build Domibus for Weblogic including all release artifacts, you must first import the required Weblogic
12.2.1.x libraries as dependencies into your local Maven repository. You must first change your working directory to 
the ```modules``` directory inside your ```<WL_HOME>``` (i.e. the root directory of your WebLogic installation or the 
```wlserver``` directory inside the directory where you installed Weblogic).

```shell
cd <WL_HOME>

cd modules
```

To install the Weblogic dependencies into your local Maven repository please run the following from within the
```modules``` directory inside your ```<WL_HOME>```:

```shell
mvn install:install-file -Dfile=com.bea.core.weblogic.workmanager.jar -DgroupId=com.oracle.weblogic -DartifactId=com.bea.core.weblogic.workmanager -Dversion=12.2.1.4.0 -Dpackaging=jar

mvn install:install-file -Dfile=com.oracle.weblogic.security.encryption.jar -DgroupId=com.oracle.weblogic -DartifactId=com.oracle.weblogic.security.encryption -Dversion=12.2.1.4.0 -Dpackaging=jar

mvn install:install-file -Dfile=com.oracle.weblogic.security.jar -DgroupId=com.oracle.weblogic -DartifactId=com.oracle.weblogic.security -Dversion=12.2.1.4.0 -Dpackaging=jar

mvn install:install-file -Dfile=com.oracle.weblogic.jms.jar -DgroupId=com.oracle.weblogic -DartifactId=com.oracle.weblogic.jms -Dversion=12.2.1.4.0 -Dpackaging=jar
```

Finally, build Domibus for Weblogic including all release artifacts using the following profiles:

```shell
mvn clean install -Pweblogic,default-plugins,database,sample-configuration,UI,distribution
```

[Top](#domibus)

### Weblogic 12.2.1.x with EU Login Support
In order to build Domibus for Weblogic with EU-Login support including all release artifacts, follow the steps you would
normally take to build [Domibus for Weblogic](#weblogic-1221x) above, but use the following profiles instead when 
running the last ```maven clean install``` build command:

```shell
mvn clean install -Pweblogic-ecas,default-plugins,database,sample-configuration,UI,distribution
```

[Top](#domibus)

## Install and run

How to install and run Domibus can be read in the Quick Start Guide and more advanced documentation is available in the Administration Guide, both available on the [Domibus Release Page](https://ec.europa.eu/digital-building-blocks/wikis/display/CEFDIGITAL/Domibus).

[Top](#domibus)

## Testing

### Ent-to-end tests

The end-to-end tests are manually performed by the testing team using SoapUI PRO. 
For further information please check the Testing Guide available on the [Domibus Release Page](https://ec.europa.eu/digital-building-blocks/wikis/display/CEFDIGITAL/Domibus). 

A restricted set of tests that do not require any interactivity may run with the soapui-pro-maven-plugin from com.smartbear.soapui. The maven command to run the tests is:

```shell
mvn com.smartbear.soapui:soapui-pro-maven-plugin:5.1.2:test
```

### Unit Tests

To run unit test via Maven, issue this command : 

```shell
mvn test
```
In Domibus there are two types of tests implemented using JUnit: unit tests (java classes ending in *Test.java) and integration tests (java classes ending in *IT.java)
To skip the unit tests from the build process:

```shell
mvn clean install -DskipTests=true -DskipITs=true
```

[Top](#domibus)

## Default plugins

The purpose of Domibus is to facilitate B2B communication. To achieve this goal it provides a very flexible plugin model which allows the integration with nearly all back office applications. 
Domibus offers three default plugins, available with the Domibus distribution:

*   Web Service plugin
*   JMS plugin
*   File System plugin

The Interface Control Document (ICD) of the default JMS plugin outlines the JMS Data Format Exchange to be used as part of the default JMS backend plugin.
The Interface Control Document (ICD) of the default WS plugin describes the WSDL and the observable behaviour of the interface provided in the default WS plugin
Both documents are available on the [Domibus Release Page](https://ec.europa.eu/digital-building-blocks/wikis/display/CEFDIGITAL/Domibus)

[Top](#domibus)

## License

Domibus is licensed under European Union Public Licence (EUPL) version 1.2.

[Top](#domibus)

## Support

Have questions? Consult our [Q&A section](https://ec.europa.eu/digital-building-blocks/wikis/display/CEFDIGITAL/Domibus+FAQs). 
Ask your thorough programming questions using [stackoverflow](http://stackoverflow.com/questions/ask).
Please use the tag `context.domibus`.

Still have questions? Contact [eDelivery support](https://ec.europa.eu/digital-building-blocks/tracker/servicedesk/customer/portal/2/create/4).

[Top](#domibus)