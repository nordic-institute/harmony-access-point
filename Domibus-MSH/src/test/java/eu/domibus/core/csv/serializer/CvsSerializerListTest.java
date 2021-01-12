package eu.domibus.core.csv.serializer;

import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static eu.domibus.core.csv.serializer.CvsSerializer.LIST_DELIMITER;

@RunWith(JMockit.class)
public class CvsSerializerListTest {

    @Tested
    CvsSerializerList cvsSerializerList;

    @Test
    public void canHandle() {
        Assert.assertFalse(cvsSerializerList.canHandle(new HashMap<>()));
        Assert.assertTrue(cvsSerializerList.canHandle(new ArrayList<>()));
    }

    @Test
    public void serialize() {
        Assert.assertEquals(cvsSerializerList.serialize(new ArrayList<>()), "");
        Assert.assertEquals(cvsSerializerList.serialize(Arrays.asList("1", "2")), "1" + LIST_DELIMITER +  "2");
    }
}