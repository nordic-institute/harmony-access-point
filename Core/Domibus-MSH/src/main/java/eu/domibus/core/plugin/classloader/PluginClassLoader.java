package eu.domibus.core.plugin.classloader;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Cosmin Baciu on 6/15/2016.
 */
public class PluginClassLoader extends URLClassLoader {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PluginClassLoader.class);

    protected Collection<File> files;

    public PluginClassLoader(Collection<File> files, ClassLoader parent) throws MalformedURLException {
        super(discoverPlugins(files), parent);
        this.files = files;
    }

    /**
     * Group the plugins and extension directories to extract the jar files url.
     * Directories listed first have higher priority: if a JAR with the same filename
     * exists in an earlier directory, later duplicates are skipped.
     * @param directories ordered collection of extension/plugins directories.
     * @return the urls of the jar files.
     * @throws MalformedURLException
     */
    protected static URL[] discoverPlugins(Collection<File> directories) throws MalformedURLException {
        Set<String> seenFilenames = new HashSet<>();
        List<URI> jarUris = new ArrayList<>();

        for (File directory : directories) {
            LOG.debug("Extracting plugin and extension jar files from directory:[{}]", directory);
            File[] jars = directory.listFiles((dir, name) -> name.endsWith(".jar"));
            if (jars == null) continue;
            for (File jar : jars) {
                if (seenFilenames.add(jar.getName())) {
                    jarUris.add(jar.toURI());
                    LOG.info("Adding the following plugin/extension to the classpath:[{}]", jar.toURI().toURL());
                } else {
                    LOG.info("Skipping duplicate plugin/extension (already loaded from higher-priority directory):[{}]", jar);
                }
            }
        }

        URL[] urls = new URL[jarUris.size()];
        for (int i = 0; i < jarUris.size(); i++) {
            urls[i] = jarUris.get(i).toURL();
        }
        return urls;
    }

    public Collection<File> getFiles() {
        return files;
    }
}
