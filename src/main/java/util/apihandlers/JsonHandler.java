package util.apihandlers;

import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import util.records.ApplicationProfile;
import util.records.Project;
import util.records.Workspace;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonHandler {
    public static Optional<List<ApplicationProfile>> getApplicationProfileFromPayload(JSONObject apiCallResult) {
        return Optional.of(apiCallResult)
                .flatMap(JsonHandler::getEmbeddedNode)
                .flatMap(JsonHandler::getApplicationsNode)
                .map(JsonHandler::getAllApplications);
    }

    private static Optional<JSONObject> getEmbeddedNode(JSONObject baseNode) {
        return tryGetElementFromJsonObject(baseNode, "_embedded")
                .filter(result -> result instanceof JSONObject)
                .map(JsonHandler::mapToJsonObject);
    }

    private static Optional<JSONArray> getApplicationsNode(JSONObject embeddedNode) {
        return tryGetElementFromJsonObject(embeddedNode, "applications")
                .filter(result -> result instanceof JSONArray)
                .map(JsonHandler::mapToJsonArray);
    }

    private static List<ApplicationProfile> getAllApplications(JSONArray allApplications) {
        List<ApplicationProfile> foundIds = new ArrayList<>();
        for (int currentIndex = 0; currentIndex < allApplications.length(); currentIndex++) {
            tryGetElementAtJsonArrayIndex(allApplications, currentIndex)
                    .map(JsonHandler::getApplication)
                    .ifPresent(foundIds::add);
        }
        return foundIds;
    }

    public static ApplicationProfile getApplication(JSONObject applicationNode) {
        return new ApplicationProfile(
                tryGetElementAsString(applicationNode, "guid").orElse(""),
                getProfileName(applicationNode));
    }

    private static String getProfileName(JSONObject applicationNode) {
        return tryGetElementFromJsonObject(applicationNode, "profile")
                .filter(result -> result instanceof JSONObject)
                .map(JsonHandler::mapToJsonObject)
                .flatMap(result -> tryGetElementAsString(result, "name"))
                .orElse("");
    }

    public static int getLastPageFromJson(JSONObject baseNode) {
        return tryGetElementFromJsonObject(baseNode, "page")
                .filter(result -> result instanceof JSONObject)
                .map(JsonHandler::mapToJsonObject)
                .flatMap(result -> tryGetElementAsInt(result, "total_pages"))
                .orElse(0);
    }

    public static Optional<List<Workspace>> getWorkspacesFromPayload(JSONObject apiCallResult) {
        return Optional.of(apiCallResult)
                .flatMap(JsonHandler::getEmbeddedNode)
                .flatMap(JsonHandler::getWorkspacesNode)
                .map(JsonHandler::getAllWorkspaces);
    }

    public static Optional<List<Project>> getProjectsFromPayload(JSONObject apiCallResult, Workspace workspace) {
        return Optional.of(apiCallResult)
                .flatMap(JsonHandler::getEmbeddedNode)
                .flatMap(JsonHandler::getProjectsNode)
                .map(projectsNode -> getAllProjects(projectsNode, workspace));
    }

    private static Optional<JSONArray> getProjectsNode(JSONObject embeddedNode) {
        return tryGetElementFromJsonObject(embeddedNode, "projects")
                .filter(result -> result instanceof JSONArray)
                .map(JsonHandler::mapToJsonArray);
    }

    private static List<Project> getAllProjects(JSONArray allProjects, Workspace workspace) {
        List<Project> foundProjects = new ArrayList<>();
        for (int currentIndex = 0; currentIndex < allProjects.length(); currentIndex++) {
            tryGetElementAtJsonArrayIndex(allProjects, currentIndex)
                    .map(projectNode -> getProject(workspace, projectNode))
                    .filter(Project::isNotContainerScan)
                    .ifPresent(foundProjects::add);
        }
        return foundProjects;
    }

    private static Optional<JSONArray> getWorkspacesNode(JSONObject embeddedNode) {
        return tryGetElementFromJsonObject(embeddedNode, "workspaces")
                .filter(result -> result instanceof JSONArray)
                .map(JsonHandler::mapToJsonArray);
    }

    private static List<Workspace> getAllWorkspaces(JSONArray allWorkspaces) {
        List<Workspace> foundWorkspaces = new ArrayList<>();
        for (int currentIndex = 0; currentIndex < allWorkspaces.length(); currentIndex++) {
            tryGetElementAtJsonArrayIndex(allWorkspaces, currentIndex)
                    .map(JsonHandler::getWorkspace)
                    .ifPresent(foundWorkspaces::add);
        }
        return foundWorkspaces;
    }

    public static Workspace getWorkspace(JSONObject workspaceNode) {
        return new Workspace(
                tryGetElementAsString(workspaceNode, "id").orElse(""),
                tryGetElementAsString(workspaceNode, "name").orElse(""),
                new ArrayList<>());
    }

    public static Project getProject(Workspace workspace, JSONObject projectNode) {
        return new Project(
                tryGetElementAsString(projectNode, "id").orElse(""),
                tryGetElementAsString(projectNode, "name").orElse(""),
                tryGetElementFromJsonObject(projectNode, "languages")
                        .filter(result -> result instanceof JSONArray)
                        .map(JsonHandler::mapToJsonArray)
                        .map(JsonHandler::commaDelimitArray)
                        .orElse(""),
                workspace);
    }

    private static Optional<String> tryGetElementAsString(JSONObject jsonObject, String elementToGet) {
        return tryGetElementFromJsonObject(jsonObject, elementToGet)
                .filter(result -> result instanceof String)
                .map(result -> (String) result);
    }

    private static Optional<Integer> tryGetElementAsInt(JSONObject jsonObject, String elementToGet) {
        return tryGetElementFromJsonObject(jsonObject, elementToGet)
                .filter(result -> result instanceof Integer)
                .map(result -> (Integer) result);
    }

    private static Optional<JSONObject> tryGetElementAtJsonArrayIndex(JSONArray allApplications, int currentIndex) {
        try {
            Object element = allApplications.get(currentIndex);
            if (element instanceof JSONObject) {
                return Optional.of((JSONObject) element);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private static Optional<Object> tryGetElementFromJsonObject(JSONObject jsonObject, String elementToGet) {
        try {
            return Optional.of(jsonObject.get(elementToGet));
        } catch (JSONException e) {
            return Optional.empty();
        }
    }

    private static String commaDelimitArray(JSONArray jsonArray) {
        StringBuilder arrayAsString = new StringBuilder();
        for (int currentIndex = 0; currentIndex < jsonArray.length(); currentIndex++) {
            if (currentIndex > 0) {
                arrayAsString.append(", ");
            }
            try {
                arrayAsString.append(jsonArray.get(currentIndex));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return arrayAsString.toString();
    }

    private static JSONObject mapToJsonObject(Object jsonResult) {
        return (JSONObject) jsonResult;
    }

    private static JSONArray mapToJsonArray(Object jsonResult) {
        return (JSONArray) jsonResult;
    }
}
