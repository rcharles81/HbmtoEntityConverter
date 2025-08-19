package com.convert.hbm.entity;

import java.util.ArrayList;
import java.util.List;

public class FilterMetadata {
    private String name;
    private String condition;
    private List<FilterParameter> parameters = new ArrayList<>();

    public static class FilterParameter {
        private String name;
        private String type;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public List<FilterParameter> getParameters() {
        return parameters;
    }

    public void addParameter(FilterParameter parameter) {
        this.parameters.add(parameter);
    }
}
