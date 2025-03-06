package com.danya.loggingstarter.property;

import java.util.ArrayList;
import java.util.List;

public class LoggingExclusionProperties {

    private List<String> excludePaths;
    private List<String> maskFields;
    private List<String> maskHeaders;


    public List<String> getMaskHeaders() {
        return new ArrayList<>(maskHeaders);
    }

    public void setMaskHeaders(List<String> maskHeaders) {
        this.maskHeaders = maskHeaders;
    }

    public List<String> getExcludePaths() {
        return new ArrayList<>(excludePaths);
    }

    public void setExcludePaths(List<String> excludePaths) {
        this.excludePaths = excludePaths;
    }

    public List<String> getMaskFields() {
        return new ArrayList<>(maskFields);
    }

    public void setMaskFields(List<String> maskFields) {
        this.maskFields = maskFields;
    }
}