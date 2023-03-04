package tr.zeltuv.ezql.settings;

public class EzqlCredentials {

    private String host,database,username,password;
    private int maxPool, port;
    private boolean useSSL;

    /**
     *
     * @param host
     * The MySQL server IP
     * @param database
     * The database you want to use from your MySQL server
     * @param username
     * The username of your user
     * @param password
     * The password of your user
     * @param maxPool
     * The maximum pool amount
     * @param port
     * The MySQL server port
     * @param useSSL
     * Using the SSL for the connection
     */
    public EzqlCredentials(String host, String database, String username, String password, int maxPool, int port, boolean useSSL) {
        this.host = host;
        this.database = database;
        this.username = username;
        this.password = password;
        this.maxPool = maxPool;
        this.port = port;
        this.useSSL = useSSL;
    }

    public String getHost() {
        return host;
    }

    public String getDatabase() {
        return database;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getMaxPool() {
        return maxPool;
    }

    public int getPort() {
        return port;
    }

    public boolean useSSL() {
        return useSSL;
    }
}
