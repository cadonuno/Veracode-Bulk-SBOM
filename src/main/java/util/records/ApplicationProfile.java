package util.records;

public final class ApplicationProfile {
    private final String applicationId;
    private final String applicationName;

    public ApplicationProfile(String applicationId, String applicationName) {
        this.applicationId = applicationId;
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getApplicationId() {
        return applicationId;
    }
}
