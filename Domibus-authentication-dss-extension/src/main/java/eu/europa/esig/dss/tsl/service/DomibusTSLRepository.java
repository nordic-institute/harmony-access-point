package eu.europa.esig.dss.tsl.service;

import eu.domibus.core.crypto.spi.dss.IgnorePivotFilenameFilter;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class DomibusTSLRepository extends TSLRepository {

    private final IgnorePivotFilenameFilter ignorePivotFilenameFilter;

    public DomibusTSLRepository(IgnorePivotFilenameFilter ignorePivotFilenameFilter) {
        this.ignorePivotFilenameFilter = ignorePivotFilenameFilter;
    }

    @Override
    List<File> getStoredFiles() {
        File cacheDir = new File(getCacheDirectoryPath());
        return Arrays.asList(cacheDir.listFiles(ignorePivotFilenameFilter));
    }
}
