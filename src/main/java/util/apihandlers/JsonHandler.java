package util.apihandlers;

import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import util.ApplicationProfile;

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

    private static Optional<String> tryGetElementAsString(JSONObject jsonObject, String elementToGet) {
        return tryGetElementFromJsonObject(jsonObject, elementToGet)
                .filter(result -> result instanceof String)
                .map(result -> (String) result);
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

    private static JSONObject mapToJsonObject(Object jsonResult) {
        return (JSONObject) jsonResult;
    }

    private static JSONArray mapToJsonArray(Object jsonResult) {
        return (JSONArray) jsonResult;
    }
}
