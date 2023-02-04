package me.deadlight.ezchestshop.storage;

import com.pixeldv.storage.model.Model;

public class Shop implements Model {

    public static Shop of(String location) {
        return new Shop(location);
    }

    protected final String location;

    public Shop(String location) {
        this.location = location;
    }

    @Override
    public String getId() {
        return location;
    }
}
