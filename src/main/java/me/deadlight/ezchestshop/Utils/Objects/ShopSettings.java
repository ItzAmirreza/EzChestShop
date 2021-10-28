package me.deadlight.ezchestshop.Utils.Objects;

import me.deadlight.ezchestshop.Data.Config;
import me.deadlight.ezchestshop.Enums.Changes;

public class ShopSettings {

    private String sloc;
    private boolean msgtoggle;
    private boolean dbuy;
    private boolean dsell;
    private String admins;
    private boolean shareincome;
    private String trans;
    private boolean adminshop;
    private String rotation;
    private EzShop assignedShop;

    public ShopSettings(String sloc, boolean msgtoggle, boolean dbuy, boolean dsell, String admins, boolean shareincome,
                        String trans, boolean adminshop, String rotation) {
        this.sloc = sloc;
        this.msgtoggle = msgtoggle;
        this.dbuy = dbuy;
        this.dsell = dsell;
        this.admins = admins;
        this.shareincome = shareincome;
        this.trans = trans;
        this.adminshop = adminshop;
        this.rotation = rotation;
    }

    private ShopSettings(ShopSettings settings) {
        this.sloc = settings.sloc;
        this.msgtoggle = settings.msgtoggle;
        this.dbuy = settings.dbuy;
        this.dsell = settings.dsell;
        this.admins = settings.admins;
        this.shareincome = settings.shareincome;
        this.trans = settings.trans;
        this.adminshop = settings.adminshop;
        this.rotation = settings.rotation;
    }

    public ShopSettings clone() {
        return new ShopSettings(this);
    }

    public boolean isMsgtoggle() {
        return msgtoggle;
    }

    public ShopSettings setMsgtoggle(boolean msgtoggle) {
        this.msgtoggle = msgtoggle;
        assignedShop.getSqlQueue().setChange(Changes.MESSAGE_TOGGLE, msgtoggle);
        return this;
    }

    public boolean isDbuy() {
        return dbuy;
    }

    public ShopSettings setDbuy(boolean dbuy) {
        this.dbuy = dbuy;
        assignedShop.getSqlQueue().setChange(Changes.DISABLE_BUY, dbuy);
        return this;
    }

    public boolean isDsell() {
        return dsell;
    }

    public ShopSettings setDsell(boolean dsell) {
        this.dsell = dsell;
        assignedShop.getSqlQueue().setChange(Changes.DISABLE_SELL, dsell);
        return this;
    }

    public String getAdmins() {
        return admins;
    }

    public ShopSettings setAdmins(String admins) {
        this.admins = admins;
        assignedShop.getSqlQueue().setChange(Changes.ADMINS_LIST, admins);
        return this;
    }

    public boolean isShareincome() {
        return shareincome;
    }

    public ShopSettings setShareincome(boolean shareincome) {
        this.shareincome = shareincome;
        assignedShop.getSqlQueue().setChange(Changes.SHAREINCOME, shareincome);
        return this;
    }

    public String getTrans() {
        return trans;
    }

    public ShopSettings setTrans(String trans) {
        this.trans = trans;
        assignedShop.getSqlQueue().setChange(Changes.TRANSACTIONS, trans);
        return this;
    }

    public boolean isAdminshop() {
        return adminshop;
    }

    public ShopSettings setAdminshop(boolean adminshop) {
        this.adminshop = adminshop;
        assignedShop.getSqlQueue().setChange(Changes.IS_ADMIN, adminshop);
        return this;
    }

    public String getRotation() {
        return rotation == null ? Config.settings_defaults_rotation : rotation;
    }

    public ShopSettings setRotation(String rotation) {
        this.rotation = rotation;
        assignedShop.getSqlQueue().setChange(Changes.ROTATION, rotation);
        return this;
    }

    public void assignToShop(EzShop shop) {
        this.assignedShop = shop;
    }
    public EzShop getAssignedShop() {
        return this.assignedShop;
    }

    public String getSloc() {
        return this.sloc;
    }
}
