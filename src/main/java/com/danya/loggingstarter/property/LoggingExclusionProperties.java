package com.danya.loggingstarter.property;

import java.util.ArrayList;
import java.util.List;

public class LoggingExclusionProperties {

    private List<String> maskFields = new ArrayList<>();
    private List<String> maskHeaders = new ArrayList<>();

    public List<String> getMaskHeaders() {
        return maskHeaders;
    }

    public void setMaskHeaders(List<String> maskHeaders) {
        this.maskHeaders = maskHeaders;
    }

    public List<String> getMaskFields() {
        return maskFields;
    }

    public void setMaskFields(List<String> maskFields) {
        this.maskFields = maskFields;
    }
}