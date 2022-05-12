package util.executionparameters;

import util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExecutionParameters {
    private final ApiCredentials apiCredentials;
    private final String targetDirectory;

    protected ExecutionParameters(ApiCredentials apiCredentials, String targetDirectory) {
        if (targetDirectory == null || targetDirectory.isEmpty()) {
            throw new IllegalArgumentException("Target Directory argument is mandatory (--target_directory, -td)");
        }
        this.apiCredentials = apiCredentials;
        this.targetDirectory = targetDirectory;
    }

    public static Optional<ExecutionParameters> of(String[] commandLineArguments) {
        Logger.log("Parsing Execution Parameters");
        return Optional.of(parseParameters(new ParameterParser(commandLineArguments)));
    }

    private static ExecutionParameters parseParameters(
            ParameterParser parameterParser) {
        return new ExecutionParameters(
                new ApiCredentials(
                        parameterParser.getParameterAsString("--veracode_id", "-vi"),
                        parameterParser.getParameterAsString("--veracode_key", "-vk")),
                parameterParser.getParameterAsString("--target_directory", "-td"));
    }

    public ApiCredentials getApiCredentials() {
        return apiCredentials;
    }

    public String getTargetDirectory() {
        return targetDirectory;
    }
}
