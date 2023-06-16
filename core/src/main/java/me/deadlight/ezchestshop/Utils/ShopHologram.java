package me.deadlight.ezchestshop.Utils;

public class ShopHologram {
    //TODO: This class will access the BlockBoundHologram class and will manage the holograms of the shops.
    // it shell provide more direct methods, further away from the BlockBoundHologram class abstractions,
    // which aims to be more general purpose to make expansions easier.
    // This class will be more specific to the plugin and will provide methods like:
    // - showCustomHologramMessage
    // - setBuyDisabled
    // - setSellDisabled
    // - showShopEmptyMessage
    // - showItemData
    // - showText
    // - showItem
    // These methods will include all placeholders needed, so the PlayerCloseToChestListener
    // class will only need to call these methods and pass the needed arguments.
    // That way new mechanics should be easier to implement and the code will be more readable.
}
