package tr.zeltuv.ezql.objects;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import tr.zeltuv.ezql.settings.DefaultHikariSettings;
import tr.zeltuv.ezql.settings.EzqlCredentials;
import tr.zeltuv.ezql.settings.CustomHikariSettings;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class EzqlDatabase {

    private EzqlCredentials credentials;
    private EzqlQuery ezqlQuery = new EzqlQuery(this);
    private CustomHikariSettings customHikariSettings;
    private HikariDataSource hikariDataSource;
    private Map<String, EzqlTable> tables = new HashMap<>();

    /**
     * Main constructor, will apply defaults settings for hikari config
     *
     * @param credentials Needed for the API to connect to your database server
     */
    public EzqlDatabase(EzqlCredentials credentials) {
        this.credentials = credentials;
        this.customHikariSettings = new DefaultHikariSettings();
    }

    /**
     * @param credentials          Needed for the API to connect to your database server
     * @param customHikariSettings Changes the hikari config overriding it with your custom settings
     */
    public EzqlDatabase(EzqlCredentials credentials, CustomHikariSettings customHikariSettings) {
        this.credentials = credentials;
        this.customHikariSettings = customHikariSettings;
    }

    /**
     *
     * @return Create a connection to the database
     * @throws SQLException
     */
    public Connection getConnection() throws SQLException {
        try {
            if (hikariDataSource == null) {
                connect();
            }
            return hikariDataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Connect to the MySQL server
     */
    public void connect() {
        HikariConfig hikariConfig = customHikariSettings.getHikariConfig(credentials);

        hikariDataSource = new HikariDataSource(hikariConfig);
        hikariDataSource.addDataSourceProperty( "autoReconnect" , "true" );
    }

    /**
     * Disconnect from the MySQL server
     */
    public void disconnect() {
        hikariDataSource.close();
    }

    /**
     *
     * @param name The table name
     * @return returns an EzqlTable object
     */
    public EzqlTable getTable(String name) {
        return tables.get(name);
    }

    /**
     *
     * @param name The table mame
     * @param ezqlColumns The table columns
     * @return returns an EzqlTable object
     */
    public EzqlTable addTable(String name, EzqlColumn... ezqlColumns) {
        EzqlTable ezqlTable = new EzqlTable(name, this, ezqlColumns);

        tables.put(name, ezqlTable);

        ezqlQuery.createTable(ezqlTable);

        return ezqlTable;
    }


    protected EzqlQuery getEzqlQuery() {
        return ezqlQuery;
    }

    public boolean hasTable(String table) {
        return tables.containsKey(table);
    }
}
