package me.deadlight.ezchestshop.storage;

import java.io.Closeable;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public interface Storage extends Closeable {

    void load() throws IOException;

    Shop findByLocation(String location);

    Set<Shop> findAllShops();

    void deleteShopByLocation(String location);

    PlayerData findPlayerData(UUID id);

    void deletePlayerDataById(UUID id);

    Set<PlayerData> findAllPlayerData();

}
