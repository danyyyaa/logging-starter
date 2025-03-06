package com.danya.loggingstarter.util;

import java.util.List;
import java.util.Map;

public class HeaderMaskingUtil {

    private static final String MASKED_VALUE = "****";

    HeaderMaskingUtil() {
    }

    public static void maskHeaders(Map<String, String> headers, List<String> headersToMask) {
        headers.replaceAll((key, value) -> shouldMaskHeader(key, headersToMask) ? MASKED_VALUE : value);
    }

    private static boolean shouldMaskHeader(String headerName, List<String> headersToMask) {
        return headersToMask.stream().anyMatch(headerName::equalsIgnoreCase);
    }
}