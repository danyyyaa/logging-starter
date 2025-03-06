package com.danya.loggingstarter.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

public class JsonMaskingUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    JsonMaskingUtil() {
    }

    public static String maskFields(String json, List<String> maskPaths) throws Exception {
        String jsonString = json.substring(json.indexOf('=') + 1);

        JsonNode rootNode = objectMapper.readTree(jsonString);
        for (String path : maskPaths) {
            maskField(rootNode, path.split("\\."));
        }
        return objectMapper.writeValueAsString(rootNode);
    }

    private static void maskField(JsonNode node, String[] pathParts) {
        if (pathParts.length == 0) {
            return;
        }
        String currentPart = pathParts[0];
        if (node.has(currentPart)) {
            if (pathParts.length == 1) {
                ((ObjectNode) node).put(currentPart, "****");
            } else {
                maskField(node.get(currentPart), subArray(pathParts, 1));
            }
        }
    }

    private static String[] subArray(String[] array, int startIndex) {
        String[] subArray = new String[array.length - startIndex];
        System.arraycopy(array, startIndex, subArray, 0, array.length - startIndex);
        return subArray;
    }
}