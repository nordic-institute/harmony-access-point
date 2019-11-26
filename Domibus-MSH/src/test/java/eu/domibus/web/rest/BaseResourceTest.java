package eu.domibus.web.rest;

import eu.domibus.api.jms.JmsMessage;
import eu.domibus.core.csv.CsvCustomColumns;
import eu.domibus.core.csv.CsvExcludedItems;
import eu.domibus.core.csv.CsvServiceImpl;
import mockit.Expectations;
import mockit.FullVerifications;
import mockit.Injectable;
import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Catalin Enache
 * @since 4.1.1
 */
@RunWith(JMockit.class)
public class BaseResourceTest {

    @Tested
    BaseResource baseResource;

    @Injectable
    CsvServiceImpl csvServiceImpl;

    @Test
    public void testExportToCSV_JMS() {
        final List<JmsMessage> jmsMessageList = getJmsMessageList();

        final String moduleName = "test";
        new Expectations(baseResource) {{
            baseResource.getCsvService();
            result = csvServiceImpl;

            csvServiceImpl.getCsvFilename(moduleName);
            result = moduleName;
        }};

        //tested method
        final ResponseEntity<String> responseEntity = baseResource.exportToCSV(jmsMessageList, JmsMessage.class,
                CsvCustomColumns.JMS_RESOURCE.getCustomColumns(), CsvExcludedItems.JMS_RESOURCE.getExcludedItems(), moduleName);

        Assert.assertNotNull(responseEntity);
        Assert.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assert.assertEquals(MediaType.parseMediaType(CsvServiceImpl.APPLICATION_EXCEL_STR), responseEntity.getHeaders().getContentType());

        new FullVerifications() {{
            csvServiceImpl.exportToCSV(jmsMessageList, JmsMessage.class,
                    CsvCustomColumns.JMS_RESOURCE.getCustomColumns(), CsvExcludedItems.JMS_RESOURCE.getExcludedItems());
        }};
    }


    private List<JmsMessage> getJmsMessageList() {
        List<JmsMessage> result = new ArrayList<>();

        JmsMessage jmsMessage = new JmsMessage();
        jmsMessage.setId("ID:localhost-10762-1561728161168-6:48:4:1:1");
        Map<String, Object> customProperties = new HashMap<>();
        customProperties.put("DOMAIN", "default");
        customProperties.put("dlqDeliveryFailureCause", "java.lang.Throwable: Delivery[1] exceeds redelivery policy limit:RedeliveryPolicy {destination = null, collisionAvoidanceFactor = 0.15, maximumRedeliveries = 0, maximumRedeliveryDelay = -1, initialRedeliveryDelay = 1000, useCollisionAvoidance = false, useExponentialBackOff = false, backOffMultiplier = 5.0, redeliveryDelay = 1000, preDispatchCheck = true}, cause:null");
        customProperties.put("originalExpiration", new Long(0));
        customProperties.put("originalQueue", "domibus.fsplugin.send.queue");
        customProperties.put("FILE_NAME", "/home/edelivery/domibus/fs_plugin_data/MAIN/OUT/test.txt");

        Map<String, Object> jmsProperties = new HashMap<>();
        jmsProperties.put("JMSMessageID", " -> ID:localhost-10762-1561728161168-6:48:4:1:1");
        jmsProperties.put("JMSDestination", "queue://domibus.DLQ");
        jmsProperties.put("JMSDeliveryMode", "PERSISTENT");

        jmsMessage.setCustomProperties(customProperties);
        jmsMessage.setProperties(jmsProperties);

        result.add(jmsMessage);

        return result;
    }


}