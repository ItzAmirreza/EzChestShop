package tr.zeltuv.ezql.objects;

public class EzqlColumn{

    private DataType dataType;
    private String name;
    private int length;
    private boolean primary;

    public static EzqlColumn get(DataType dataType, String name, int length, boolean primary){
        return new EzqlColumn(dataType,name,length,primary);
    }
    public static EzqlColumn get(DataType dataType, String name, int length){
        return new EzqlColumn(dataType,name,length,false);
    }
    public static EzqlColumn get(DataType dataType, String name, boolean primary){
        return new EzqlColumn(dataType,name,0,primary);
    }
    public static EzqlColumn get(DataType dataType, String name){
        return new EzqlColumn(dataType,name,0,false);
    }

    private EzqlColumn(DataType dataType, String name, int length, boolean primary) {
        this.dataType = dataType;
        this.name = name;
        this.length = length;
        this.primary = primary;
    }

    public DataType getDataType() {
        return dataType;
    }

    public String getName() {
        return name;
    }

    public int getLength() {
        return length;
    }

    public boolean isPrimary() {
        return primary;
    }

}
