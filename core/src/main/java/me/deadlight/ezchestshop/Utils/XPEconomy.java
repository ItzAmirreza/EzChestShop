package me.deadlight.ezchestshop.Utils;

import me.deadlight.ezchestshop.EzChestShop;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Debug;

public class XPEconomy {

    public static double getXP(OfflinePlayer player) {
        if (player.getPlayer() != null) {
            return getPlayerXpPoints(player.getPlayer());
        }
        return 0;
    }

    public static boolean has(OfflinePlayer player, double price) {
        if (player.getPlayer() != null) {
            return getPlayerXpPoints(player.getPlayer()) >= price;
        }
        EzChestShop.logDebug("Player was null!");
        return false;
    }

    public static boolean withDrawPlayer(OfflinePlayer player, double price) {
        Player p = player.getPlayer();
        if (p != null) {
            updatePlayerXp(p,getPlayerXpPoints(p) - price);
            return true;
        }
        return false;
    }

    public static void depositPlayer(OfflinePlayer player, double price) {
        Player p = player.getPlayer();
        if (p != null) {
            updatePlayerXp(p,getPlayerXpPoints(p) + price);
        }
    }

    private static double getPointsFromLevel(int level) {
        double points;
        if (level < 17) {
            points = Math.pow(level, 2) + 6 * level;
        } else if (level < 32) {
            points = 2.5 * Math.pow(level, 2) - 40.5 * level + 360;
        } else {
            points = 4.5 * Math.pow(level, 2) - 162.5 * level + 2220;
        }
        return points;
    }

    private static LevelPoints getLevelFromPoints(double points) {
        int level;
        if (points < 352) {
            EzChestShop.logDebug("1");
            level = (int) (Math.sqrt(points + 9) - 3);
        } else if (points < 1507) {
            level = (int) (81.0 / 10.0 + Math.sqrt((2.0 / 5.0) * (points - 7839.0 / 40.0)));
            EzChestShop.logDebug("2");
        } else {
            level = (int) (325.0 / 18.0 + Math.sqrt((2.0 / 9.0) * (points - 54215.0 / 72.0)));
            EzChestShop.logDebug("3");
        }
        EzChestShop.logDebug("Points are: " + points + " and level is: " + level);
        return new LevelPoints(level, points - getPointsFromLevel(level));
    }

    private static double getPlayerXpPoints(Player player) {
//        EzChestShop.logDebug("level: " + player.getLevel() + ", " + Math.round(player.getExp() * player.getExpToLevel()));
        return getPointsFromLevel(player.getLevel()) + Math.round(player.getExp() * player.getExpToLevel());
    }

    private static void updatePlayerXp(Player player, double points) {
        LevelPoints lp = getLevelFromPoints(points);
        EzChestShop.logDebug("Updating levels to " + lp.level + ", " + lp.points + "!");
        player.setLevel(lp.level);
        player.setExp((float) (lp.points / player.getExpToLevel()));
    }

    private static class LevelPoints {
        public int level;
        public double points;
        public LevelPoints(int level, double points) {
            this.level = level;
            this.points = points;
            EzChestShop.logDebug("Lvl: " + level + ", Points: " + points);
        }
    }

}
