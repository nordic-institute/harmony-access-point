package eu.domibus.plugin.fs;

import mockit.Tested;
import mockit.integration.junit4.JMockit;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@RunWith(JMockit.class)
public class FSMimeTypeHelperImplTest {

    @Tested
    protected FSMimeTypeHelperImpl fsMimeTypeHelper;
    
    public FSMimeTypeHelperImplTest() {
    }

    @Test
    public void testGetMimeType_Text() {
        String result = fsMimeTypeHelper.getMimeType("file.txt");
        
        Assert.assertEquals("text/plain", result);
    }
    
    @Test
    public void testGetMimeType_Xml() {
        String result = fsMimeTypeHelper.getMimeType("file.xml");
        
        Assert.assertEquals("application/xml", result);
    }
    
    @Test
    public void testGetMimeType_Pdf() {
        String result = fsMimeTypeHelper.getMimeType("file.pdf");
        
        Assert.assertEquals("application/pdf", result);
    }

    @Test
    public void testGetExtension_Text() throws Exception {
        String result = fsMimeTypeHelper.getExtension("text/plain");
        
        Assert.assertEquals(".txt", result);
    }

    @Test
    public void testGetExtension_Xml() throws Exception {
        String result = fsMimeTypeHelper.getExtension("application/xml");
        
        Assert.assertEquals(".xml", result);
    }
    
    @Test
    public void testGetExtension_TextXml() throws Exception {
        String result = fsMimeTypeHelper.getExtension("text/xml");
        
        Assert.assertEquals(".xml", result);
    }

    @Test
    public void testGetExtension_Pdf() throws Exception {
        String result = fsMimeTypeHelper.getExtension("application/pdf");
        
        Assert.assertEquals(".pdf", result);
    }
    
}
