package util.executionparameters;

import java.util.Locale;
import java.util.Optional;

public enum PlatformInstanceEnum {
    Eu("EU", "eu"),
    Commercial("Commercial", "com");

    private final String instanceName;
    private final String topLevelDomain;

    PlatformInstanceEnum(String instanceName, String topLevelDomain) {
        this.instanceName = instanceName;
        this.topLevelDomain = topLevelDomain;
    }

    public static Optional<PlatformInstanceEnum> getFromParameterValue(String parameterValue) {
        if (parameterValue == null || parameterValue.trim().equals("")) {
            return Optional.empty();
        }
        if (parameterValue.toLowerCase(Locale.US).equals("eu")) {
            return Optional.of(Eu);
        }
        return Optional.of(Commercial);
    }

    public String getAsString() {
        return this.instanceName;
    }

    public String getTopLevelDomain() {
        return topLevelDomain;
    }
}
