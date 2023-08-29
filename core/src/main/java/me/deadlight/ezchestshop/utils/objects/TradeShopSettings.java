package me.deadlight.ezchestshop.utils.objects;

import me.deadlight.ezchestshop.data.Config;
import me.deadlight.ezchestshop.data.ShopContainer;
import me.deadlight.ezchestshop.data.TradeShopContainer;
import me.deadlight.ezchestshop.enums.Changes;
import org.bukkit.Location;

import java.util.*;
import java.util.stream.Collectors;

public class TradeShopSettings {

    private String sloc;
    private boolean msgtoggle;
    private TradeDirection tradeDirection;
    private String admins;
    private boolean adminshop;
    private String rotation;
    private List<String> customMessages;
    private SqlQueue sqlQueue;
    private EzTradeShop assignedShop;
    // Use some form of static Hashmap to cash this per shop/location/player smth.
    // Querying is only viable once, since we have the SQL Queue which makes things
    // pretty hard to track.
    // Unless I compare it with the previous customMessages, then it could work!
    // Seems like less of a hassle.
    private static List<String> customMessagesInitialChecked = new ArrayList<>();
    private static Map<UUID, Map<Location, String>> customMessagesTotal = new HashMap<>();

    /**
     * The direction of the trade.
     * <ul>
     *     <li>ITEM1_TO_ITEM2 means that the first item is the item that the player is selling.</li>
     *     <li>ITEM2_TO_ITEM1 means that the first item is the item that the player is buying.</li>
     *     <li>BOTH means that the player can trade both ways.</li>
     *     <li>DISABLED means that the player cannot trade.</li>
     * </ul>
     */
    public enum TradeDirection {
        ITEM1_TO_ITEM2,
        ITEM2_TO_ITEM1,
        BOTH,
        DISABLED
    }

    public TradeShopSettings(String sloc, boolean msgtoggle, TradeDirection tradeDirection, String admins,
                             boolean adminshop, String rotation, List<String> customMessages) {
        this.sloc = sloc;
        this.msgtoggle = msgtoggle;
        this.tradeDirection = tradeDirection;
        this.admins = admins;
        this.adminshop = adminshop;
        this.rotation = rotation;
        this.customMessages = customMessages;
    }

    private TradeShopSettings(TradeShopSettings settings) {
        this.sloc = settings.sloc;
        this.msgtoggle = settings.msgtoggle;
        this.tradeDirection = settings.tradeDirection;
        this.admins = settings.admins;
        this.adminshop = settings.adminshop;
        this.rotation = settings.rotation;
        this.customMessages = settings.customMessages;
        this.assignedShop = settings.assignedShop;
        this.sqlQueue = settings.sqlQueue;
    }

    public TradeShopSettings clone() {
        return new TradeShopSettings(this);
    }

    public boolean isMsgtoggle() {
        return msgtoggle;
    }

    public TradeShopSettings setMsgtoggle(boolean msgtoggle) {
        sqlQueue.setChange(Changes.MESSAGE_TOGGLE, msgtoggle);
        this.msgtoggle = msgtoggle;
        return this;
    }

    public TradeDirection getTradeDirection() {
        return tradeDirection;
    }

    public TradeShopSettings setTradeDirection(TradeDirection tradeDirection) {
        sqlQueue.setChange(Changes.TRADE_DIRECTION, tradeDirection.toString());
        this.tradeDirection = tradeDirection;
        return this;
    }

    public String getAdmins() {
        return admins;
    }

    public TradeShopSettings setAdmins(String admins) {
        sqlQueue.setChange(Changes.ADMINS_LIST, admins);
        this.admins = admins;
        return this;
    }



    public boolean isAdminshop() {
        return adminshop;
    }

    public TradeShopSettings setAdminshop(boolean adminshop) {
        sqlQueue.setChange(Changes.IS_ADMIN, adminshop);
        this.adminshop = adminshop;
        return this;
    }

    public String getRotation() {
        return rotation == null ? Config.settings_defaults_rotation : rotation;
    }

    public TradeShopSettings setRotation(String rotation) {
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

    public TradeShopSettings setCustomMessages(List<String> customMessages) {
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

        List<EzTradeShop> ezShops = TradeShopContainer.getTradeShopFromOwner(UUID.fromString(owner)).stream().filter(
                ezShop -> !ezShop.getSettings().customMessages.isEmpty()
        ).collect(Collectors.toList());

        Map<Location, String> stringMap = new HashMap<>();

        for (EzTradeShop ezShop : ezShops) {
            stringMap.put(ezShop.getLocation(),ezShop.getSettings().customMessages.get(0));
        }

        return stringMap;
    }

    private static Map<Location, String> fetchAllCustomMessages(String owner) {
        return getAllCustomMessages(owner);
    }

    public void assignShop(EzTradeShop shop) {
        this.assignedShop = shop;
    }

    public EzTradeShop getAssignedShop() {
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
