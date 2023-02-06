package me.deadlight.ezchestshop.storage.sql;

import com.pixeldv.storage.ModelService;
import com.pixeldv.storage.sql.SQLModelService;
import com.zaxxer.hikari.HikariDataSource;
import me.deadlight.ezchestshop.storage.PlayerData;
import me.deadlight.ezchestshop.storage.Shop;
import me.deadlight.ezchestshop.storage.Storage;
import me.deadlight.ezchestshop.storage.sql.player.SQLPlayerData;
import me.deadlight.ezchestshop.storage.sql.shop.SQLShop;
import me.deadlight.ezchestshop.storage.sql.shop.ShopMapper;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SqlStorage implements Storage {

    private ModelService<SQLShop> shopModelService;
    private ModelService<SQLPlayerData> playerDataModelService;
    private final HikariDataSource dataSource;

    public SqlStorage(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void load() {
        com.pixeldv.storage.sql.connection.SQLClient sqlClient = SQLClient.of(dataSource);
        this.shopModelService = SQLModelService.builder(SQLShop.class)
                .client(sqlClient)
                .rowMapper(new ShopMapper())
                .build();
        this.playerDataModelService = SQLModelService.builder(SQLPlayerData.class)
                .client(sqlClient)
                .build();
    }

    @Override
    public Shop findByLocation(String location) {
        return shopModelService.findSync(location);
    }

    @Override
    public Set<Shop> findAllShops() {
        return new HashSet<>(shopModelService.findAllSync());
    }

    @Override
    public void deleteShopByLocation(String location) {
        shopModelService.deleteSync(location);
    }

    @Override
    public PlayerData findPlayerData(UUID id) {
        return playerDataModelService.findSync(id.toString());
    }

    @Override
    public void deletePlayerDataById(UUID id) {
        playerDataModelService.deleteSync(id.toString());
    }

    @Override
    public Set<PlayerData> findAllPlayerData() {
        return new HashSet<>(playerDataModelService.findAllSync());
    }

    @Override
    public void close() {
        if (dataSource.isClosed()) {
            return;
        }
        dataSource.close();
    }
}
