package eu.domibus.api.diagnostics;

/**
 * @author Breaz Ionut
 * @since 5.1.9
 */
public class VersionInfo {
    private String artifactName;
    private String artifactVersion;
    private String builtTime;
    private String versionNumber;

    public VersionInfo() {
    }

    public VersionInfo(String artifactName, String artifactVersion, String builtTime, String versionNumber) {
        this.artifactName = artifactName;
        this.artifactVersion = artifactVersion;
        this.builtTime = builtTime;
        this.versionNumber = versionNumber;
    }

    public String getArtifactName() {
        return artifactName;
    }

    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }

    public String getArtifactVersion() {
        return artifactVersion;
    }

    public void setArtifactVersion(String artifactVersion) {
        this.artifactVersion = artifactVersion;
    }

    public String getBuiltTime() {
        return builtTime;
    }

    public void setBuiltTime(String builtTime) {
        this.builtTime = builtTime;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Artifact Name: ").append(artifactName).append("\n");
        sb.append("Artifact Version: ").append(artifactVersion).append("\n");
        sb.append("Build Time: ").append(builtTime).append("\n");
        sb.append("Version Number: ").append(versionNumber);
        return sb.toString();
    }
}