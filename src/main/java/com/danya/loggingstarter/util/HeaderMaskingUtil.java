package com.danya.loggingstarter.util;

import com.danya.loggingstarter.property.LoggingExclusionProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static com.danya.loggingstarter.util.Constants.MASKED_VALUE;

public class HeaderMaskingUtil {

    @Autowired
    private LoggingExclusionProperties loggingExclusionProperties;

    public void maskHeaders(Map<String, String> headers) {
        List<String> headersToMask = loggingExclusionProperties.getMaskHeaders();
        headers.replaceAll((key, value) -> shouldMaskHeader(key, headersToMask) ? MASKED_VALUE : value);
    }

    private boolean shouldMaskHeader(String headerName, List<String> headersToMask) {
        return headersToMask.stream().anyMatch(headerName::equalsIgnoreCase);
    }
}
