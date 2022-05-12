import util.apihandlers.ApiCaller;
import util.executionparameters.ExecutionParameters;


public class Main {

    public static void main(final String[] commandLineArguments) {
        ExecutionParameters.of(commandLineArguments)
                .ifPresent(ApiCaller::handleApiCalls);
    }
}