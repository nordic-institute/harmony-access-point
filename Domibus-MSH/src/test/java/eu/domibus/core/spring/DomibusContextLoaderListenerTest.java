package eu.domibus.core.spring;

import ch.qos.logback.classic.LoggerContext;
import eu.domibus.core.plugin.classloader.PluginClassLoader;
import eu.domibus.logging.DomibusLogger;
import mockit.*;
import mockit.integration.junit4.JMockit;
import net.sf.ehcache.constructs.web.ShutdownListener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContextEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

/**
 * @author François Gautier
 * @since 4.2
 * <p>
 * Methods {@link DomibusContextLoaderListener#shutdownEhCache}
 * {@link DomibusContextLoaderListener#shutdownPluginClassLoader}
 * {@link DomibusContextLoaderListener#shutdownLogger}
 * are NOT tested separately because it's hard to partially mocked local method
 * AND super method {@link ContextLoaderListener#contextDestroyed}
 */
@RunWith(JMockit.class)
public class DomibusContextLoaderListenerTest {

    DomibusContextLoaderListener domibusContextLoaderListener;

    MockedPluginClassLoader pluginClassLoader;

    @Mocked
    WebApplicationContext context;

    @Before
    public void setUp() throws MalformedURLException {
        pluginClassLoader = new MockedPluginClassLoader(new HashSet<>(), null);
        domibusContextLoaderListener = new DomibusContextLoaderListener(context, pluginClassLoader);
        // Since we mock the LoggerFactory, we have to mock the DomibusLogger
    }

    @Test
    public void contextDestroyed_ok(@Mocked ServletContextEvent servletContextEvent,
                                    @Mocked ContextLoaderListener contextLoaderListener,
                                    @Mocked ShutdownListener shutdownListener,
                                    @Mocked LoggerFactory loggerFactory,
                                    @Mocked LoggerContext loggerContext,
                                    @Mocked DomibusLogger domibusLogger) {
        Deencapsulation.setField(domibusContextLoaderListener, "LOG", domibusLogger);

        new Expectations() {{
            new ShutdownListener();
            result = shutdownListener;

            LoggerFactory.getILoggerFactory();
            result = loggerContext;
        }};

        domibusContextLoaderListener.contextDestroyed(servletContextEvent);

        Assert.assertTrue(pluginClassLoader.isCloseBeingCalled());
        new FullVerificationsInOrder() {{
            //super.contextDestroyed
            contextLoaderListener.contextDestroyed(servletContextEvent);
            times = 1;

            domibusLogger.info("Shutting down net.sf.ehcache");
            times = 1;

            shutdownListener.contextDestroyed(servletContextEvent);
            times = 1;

            domibusLogger.info("Closing PluginClassLoader");
            times = 1;

            domibusLogger.info("Stop ch.qos.logback.classic.LoggerContext");
            times = 1;

            loggerContext.stop();
            times = 1;
        }};
    }

    @Test
    public void contextDestroyed_pluginClassLoaderNull(@Mocked LoggerFactory loggerFactory,
                                                       @Mocked DomibusLogger domibusLogger) {
        Deencapsulation.setField(domibusContextLoaderListener, "LOG", domibusLogger);

        Deencapsulation.setField(domibusContextLoaderListener, "pluginClassLoader", null);

        domibusContextLoaderListener.shutdownPluginClassLoader();

        Assert.assertFalse(pluginClassLoader.isCloseBeingCalled());
        new Verifications() {{
            domibusLogger.info("Closing PluginClassLoader");
            times = 0;
        }};
    }

    @Test
    public void contextDestroyed_exception(@Mocked ServletContextEvent servletContextEvent,
                                           @Mocked ContextLoaderListener contextLoaderListener,
                                           @Mocked ShutdownListener shutdownListener,
                                           @Mocked LoggerFactory loggerFactory,
                                           @Mocked LoggerContext loggerContext,
                                           @Mocked DomibusLogger domibusLogger) {
        Deencapsulation.setField(domibusContextLoaderListener, "LOG", domibusLogger);

        pluginClassLoader.throwExceptionOnClose();

        new Expectations() {{
            new ShutdownListener();
            result = shutdownListener;

            LoggerFactory.getILoggerFactory();
            result = loggerContext;

        }};

        domibusContextLoaderListener.contextDestroyed(servletContextEvent);

        Assert.assertTrue(pluginClassLoader.isCloseBeingCalled());
        new FullVerificationsInOrder() {{
            //super.contextDestroyed
            contextLoaderListener.contextDestroyed(servletContextEvent);
            times = 1;

            domibusLogger.info("Shutting down net.sf.ehcache");
            times = 1;

            shutdownListener.contextDestroyed(servletContextEvent);
            times = 1;

            domibusLogger.info("Closing PluginClassLoader");
            times = 1;

            domibusLogger.warn(anyString, (Throwable) any);
            times = 1;

            domibusLogger.info("Stop ch.qos.logback.classic.LoggerContext");
            times = 1;

            loggerContext.stop();
            times = 1;
        }};
    }

    /**
     * As JMockit cannot mock the class {@link ClassLoader}
     * see https://github.com/jmockit/jmockit1/blob/master/main/test/mockit/JREMockingTest.java
     * for unmockable classes from the JRE
     */
    static class MockedPluginClassLoader extends PluginClassLoader {
        boolean closeBeingCalled = false;
        boolean throwExceptionOnClose = false;

        public MockedPluginClassLoader(Set<File> files, ClassLoader parent) throws MalformedURLException {
            super(files, parent);
        }

        /**
         * And that, my little children, is how you are doing mocking back in the days
         */
        public void close() throws IOException {
            closeBeingCalled = true;
            if (throwExceptionOnClose) {
                throw new IOException("Test");
            }
        }

        public boolean isCloseBeingCalled() {
            return closeBeingCalled;
        }

        public void throwExceptionOnClose() {
            throwExceptionOnClose = true;
        }
    }
}