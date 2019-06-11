package eu.domibus.core.crypto.spi.dss;

import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

public class IgnorePivotFilenameFilter implements FilenameFilter {

    @Value("${domibus.exclude.pivot.file.regex}")
    private String ignorePivotRegex;

    private Pattern fileNamePattern;

    @PostConstruct
    public void init() {
        fileNamePattern = Pattern.compile(ignorePivotRegex);
    }

    @Override
    public boolean accept(File dir, String name) {
        return fileNamePattern.matcher(name).matches();
    }
}
