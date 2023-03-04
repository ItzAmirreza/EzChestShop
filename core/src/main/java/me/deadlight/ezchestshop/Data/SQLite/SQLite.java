package me.deadlight.ezchestshop.Data.SQLite;
import me.deadlight.ezchestshop.Data.DatabaseManager;
import me.deadlight.ezchestshop.Data.SQLite.Structure.SQLColumn;
import me.deadlight.ezchestshop.Data.SQLite.Structure.SQLTable;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.Objects.EzShop;
import me.deadlight.ezchestshop.Utils.Objects.ShopSettings;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SQLite extends DatabaseManager {

        String dbname;
        EzChestShop plugin;
        Connection connection;

        public SQLite(EzChestShop instance) {
            plugin = instance;
            dbname = "ecs-database"; // Set the table name here
        }
        public void initialize() {
            connection = getSQLConnection();
        }

        List<String> statements = new ArrayList<>();

        // SQL creation stuff, You can leave the blow stuff untouched.
        public Connection getSQLConnection() {
            File dataFolder = new File(plugin.getDataFolder(), dbname + ".db");
            if (!dataFolder.exists()) {
                try {
                    dataFolder.createNewFile();
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "File write error: " + dbname + ".db");
                }
            }
            try {
                if (connection != null && !connection.isClosed()) {
                    return connection;
                }
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
                return connection;
            } catch (SQLException ex) {
                plugin.getLogger().log(Level.SEVERE, "SQLite exception on initialize", ex);
            } catch (ClassNotFoundException ex) {
                plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
            }
            return null;
        }

        @Override
        public void load() {
            connection = getSQLConnection();
            initstatements();
            for (String i : statements) {
                try {
                    Statement s = connection.createStatement();
                    s.executeUpdate(i);
                    s.close();
                } catch (SQLException e) {
                    if (!e.getMessage().contains("duplicate")) e.printStackTrace();
                }
            }
            initialize();
        }

        public void disconnect() {
            connection = getSQLConnection();
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        public void initstatements() {
            // Add all the tables if they don't already exist.
            this.statements.addAll(convertObjecttoInsertStatement());
            // Alter the tables:
            this.statements.addAll(convertObjecttoAlterStatement());
            this.statements.removeIf(Objects::isNull);
        }


    //TODO Don't forget to change this when adding a new database table that works with Player data!
    public static List<String> playerTables = Arrays.asList("playerdata");



    /**************************************
     *         DATABASE STRUCTURE         *
     *           Very Important           *
     **************************************/
    public LinkedHashMap<String, SQLTable> getTableObjects() {
        LinkedHashMap<String, SQLTable> tables = new LinkedHashMap<>();
        tables.put("shopdata", new SQLTable(new LinkedHashMap<String, SQLColumn>() {
            {
                put("location", new SQLColumn("STRING (32)", true, false));
                put("owner", new SQLColumn("STRING (32)", false, false));
                put("item", new SQLColumn("STRING (32)", false, false));
                put("buyPrice", new SQLColumn("DOUBLE", false, false));
                put("sellPrice", new SQLColumn("DOUBLE", false, false));
                put("msgToggle", new SQLColumn("BOOLEAN", false, false));
                put("buyDisabled", new SQLColumn("BOOLEAN", false, false));
                put("sellDisabled", new SQLColumn("BOOLEAN", false, false));
                put("admins", new SQLColumn("STRING (32)", false, false));
                put("shareIncome", new SQLColumn("BOOLEAN", false, false));
                put("adminshop", new SQLColumn("BOOLEAN", false, false));
                put("rotation", new SQLColumn("STRING (32)", false, false));
                put("customMessages", new SQLColumn("STRING (32)", false, false));

            }
        }));
        tables.put("playerdata", new SQLTable(new LinkedHashMap<String, SQLColumn>() {
            {
                put("uuid", new SQLColumn("STRING (32)", true, false));
                put("checkprofits", new SQLColumn("STRING (32)", false, false));
            }
        }));

        return tables;
        }


    //Insert statements:
    private List<String> convertObjecttoInsertStatement() {
        return getTableObjects().entrySet().stream().map(x -> {// Get the tables
            return "CREATE TABLE IF NOT EXISTS " + x.getKey() + " ("
                    + x.getValue().getTable().entrySet().stream().map(y -> {// Start collecting the lines
                SQLColumn col = y.getValue();// get one column
                String line = y.getKey() + " ";
                line = line.concat(col.getType()); // add the type
                if (col.isCanbenull()) {
                    line = line.concat(" NOT NULL ");// add null possibility
                }
                if (col.isPrimarykey()) {
                    line = line.concat(" PRIMARY KEY "); // add primary key possibility
                }
                return line; // return this colomn as a string line.
            }).collect(Collectors.joining(", ")) + ");";// collect them columns together and join to a table
        }).collect(Collectors.toList()); // Join the tables to a list together.
    }

    //Alter statements:
    private List<String> convertObjecttoAlterStatement() {
        return getTableObjects().entrySet().stream().map(x -> x.getValue().getTable()
                .entrySet().stream().map(y -> {
                    SQLColumn col = y.getValue();// get one column
                    if (col.isPrimarykey())
                        return null; // primary key's can't be changed anyway.
                    String line = "ALTER TABLE " + x.getKey() + " ADD COLUMN " + y.getKey() + " ";
                    line = line.concat(col.getType()); // add the type
                    if (col.isCanbenull()) {
                        line = line.concat(" NOT NULL ");// add null possibility
                    }
                    if (col.isPrimarykey()) {
                        line = line.concat(" PRIMARY KEY "); // add primary key possibility
                    }
                    line = line.concat(";");
                    return line; // return this colomn as a string line.
                }).collect(Collectors.toList())).flatMap(Collection::stream).collect(Collectors.toList());
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public void preparePlayerData(String table, String uuid) {
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
    @Override
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
    @Override
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

    @Override
    public void insertShop(String sloc, String owner, String item, double buyprice, double sellprice, boolean msgtoggle,
                           boolean dbuy, boolean dsell, String admins, boolean shareincome , boolean adminshop, String rotation, List<String> customMessages) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(
                    "REPLACE INTO shopdata (location,owner,item,buyPrice,sellPrice,msgToggle,"
                            + "buyDisabled,sellDisabled,admins,shareIncome,adminshop,rotation,customMessages) "
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
            ps.setBoolean(11, adminshop);
            ps.setString(12, rotation);
            ps.setString(13, customMessages.stream().collect(Collectors.joining("#,#")));
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

    public HashMap<Location, EzShop> queryShops() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM shopdata;");

            rs = ps.executeQuery();
            HashMap<Location, EzShop> map = new HashMap<>();
            while (rs.next()) {
                String sloc = rs.getString("location");
                String customMessages = rs.getString("customMessages");
                if (customMessages == null) customMessages = "";
                map.put(Utils.StringtoLocation(sloc), new EzShop(Utils.StringtoLocation(sloc), rs.getString("owner"), Utils.decodeItem(rs.getString("item")),
                        rs.getDouble("buyPrice"), rs.getDouble("sellPrice"), new ShopSettings(sloc, rs.getBoolean("msgToggle"),
                        rs.getBoolean("buyDisabled"), rs.getBoolean("sellDisabled"), rs.getString("admins"),
                        rs.getBoolean("shareIncome"), rs.getBoolean("adminshop"),
                        rs.getString("rotation"), Arrays.asList(customMessages.split("#,#")))));
            }
            return map;
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

}
