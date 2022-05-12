package util.executionparameters;

import util.Logger;

import java.util.*;
import java.util.stream.Collectors;

public final class ParameterParser {
    private final Map<String, String> parsedParameters;

    public ParameterParser(String[] commandLineArguments) {
        parsedParameters = new HashMap<>();
        String currentParameter = "";
        for (String argument : commandLineArguments) {
            if (argument.charAt(0) == '-') {
                currentParameter = argument.toLowerCase(Locale.ROOT);
            } else {
                Logger.log("Reading Parameter: " + currentParameter + " with value " + argument);
                parsedParameters.put(currentParameter, argument);
            }
        }
    }

    private List<String> parseParameterAsList(String parameter) {
        return Arrays.stream(Optional.ofNullable(parameter)
                .map(nonNullParameter -> nonNullParameter.split(","))
                .orElse(new String[0]))
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public String getParameterAsString(String fullName, String simpleName) {
        return parsedParameters.getOrDefault(fullName, parsedParameters.get(simpleName));
    }
}
