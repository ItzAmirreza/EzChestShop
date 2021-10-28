package me.deadlight.ezchestshop.Listeners;

import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Data.LanguageManager;
import me.deadlight.ezchestshop.Data.PlayerContainer;
import me.deadlight.ezchestshop.Data.ShopContainer;
import me.deadlight.ezchestshop.EzChestShop;
import me.deadlight.ezchestshop.Utils.ASHologram;
import me.deadlight.ezchestshop.Utils.FloatingItem;
import me.deadlight.ezchestshop.Utils.Objects.EzShop;
import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.tuple.Pair;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.RayTraceResult;

import java.util.HashMap;
import java.util.List;


public class PlayerCloseToChestListener implements Listener {

    private static LanguageManager lm = new LanguageManager();
    public static HashMap<Player, List<Pair<Location, ASHologram>>> HoloTextMap = new HashMap<>();
    public static HashMap<Player, List<Pair<Location, FloatingItem>>> HoloItemMap = new HashMap<>();

    private static HashMap<Location, List<Player>> playershopmap = new HashMap<>();
    private static HashMap<Location, List<Player>> playershopTextmap = new HashMap<>();

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        //Ignore this Event if Holograms are disabled
        if (Config.showholo) {
            //Get the Player variables
            Player player = event.getPlayer();
            PlayerContainer pc = PlayerContainer.get(player);

            //Show the item on hover if looking at them directly
            if (Config.holodistancing_show_item_first) {
                RayTraceResult result = player.rayTraceBlocks(5);
                if (result != null) {
                    Block target = result.getHitBlock();
                    if (Utils.isApplicableContainer(target)) {
                        target = Utils.getCorrectBlock(target);
                        Location loc = target.getLocation();
                        if (ShopContainer.isShop(loc)) {
                            if (!ShopContainer.getShop(loc).getShopViewers().contains(player.getUniqueId())) {
                                //Show the Hologram
                                EzShop.showHologramLook(player, loc);
                            }
                        } else {
                            EzShop.hideHologramLook(player, pc.getLookedAtShop());
                        }
                    } else {
                        EzShop.hideHologramLook(player, pc.getLookedAtShop());
                    }
                } else {
                    EzShop.hideHologramLook(player, pc.getLookedAtShop());
                }
            }

            //Reduce Lag by only showing stuff if the player has moved his body
            if (!hasMovedXYZ(event))
                return;

            Location loc = player.getLocation();
            ShopContainer.getShops().stream()
                    .filter(sloc -> sloc != null && loc.getWorld().equals(sloc.getWorld()) && loc.distance(sloc) < Config.holodistancing_distance + 5)
                    .forEach(sloc -> {
                        double dist = loc.distance(sloc);
                        //Show the Hologram if Player close enough
                        if (dist < Config.holodistancing_distance) {
                            if (ShopContainer.getShop(sloc).getShopLoaders().contains(player.getUniqueId()))
                                return; //forEach -> acts as continue;
                            EzShop.showHologram(player, sloc);
                        }
                        //Hide the Hologram that is too far away from the player
                        else if (dist > Config.holodistancing_distance + 1 && dist < Config.holodistancing_distance + 3){
                            //Hide the Hologram - requires spawn Position of the Hologram and Position of the Shop
                            EzShop.hideHologram(player, sloc);
                        }
                    });
        }
    }

    @EventHandler
    public void onPlayerLogout(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Location loc = player.getLocation();
        ShopContainer.getShops().stream()
                .filter(sloc -> sloc != null && loc.getWorld().equals(sloc.getWorld()) && loc.distance(sloc) < Config.holodistancing_distance + 5)
                .forEach(sloc -> EzShop.hideHologram(player, sloc));
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getFrom();
        ShopContainer.getShops().stream()
                .filter(sloc -> sloc != null && loc.getWorld().equals(sloc.getWorld()) && loc.distance(sloc) < Config.holodistancing_distance + 5)
                .forEach(sloc -> EzShop.hideHologram(player, sloc));
    }



    private boolean hasMovedXYZ(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getX() != to.getX())
            return true;
        if (from.getY() != to.getY())
            return true;
        if (from.getZ() != to.getZ())
            return true;
        return false;
    }





}
