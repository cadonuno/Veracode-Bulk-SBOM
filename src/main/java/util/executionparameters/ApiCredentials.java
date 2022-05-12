package util.executionparameters;

public class ApiCredentials {
    private final String apiId;
    private final String apiKey;

    public ApiCredentials(String apiId, String apiKey) {
        if (apiId == null || apiId.trim().isEmpty()) {
            throw new IllegalArgumentException("Veracode ID argument is mandatory (--veracode_id, -vi)");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Veracode Key argument is mandatory (--veracode_key, -vk)");
        }
        this.apiId = apiId;
        this.apiKey = apiKey;
    }

    public String getApiId() {
        return this.apiId;
    }

    public String getApiKey() {
        return this.apiKey;
    }
}
