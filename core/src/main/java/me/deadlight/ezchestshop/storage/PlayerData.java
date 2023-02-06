package me.deadlight.ezchestshop.storage;

import com.pixeldv.storage.model.Model;

import java.util.UUID;

public class PlayerData implements Model {

    protected final UUID uniqueId;
    protected String checkprofits;

    public PlayerData(UUID uniqueId) {
        this(uniqueId, null);
    }

    public PlayerData(UUID uniqueId, String checkprofits) {
        this.uniqueId = uniqueId;
        this.checkprofits = checkprofits;
    }

    @Override
    public String getId() {
        return uniqueId.toString();
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public String getCheckprofits() {
        return checkprofits;
    }
}
