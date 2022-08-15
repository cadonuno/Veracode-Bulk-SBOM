package util.executionparameters;

public class ApiCredentials {
    private final String apiId;
    private final String apiKey;
    private final PlatformInstanceEnum platformInstance;

    public ApiCredentials(String apiId, String apiKey) {
        if (apiId == null || apiId.trim().isEmpty()) {
            throw new IllegalArgumentException("Veracode ID argument is mandatory (--veracode_id, -vi)");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Veracode Key argument is mandatory (--veracode_key, -vk)");
        }
        if (apiId.startsWith("vera01") && apiKey.startsWith("vera01")) {
            this.platformInstance = PlatformInstanceEnum.Eu;
            this.apiId = apiId.substring(apiId.indexOf('-') + 1);
            this.apiKey = apiKey.substring(apiId.indexOf('-') + 1);
        } else {
            this.platformInstance = PlatformInstanceEnum.Commercial;
            this.apiId = apiId;
            this.apiKey = apiKey;
        }
    }

    public String getApiId() {
        return this.apiId;
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public PlatformInstanceEnum getPlatformInstance() {
        return this.platformInstance;
    }
}
