package me.deadlight.ezchestshop.storage;

import com.pixeldv.storage.model.Model;

public class Shop implements Model {

    public static Shop of(String location) {
        return new Shop(location);
    }

    protected final String location;
    protected String owner, item;
    protected double buyPrice, sellPrice;
    protected boolean msgToggle, buyDisabled, sellDisabled;
    protected String admins;
    protected boolean shareIncome;
    protected String transactions;
    protected boolean adminshop;
    protected String rotation, customMessages;

    public Shop(
            String location,
            String owner,
            String item,
            double buyPrice,
            double sellPrice,
            boolean msgToggle,
            boolean buyDisabled,
            boolean sellDisabled,
            String admins,
            boolean shareIncome,
            String transactions,
            boolean adminshop,
            String rotation,
            String customMessages
    ) {
        this.location = location;
        this.owner = owner;
        this.item = item;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.msgToggle = msgToggle;
        this.buyDisabled = buyDisabled;
        this.sellDisabled = sellDisabled;
        this.admins = admins;
        this.shareIncome = shareIncome;
        this.transactions = transactions;
        this.adminshop = adminshop;
        this.rotation = rotation;
        this.customMessages = customMessages;
    }

    public Shop(String location) {
        this.location = location;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public String getId() {
        return location;
    }
}
