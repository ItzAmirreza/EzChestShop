package me.deadlight.ezchestshop.storage.sql.shop;

import com.pixeldv.storage.model.Model;
import com.pixeldv.storage.sql.identity.MapSerializer;
import me.deadlight.ezchestshop.storage.Shop;
import me.deadlight.ezchestshop.storage.sql.Mapper;

import java.util.Map;

public class SQLShop extends Shop implements Model, MapSerializer {

    public SQLShop(Shop shop) {
        this(shop.getId());
    }

    public SQLShop(String location) {
        super(location, owner, item);
    }

    @Override
    public String getId() {
        return location;
    }

    @Override
    public Map<String, Object> toMap() {
        return Mapper.builder()
                .build();
    }

}
