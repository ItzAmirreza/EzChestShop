package me.deadlight.ezchestshop.Data.SQLite.Structure;

import java.util.LinkedHashMap;

public class SQLTable {
    private LinkedHashMap<String, SQLColumn> table;

    public SQLTable(LinkedHashMap<String, SQLColumn> table) {
        this.table = table;
    }

    public LinkedHashMap<String, SQLColumn> getTable() {
        return table;
    }

    public void setTable(LinkedHashMap<String, SQLColumn> table) {
        this.table = table;
    }
}
