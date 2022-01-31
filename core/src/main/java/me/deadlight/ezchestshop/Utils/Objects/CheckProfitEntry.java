package me.deadlight.ezchestshop.Utils.Objects;

import me.deadlight.ezchestshop.Utils.Utils;
import org.bukkit.inventory.ItemStack;

public class CheckProfitEntry {


    public static String itemSpacer = "#&#";
    public static String itemInlineSpacer = "#,#";

    // Id,ItemStack,BuyAmount,BuyPrice,SellAmount,SellPrice
    private String id;
    private ItemStack item;
    private Integer buyAmount;
    private Double buyPrice;
    private Double buyUnitPrice;
    private Integer sellAmount;
    private Double sellPrice;
    private Double sellUnitPrice;

    // Constructors:
    public CheckProfitEntry(String id, ItemStack item, Integer buyAmount, Double buyPrice, Double buyUnitPrice, Integer sellAmount,
                            Double sellPrice, Double sellUnitPrice) {
        this.id = id;
        this.item = item;
        this.buyAmount = buyAmount;
        this.buyPrice = buyPrice;
        this.buyUnitPrice = buyUnitPrice;
        this.sellAmount = sellAmount;
        this.sellPrice = sellPrice;
        this.sellUnitPrice = sellUnitPrice;
    }

    public CheckProfitEntry(String string) {
        if (string != null && !string.equals("") && !string.contains("null")) {
            String[] split = string.split(itemInlineSpacer);
            id = split[0];
            item = Utils.decodeItem(split[1]);
            buyAmount = Integer.valueOf(split[2]);
            buyPrice = Double.valueOf(split[3]);
            buyUnitPrice = Double.valueOf(split[4]);
            sellAmount = Integer.valueOf(split[5]);
            sellPrice = Double.valueOf(split[6]);
            sellUnitPrice = Double.valueOf(split[7]);
        }

    }

    public String toString() {
        return id + itemInlineSpacer + Utils.encodeItem(item) + itemInlineSpacer + buyAmount + itemInlineSpacer
                + buyPrice + itemInlineSpacer + buyUnitPrice + itemInlineSpacer + sellAmount + itemInlineSpacer
                + sellPrice + itemInlineSpacer + sellUnitPrice;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public Integer getBuyAmount() {
        return buyAmount;
    }

    public void setBuyAmount(Integer buyAmount) {
        this.buyAmount = buyAmount;
    }

    public Double getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(Double buyPrice) {
        this.buyPrice = buyPrice;
    }

    public Integer getSellAmount() {
        return sellAmount;
    }

    public void setSellAmount(Integer sellAmount) {
        this.sellAmount = sellAmount;
    }

    public Double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(Double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public Double getBuyUnitPrice() {
        return buyUnitPrice;
    }

    public Double getSellUnitPrice() {
        return sellUnitPrice;
    }
}
