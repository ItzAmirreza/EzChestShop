package me.deadlight.ezchestshop.Utils;

import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class CustomAdvancement implements Advancement {
    @NotNull
    @Override
    public Collection<String> getCriteria() {
        return null;
    }

    @NotNull
    @Override
    public NamespacedKey getKey() {
        return null;
    }
}
