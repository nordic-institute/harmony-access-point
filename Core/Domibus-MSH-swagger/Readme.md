# Domibus openAPI json document generation

The current swagger plugin *io.swagger.core.v3:swagger-maven-plugin* does not have an option to 
generate openAPI JSON document from the Spring MVC Rest API annotation. To generate OpenAPi document from 
domibus EXT Rest endpoint, the tool from "org.springdoc" is used. 

The SpringDoc needs a "running" endpoint to generate the JSON. 
As a lightweight alternative to Springboot, the maven project uses MockMVC to provide a "running" endpoint. 

# Autowire spring beans 

In order to startup module domibus-ext-services-delegate for generating the  openAPI JSON document all dependand  
spring beans must be defined in class.  

*OpenApiConfig.java*. 
Example:

    @Bean
    public PartyExtService beanPartyExtService() {
        return Mockito.mock(PartyExtService.class);
    }  

