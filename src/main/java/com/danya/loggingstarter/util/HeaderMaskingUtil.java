package com.danya.loggingstarter.util;

import com.danya.loggingstarter.property.LoggingExclusionProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HeaderMaskingUtil {

    @Autowired
    private LoggingExclusionProperties loggingExclusionProperties;

    private static final String MASKED_VALUE = "****";

    public Map<String, String> getMaskedHeaders(Map<String, String> headers) {
        List<String> headersToMask = loggingExclusionProperties.getMaskHeaders();
        return headers.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> shouldMaskHeader(entry.getKey(), headersToMask)
                                ? MASKED_VALUE
                                : entry.getValue()
                ));
    }

    private boolean shouldMaskHeader(String headerName, List<String> headersToMask) {
        return headersToMask.stream().anyMatch(headerName::equalsIgnoreCase);
    }
}
