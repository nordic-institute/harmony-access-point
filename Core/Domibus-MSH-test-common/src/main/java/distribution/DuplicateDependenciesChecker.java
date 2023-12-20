package distribution;

import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;

public class DuplicateDependenciesChecker {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DuplicateDependenciesChecker.class);

    public void checkDuplicateDependencies(String distributionName, List<List<String>> listOfEquivalentDependencies) {
        doCheckDuplicateDependencies(distributionName, listOfEquivalentDependencies);
    }

    private void doCheckDuplicateDependencies(String warPathWebInfLib, List<List<String>> listOfEquivalentDependencies) throws RuntimeException {
        final File webInfLibFile = new File(warPathWebInfLib);
        LOG.info("Checking for duplicate dependencies in [{}]", webInfLibFile.getAbsolutePath());

        //a map with key=dependency name without version, value=list of dependency names with version
        Map<String, List<String>> dependenciesAggregated = new HashMap<>();

        //a list of all dependencies without version
        List<String> dependenciesListWithoutVersion = new ArrayList<>();

        final Collection<File> dependencyFileList = FileUtils.listFiles(webInfLibFile, null, false);

        for (File dependencyFile : dependencyFileList) {
            String dependencyNameWithVersion = dependencyFile.getName();
            final String dependencyNameWithoutVersion = getDependencyNameWithoutVersion(dependencyNameWithVersion);
            List<String> dependenciesWithVersion = dependenciesAggregated.get(dependencyNameWithoutVersion);
            if (dependenciesWithVersion == null) {
                dependenciesWithVersion = new ArrayList<>();
                dependenciesAggregated.put(dependencyNameWithoutVersion, dependenciesWithVersion);
            }
            dependenciesWithVersion.add(dependencyNameWithVersion);
            dependenciesListWithoutVersion.add(dependencyNameWithoutVersion);
        }
        LOG.info("Dependency list aggregated: [{}]", dependenciesAggregated);

        checkForDuplicateDependencies(dependenciesAggregated);
        checkForDuplicateDependenciesWithDifferentArtifactId(dependenciesListWithoutVersion, listOfEquivalentDependencies);
    }

    private void checkForDuplicateDependenciesWithDifferentArtifactId(List<String> dependenciesListWithoutVersion, List<List<String>> listOfEquivalentDependencies) {
        LOG.info("Checking for duplicate dependencies with different artifact ids");

        for (List<String> equivalentDependencies : listOfEquivalentDependencies) {
            checkForDuplicateDependencies(dependenciesListWithoutVersion, equivalentDependencies);
        }

        LOG.info("No duplicate dependencies detected with different artifact id");
    }

    private void checkForDuplicateDependencies(List<String> dependenciesListWithoutVersion, List<String> equivalentDependencies) {
        LOG.info("Checking for duplicate dependencies with different artifact id [{}]", equivalentDependencies);
        int numberOfMatches = 0;
        for (String equivalentDependency : equivalentDependencies) {
            if (dependenciesListWithoutVersion.contains(equivalentDependency)) {
                numberOfMatches++;
            }
        }
        if (numberOfMatches > 1) {
            throw new RuntimeException("Found duplicate dependencies with different artifact ids [" + equivalentDependencies + "]");
        }
        LOG.info("No duplicate dependencies with different artifact id [{}] found", equivalentDependencies);
    }

    /**
     * Check for duplicate dependencies having the same artifact id
     */
    private void checkForDuplicateDependencies(Map<String, List<String>> dependenciesAggregated) throws RuntimeException {
        LOG.info("Checking for duplicate dependencies with the same artifact id");

        //check if there are duplicate dependencies
        for (Map.Entry<String, List<String>> dependencyEntry : dependenciesAggregated.entrySet()) {
            final List<String> listOfDependenciesPerDependency = dependencyEntry.getValue();
            if (listOfDependenciesPerDependency.size() > 1) {
                throw new RuntimeException("Multiple dependencies detected [" + listOfDependenciesPerDependency + "]");
            }
        }
        LOG.info("No duplicate dependencies detected with the same artifact id");
    }

    private String getDependencyNameWithoutVersion(String dependencyName) {
        return StringUtils.substringBeforeLast(dependencyName, "-");
    }

}
