package me.deadlight.ezchestshop.storage.sql;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ShopMapper implements RowMapper<SqlShop> {
    @Override
    public SqlShop map(ResultSet rs, StatementContext ctx) throws SQLException {
        return null;
    }
}
