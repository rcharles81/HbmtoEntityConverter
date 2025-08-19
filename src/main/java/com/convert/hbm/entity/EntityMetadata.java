package com.convert.hbm.entity;

import java.util.ArrayList;
import java.util.List;

public class EntityMetadata {
    private String className;
    private String tableName;
    private IdMetadata id;
    private List<PropertyMetadata> properties = new ArrayList<>();
    private List<QueryMetadata> queries = new ArrayList<>();
    private List<SqlQueryMetadata> sqlQueries = new ArrayList<>();
    private List<FilterMetadata> filters = new ArrayList<>();

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public IdMetadata getId() {
        return id;
    }

    public void setId(IdMetadata id) {
        this.id = id;
    }

    public List<PropertyMetadata> getProperties() {
        return properties;
    }

    public void addProperty(PropertyMetadata property) {
        this.properties.add(property);
    }

    public void addQuery(QueryMetadata query) {
        this.queries.add(query);
    }

    public List<QueryMetadata> getQueries() {
        return queries;
    }

    public void addSqlQuery(SqlQueryMetadata sqlQuery) {
        this.sqlQueries.add(sqlQuery);
    }

    public List<SqlQueryMetadata> getSqlQueries() {
        return sqlQueries;
    }

    public void addFilter(FilterMetadata filter) {
        this.filters.add(filter);
    }

    public List<FilterMetadata> getFilters() {
        return filters;
    }
}
