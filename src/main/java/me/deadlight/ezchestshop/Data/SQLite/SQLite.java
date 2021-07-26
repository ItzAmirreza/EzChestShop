package me.deadlight.ezchestshop.Data.SQLite;

import me.deadlight.ezchestshop.Data.SQLite.Structure.SQLColumn;
import me.deadlight.ezchestshop.Data.SQLite.Structure.SQLTable;
import me.deadlight.ezchestshop.EzChestShop;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class SQLite extends Database {

        String dbname;

        public SQLite(EzChestShop instance) {
            super(instance);
            dbname = "ecs-database"; // Set the table name here
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

}
