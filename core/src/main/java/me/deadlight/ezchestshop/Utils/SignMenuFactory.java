package me.deadlight.ezchestshop.Utils;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

public final class SignMenuFactory {

    public static final int ACTION_INDEX = 9;
    public static final int SIGN_LINES = 4;

    public static final String NBT_FORMAT = "{\"text\":\"%s\"}";
    public static final String NBT_BLOCK_ID = "minecraft:sign";

    private final Plugin plugin;

    private final Map<Player, Menu> inputs;


    public SignMenuFactory(Plugin plugin) {
        this.plugin = plugin;
        this.inputs = new HashMap<>();
        this.listen();
    }

    public Menu newMenu(List<String> text) {
        return new Menu(text);
    }

    private void listen() {
        Utils.versionUtils.signFactoryListen(this);
    }

    public final class Menu {

        private final List<String> text;

        private BiPredicate<Player, String[]> response;
        private boolean reopenIfFail;

        private Location location;

        private boolean forceClose;

        Menu(List<String> text) {
            this.text = text;
        }

        public Menu reopenIfFail(boolean value) {
            this.reopenIfFail = value;
            return this;
        }

        public Menu response(BiPredicate<Player, String[]> response) {
            this.response = response;
            return this;
        }

        public void open(Player player) {
            Utils.versionUtils.openMenu(this, player);
        }

        /**
         * closes the menu. if force is true, the menu will close and will ignore the reopen
         * functionality. false by default.
         *
         * @param player the player
         * @param force decides whether or not it will reopen if reopen is enabled
         */
        public void close(Player player, boolean force) {
            this.forceClose = force;
            if (player.isOnline()) {
                player.closeInventory();
            }
        }

        public BiPredicate<Player, String[]> getResponse() {
            return response;
        }

        public boolean isForceClose() {
            return forceClose;
        }

        public boolean isReopenIfFail() {
            return reopenIfFail;
        }

        public Location getLocation() {
            return location;
        }

        public List<String> getText() {
            return text;
        }

        public void setLocation(Location location) {
            this.location = location;
        }

        public void close(Player player) {
            close(player, false);
        }

        public String color(String input) {
            return ChatColor.translateAlternateColorCodes('&', input);
        }

        public SignMenuFactory getFactory() {
            return SignMenuFactory.this;
        }
    }

    public Map<Player, Menu> getInputs() {
        return inputs;
    }
}