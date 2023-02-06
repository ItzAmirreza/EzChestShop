package me.deadlight.ezchestshop.storage.sql.shop;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ShopMapper implements RowMapper<SQLShop> {
    @Override
    public SQLShop map(ResultSet rs, StatementContext ctx) throws SQLException {
        return null;
    }
}
