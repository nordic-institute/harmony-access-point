package eu.domibus.core.payload.temp;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Cosmin Baciu
 * @since 4.1.1
 */
public class RegexIOFileFilter implements IOFileFilter {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(RegexIOFileFilter.class);

    protected Pattern pattern;

    public RegexIOFileFilter(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean accept(File file) {
        return accept(file.getName());
    }

    @Override
    public boolean accept(File dir, String name) {
        return accept(name);
    }

    protected boolean accept(String fileName) {
        Matcher matcher = pattern.matcher(fileName);
        final boolean matches = matcher.matches();
        LOG.trace("File [{}] matched by filter? [{}]", fileName, matches);
        return matches;
    }
}
