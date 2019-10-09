package eu.domibus.core.crypto.spi.dss;

import mockit.*;
import mockit.integration.junit4.JMockit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static org.junit.Assert.*;

@RunWith(JMockit.class)
public class DssConfigurationTest {

    @Injectable String dssTlsTrustStorePassword="pwd";


    @Tested
    private DssConfiguration dssConfiguration;
    @Test
    public void mergeCustomTlsTrustStoreWithCacert( @Mocked KeyStore customTlsTrustStore,@Mocked Logger logger) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        new Expectations(FileInputStream.class){{
            FileInputStream fileInputStream = new FileInputStream(anyString);
        }};
        new Expectations(){{
            KeyStore.getInstance(anyString);
            result = customTlsTrustStore;
          //HEll  keyStore.load((FileInputStream)any,dssTlsTrustStorePassword.toCharArray());
            /*LoggerFactory.getLogger(DssConfiguration.class);
            result=logger;*/
            //final FileInputStream fileInputStream = new FileInputStream(anyString);
//            fileInputStream.read(); result = 123;
  //          fileInputStream.close();



        }};
        dssConfiguration.mergeCustomTlsTrustStoreWithCacert();
    }
}