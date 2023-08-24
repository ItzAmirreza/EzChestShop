package me.deadlight.ezchestshop.data.sqlite.Structure;

public class SQLColumn {
    private String type;
    private boolean primarykey = false;
    private boolean canbenull = false;


    public SQLColumn(String type, boolean primarykey, boolean canbenull) {
        this.type = type;
        this.primarykey = primarykey;
        this.canbenull = canbenull;
    }


    //Type
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    //Primary Key
    public boolean isPrimarykey() {
        return this.primarykey;
    }
    public void setPrimarykey(boolean primarykey) {
        this.primarykey = primarykey;
    }
    //Null
    public boolean isCanbenull() {
        return this.canbenull;
    }
    public void setCanbenull(boolean canbenull) {
        this.canbenull = canbenull;
    }
}
