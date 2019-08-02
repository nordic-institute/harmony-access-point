package eu.domibus.api.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;


/**
 * Created by Cosmin Baciu on 13-Jul-16.
 */
public interface HttpUtil {
    ByteArrayInputStream downloadURL(String url) throws IOException, NoSuchAlgorithmException, KeyManagementException;

    ByteArrayInputStream downloadURLDirect(String url) throws IOException, NoSuchAlgorithmException, KeyManagementException;

    ByteArrayInputStream downloadURLViaProxy(String url) throws IOException, NoSuchAlgorithmException, KeyManagementException;

}
