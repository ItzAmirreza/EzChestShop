package tr.zeltuv.ezql.objects;

import java.sql.PreparedStatement;

public enum DataType {

    //Integer
    TINYINT(DataCategory.INTEGER),
    SMALLINT(DataCategory.INTEGER),
    MEDIUMINT(DataCategory.INTEGER),
    INT(DataCategory.INTEGER),
    BIGINT(DataCategory.INTEGER),
    BIT(DataCategory.INTEGER),

    //Real
    FLOAT(DataCategory.REAL),
    DOUBLE(DataCategory.REAL),
    DECIMAL(DataCategory.REAL),

    //Text
    VARCHAR(DataCategory.TEXT),
    CHAR(DataCategory.TEXT),
    TINYTEXT(DataCategory.TEXT),
    TEXT(DataCategory.TEXT),
    MEDIUMTEXT(DataCategory.TEXT),
    LONGTEXT(DataCategory.TEXT),

    //Binary
    BINARY(DataCategory.BINARY),
    VARBINARY(DataCategory.BINARY),
    TINYBLOB(DataCategory.BINARY),
    BLOB(DataCategory.BINARY),
    MEDIUMBLOB(DataCategory.BINARY),
    LONGBLOB(DataCategory.BINARY),

    //Temporal
    DATE(DataCategory.TEMPORAL),
    TIME(DataCategory.TEMPORAL),
    YEAR(DataCategory.TEMPORAL),
    DATETIME(DataCategory.TEMPORAL),
    TIMESTAMP(DataCategory.TEMPORAL),

    //Spatial
    POINT(DataCategory.SPATIAL),
    LINESTRING(DataCategory.SPATIAL),
    POLYGON(DataCategory.SPATIAL),
    GEOMETRY(DataCategory.SPATIAL),
    MULTIPOINT(DataCategory.SPATIAL),
    MULTILINESTRING(DataCategory.SPATIAL),
    MULTIPOLYGON(DataCategory.SPATIAL),
    GEOMETRYCOLLECTION(DataCategory.SPATIAL),

    //Other
    UNKNOWN(DataCategory.OTHER),
    ENUM(DataCategory.OTHER),
    SET(DataCategory.OTHER);

    private DataCategory dataCategory;

    DataType(DataCategory dataCategory){
        this.dataCategory = dataCategory;
    }

    public DataCategory getDataCategory() {
        return dataCategory;
    }

    enum DataCategory{
        INTEGER,REAL,TEXT,BINARY,TEMPORAL,SPATIAL,OTHER;
    }
}
