package distribution;

import org.apache.maven.enforcer.rule.api.AbstractEnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.project.MavenProject;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author François Gautier
 * @since 5.0
 *
 * Check for duplicate dependencies with different version
 *
 * The build will fail if the folder /target/.../WEB-INF/lib has a duplicate
 */
@Named("duplicateDependenciesRule")
public class DuplicateDependenciesRule extends AbstractEnforcerRule {

    @Inject
    private MavenProject project;

    public void execute() throws EnforcerRuleException {

        getLog().info("Retrieved Target Folder: " + project.getBuild().getDirectory());
        getLog().info("Retrieved ArtifactId: " + project.getArtifactId());
        getLog().info("Retrieved Project: " + project);


        List<String> duplicates = new ArrayList<>();
        Map<String, String> fileNames = new HashMap<>();

        String pathname = project.getBasedir().getPath() +
                "/target/" + project.getArtifactId() + "-" + project.getVersion() + "/WEB-INF/lib";
        getLog().info("Folder to be check for duplicate dependencies: " + pathname);
        File[] files = new java.io.File(pathname).listFiles();

        if (files == null) {
            throw new EnforcerRuleException("Failing because dependency folder not found.");
        }
        for (File file : files) {
            if (file.isFile()) {
                String key = org.apache.commons.lang3.StringUtils.left(
                        file.getName(),
                        org.apache.commons.lang3.StringUtils.lastIndexOf(file.getName(), "-"));
                String value = fileNames.get(key);
                if (value == null) {
                    fileNames.put(key, file.getName());
                    getLog().debug("found file " + key + "->" + file.getName());
                } else {
                    getLog().warnOrError("Duplicate found for " + key + "->" + file.getName() + "/" + value);
                    duplicates.add(value + "->" + file.getName());
                }
            }
        }

        if (!duplicates.isEmpty()) {
            throw new EnforcerRuleException("Failing because a duplicate was found: "+ String.join(",", duplicates));
        }
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
