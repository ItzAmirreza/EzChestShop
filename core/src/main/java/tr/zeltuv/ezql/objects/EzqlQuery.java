package tr.zeltuv.ezql.objects;

import java.sql.*;
import java.util.*;

public class EzqlQuery {

    private EzqlDatabase database;

    public EzqlQuery(EzqlDatabase database) {
        this.database = database;
    }

    public void createTable(EzqlTable table) {
        String name = table.getName();
        List<EzqlColumn> columns = table.getColumns();

        StringJoiner stringJoiner = new StringJoiner(",");

        for (EzqlColumn column : columns) {
            stringJoiner.add(column.getName() + " " + column.getDataType() + (column.getLength() == 0 ? "" : "(" + column.getLength() + ")"));
            if (column.isPrimary()) {
                stringJoiner.add("PRIMARY KEY(" + column.getName() + ")");
            }
        }

        try (
                Connection connection = database.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "CREATE TABLE IF NOT EXISTS " + name + " (" + stringJoiner + ") CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")) {
            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public void addRow(EzqlTable ezqlTable, Object[] values) {
        StringJoiner fields = new StringJoiner(",");
        StringJoiner questionMarks = new StringJoiner(",");
        String name = ezqlTable.getName();

        for (EzqlColumn ezqlColumn : ezqlTable.getColumns()) {
            questionMarks.add("?");
            fields.add("`" + ezqlColumn.getName() + "`");
        }

        try (

                Connection connection = database.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO " + name + " (" + fields + ") VALUES (" + questionMarks + ")");
        ) {
            for (int i = 0; i < values.length; i++) {
                preparedStatement.setObject(i + 1, values[i]);
            }
            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

    }

    public List<EzqlRow> getRows(EzqlTable table, String where, Object whereValue, Set<String> neededColumns) {
        List<EzqlRow> resultRows = new ArrayList<>();

        try {
            Connection connection = database.getConnection();

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM " + table.getName() + " WHERE `" + where + "`= ?");

            preparedStatement.setObject(1, whereValue);

            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                EzqlRow results = new EzqlRow();

                for (String column : neededColumns) {
                    if (!column.equals(where)) {
                        Object result = resultSet.getObject(column);
                        results.addValue(column, result);
                    } else {
                        results.addValue(column, whereValue);
                    }
                }

                resultRows.add(results);
            }
            preparedStatement.close();
            resultSet.close();
            connection.close();


        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        return resultRows;
    }

    public EzqlRow getSingleRow(EzqlTable table, String where, Object whereValue, Set<String> neededColumns) {
        EzqlRow results = new EzqlRow();

        try {
            Connection connection = database.getConnection();

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM " + table.getName() + " WHERE `" + where + "`= ?");

            preparedStatement.setObject(1, whereValue);

            ResultSet resultSet = preparedStatement.executeQuery();

            if(!resultSet.next()){
                return results;
            }

            for (String column : neededColumns) {
                if (!column.equals(where)) {
                    Object result = resultSet.getObject(column);
                    results.addValue(column, result);
                } else {
                    results.addValue(column, whereValue);
                }
            }


            preparedStatement.close();
            resultSet.close();
            connection.close();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        return results;
    }

    public boolean exists(EzqlTable ezqlTable, String where, Object value){
        try {
            Connection connection = database.getConnection();

            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM " + ezqlTable.getName() + " WHERE `" + where + "`= ?");

            preparedStatement.setObject(1, value);

            ResultSet resultSet = preparedStatement.executeQuery();

            boolean exists = resultSet.next();

            preparedStatement.close();
            resultSet.close();
            connection.close();

            return exists;
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        return false;
    }

    public LinkedList<EzqlRow> getAllRows(EzqlTable table, Set<String> neededColumns) {
        LinkedList<EzqlRow> resultRows = new LinkedList<>();

        try (
                Connection connection = database.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT * FROM " + table.getName());
                ResultSet resultSet = preparedStatement.executeQuery();) {

            while (resultSet.next()) {
                EzqlRow results = new EzqlRow();

                for (String column : neededColumns) {
                    Object result = resultSet.getObject(column);
                    results.addValue(column, result);
                }

                resultRows.add(results);
            }
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }

        return resultRows;
    }

    public void truncate(EzqlTable table) {
        try (
                Connection connection = database.getConnection();

                PreparedStatement preparedStatement = connection.prepareStatement(
                        "TRUNCATE TABLE " + table.getName()
                )) {
            preparedStatement.executeUpdate();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public void remove(EzqlTable table, String where, Object whereValue) {
        try {
            Connection connection = database.getConnection();

            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM `" + table.getName() + "` WHERE `" + where + "`= ?");
            preparedStatement.setObject(1, whereValue);

            preparedStatement.executeUpdate();
            preparedStatement.close();
            connection.close();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    public void update(EzqlTable table, String where, Object whereValue, EzqlRow ezqlRow) {
        try {
            Connection connection = database.getConnection();

            for (String key : ezqlRow.getValues().keySet()) {
                PreparedStatement preparedStatement = connection.prepareStatement("UPDATE " + table.getName() + " SET " + key + "=? WHERE " + where + "=?");

                preparedStatement.setObject(1, ezqlRow.getValue(key));
                preparedStatement.setObject(2, whereValue);

                preparedStatement.executeUpdate();
                preparedStatement.close();
            }

            connection.close();
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }
}
