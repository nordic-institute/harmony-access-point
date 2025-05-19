package eu.domibus.core.certificate.crl;

import junit.framework.TestCase;
import org.junit.Test;

/**
 * @author G. Maier
 * @since 5.2
 */
public class CRLUrlTypeTest extends TestCase {

    @Test
    public void testCanHandleURL() {
        assertTrue(CRLUrlType.FILE.canHandleURL("file:///path/to/file"));
        assertTrue(CRLUrlType.FILE.canHandleURL("file:/path/to/file"));
        assertTrue(CRLUrlType.FILE.canHandleURL("file://path/to/file"));
        assertFalse(CRLUrlType.FILE.canHandleURL("ftp://path/to/file"));
        assertFalse(CRLUrlType.FILE.canHandleURL("file:\\path/to/file"));
    }
}