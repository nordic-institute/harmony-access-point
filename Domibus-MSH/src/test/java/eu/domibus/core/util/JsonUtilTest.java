package eu.domibus.core.util;

import com.google.gson.reflect.TypeToken;
import eu.domibus.core.message.UserMessageLog;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Cosmin Baciu on 22-Aug-16.
 */
public class JsonUtilTest {

    JsonUtilImpl jsonUtil = new JsonUtilImpl();

    @Test
    public void testJsonToList() throws Exception {
        String json = "['1', '2']";
        List<String> strings = jsonUtil.jsonToList(json);
        Assert.assertTrue(strings.get(0).equals("1"));
    }

    @Test
    public void testJsonToListOfT() throws Exception {
        String json = "['1', '2']";
        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();

        List<String> strings = jsonUtil.jsonToList(json, type);
        Assert.assertTrue(strings.get(0).equals("1"));
    }
}
