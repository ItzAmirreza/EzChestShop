package me.deadlight.ezchestshop.storage.sql.player;

import com.pixeldv.storage.sql.identity.MapSerializer;
import me.deadlight.ezchestshop.storage.PlayerData;
import me.deadlight.ezchestshop.storage.sql.Mapper;

import java.util.Map;
import java.util.UUID;

public class SQLPlayerData extends PlayerData implements MapSerializer {
    public SQLPlayerData(UUID uniqueId) {
        super(uniqueId);
    }

    public SQLPlayerData(UUID uniqueId, String checkprofits) {
        super(uniqueId, checkprofits);
    }

    @Override
    public Map<String, Object> toMap() {
        return Mapper.builder()
                .build();
    }
}
