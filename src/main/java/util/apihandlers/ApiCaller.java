package util.apihandlers;

import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.tinkerpop.shaded.jackson.databind.ObjectMapper;
import org.apache.tinkerpop.shaded.jackson.databind.SerializationFeature;
import util.executionparameters.PlatformInstanceEnum;
import util.records.ApplicationProfile;
import util.HmacRequestSigner;
import util.Logger;
import util.executionparameters.ApiCredentials;
import util.executionparameters.ExecutionParameters;
import util.executionparameters.SbomSourceEnum;
import util.records.Project;
import util.records.Workspace;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ApiCaller {
    private static final String APIS_BASE_URL = "api.veracode.";
    private static final String APPLICATIONS_API_URL = "/appsec/v1/applications/";
    private static final String WORKSPACES_API_URL = "/srcclr/v3/workspaces/";
    private static final String PROJECTS_API_URL_END = "/projects/";
    private static final String GET_REQUEST = "GET";
    public static final String SBOM_API_URL_BASE = "/srcclr/sbom/v1/targets/";
    public static final String UPLOAD_AND_SCAN_SBOM_URL_END = "/cyclonedx?type=application";
    public static final String AGENT_BASED_SBOM_URL_END = "/cyclonedx?type=agent";
    private static String instanceUrl;

    public static void handleApiCalls(ExecutionParameters executionParameters) {
        instanceUrl = APIS_BASE_URL + executionParameters.getPlatformInstance().getTopLevelDomain();
        Logger.log("Running on " + executionParameters.getPlatformInstance().getAsString() + " instance");
        if (executionParameters.getSbomSource() == SbomSourceEnum.AgentBasedScan) {
            getSbomFromAgentBasedScans(executionParameters);
        } else {
            getSbomFromUploadAndScan(executionParameters);
        }
    }

    private static boolean isEuInstance(ExecutionParameters executionParameters) {
        return executionParameters.getPlatformInstance() == PlatformInstanceEnum.Eu;
    }

    private static void getSbomFromAgentBasedScans(ExecutionParameters executionParameters) {
        Logger.log("Obtaining list of workspaces");
        List<Workspace> allWorkspaces = getAllWorkspaces(executionParameters.getApiCredentials());
        addProjectsToWorkspaces(executionParameters.getApiCredentials(), allWorkspaces);
        allWorkspaces = allWorkspaces.stream()
                .filter(workspace -> !workspace.getProjects().isEmpty())
                .collect(Collectors.toList());
        Logger.log("Found " + allWorkspaces.size() + " workspaces with projects to read");
        for (Workspace workspace : allWorkspaces) {
            Logger.log("  Obtaining SBOM for Workspace: " + workspace.getName());
            workspace.getProjects().forEach(project ->
                    saveAgentBasedSBOM(executionParameters, project));
        }
        Logger.log("Finished obtaining SBOM for " + allWorkspaces.size() + " workspaces");
    }

    private static List<Workspace> getAllWorkspaces(ApiCredentials apiCredentials) {
        return runApi(WORKSPACES_API_URL, GET_REQUEST, null, apiCredentials)
                .flatMap(JsonHandler::getWorkspacesFromPayload)
                .orElse(Collections.emptyList());
    }

    private static void addProjectsToWorkspaces(ApiCredentials apiCredentials, List<Workspace> allWorkspaces) {
        allWorkspaces.forEach(workspace ->
                workspace.getProjects().addAll(
                        getAllProjectsFromWorkspace(apiCredentials, workspace)));
    }

    private static List<Project> getAllProjectsFromWorkspace(ApiCredentials apiCredentials, Workspace workspace) {
        return runApi(WORKSPACES_API_URL + workspace.getGuid() + PROJECTS_API_URL_END, GET_REQUEST, null, apiCredentials)
                .flatMap((JSONObject apiCallResult) -> JsonHandler.getProjectsFromPayload(apiCallResult, workspace))
                .orElse(Collections.emptyList());
    }

    private static void getSbomFromUploadAndScan(ExecutionParameters executionParameters) {
        Logger.log("Obtaining list of application profiles");
        List<ApplicationProfile> allApplications = getAllApplicationProfiles(executionParameters.getApiCredentials());
        Logger.log("Found " + allApplications.size() + " applications");
        for (ApplicationProfile applicationProfile : allApplications) {
            Logger.log("  Obtaining SBOM for application: " + applicationProfile.getApplicationName());
            saveUploadAndScanSBOM(executionParameters, applicationProfile);
        }
        Logger.log("Finished obtaining SBOM for " + allApplications.size() + " applications");
    }

    private static void saveUploadAndScanSBOM(ExecutionParameters executionParameters, ApplicationProfile applicationProfile) {
        runApi(SBOM_API_URL_BASE +
                        applicationProfile.getApplicationId() + UPLOAD_AND_SCAN_SBOM_URL_END,
                GET_REQUEST, null, executionParameters.getApiCredentials())
                .ifPresent(jsonPayload -> saveApplicationProfileFile(executionParameters.getTargetDirectory(),
                        jsonPayload, applicationProfile));
    }

    private static void saveAgentBasedSBOM(ExecutionParameters executionParameters, Project project) {
        Logger.log("    Obtaining SBOM for Project: " + project.getName());
        runApi(SBOM_API_URL_BASE +
                        project.getGuid() + AGENT_BASED_SBOM_URL_END,
                GET_REQUEST, null, executionParameters.getApiCredentials())
                .ifPresent(jsonPayload ->
                        saveProjectFile(new File(executionParameters.getTargetDirectory(),
                                        escapeForFileName(project.getWorkspace().getName())),
                                jsonPayload, project));
    }

    private static void saveProjectFile(File directory, JSONObject jsonPayload, Project project) {
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File fileToSave = new File(directory, escapeForFileName(project.getName()) + ".json");
        saveJsonToFile(jsonPayload, fileToSave);
    }

    private static void saveApplicationProfileFile(String targetDirectory, JSONObject jsonPayload,
                                                   ApplicationProfile applicationProfile) {
        File fileToSave = new File(targetDirectory,
                escapeForFileName(applicationProfile.getApplicationName()) + ".json");
        saveJsonToFile(jsonPayload, fileToSave);
    }

    private static void saveJsonToFile(JSONObject jsonPayload, File fileToSave) {
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
        HttpsURLConnection connection = null;
        try {
            final URL applicationsApiUrl = new URL("https://" + instanceUrl + apiUrl);
            final String authorizationHeader =
                    HmacRequestSigner.getVeracodeAuthorizationHeader(apiCredentials, applicationsApiUrl, requestType);

            connection = (HttpsURLConnection) applicationsApiUrl.openConnection();
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
            Logger.log(" ** Unable to run API at: " + apiUrl + "\nWith parameters: " + jsonParameters);
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

    private static String escapeForFileName(String name) {
        return name.replaceAll("\\W+", "-");
    }
}
