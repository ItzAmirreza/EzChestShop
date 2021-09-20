package me.deadlight.ezchestshop.Data.SQLite;

import me.deadlight.ezchestshop.EzChestShop;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public abstract class Database {
    EzChestShop plugin;
    Connection connection;

    public Database(EzChestShop instance) {
        plugin = instance;
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    public abstract void disconnect();

    public void initialize() {
        connection = getSQLConnection();
    }

    public void close(PreparedStatement ps, ResultSet rs) {
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException e) {
            Error.close(plugin, e);
        }
    }

    /**
     * Query the Database for a String value
     *
     * @param primary_key the name of the primary key row.
     * @param key the value of the primary key that is to query
     * @param column the name of the column whose data needs to be queried
     * @param table the table that is to be queried
     * @return the resulting String or null
     */
    public String getString(String primary_key, String key, String column, String table) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE " + primary_key + " = ?;");
            ps.setString(1, key);

            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString(column);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }
        return null;
    }

    /**
     * Query the Database for a Integer value
     *
     * @param primary_key the name of the primary key row.
     * @param key the value of the primary key that is to query
     * @param column the name of the column whose data needs to be queried
     * @param table the table that is to be queried
     * @return the resulting Integer or null
     */
    public Integer getInt(String primary_key, String key, String column, String table) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE " + primary_key + " = ?;");
            ps.setString(1, key);

            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(column); // Removed check, let's see if this breaks anything...
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }
        return null;
    }

    /**
     * Query the Database for a Boolean value
     *
     * @param primary_key the name of the primary key row.
     * @param key the value of the primary key that is to query
     * @param column the name of the column whose data needs to be queried
     * @param table the table that is to be queried
     * @return the resulting Boolean or false
     */
    public boolean getBool(String primary_key, String key, String column, String table) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE " + primary_key + " = ?;");
            ps.setString(1, key);

            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getBoolean(column);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }
        return false;
    }

    /**
     * Query the Database for a Long value
     *
     * @param primary_key the name of the primary key row.
     * @param key the value of the primary key that is to query
     * @param column the name of the column whose data needs to be queried
     * @param table the table that is to be queried
     * @return the resulting long or 0
     */
    public long getBigInt(String primary_key, String key, String column, String table) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE " + primary_key + " = ?;");
            ps.setString(1, key);

            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong(column);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }
        return 0;
    }

    /**
     * Query the Database for a Double value
     *
     * @param primary_key the name of the primary key row.
     * @param key the value of the primary key that is to query
     * @param column the name of the column whose data needs to be queried
     * @param table the table that is to be queried
     * @return the resulting long or 0
     */
    public double getDouble(String primary_key, String key, String column, String table) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE " + primary_key + " = ?;");
            ps.setString(1, key);

            rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(column);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }
        return 0;
    }


    /**
     * Set a String in the Database.
     *
     * @param primary_key the name of the primary key row.
     * @param key the value of the primary key that is to query
     * @param column the name of the column whose data needs to be queried
     * @param table the table that is to be queried
     * @param data the new String to be set
     */
    public void setString(String primary_key, String key, String column, String table, String data) {
        setString(primary_key, key, column, table, data, false);
    }

    /**
     * Set a String in the Database and make sure to check if the value exists, else add it.
     * (not completely working)
     *
     * @param primary_key the name of the primary key row.
     * @param key the value of the primary key that is to query
     * @param column the name of the column whose data needs to be queried
     * @param table the table that is to be queried
     * @param data the new String to be set
     * @param checkExsting boolean checking if a the row has any entries so far already.
     */
    public void setString(String primary_key, String key, String column, String table, String data,
                          boolean checkExsting) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            //Check if existing -> if not insert new entry
            if (checkExsting && !hasKey(table, primary_key, key)) {
                //String statement = "REPLACE INTO " + table + " (" + primary_key + ", " + column + ") VALUES('" + key + "','" + data + "');";
                ps = conn.prepareStatement("REPLACE INTO " + table +
                        " (" + primary_key + ", " + column + ") VALUES(?,?);");
                ps.setString(1, key);
                ps.setString(2, data);
            } else {
                //if existing, update old data
                ps = conn.prepareStatement("UPDATE " + table + " SET " + column + " = ? WHERE "
                        + primary_key + " = ?;");
                ps.setString(1, data);
                ps.setString(2, key);
            }

            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }
    }

    /**
     * Set a Int in the Database.
     *
     * @param primary_key the name of the primary key row.
     * @param key the value of the primary key that is to query
     * @param column the name of the column whose data needs to be queried
     * @param table the table that is to be queried
     * @param data the int to be set
     */
    public void setInt(String primary_key, String key, String column, String table, int data) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("UPDATE " + table + " SET " + column + " = ? WHERE " + primary_key
                    + " = ?;");
            ps.setInt(1, data);
            ps.setString(2, key);

            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }
    }

    /**
     * Set a Double in the Database.
     *
     * @param primary_key the name of the primary key row.
     * @param key the value of the primary key that is to query
     * @param column the name of the column whose data needs to be queried
     * @param table the table that is to be queried
     * @param data the int to be set
     */
    public void setDouble(String primary_key, String key, String column, String table, double data) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("UPDATE " + table + " SET " + column + " = ? WHERE " + primary_key
                    + " = ?;");
            ps.setDouble(1, data);
            ps.setString(2, key);

            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }
    }

    /**
     * Set a Boolean in the Database.
     *
     * @param primary_key the name of the primary key row.
     * @param key the value of the primary key that is to query
     * @param column the name of the column whose data needs to be queried
     * @param table the table that is to be queried
     * @param data the Boolean to be set
     */
    public void setBool(String primary_key, String key, String column, String table, Boolean data) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("UPDATE " + table + " SET " + column + " = ? WHERE "
                    + primary_key + " = ?;");
            ps.setBoolean(1, data);
            ps.setString(2, key);

            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }
    }

    /**
     * Get a Int in the Database and increment it's value by a given value.
     *
     * @param primary_key the name of the primary key row.
     * @param key the value of the primary key that is to query
     * @param column the name of the column whose data needs to be queried
     * @param table the table that is to be queried
     * @param increment the int value that the resulting data will be incremented by.
     */
    public void incrementInt(String primary_key, String key, String column, String table, int increment) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("UPDATE " + table + " SET " + column + " = " + column + " + ? WHERE " + primary_key + " = ?;");
            ps.setInt(1, increment);
            ps.setString(2, key);

            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }
    }

    /**
     * Reset a row in the Database.
     *
     * @param primary_key the name of the primary key row.
     * @param key the value of the primary key that is to query
     * @param table the table that is to be queried
     */
    public void deleteEntry(String primary_key, String key, String table) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("DELETE FROM " + table + " WHERE " + primary_key + " = ?;");
            ps.setString(1, key);

            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }
    }

    /**
     * Prepares a Column for future data insertion.
     *
     * @param table the table that is to be queried
     * @param primary_key the name of the primary key row.
     * @param key the value of the primary key that is to query
     */
    public void prepareColumn(String table, String primary_key, String key) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO " + table + " (" + primary_key + ") VALUES(?)");

            ps.setString(1, key);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }
    }

    /**
     * Query the Database and return a List of all primary Keys that have a value present in a given column.
     *
     * @param primary_key the name of the primary key row.
     * @param column the name of the column whose data needs to be queried
     * @param table the table that is to be queried
     * @return a List of all resulting Keys, if none, the List will be empty
     */
    public List<String> getKeysByExistance(String primary_key, String column, String table) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        List<String> keys = new ArrayList<>();

        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(
                    "SELECT " + primary_key + " FROM " + table + " WHERE " + column + " IS NOT NULL;");

            rs = ps.executeQuery();
            while (rs.next()) {
                keys.add(rs.getString(primary_key));
            }
            return keys;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }
        return keys;
    }

    /**
     * Query the Database and return a List of all primary Keys based on a Expression that is executed on a Column.<br>
     * Example: <br>
     * getKeysByExpresiion("location", "owner", "shopdata", "IS \"8224bb2a-4c45-4798-84dc-73a0ed8048f5\"");
     *
     * @param primary_key the name of the primary key row.
     * @param column the name of the column whose data needs to be queried
     * @param table the table that is to be queried
     * @param expression the Expression to be queried with
     * @return a List of the resulting keys, if none the List will be empty
     */
    public List<String> getKeysByExpresiion(String primary_key, String column, String table, String expression) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        List<String> keys = new ArrayList<>();

        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(
                    "SELECT " + primary_key + " FROM " + table + " WHERE " + column + " " + expression + ";");

            rs = ps.executeQuery();
            while (rs.next()) {
                keys.add(rs.getString(primary_key));
            }
            return keys;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }
        return keys;
    }

    /**
     * Query the Database for a List of all primary Keys of a given table
     *
     * @param primary_key the name of the primary key row.
     * @param table the table that is to be queried
     * @return the resulting keys, if none the List will be Empty
     */
    public List<String> getKeys(String primary_key, String table) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        List<String> keys = new ArrayList<>();

        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(
                    "SELECT " + primary_key + " FROM " + table + ";");

            rs = ps.executeQuery();
            while (rs.next()) {
                keys.add(rs.getString(primary_key));
            }
            return keys;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }
        return keys;
    }

    public void PreparePlayerdata(String table, String uuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO " + table + " (uuid) VALUES(?)");

            ps.setString(1, uuid);
            ps.executeUpdate();
            return;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }
        return;
    }

    // Check if Player is in DB:
    public boolean hasPlayer(String table, UUID key) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE uuid = ?;");
            ps.setString(1, (key.toString()));

            rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            } else
                return false;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }
        return false;
    }

    /**
     * Check if the Database contains a given primary key.
     *
     * @param table the table that is to be queried
     * @param primary_key the name of the primary key row.
     * @param key the value of the primary key that is to query
     * @return a boolean if the primary key exists
     */
    public boolean hasKey(String table, String primary_key, String key) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + table + " WHERE " + primary_key + " = ?;");
            ps.setString(1, key);

            rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }
        return false;
    }

    /**
     * Check if the Database contains a given Table
     *
     * @param table the table that is to be queried
     * @return a boolean based on the existence of the queried table
     */
    public boolean hasTable(String table) {
        // SELECT name FROM sqlite_master WHERE type='table' AND name='{table_name}';
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT name FROM sqlite_master WHERE type='table' AND name=?");
            ps.setString(1, table);

            rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }
        return false;
    }



    /**
     * Adds quotes to a String. Required for certain expressions.
     *
     * @param s a string that should be surrounded with quotes
     * @return a string surrounded with quotes
     */
    private String addQuotes(String s) {
        if (s == null)
            return null;
        if (s.equals(""))
            return null;
        return "'" + s + "'";
    }


    public void insertShop(String sloc, String owner, String item, double buyprice, double sellprice, boolean msgtoggle,
                           boolean dbuy, boolean dsell, String admins, boolean shareincome, String trans, boolean adminshop, String rotation) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(
                    "REPLACE INTO shopdata (location,owner,item,buyPrice,sellPrice,msgToggle,"
                            + "buyDisabled,sellDisabled,admins,shareIncome,transactions,adminshop,rotation) "
                            + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)");

            ps.setString(1, sloc);
            ps.setString(2, owner);
            ps.setString(3, item);
            ps.setDouble(4, buyprice);
            ps.setDouble(5, sellprice);
            ps.setBoolean(6, msgtoggle);
            ps.setBoolean(7, dbuy);
            ps.setBoolean(8, dsell);
            ps.setString(9, admins);
            ps.setBoolean(10, shareincome);
            ps.setString(11, trans);
            ps.setBoolean(12, adminshop);
            ps.setString(13, rotation);
            ps.executeUpdate();
            return;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            try {
                if (ps != null)
                    ps.close();
                if (conn != null)
                    conn.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }
        return;
    }
}
