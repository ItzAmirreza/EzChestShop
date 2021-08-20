package me.deadlight.ezchestshop.Utils.Objects;

import me.deadlight.ezchestshop.Data.SQLite.Database;
import me.deadlight.ezchestshop.EzChestShop;

public class ShopSettings {

    private String sloc;
    private boolean msgtoggle;
    private boolean dbuy;
    private boolean dsell;
    private String admins;
    private boolean shareincome;
    private String trans;
    private boolean adminshop;

    public ShopSettings(String sloc, boolean msgtoggle, boolean dbuy, boolean dsell, String admins, boolean shareincome,
                        String trans, boolean adminshop) {
        this.sloc = sloc;
        this.msgtoggle = msgtoggle;
        this.dbuy = dbuy;
        this.dsell = dsell;
        this.admins = admins;
        this.shareincome = shareincome;
        this.trans = trans;
        this.adminshop = adminshop;
    }

    public boolean isMsgtoggle() {
        return msgtoggle;
    }

    public void setMsgtoggle(boolean msgtoggle) {
        this.msgtoggle = msgtoggle;
        Database db = EzChestShop.getPlugin().getDatabase();
        db.setBool("location", sloc,
                "msgToggle", "shopdata", msgtoggle);
    }

    public boolean isDbuy() {
        return dbuy;
    }

    public void setDbuy(boolean dbuy) {
        this.dbuy = dbuy;
        Database db = EzChestShop.getPlugin().getDatabase();
        db.setBool("location", sloc,
                "buyDisabled", "shopdata", dbuy);
    }

    public boolean isDsell() {
        return dsell;
    }

    public void setDsell(boolean dsell) {
        this.dsell = dsell;
        Database db = EzChestShop.getPlugin().getDatabase();
        db.setBool("location", sloc,
                "sellDisabled", "shopdata", dsell);
    }

    public String getAdmins() {
        return admins;
    }

    public void setAdmins(String admins) {
        this.admins = admins;
        Database db = EzChestShop.getPlugin().getDatabase();
        db.setString("location", sloc,
                "admins", "shopdata", admins);
    }

    public boolean isShareincome() {
        return shareincome;
    }

    public void setShareincome(boolean shareincome) {
        this.shareincome = shareincome;
        Database db = EzChestShop.getPlugin().getDatabase();
        db.setBool("location", sloc,
                "shareIncome", "shopdata", shareincome);
    }

    public String getTrans() {
        return trans;
    }

    public void setTrans(String trans) {
        this.trans = trans;
        Database db = EzChestShop.getPlugin().getDatabase();
        db.setString("location", sloc,
                "transactions", "shopdata", trans);
    }

    public boolean isAdminshop() {
        return adminshop;
    }

    public void setAdminshop(boolean adminshop) {
        this.adminshop = adminshop;
        Database db = EzChestShop.getPlugin().getDatabase();
        db.setBool("location", sloc,
                "adminshop", "shopdata", adminshop);
    }
}
