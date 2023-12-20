package distribution;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.enforcer.rule.api.AbstractEnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.project.MavenProject;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author François Gautier
 * @since 5.0
 * <p>
 * Check for duplicate dependencies with different version
 * <p>
 * The build will fail if the folder /target/.../WEB-INF/lib has a duplicate
 */
@Named("duplicateDependenciesRule")
public class DuplicateDependenciesRule extends AbstractEnforcerRule {

    @Inject
    private MavenProject project;

    private List<String> equivalentDuplicateDependencies;

    public void execute() throws EnforcerRuleException {
        final List<List<String>> configuredEquivalentDependencies = parseEquivalentDependencies(equivalentDuplicateDependencies);
        getLog().info("Configured equivalentDuplicateDependencies: " + configuredEquivalentDependencies);
        getLog().info("Retrieved Target Folder: " + project.getBuild().getDirectory());
        getLog().info("Retrieved ArtifactId: " + project.getArtifactId());
        getLog().info("Retrieved Project: " + project);

        String webInfLib = project.getBasedir().getPath() + "/target/" + project.getArtifactId() + "-" + project.getVersion() + "/WEB-INF/lib";
        getLog().info("Folder to be check for duplicate dependencies: " + webInfLib);

        DuplicateDependenciesChecker duplicateDependenciesChecker = new DuplicateDependenciesChecker();
        duplicateDependenciesChecker.checkDuplicateDependencies(webInfLib, configuredEquivalentDependencies);
    }

    protected List<List<String>> parseEquivalentDependencies(List<String> equivalentDuplicateDependenciesSeparatedByComma) {
        List<List<String>> result = new ArrayList<>();

        for (String equivalentDuplicateDependencyStringSeparatedByComma : equivalentDuplicateDependenciesSeparatedByComma) {
            final List<String> equivalentDuplicateDependencies = Arrays.stream(StringUtils.split(equivalentDuplicateDependencyStringSeparatedByComma, ","))
                    .map(dependencyName -> StringUtils.trim(dependencyName))
                    .collect(Collectors.toList());
            result.add(equivalentDuplicateDependencies);
        }
        return result;
    }

    /**
     * If your rule is cacheable, you must return a unique id when parameters or conditions
     * change that would cause the result to be different. Multiple cached results are stored
     * based on their id.
     * <p>
     * The easiest way to do this is to return a hash computed from the values of your parameters.
     * <p>
     * If your rule is not cacheable, then you don't need to override this method or return null
     */
    @Override
    public String getCacheId() {
        //no hash on boolean...only parameter so no hash is needed.
        return null;
    }

    /**
     * A good practice is provided toString method for Enforcer Rule.
     * <p>
     * Output is used in verbose Maven logs, can help during investigate problems.
     *
     * @return rule description
     */
    @Override
    public String toString() {
        return "duplicateDependenciesRule: [fail the build if a dependency is found twice with different version in the folder WEB-INF/lib";
    }


}
