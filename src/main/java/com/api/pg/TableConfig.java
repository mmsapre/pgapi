package com.api.pg;

import java.util.List;

public class TableConfig {
    private List<String> columns;
    private List<String> jsonb;

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    public List<String> getJsonb() {
        return jsonb;
    }

    public void setJsonb(List<String> jsonb) {
        this.jsonb = jsonb;
    }
}
