# Domibus eArchive client webhook OpenAPI 

To implement the full integration with the Domibus, an archiving client MUST implement the REST methods to receive the 
batch status messages. The REST endpoint URLs that Domibus will use to notify the archiving client shall be configured 
in the Domibus properties.

This module aims to generate an OpenAPI document (see the: src/main/webapp/domibus-archive-webhook-openapi.json) for the 
REST webhook methods archiving clients must implement.  

The maven project is a separate module to allow an option to put the definition to the Domibus swagger in the future as 
Webhook defined in OpenAPI 3.1.0

The result of this module is also a war bundle with the 'swagger-ui' pages. The war bundle is a pure Javascript/HTML 
application, and it is deployable to any web server.

## Project structure 

Project contains java interface with javax servlet and swagger annotations. The plugin *io.swagger.core.v3:swagger-maven-plugin* 
generates JSON OpenApi document at the phase: compile.

The swagger-ui pages are shipped with the project. To refresh or update swagger-ui pages with the latest version of the swagger-ui
update the maven property: 
```XML 
<swagger-ui.version>3.52.3</swagger-ui.version>
``` 
to the latest version and build the project. 


## How to build the project:

To generated OpenApi document (src/main/webapp/domibus-archive-webhook-openapi.json) 

    mvn clean compile 

To build war bundle
 
    mvn clean package
    
To refresh swagger-ui pages and build war bundle     
    
    mvn clean package 
    
    
   