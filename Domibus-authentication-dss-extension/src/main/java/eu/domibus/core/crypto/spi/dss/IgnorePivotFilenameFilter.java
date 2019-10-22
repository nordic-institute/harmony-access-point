package eu.domibus.core.crypto.spi.dss;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

public class IgnorePivotFilenameFilter implements FilenameFilter {

    private static final Logger LOG = LoggerFactory.getLogger(IgnorePivotFilenameFilter.class);

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
