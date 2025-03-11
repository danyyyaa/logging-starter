package com.danya.loggingstarter.util;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeaderMaskingUtil {

    private static final String MASKED_VALUE = "****";

    private HeaderMaskingUtil() {
    }

    public static void maskHeaders(Map<String, String> headers, List<String> headersToMask) {
        headers.replaceAll((key, value) -> shouldMaskHeader(key, headersToMask) ? MASKED_VALUE : value);
    }

    private static boolean shouldMaskHeader(String headerName, List<String> headersToMask) {
        return headersToMask.stream().anyMatch(mask -> isMatchHeader(headerName, mask));
    }

    private static boolean isMatchHeader(String headerName, String maskPattern) {
        String cleanedPattern = maskPattern.replaceAll("<[^>]+>", "").trim();

        Pattern pattern = Pattern.compile("(?i)(?:.*\\.headers\\.)?(.*)");
        Matcher matcher = pattern.matcher(cleanedPattern);
        String extractedName = cleanedPattern;
        if (matcher.matches()) {
            extractedName = matcher.group(1).trim();
        }

        return headerName.equalsIgnoreCase(extractedName);
    }
}