package me.deadlight.ezchestshop.utils;

import org.bukkit.OfflinePlayer;

public class XPEconomy {

    public static double getXP(OfflinePlayer player) {
        ImprovedOfflinePlayer iop = ImprovedOfflinePlayer.improvedOfflinePlayer.fromOfflinePlayer(player);
        return iop.hasPlayedBefore() ? getPlayerXpPoints(iop) : 0.0;
    }

    public static boolean has(OfflinePlayer player, double price) {
        ImprovedOfflinePlayer iop = ImprovedOfflinePlayer.improvedOfflinePlayer.fromOfflinePlayer(player);
        return iop.hasPlayedBefore() ? getPlayerXpPoints(iop) >= price : false;
    }

    public static boolean withDrawPlayer(OfflinePlayer player, double price) {
        ImprovedOfflinePlayer iop = ImprovedOfflinePlayer.improvedOfflinePlayer.fromOfflinePlayer(player);
        if (iop.getLevel() > 200000000) {
            return iop.hasPlayedBefore() ? updatePlayerXp(iop, calculateLevelPointDifference(iop, -price)): false;
        } else {
            return iop.hasPlayedBefore() ? updatePlayerXp(iop, getPlayerXpPoints(iop) - price): false;
        }
    }

    public static void depositPlayer(OfflinePlayer player, double price) {
        ImprovedOfflinePlayer iop = ImprovedOfflinePlayer.improvedOfflinePlayer.fromOfflinePlayer(player);
        if (iop.getLevel() > 200000000) {
            updatePlayerXp(iop, calculateLevelPointDifference(iop, price));
        } else {
            updatePlayerXp(iop, getPlayerXpPoints(iop) + price);
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
            level = (int) (Math.sqrt(points + 9) - 3);
        } else if (points < 1507) {
            level = (int) (81.0 / 10.0 + Math.sqrt((2.0 / 5.0) * (points - 7839.0 / 40.0)));
        } else {
            level = (int) (325.0 / 18.0 + Math.sqrt((2.0 / 9.0) * (points - 54215.0 / 72.0)));
        }
        return new LevelPoints(level, points - getPointsFromLevel(level));
    }

    private static double getPlayerXpPoints(ImprovedOfflinePlayer player) {
        return getPointsFromLevel(player.getLevel()) + Math.round(player.getExp() * player.getExpToLevel());
    }

    private static boolean updatePlayerXp(ImprovedOfflinePlayer player, double points) {
        LevelPoints lp = getLevelFromPoints(points);
        return updatePlayerXp(player, lp);
    }

    private static boolean updatePlayerXp(ImprovedOfflinePlayer player,LevelPoints lp) {
        player.setLevel(lp.level);
        player.setExp((float) (lp.points / player.getExpToLevel()));
        if (!player.isOnline()) {
            return player.savePlayerData();
        } else {
            return true;
        }
    }

    private static LevelPoints calculateLevelPointDifference(ImprovedOfflinePlayer player, double points) {
        // These values get modified and reflect the current state of the player
        double currentPoints = Math.round(player.getExp() * player.getExpToLevel());
        int currentLevel = player.getLevel();
        if (points < 0) {
            // subtract points
            // repeat until points is 0
            while (points < 0) {
                if (currentLevel == 0 && currentPoints == 0) {
                    // if the player is level 0, he can't lose any more points
                    break;
                }
                // subtract the points and go down one level or break the loop if no points are left to subtract.
                if (currentPoints >= points * -1) {
                    currentPoints = currentPoints + points;
                    break;
                } else {
                    points = points + currentPoints;
                    currentLevel--;
                    currentPoints = getRequiredPointsToLevelUp(currentLevel);
                }
            }
            return new LevelPoints(currentLevel, currentPoints);
        } else if (points > 0) {
            // add points
            // repeat until points is 0
            while (points > 0) {
                if (currentLevel == Integer.MAX_VALUE && currentPoints == 0) {
                    // if the player is at max level, he can't receive any more points
                    break;
                }
                int pointsRequiredForLevel = getRequiredPointsToLevelUp(currentLevel);
                // add the points and go up one level or break the loop if no points are left to add.
                if ((pointsRequiredForLevel - currentPoints) > points) {
                    currentPoints = currentPoints + points;
                    break;
                } else {
                    points = points - pointsRequiredForLevel;
                    currentLevel++;
                    currentPoints = 0;
                }
            }
            return new LevelPoints(currentLevel, currentPoints);
        } else {
            // do nothing
            return new LevelPoints(player.getLevel(), player.getExp());
        }
    }

    public static int getRequiredPointsToLevelUp(int level) {
        int requiredPoints;
        if (level < 16) {
            requiredPoints = 2 * level + 7;
        } else if (level < 31) {
            requiredPoints = 5 * level - 38;
        } else {
            requiredPoints = 9 * level - 158;
        }
        return requiredPoints;
    }

    private static class LevelPoints {
        public int level;
        public double points;
        public LevelPoints(int level, double points) {
            this.level = level;
            this.points = points;
        }
    }

}
