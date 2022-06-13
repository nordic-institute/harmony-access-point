package eu.domibus.core.crypto.spi.dss;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

public class IgnorePivotFilenameFilter implements FilenameFilter {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(IgnorePivotFilenameFilter.class);

    @Value("${domibus.exclude.pivot.file.regex}")
    private String ignorePivotRegex;

    private Pattern fileNamePattern;

    @PostConstruct
    public void init() {
        fileNamePattern = Pattern.compile(ignorePivotRegex);
    }

    @Override
    public boolean accept(File dir, String name) {
        boolean matches = fileNamePattern.matcher(name).matches();
        LOG.trace("Pivot accept file name:[{}] within:[{}]->[{}]", name, dir, matches);
        return matches;
    }
}
