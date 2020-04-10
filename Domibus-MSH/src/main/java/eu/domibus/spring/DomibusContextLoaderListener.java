package eu.domibus.spring;

import com.google.common.collect.Sets;
import eu.domibus.api.exceptions.DomibusCoreErrorCode;
import eu.domibus.api.plugin.PluginException;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.classloader.PluginClassLoader;
import eu.domibus.core.property.PropertyResolver;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.web.context.ContextLoaderListener;

import javax.crypto.Cipher;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Arrays;
import java.util.Set;

/**
 * Created by Cosmin Baciu on 6/13/2016.
 */
public class DomibusContextLoaderListener extends ContextLoaderListener {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusContextLoaderListener.class);

    // As recommended, make sure that the Sun security provider remains at a higher preference (i.e. index 2 on Weblogic)
    private static final int HIGHEST_RECOMMENDED_POSITION_IN_ORDER_OF_PREFERENCES = 3;

    private static final int LIMITED_STRENGTH_MAX_KEY_LENGTH = 128;

    private static final int UNLIMITED_STRENGTH_MAX_KEY_LENGTH = Integer.MAX_VALUE;

    PluginClassLoader pluginClassLoader = null;

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        registerBouncyCastle();
        checkStrengthJurisdictionPolicyLevel();

        ServletContext servletContext = servletContextEvent.getServletContext();
        String pluginsLocation = servletContext.getInitParameter("pluginsLocation");
        String extensionsLocation = servletContext.getInitParameter("extensionsLocation");
        if (StringUtils.isEmpty(pluginsLocation)) {
            throw new PluginException(DomibusCoreErrorCode.DOM_001, "pluginsLocation context param should not be empty");
        }
        String resolvedPluginsLocation = new PropertyResolver().getResolvedValue(pluginsLocation);
        Set<File> pluginsDirectories = Sets.newHashSet(new File(resolvedPluginsLocation));
        if (StringUtils.isNotEmpty(extensionsLocation)) {
            String resolvedExtensionsLocation = new PropertyResolver().getResolvedValue(extensionsLocation);
            pluginsDirectories.add(new File(resolvedExtensionsLocation));
            LOG.info("Resolved extension location [{}] to [{}]", extensionsLocation, resolvedExtensionsLocation);
        }
        LOG.info("Resolved plugins location [{}] to [{}]", pluginsLocation, resolvedPluginsLocation);

        try {
            pluginClassLoader = new PluginClassLoader(pluginsDirectories, Thread.currentThread().getContextClassLoader());
        } catch (MalformedURLException e) {
            throw new PluginException(DomibusCoreErrorCode.DOM_001, "Malformed URL Exception", e);
        }
        Thread.currentThread().setContextClassLoader(pluginClassLoader);
        super.contextInitialized(servletContextEvent);
    }

    protected void registerBouncyCastle() {
        LOG.info("Registering the Bouncy Castle provider as the third highest preferred security provider");
        try {
            Security.insertProviderAt(new BouncyCastleProvider(), HIGHEST_RECOMMENDED_POSITION_IN_ORDER_OF_PREFERENCES);
            LOG.debug("Security providers in order of preferences [{}]", Arrays.toString(Security.getProviders()));
        } catch (SecurityException e) {
            LOG.error("An error registering Bouncy Castle provider as the second highest preferred security provider", e);
        }
    }

    protected void checkStrengthJurisdictionPolicyLevel() {
        int maxKeyLen = 0;
        try {
            maxKeyLen = Cipher.getMaxAllowedKeyLength("AES");
        } catch (NoSuchAlgorithmException e) { /*ignore*/ }

        LOG.info("Using {} strength jurisdiction policy: maxKeyLen=[{}]",
                maxKeyLen == LIMITED_STRENGTH_MAX_KEY_LENGTH
                        ? "Limited"
                        : maxKeyLen == UNLIMITED_STRENGTH_MAX_KEY_LENGTH
                        ? "Unlimited"
                        : "Unknown",
                maxKeyLen);
    }
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        super.contextDestroyed(servletContextEvent);

        if (pluginClassLoader != null) {
            try {
                pluginClassLoader.close();
            } catch (IOException e) {
                LOG.warn("Error closing PluginClassLoader", e);
            }
        }
    }
}
