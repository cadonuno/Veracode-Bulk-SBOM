package util.records;

import java.util.Objects;

public final class Project {
    private final String guid;
    private final String name;
    private final boolean isContainerScan;
    private final Workspace workspace;

    public Project(String guid, String name, String languages, Workspace workspace) {
        this.guid = guid;
        this.name = name;
        this.isContainerScan = "OS".equals(languages);
        this.workspace = workspace;
    }

    public String getFullName() {
        return workspace.getName() + "->" + name;
    }

    public String getGuid() {
        return guid;
    }

    public String getName() {
        return name;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        Project that = (Project) obj;
        return Objects.equals(this.guid, that.guid) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.workspace, that.workspace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(guid, name, workspace);
    }

    @Override
    public String toString() {
        return "Project[" +
                "guid=" + guid + ", " +
                "name=" + name + ", " +
                "workspace=" + workspace + ']';
    }

    public boolean isNotContainerScan() {
        return !isContainerScan;
    }
}
