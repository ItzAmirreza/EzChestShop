package tr.zeltuv.ezql.settings;

import com.zaxxer.hikari.HikariConfig;

public class DefaultHikariSettings implements CustomHikariSettings{
    @Override
    public HikariConfig getHikariConfig(EzqlCredentials creds) {
        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl("jdbc:mysql://" + creds.getHost() + ":"
                + creds.getPort() + "/" + creds.getDatabase() + "?useSSL=" + creds.useSSL());
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setUsername(creds.getUsername());
        hikariConfig.setPassword(creds.getPassword());
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.setMinimumIdle(1);
        hikariConfig.setAutoCommit(true);
        hikariConfig.setMaximumPoolSize(creds.getMaxPool());

        return hikariConfig;
    }
}
