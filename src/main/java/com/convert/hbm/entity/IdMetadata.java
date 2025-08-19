package com.convert.hbm.entity;

import java.util.HashMap;
import java.util.Map;

public class IdMetadata {
    private String name;
    private String type;
    private String columnName;
    private String generatorClass;
    private Map<String, String> generatorParams = new HashMap<>();

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

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getGeneratorClass() {
        return generatorClass;
    }

    public void setGeneratorClass(String generatorClass) {
        this.generatorClass = generatorClass;
    }

    public Map<String, String> getGeneratorParams() {
        return generatorParams;
    }

    public void setGeneratorParams(Map<String, String> generatorParams) {
        this.generatorParams = generatorParams;
    }
}
