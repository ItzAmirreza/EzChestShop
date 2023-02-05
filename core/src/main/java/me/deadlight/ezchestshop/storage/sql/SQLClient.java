package me.deadlight.ezchestshop.storage.sql;

import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class SQLClient {

    private static final Constructor<com.pixeldv.storage.sql.connection.SQLClient> SQL_CLIENT_CONSTRUCTOR;

    static {
        try {
            SQL_CLIENT_CONSTRUCTOR = com.pixeldv.storage.sql.connection.SQLClient.class.getDeclaredConstructor(HikariDataSource.class);
            SQL_CLIENT_CONSTRUCTOR.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static com.pixeldv.storage.sql.connection.@NotNull SQLClient of(HikariDataSource dataSource) {
        try {
            return SQL_CLIENT_CONSTRUCTOR.newInstance(dataSource);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
