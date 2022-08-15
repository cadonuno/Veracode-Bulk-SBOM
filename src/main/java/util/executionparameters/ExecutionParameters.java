package util.executionparameters;

import util.Logger;

import java.util.Optional;

public class ExecutionParameters {
    private final ApiCredentials apiCredentials;
    private final PlatformInstanceEnum platformInstance;
    private final SbomSourceEnum sbomSource;
    private final String targetDirectory;

    protected ExecutionParameters(ApiCredentials apiCredentials, String targetDirectory,
                                  String sbomSourceAsString, PlatformInstanceEnum platformInstance) {
        if (targetDirectory == null || targetDirectory.isEmpty()) {
            throw new IllegalArgumentException("Target Directory argument is mandatory (--target_directory, -td)");
        }
        this.sbomSource = SbomSourceEnum.getFromParameterValue(sbomSourceAsString);

        if (this.sbomSource == null) {
            throw new IllegalStateException("SBOM Source argument is mandatory (--source, -s)");
        } else if (this.sbomSource == SbomSourceEnum.Invalid) {
            throw new IllegalStateException("'" + sbomSourceAsString + "' is not a valid SBOM source, allowed values are: \n" +
                    SbomSourceEnum.getValidParameterValues());
        }
        this.platformInstance = platformInstance;
        this.apiCredentials = apiCredentials;
        this.targetDirectory = targetDirectory;
    }

    public static Optional<ExecutionParameters> of(String[] commandLineArguments) {
        Logger.log("Parsing Execution Parameters");
        return Optional.of(parseParameters(new ParameterParser(commandLineArguments)));
    }

    private static ExecutionParameters parseParameters(
            ParameterParser parameterParser) {
        ApiCredentials apiCredentials = new ApiCredentials(
                parameterParser.getParameterAsString("--veracode_id", "-vi"),
                parameterParser.getParameterAsString("--veracode_key", "-vk"));
        return new ExecutionParameters(
                apiCredentials,
                parameterParser.getParameterAsString("--target_directory", "-td"),
                parameterParser.getParameterAsString("--source", "-s"),
                apiCredentials.getPlatformInstance());
    }

    public ApiCredentials getApiCredentials() {
        return apiCredentials;
    }

    public String getTargetDirectory() {
        return targetDirectory;
    }

    public SbomSourceEnum getSbomSource() {
        return sbomSource;
    }

    public PlatformInstanceEnum getPlatformInstance() {
        return platformInstance;
    }
}
