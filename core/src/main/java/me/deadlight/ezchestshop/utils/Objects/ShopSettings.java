package me.deadlight.ezchestshop.utils.Objects;

import me.deadlight.ezchestshop.data.Config;
import me.deadlight.ezchestshop.data.ShopContainer;
import me.deadlight.ezchestshop.enums.Changes;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class ShopSettings {

    private String sloc;
    private boolean msgtoggle;
    private boolean dbuy;
    private boolean dsell;
    private String admins;
    private boolean shareincome;
    private boolean adminshop;
    private String rotation;
    private List<String> customMessages;
    private SqlQueue sqlQueue;
    private EzShop assignedShop;
    // Use some form of static Hashmap to cash this per shop/location/player smth.
    // Querying is only viable once, since we have the SQL Queue which makes things
    // pretty hard to track.
    // Unless I compare it with the previous customMessages, then it could work!
    // Seems like less of a hassle.
    private static List<String> customMessagesInitialChecked = new ArrayList<>();
    private static Map<UUID, Map<Location, String>> customMessagesTotal = new HashMap<>();

    public ShopSettings(String sloc, boolean msgtoggle, boolean dbuy, boolean dsell, String admins, boolean shareincome,
             boolean adminshop, String rotation, List<String> customMessages) {
        this.sloc = sloc;
        this.msgtoggle = msgtoggle;
        this.dbuy = dbuy;
        this.dsell = dsell;
        this.admins = admins;
        this.shareincome = shareincome;
        this.adminshop = adminshop;
        this.rotation = rotation;
        this.customMessages = customMessages;
    }

    private ShopSettings(ShopSettings settings) {
        this.sloc = settings.sloc;
        this.msgtoggle = settings.msgtoggle;
        this.dbuy = settings.dbuy;
        this.dsell = settings.dsell;
        this.admins = settings.admins;
        this.shareincome = settings.shareincome;
        this.adminshop = settings.adminshop;
        this.rotation = settings.rotation;
        this.customMessages = settings.customMessages;
        this.assignedShop = settings.assignedShop;
        this.sqlQueue = settings.sqlQueue;
    }

    public ShopSettings clone() {
        return new ShopSettings(this);
    }

    public boolean isMsgtoggle() {
        return msgtoggle;
    }

    public ShopSettings setMsgtoggle(boolean msgtoggle) {
        sqlQueue.setChange(Changes.MESSAGE_TOGGLE, msgtoggle);
        this.msgtoggle = msgtoggle;
        return this;
    }

    public boolean isDbuy() {
        return dbuy;
    }

    public ShopSettings setDbuy(boolean dbuy) {
        sqlQueue.setChange(Changes.DISABLE_BUY, dbuy);
        this.dbuy = dbuy;
        return this;
    }

    public boolean isDsell() {
        return dsell;
    }

    public ShopSettings setDsell(boolean dsell) {
        sqlQueue.setChange(Changes.DISABLE_SELL, dsell);
        this.dsell = dsell;
        return this;
    }

    public String getAdmins() {
        return admins;
    }

    public ShopSettings setAdmins(String admins) {
        sqlQueue.setChange(Changes.ADMINS_LIST, admins);
        this.admins = admins;
        return this;
    }

    public boolean isShareincome() {
        return shareincome;
    }

    public ShopSettings setShareincome(boolean shareincome) {
        sqlQueue.setChange(Changes.SHAREINCOME, shareincome);
        this.shareincome = shareincome;
        return this;
    }


    public boolean isAdminshop() {
        return adminshop;
    }

    public ShopSettings setAdminshop(boolean adminshop) {
        sqlQueue.setChange(Changes.IS_ADMIN, adminshop);
        this.adminshop = adminshop;
        return this;
    }

    public String getRotation() {
        return rotation == null ? Config.settings_defaults_rotation : rotation;
    }

    public ShopSettings setRotation(String rotation) {
        sqlQueue.setChange(Changes.ROTATION, rotation);
        this.rotation = rotation;
        return this;
    }

    public List<String> getCustomMessages() {
        if (!customMessagesInitialChecked.contains(assignedShop.getOwnerID().toString())) {
            customMessagesInitialChecked.add(assignedShop.getOwnerID().toString());
            customMessagesTotal.put(assignedShop.getOwnerID(),
                    fetchAllCustomMessages(assignedShop.getOwnerID().toString()));
        }
        return customMessages;
    }

    public ShopSettings setCustomMessages(List<String> customMessages) {
        String newMessages = customMessages.stream().collect(Collectors.joining("#,#"));
        if (!newMessages.equals(this.customMessages)) {
            if (newMessages.isEmpty()) {
                customMessagesTotal.get(assignedShop.getOwnerID()).remove(assignedShop.getLocation());
            } else {
                customMessagesTotal.get(assignedShop.getOwnerID()).put(assignedShop.getLocation(), newMessages);
            }
        }
        sqlQueue.setChange(Changes.CUSTOM_MESSAGES, newMessages);
        this.customMessages = customMessages;
        return this;
    }

    public static Map<Location, String> getAllCustomMessages(String owner) {

        Player player = Bukkit.getPlayer(UUID.fromString(owner));

        List<EzShop> ezShops = ShopContainer.getShopFromOwner(UUID.fromString(owner)).stream().filter(
                ezShop -> !ezShop.getSettings().customMessages.isEmpty()
        ).collect(Collectors.toList());

        Map<Location, String> stringMap = new HashMap<>();

        for (EzShop ezShop : ezShops) {
            stringMap.put(ezShop.getLocation(),ezShop.getSettings().customMessages.get(0));
        }

        return stringMap;

        /*if (!customMessagesInitialChecked.contains(owner)) {

                DatabaseManager db = EzChestShop.getPlugin().getDatabase();

                Map<String, String> data = db.getKeysWithValueByExpresion("location", "customMessages", "owner", "shopdata",
                        "IS \"" + owner + "\" AND customMessages IS NOT NULL AND TRIM(customMessages, \" \") IS NOT \"\"");
                Map<Location, String> converted = data.entrySet().stream()
                        .collect(Collectors.toMap(e -> Utils.StringtoLocation(e.getKey()), e -> e.getValue()));

                customMessagesInitialChecked.add(owner);
                customMessagesTotal.put(UUID.fromString(owner), converted);
                return converted;
            }
            return customMessagesTotal.get(UUID.fromString(owner));*/


    }

    private static Map<Location, String> fetchAllCustomMessages(String owner) {
        return getAllCustomMessages(owner);
    }

    public void assignShop(EzShop shop) {
        this.assignedShop = shop;
    }

    public EzShop getAssignedShop() {
        return this.assignedShop;
    }

    public String getSloc() {
        return this.sloc;
    }

    public SqlQueue getSqlQueue() {
        return this.sqlQueue;
    }

    public void createSqlQueue() {
        this.sqlQueue = new SqlQueue(assignedShop.getLocation(), this, getAssignedShop());
    }

}
