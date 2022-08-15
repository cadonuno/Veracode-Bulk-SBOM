import util.apihandlers.ApiCaller;
import util.executionparameters.ExecutionParameters;


public class Main {

    public static void main(final String[] commandLineArguments) {
        try {
            ExecutionParameters.of(commandLineArguments)
                    .ifPresent(ApiCaller::handleApiCalls);
        } catch (IllegalStateException | IllegalArgumentException e) {
            System.out.println("Initialization error:");
            System.out.println(e.getMessage());
        }
    }
}