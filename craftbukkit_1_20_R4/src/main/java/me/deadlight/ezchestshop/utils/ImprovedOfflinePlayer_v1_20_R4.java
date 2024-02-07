package me.deadlight.ezchestshop.utils;

import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class ImprovedOfflinePlayer_v1_20_R4 extends ImprovedOfflinePlayer {

    private File file;
    private CompoundTag compound;

    public ImprovedOfflinePlayer_v1_20_R4() {
        super();
    }

    public ImprovedOfflinePlayer_v1_20_R4(OfflinePlayer player) {
        super(player);
    }

    @Override
    public ImprovedOfflinePlayer fromOfflinePlayer(OfflinePlayer player) {
        return new ImprovedOfflinePlayer_v1_20_R4(player);
    }

    @Override
    public boolean loadPlayerData() {
        try {
            for(World w : Bukkit.getWorlds()) {
                this.file = new File(w.getWorldFolder(), "playerdata" + File.separator + this.player.getUniqueId() + ".dat");
                if(this.file.exists()){
                    this.compound = NbtIo.readCompressed(new FileInputStream(this.file), NbtAccounter.unlimitedHeap());
                    return true;
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean savePlayerData() {
        if(this.exists) {
            try {
                NbtIo.writeCompressed(this.compound, new FileOutputStream(this.file));
                return true;
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public int getLevel() {
        if (isOnline) {
            return player.getPlayer().getLevel();
        } else {
            return compound.getInt("XpLevel");
        }
    }

    @Override
    public void setLevel(int level) {
        if (isOnline) {
            player.getPlayer().setLevel(level);
        } else {
            compound.putInt("XpLevel", level);
        }
    }

    @Override
    public float getExp() {
        if (isOnline) {
            return player.getPlayer().getExp();
        } else {
            return compound.getFloat("XpP");
        }
    }

    @Override
    public void setExp(float exp) {
        if (isOnline) {
            player.getPlayer().setExp(exp);
        } else {
            compound.putFloat("XpP", exp);
        }
    }

    @Override
    public int getExpToLevel() {
        if (isOnline) {
            return player.getPlayer().getExpToLevel();
        } else {
            int level = getLevel();
            return XPEconomy.getRequiredPointsToLevelUp(level);
        }
    }


}
