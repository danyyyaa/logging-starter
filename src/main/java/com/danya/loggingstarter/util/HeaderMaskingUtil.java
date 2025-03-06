package com.danya.loggingstarter.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class HeaderMaskingUtil {

    private static final String MASKED_VALUE = "****";
    private static final List<String> HEADERS_TO_MASK = Arrays.asList("Authorization", "Cookie");

    HeaderMaskingUtil() {
    }

    public static void maskHeaders(Map<String, String> headers) {
        headers.replaceAll((key, value) -> shouldMaskHeader(key) ? MASKED_VALUE : value);
    }

    private static boolean shouldMaskHeader(String headerName) {
        return HEADERS_TO_MASK.stream().anyMatch(headerName::equalsIgnoreCase);
    }
}