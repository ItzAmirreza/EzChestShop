package tr.zeltuv.ezql.objects;

import com.google.common.collect.Sets;
import tr.zeltuv.ezql.exception.EzqlColumnOrderException;

import java.util.*;
import java.util.stream.Collectors;

public class EzqlTable {

    private String name;
    private EzqlDatabase database;

    private List<EzqlColumn> columns = new ArrayList<>();

    /**
     *
     * @param name
     * The table nam
     * @param database
     * The database where the table should be created
     * @param ezqlColumn
     * The columns of the table
     */
    protected EzqlTable(String name, EzqlDatabase database, EzqlColumn... ezqlColumn) {
        this.name = name;
        this.database = database;
        columns.addAll(Arrays.asList(ezqlColumn));
    }

    /**
     *
     * @return A list of the columns
     */
    public List<EzqlColumn> getColumns() {
        return columns;
    }

    /**
     *
     * @param name The column name
     * @return Returns an EzqlColumn object
     */
    public EzqlColumn getColumn(String name) {
        return columns.stream().filter(column -> column.getName().equalsIgnoreCase(name)).findAny().orElse(null);
    }

    public String getName() {
        return name;
    }

    /**
     * Enter the elements in order
     *
     * @param values
     * The values to enter the table
     */
    public void pushRow(Object... values) {
        if (values.length != columns.size())
            throw new EzqlColumnOrderException(values.length, columns.size());

        database.getEzqlQuery().addRow(this, values);
    }

    /**
     *
     * @param neededColumns The columns you want to query
     * @return Return a list of EzqlRow
     */
    public LinkedList<EzqlRow> getAllRows(Set<String> neededColumns){
        return database.getEzqlQuery().getAllRows(this,neededColumns);
    }

    /**
     *
     * @return Return a list of all the rows in database as an EzqlRow List
     */
    public LinkedList<EzqlRow> getAllRows(){
        return database.getEzqlQuery().getAllRows(this,getColumnsName());
    }

    /**
     *
     * @return Return the name of all the columns
     */
    public Set<String> getColumnsName(){
        return columns.stream().map(EzqlColumn::getName).collect(Collectors.toSet());
    }

    /**
     *
     * @param where The condition field
     * @param whereValue The condition value
     * @return Return a list of EzqlRow
     */
    public List<EzqlRow> getRows(String where, String whereValue) {
        return getRows(where,whereValue,getColumnsName());
    }

    /**
     *
     * @param where The condition field
     * @param whereValue The condition value
     * @return Return a EzqlRow
     */
    public EzqlRow getSingleRow(String where, String whereValue) {
        return getSingleRow(where,whereValue,getColumnsName());
    }

    /**
     *
     * @param where The condition field
     * @param whereValue The condition value
     * @param neededColumns The columns you want to query
     * @return Return a list of EzqlRow
     */
    public List<EzqlRow> getRows(String where, String whereValue, Set<String> neededColumns) {
        return database.getEzqlQuery().getRows(this,where,whereValue, neededColumns);
    }

    /**
     *
     * @param where The condition field
     * @param whereValue The condition value
     * @param neededColumns The columns you want to query
     * @return Return a EzqlRow
     */
    public EzqlRow getSingleRow(String where, String whereValue, Set<String> neededColumns) {
        return database.getEzqlQuery().getSingleRow(this,where,whereValue,neededColumns);
    }

    /**
     *
     * @param requestedValue The column you want to query
     * @param where The condition field
     * @param whereValue The condition value
     * @return Return the value as an object
     */
    public <T> T getSingleValue(String requestedValue, String where, String whereValue,Class<T> clazz) {
        EzqlRow ezqlRow = getSingleRow(where,whereValue, Sets.newHashSet(requestedValue));

        if(ezqlRow.getValues().isEmpty())
            return null;

        return (T)ezqlRow.getValue(requestedValue);
    }

    /**
     *
     * @param where The condition field
     * @param whereValue The condition value
     */
    public void removeRows(String where,Object whereValue){
        database.getEzqlQuery().remove(this,where,whereValue);
    }

    /**
     *
     * @param where The condition field
     * @param whereValue The condition value
     * @param ezqlRow The new row you want it to become
     */
    public void updateRow(String where, String whereValue, EzqlRow ezqlRow){
        database.getEzqlQuery().update(this,where,whereValue,ezqlRow);
    }

    public boolean exists(String where,String whereValue){
        return database.getEzqlQuery().exists(this,where,whereValue);
    }

    /**
     *
     * @return Return the database
     */
    protected EzqlDatabase getDatabase() {
        return database;
    }
}
