package me.deadlight.ezchestshop.storage.sql;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class Mapper {

    @Contract(" -> new")
    public static @NotNull Mapper builder() {
        return new Mapper();
    }

    private final Map<String, Object> data;

    public Mapper() {
        this(new HashMap<>());
    }

    public Mapper(Map<String, Object> data) {
        this.data = data;
    }

    public Mapper bindNonNull(String name, Object value) {
        if (value != null) {
            return bind(name, value);
        }
        return this;
    }

    public Mapper bind(String name, Object value) {
        data.put(name, value);
        return this;
    }

    public Map<String, Object> build() {
        return data;
    }

}
