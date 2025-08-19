package com.convert.hbm.entity;

import java.util.ArrayList;
import java.util.List;

public class SqlQueryMetadata {
    private String name;
    private String query;
    private List<ResultMapping> resultMappings = new ArrayList<>();

    public static class ResultMapping {
        private String alias;
        private String className;
        private boolean isScalar;
        private String columnName;
        private String columnType;

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public boolean isScalar() {
            return isScalar;
        }

        public void setScalar(boolean scalar) {
            isScalar = scalar;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnType() {
            return columnType;
        }

        public void setColumnType(String columnType) {
            this.columnType = columnType;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<ResultMapping> getResultMappings() {
        return resultMappings;
    }

    public void addResultMapping(ResultMapping mapping) {
        this.resultMappings.add(mapping);
    }
}
