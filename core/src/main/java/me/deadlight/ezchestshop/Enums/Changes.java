package me.deadlight.ezchestshop.Enums;

public enum Changes {
    SHOP_CREATE(null, null), //for now these two are not used because no solution for them YET
    SHOP_REMOVE(null, null),
    SHOP_OWNER(String.class, "owner"),
    BUY_PRICE(Double.class, "buyPrice"),
    SELL_PRICE(Double.class, "sellPrice"),
    ADMINS_LIST(String.class, "admins"),
    IS_ADMIN(Boolean.class, "adminshop"),
    SHAREINCOME(Boolean.class, "shareIncome"),
    TRANSACTIONS(String.class, "transactions"),
    ROTATION(String.class, "rotation"),
    CUSTOM_MESSAGES(String.class, "customMessages"),
    MESSAGE_TOGGLE(Boolean.class, "msgToggle"),
    DISABLE_BUY(Boolean.class, "buyDisabled"),
    DISABLE_SELL(Boolean.class, "sellDisabled");

    public final Class<?> theClass;
    public final String databaseValue;

    Changes(Class<?> theClass, String databaseValue) {
        this.theClass = theClass;
        this.databaseValue = databaseValue;
    }

}
