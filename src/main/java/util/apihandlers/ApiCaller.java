package util.apihandlers;

import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.tinkerpop.shaded.jackson.databind.ObjectMapper;
import org.apache.tinkerpop.shaded.jackson.databind.SerializationFeature;
import util.ApplicationProfile;
import util.HmacRequestSigner;
import util.Logger;
import util.executionparameters.ApiCredentials;
import util.executionparameters.ExecutionParameters;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ApiCaller {
    private static final String URL_BASE = "api.veracode.com";
    private static final String APPLICATIONS_API_URL = "/appsec/v1/applications/";
    private static final String GET_REQUEST = "GET";
    public static final String SRCCLR_SBOM_URL_BASE = "/srcclr/sbom/v1/targets/";
    public static final String SRCCLR_SBOM_URL_END = "/cyclonedx?type=application";

    public static void handleApiCalls(ExecutionParameters executionParameters) {
        Logger.log("Obtaining list of application profiles");
        List<ApplicationProfile> allApplications = getAllApplicationProfiles(executionParameters.getApiCredentials());
        Logger.log("Obtaining SBOM for " + allApplications.size() + " applications");
        for (ApplicationProfile applicationProfile : allApplications) {
            saveSBOM(executionParameters, applicationProfile);
        }
        Logger.log("Finished obtaining SBOM for " + allApplications.size() + " applications");
    }

    private static void saveSBOM(ExecutionParameters executionParameters, ApplicationProfile applicationProfile) {
        runApi(SRCCLR_SBOM_URL_BASE +
                        applicationProfile.getApplicationId() + SRCCLR_SBOM_URL_END,
                GET_REQUEST, null, executionParameters.getApiCredentials())
                .ifPresent(jsonPayload -> saveFile(executionParameters.getTargetDirectory(),
                        jsonPayload, applicationProfile));
    }

    private static void saveFile(String targetDirectory, JSONObject jsonPayload,
                                 ApplicationProfile applicationProfile) {
        File fileToSave = new File(targetDirectory,
                applicationProfile.getApplicationName() + ".json");
        if (fileToSave.exists()) {
            fileToSave.delete();
        }
        try (PrintWriter fileOutput = new PrintWriter(fileToSave)) {
            fileOutput.println(beautifyJson(jsonPayload));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String beautifyJson(JSONObject jsonPayload) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        try {
            return objectMapper.writeValueAsString(objectMapper.readTree(jsonPayload.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static List<ApplicationProfile> getAllApplicationProfiles(ApiCredentials apiCredentials) {
        return runApi(APPLICATIONS_API_URL, GET_REQUEST, null, apiCredentials)
                .flatMap(JsonHandler::getApplicationProfileFromPayload)
                .orElse(Collections.emptyList());
    }

    private static Optional<JSONObject> runApi(String apiUrl, String requestType,
                                               String jsonParameters, ApiCredentials apiCredentials) {
        try {
            final URL applicationsApiUrl = new URL("https://" + URL_BASE + apiUrl);
            final String authorizationHeader =
                    HmacRequestSigner.getVeracodeAuthorizationHeader(apiCredentials, applicationsApiUrl, requestType);

            final HttpsURLConnection connection = (HttpsURLConnection) applicationsApiUrl.openConnection();
            connection.setRequestMethod(requestType);
            connection.setRequestProperty("Authorization", authorizationHeader);

            if (jsonParameters != null) {
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                try (OutputStream outputStream = connection.getOutputStream()) {
                    byte[] input = jsonParameters.getBytes(StandardCharsets.UTF_8);
                    outputStream.write(input, 0, input.length);
                }
            }

            try (InputStream responseInputStream = connection.getInputStream()) {
                return Optional.of(readResponse(responseInputStream));
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException | IllegalStateException
                | IOException | JSONException e) {
            Logger.log("Unable to run API at: " + apiUrl + "\nWith parameters: " + jsonParameters);
        }
        return Optional.empty();
    }

    /*
     * A simple method to read an input stream (containing JSON) to System.out.
     */
    private static JSONObject readResponse(InputStream responseInputStream) throws IOException, JSONException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] responseBytes = new byte[16384];
        int x;
        while ((x = responseInputStream.read(responseBytes, 0, responseBytes.length)) != -1) {
            outputStream.write(responseBytes, 0, x);
        }
        outputStream.flush();
        return new JSONObject(outputStream.toString());
    }
}
