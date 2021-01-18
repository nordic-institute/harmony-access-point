package eu.domibus.core.util;

import com.google.gson.reflect.TypeToken;
import eu.domibus.api.message.MessageSubtype;
import eu.domibus.api.model.UserMessageLogDto;
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
        Assert.assertEquals("1", strings.get(0));
    }

    @Test
    public void serializaDeserializeIT() {

        String id1 = "id1";
        String id2 = "id2";
        List<String> ids = Arrays.asList(id1, id2);
        String json = jsonUtil.listToJson(ids);
        List<String> list = jsonUtil.jsonToList(json);

        Assert.assertEquals(2, list.size());
        Assert.assertEquals(id1, list.get(0));
        Assert.assertEquals(id2, list.get(1));
    }

    @Test
    public void serializaDeserializeUserMessageLogDtoIT() {
        String id1 = "id1";
        String id2 = "id2";
        String backend = "ws";
        UserMessageLogDto uml1 = new UserMessageLogDto(id1, MessageSubtype.TEST, backend, null);
        UserMessageLogDto uml2 = new UserMessageLogDto(id2, MessageSubtype.TEST, backend, null);
        List<UserMessageLogDto> umls = Arrays.asList(uml1, uml2);
        String json = jsonUtil.listToJson(umls);

        Type type = new TypeToken<ArrayList<UserMessageLogDto>>() {
        }.getType();
        List<UserMessageLogDto> list = jsonUtil.jsonToList(json, type);

        Assert.assertEquals(2, list.size());
        Assert.assertEquals(id1, list.get(0).getMessageId());
        Assert.assertEquals(id2, list.get(1).getMessageId());
        Assert.assertEquals(backend, list.get(1).getBackend());
    }
}
