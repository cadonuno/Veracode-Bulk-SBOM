package util.executionparameters;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public enum SbomSourceEnum {
    UploadAndScan,
    AgentBasedScan,
    Invalid;


    private static final List<String> VALUES_FOR_UPLOAD_AND_SCAN = Arrays.asList("uploadandscan", "upload and scan",
            "u&s", "uands", "upload");
    private static final List<String> VALUES_FOR_AGENT_BASED = Arrays.asList("agentbased", "agent based",
            "agent-based", "ab", "agent");

    public static SbomSourceEnum getFromParameterValue(String sbomSourceParameterValue) {
        if (sbomSourceParameterValue == null || sbomSourceParameterValue.trim().equals("")) {
            return null;
        }
        if (VALUES_FOR_UPLOAD_AND_SCAN.contains(sbomSourceParameterValue.trim().toLowerCase(Locale.US))) {
            return UploadAndScan;
        }
        if (VALUES_FOR_AGENT_BASED.contains(sbomSourceParameterValue.trim().toLowerCase(Locale.US))) {
            return AgentBasedScan;
        }
        return Invalid;
    }

    public static String getValidParameterValues() {
        return "Upload and Scan:" +  VALUES_FOR_UPLOAD_AND_SCAN.stream().reduce("",
                SbomSourceEnum::parameterValuesAccumulator) + "\n" +
                "Agent Based:" +  VALUES_FOR_AGENT_BASED.stream().reduce("",
                SbomSourceEnum::parameterValuesAccumulator);
    }

    private static String parameterValuesAccumulator(String currentValue, String newValue) {
        return currentValue + "\n  -" + newValue;
    }
}
