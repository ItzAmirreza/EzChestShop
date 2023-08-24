package me.deadlight.ezchestshop.utils.WorldGuard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.entity.Player;

public class WorldGuardUtils {

    public static Location convertLocation(org.bukkit.Location loc) {
        return BukkitAdapter.adapt(loc);
    }

    public static World convertWorld(org.bukkit.World world) {
        return BukkitAdapter.adapt(world);
    }

    public static ApplicableRegionSet queryRegionSet(org.bukkit.Location loc) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        return  ((RegionQuery) query).getApplicableRegions(convertLocation(loc));
    }

    public static boolean queryStateFlag(StateFlag flag, Player player) {
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        return queryRegionSet(player.getLocation()).testState(localPlayer, flag);
    }

    public static boolean queryStateFlag(StateFlag flag, org.bukkit.Location location) {
        return queryRegionSet(location).testState(null, flag);
    }
}
