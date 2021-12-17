
[![License badge](https://img.shields.io/badge/license-EUPL-blue.svg)](LICENSE.md)
[![Documentation badge](https://img.shields.io/badge/docs-latest-brightgreen.svg)](https://github.com/nordic-institute/harmony-common/tree/main/doc)
[![Support badge]( https://img.shields.io/badge/support-sof-yellowgreen.svg)](https://edelivery.digital/contact)

# Harmony eDelivery Access - Access Point

![Harmony eDelivery Access logo](harmony-logo.png)

## About the Repository

This repository contains the source code of the AS4 Access Point component of Harmony eDelivery Access. 

Harmony eDelivery Access by [NIIS](https://niis.org) is a free and actively maintained open-source component for joining one or more eDelivery policy domains.

Harmony Access Point is based on upon the [Domibus](https://ec.europa.eu/cefdigital/code/projects/EDELIVERY/repos/domibus/) open source project by the [European Commission](https://ec.europa.eu/). 

## Documentation

The official Harmony documentation is available in a separate repository that can be found [here](https://github.com/nordic-institute/harmony-common/).

In addition, the following documents that are available on the [Domibus release page](https://ec.europa.eu/cefdigital/wiki/display/CEFDIGITAL/Domibus) are applicable for the Harmony Access Point too:

 * Administration Guide 
 * Testing guide
 * Interface Control Documents of the default plugins
 * Plugin cookbook 
 * Software Architecture Document.

## Build

In order to build Harmony Access Point including all release artifacts use the following profiles:

    mvn -f harmony-pom.xml clean install -Ptomcat -Pdefault-plugins -Pdatabase -PUI 

**Note:** Running the tests takes a long time (~20 min or more).

Integration tests can be skipped using the `skipITs` property:

    mvn -f harmony-pom.xml clean install -Ptomcat -Pdefault-plugins -Pdatabase -PUI -DskipITs=true

All tests can be skipped using the `maven.test.skip` property

    mvn -f harmony-pom.xml clean install -Ptomcat -Pdefault-plugins -Pdatabase -PUI -Dmaven.test.skip=true

Full build instruction are available in the `harmony-common` [repository]([here](https://github.com/nordic-institute/harmony-common/)).

## Install and Run

Instructions to install and run Harmony Access Point are available in the `harmony-common` [repository]([here](https://github.com/nordic-institute/harmony-common/)).